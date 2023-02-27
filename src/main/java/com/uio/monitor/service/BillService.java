package com.uio.monitor.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.uio.monitor.common.BackEnum;
import com.uio.monitor.common.BillProduceWayTypeEnum;
import com.uio.monitor.common.BillTypeEnum;
import com.uio.monitor.common.CustomException;
import com.uio.monitor.controller.req.AddBillReq;
import com.uio.monitor.controller.req.AddPeriodBillReq;
import com.uio.monitor.controller.req.UpdateBillReq;
import com.uio.monitor.controller.resp.BillConfigDTO;
import com.uio.monitor.controller.resp.BillDTO;
import com.uio.monitor.controller.resp.BillStatisticsDTO;
import com.uio.monitor.controller.resp.PeriodBillDTO;
import com.uio.monitor.entity.BillDO;
import com.uio.monitor.entity.PeriodBillDO;
import com.uio.monitor.entity.dto.BillExtraDTO;
import com.uio.monitor.manager.BillManager;
import com.uio.monitor.manager.ConfigManager;
import com.uio.monitor.manager.PeriodBillManager;
import com.uio.monitor.utils.Utils;
import com.uio.monitor.vo.BillExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author han xun
 * Date 2022/1/2 16:17
 * Description:
 */
@Service
@Slf4j
public class BillService {

    private static final String TOTAL_AMOUNT_CATEGORY = "总金额";
    private static final String LARGE_ITEM = "大件消费总额";
    private static final String AVE_MONTH_LARGE_ITEM = "大件消费月均";
    private static final String AVE_DAY_LARGE_ITEM = "大件消费日均";

    @Autowired
    private BillManager billManager;
    @Autowired
    private ConfigManager configManager;
    @Autowired
    private PeriodBillManager periodBillManager;

    /**
     * 插入一条账单记录
     *
     * @param addBillReq
     * @param userId
     * @return
     */
    public Boolean addBill(AddBillReq addBillReq, Long userId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(addBillReq.getProductTime());
        } catch (ParseException e) {
            log.warn("parse date error, req:{}", JSON.toJSONString(addBillReq));
            throw new CustomException(BackEnum.DATA_ERROR);
        }
        BillDO billDO = new BillDO();
        billDO.setUserId(userId);
        billDO.setCreator(userId.toString());
        billDO.setModifier(userId.toString());
        billDO.setProduceTime(date);
        billDO.setBillType(addBillReq.getBillType());
        billDO.setProduceWay(addBillReq.getProduceWayType());
        billDO.setAmount(new BigDecimal(addBillReq.getAmount()));
        billDO.setDescription(addBillReq.getDesc());
        billDO.setCategory(addBillReq.getType());
        billDO.setDeleted(false);
        // 默认加入账单不为大件
        billDO.setLargeItem(false);
        billManager.insert(billDO);
        return true;
    }

    /**
     * 添加周期性账单
     */
    public void addPeriodBill(AddPeriodBillReq addPeriodBillReq, Long userId) {
        PeriodBillDO periodBillDO = this.convertPeriodBillDO(addPeriodBillReq, userId);
        periodBillManager.insert(periodBillDO);
    }

    public List<PeriodBillDTO> getPeriodBillListByUserId(Long userId) {
        List<PeriodBillDO> periodBillDOS = periodBillManager.queryPeriodBillListByUserId(userId);
        return CollectionUtils.isEmpty(periodBillDOS) ? new ArrayList<>(0) : periodBillDOS.stream()
                .map(this::convertPeriodBillDTO).collect(Collectors.toList());
    }

    private PeriodBillDTO convertPeriodBillDTO(PeriodBillDO periodBillDO) {
        PeriodBillDTO periodBillDTO = new PeriodBillDTO();
        periodBillDTO.setBillType(periodBillDO.getBillType());
        periodBillDTO.setProduceWayType(BillProduceWayTypeEnum.getByName(periodBillDO.getProduceWay()) == null ?
                "" : BillProduceWayTypeEnum.getByName(periodBillDO.getProduceWay()).getDesc());
        periodBillDTO.setGenerateDay(periodBillDO.getGenerateDay());
        periodBillDTO.setGenerateCount(periodBillDO.getGenerateCount());
        periodBillDTO.setAmount(periodBillDO.getAmount().toString());
        periodBillDTO.setDescription(periodBillDO.getDescription());
        periodBillDTO.setCategory(periodBillDO.getCategory());
        return periodBillDTO;
    }

    /**
     * 获得账单列表 分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param category
     * @param largeItem 空的时候不筛选
     * @param billType
     * @return
     */
    public PageInfo<BillDTO> getBillList(Integer pageNum, Integer pageSize, Long userId, String category,
                                         Date startTime, Date endTime, Boolean largeItem,
                                         String billType) {
        PageInfo<BillDTO> res = new PageInfo<>();
        res.setPageNum(pageSize);
        res.setPageSize(pageNum);
        res.setTotal(0);

        List<BillDO> billDOList = billManager.queryByBillType(userId, billType, pageNum, pageSize,
                startTime, endTime, largeItem, category);
        if (CollectionUtils.isEmpty(billDOList)) {
            return res;
        }
        List<BillDTO> billDTOS = billDOList.stream().map(this::convertBillDTO).collect(Collectors.toList());
        Long count = billManager.countByType(userId, billType, startTime, endTime, category);
        res.setTotal(count);
        res.setList(billDTOS);
        return res;
    }

    public Boolean updateBill(Long userId, UpdateBillReq updateBillReq) {
        BillDO billDOInDB = billManager.queryBillById(updateBillReq.getBillId());
        if (billDOInDB == null || billDOInDB.getUserId() == null || !billDOInDB.getUserId().equals(userId)) {
            return false;
        }
        if (Boolean.TRUE.equals(updateBillReq.getDeleted())) {
            // 删除数据
            billManager.deleteById(updateBillReq.getBillId(), userId.toString());
        } else {
//            BillProduceWayTypeEnum.getByName(updateBillReq.getProduceWayType())
            if (BillTypeEnum.getByName(updateBillReq.getBillType()) == null) {
                log.warn("BillTypeEnum or BillProduceWayTypeEnum param error, billType:{}, produceWayType:{}",
                        updateBillReq.getBillType(), updateBillReq.getProduceWayType());
                throw new CustomException(BackEnum.PARAM_ERROR);
            }
            // 更新数据
            BillDO billDOUpdate = new BillDO();
            billDOUpdate.setId(updateBillReq.getBillId());
            billDOUpdate.setGmtModify(new Date());
            billDOUpdate.setModifier(userId.toString());
            billDOUpdate.setProduceTime(updateBillReq.getProduceTime());
            billDOUpdate.setBillType(updateBillReq.getBillType());
            billDOUpdate.setProduceWay(updateBillReq.getProduceWayType());
            billDOUpdate.setAmount(updateBillReq.getAmount());
            billDOUpdate.setDescription(updateBillReq.getDesc());
            billDOUpdate.setCategory(updateBillReq.getCategory());
            billDOUpdate.setLargeItem(updateBillReq.getLargeItem());
            billManager.updateBillById(billDOUpdate);
        }
        return true;
    }

    /**
     * 根据配置文件获得金额对应的类型
     *
     * @param userId
     * @param amount
     * @return
     */
    public List<String> getConsumptionTypeService(Long userId, String amount, String desc) {
        List<String> resultList = new ArrayList<>();

        // 从配置文件查询类型 - 金额范围是否对应上，对应上则返回对应配置的类型
        List<BillConfigDTO> billConfigDTOS = configManager.getBillConfig(userId);
        Optional.ofNullable(billConfigDTOS).orElse(Collections.emptyList()).forEach(item -> {
            String[] priceScopes = item.getPriceScope().split("-");
            if (priceScopes.length != 2) {
                log.warn("priceScopes parse error, billConfig:{}", JSON.toJSONString(item));
                return;
            }
            int lowPrice = Integer.parseInt(priceScopes[0]);
            int highPrice = Integer.parseInt(priceScopes[1]);
            if (lowPrice <= Double.parseDouble(amount) && Double.parseDouble(amount) <= highPrice) {
                if (!resultList.contains(item.getCategory())) {
                    resultList.add(item.getCategory());
                }
            }
        });

        // 从历史提交记录查询对应类型 - 查相同金额且相同描述的账单 没有则查金额相同或描述相同的账单
        List<BillDO> billDOS = billManager.queryByDescAndAmount(userId, new BigDecimal(amount), desc);
        if (CollectionUtils.isEmpty(billDOS)) {
            billDOS = billManager.queryByDescAndAmount(userId, null, desc);
        }
        if (!CollectionUtils.isEmpty(billDOS)) {
            // 有相同描述相同金额 或 相同描述的优先放入该类型
            Map<String, List<BillDO>> billMap = billDOS.stream().collect(Collectors.groupingBy(BillDO::getCategory));
            int maxSize = -1;
            String result = "";
            for (Map.Entry<String, List<BillDO>> entry : billMap.entrySet()) {
                String category = entry.getKey();
                int size = entry.getValue().size();
                if (maxSize < size) {
                    result = category;
                    maxSize = size;
                }
            }
            resultList.add(0, result);
        }
        // 去重返回
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 账单统计
     *
     * @param userId
     * @param startDate
     * @param endDate
     * @param largeItem
     * @return
     */
    public List<BillStatisticsDTO> getBillStatistics(Long userId, Date startDate, Date endDate, Boolean largeItem,
                                                     Boolean periodBill, String billType) {
        List<BillStatisticsDTO> res = new ArrayList<>();
        List<BillDO> billDOList = billManager.queryByDate(userId, startDate, endDate, billType);
        if (CollectionUtils.isEmpty(billDOList)) {
            // billDOList 空数据处理
            return Collections.emptyList();
        }
        if (Boolean.TRUE.equals(largeItem)) {
            // 大件统计数据
            double largeItemAmount = 0.0;
            // 属于大件的金额
            largeItemAmount = billDOList.stream().filter(BillDO::getLargeItem)
                    .mapToDouble(billDO -> billDO.getAmount().doubleValue()).sum();
            Date earliestTime = billDOList.get(0).getProduceTime();
            Date latestTime = billDOList.get(billDOList.size() - 1).getProduceTime();

            BillStatisticsDTO largeItemStatisticsDTO = new BillStatisticsDTO();
            largeItemStatisticsDTO.setCategory(LARGE_ITEM);
            largeItemStatisticsDTO.setAmount(new BigDecimal(largeItemAmount).setScale(2, BigDecimal.ROUND_HALF_DOWN));

            BillStatisticsDTO monthLargeItemStatisticsDTO = new BillStatisticsDTO();
            monthLargeItemStatisticsDTO.setCategory(AVE_MONTH_LARGE_ITEM);
            monthLargeItemStatisticsDTO.setAmount(new BigDecimal(
                    largeItemAmount / (Utils.getApartMonths(earliestTime, latestTime) + 1))
                    .setScale(2, BigDecimal.ROUND_HALF_DOWN));

            BillStatisticsDTO dayLargeItemStatisticsDTO = new BillStatisticsDTO();
            dayLargeItemStatisticsDTO.setCategory(AVE_DAY_LARGE_ITEM);
            dayLargeItemStatisticsDTO.setAmount(new BigDecimal(
                    largeItemAmount / (Utils.getApartDays(earliestTime, latestTime) + 1))
                    .setScale(2, BigDecimal.ROUND_HALF_DOWN));
            res.add(largeItemStatisticsDTO);
            res.add(monthLargeItemStatisticsDTO);
            res.add(dayLargeItemStatisticsDTO);
            log.info("record earliestTime:{}, latestTime:{}, calMonth:{}, calDays:{}",
                    earliestTime, latestTime,
                    Utils.getApartMonths(earliestTime, latestTime) + 1,
                    Utils.getApartDays(earliestTime, latestTime) + 1);
            return res;
        } else if (Boolean.TRUE.equals(periodBill)) {
            // 支持 纬度进行查询，年/月/周/日
            // 根据月份查
            List<PeriodBillDO> periodBillDOS = periodBillManager.queryPeriodBillListByUserId(userId);
            Map<String, List<PeriodBillDO>> periodBillMapByCategory = periodBillDOS.stream().collect(Collectors.groupingBy(PeriodBillDO::getCategory));
            periodBillMapByCategory.forEach((category, periodBillDOList) -> {
                BigDecimal amountByCategory = periodBillDOList.stream().map(PeriodBillDO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BillStatisticsDTO billStatisticsDTO = new BillStatisticsDTO();
                billStatisticsDTO.setCategory(category);
                billStatisticsDTO.setAmount(amountByCategory);
                res.add(billStatisticsDTO);
            });
            BigDecimal totalAmount = res.stream().map(BillStatisticsDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BillStatisticsDTO billStatisticsDTO = new BillStatisticsDTO();
            billStatisticsDTO.setAmount(totalAmount);
            billStatisticsDTO.setCategory(TOTAL_AMOUNT_CATEGORY);
            res.add(0, billStatisticsDTO);
            return res;
        } else {
            Map<String, List<BillDO>> billGroupByCategoryMap = Optional.of(billDOList).orElse(
                    new ArrayList<>(0)).stream().collect(Collectors.groupingBy(BillDO::getCategory));
            Set<Map.Entry<String, List<BillDO>>> entries = billGroupByCategoryMap.entrySet();

            double totalAmount = 0.0;
            for (Map.Entry<String, List<BillDO>> entry : entries) {
                List<BillDO> billDOListInCategory = entry.getValue();
                if (CollectionUtils.isEmpty(billDOListInCategory)) {
                    continue;
                }
                double sum = billDOListInCategory.stream().mapToDouble(billDO -> billDO.getAmount().doubleValue()).sum();

                totalAmount += sum;
                BillStatisticsDTO billStatisticsDTO = new BillStatisticsDTO();
                billStatisticsDTO.setCategory(entry.getKey());
                billStatisticsDTO.setAmount(new BigDecimal(sum)
                        .setScale(2, BigDecimal.ROUND_HALF_DOWN));
                res.add(billStatisticsDTO);
            }
            BillStatisticsDTO billStatisticsDTO = new BillStatisticsDTO();
            billStatisticsDTO.setCategory(TOTAL_AMOUNT_CATEGORY);
            billStatisticsDTO.setAmount(new BigDecimal(totalAmount)
                    .setScale(2, BigDecimal.ROUND_HALF_DOWN));
            res.add(0, billStatisticsDTO);
            return res;
        }
    }

    @Transactional
    public void exportBillList(List<BillExcelDTO> excelDTOList, Long userId) {
        excelDTOList.forEach(item -> {
            BillDO billDO = convertBillDO(item, userId);
            billManager.insert(billDO);
        });
    }

    @Transactional
    public void addBillByPeriodBill(PeriodBillDO periodBillDO) {
        log.info("about to add period bill, periodBillDO:{}", JSON.toJSONString(periodBillDO));
        int count = periodBillManager.updateGenerateCountAndGenerateNextDate(periodBillDO.getGenerateCount(), periodBillDO.getId(),
                periodBillDO.getUserId(), periodBillDO.getNextAddTime());
        if (count == 0) {
            log.info("updateGenerateCount failed, generateCount:{},id:{}", periodBillDO.getGenerateCount(),
                    periodBillDO.getId());
            return;
        }
        BillDO billDO = convertBillDO(periodBillDO);
        billManager.insert(billDO);
    }

    private PeriodBillDO convertPeriodBillDO(AddPeriodBillReq addPeriodBillReq, Long userId) {
        BillTypeEnum billTypeEnum = BillTypeEnum.getByName(addPeriodBillReq.getBillType());
        BillProduceWayTypeEnum billProduceWayTypeEnum = BillProduceWayTypeEnum.getByName(
                addPeriodBillReq.getProduceWayType());
        if (billTypeEnum == null) {
            throw new CustomException(BackEnum.PARAM_ERROR);
        }
        Integer generateDay = addPeriodBillReq.getGenerateDay();
        Calendar cale = Calendar.getInstance();
        int curDay = cale.get(Calendar.DAY_OF_MONTH);
        int curMonth = cale.get(Calendar.MONTH);
        if (curDay > generateDay) {
            // 当前天大于生成天日期，下月生成账单,eg:当前5号，输入1-5号都直接生成下个月开始的周期账单
            cale.set(Calendar.MONTH, curMonth + 1);
        }
        cale.set(Calendar.DAY_OF_MONTH, generateDay);

        PeriodBillDO periodBillDO = new PeriodBillDO();
        periodBillDO.setUserId(userId);
        periodBillDO.setCreator(userId.toString());
        periodBillDO.setModifier(userId.toString());
        periodBillDO.setGenerateDay(addPeriodBillReq.getGenerateDay());
        periodBillDO.setGenerateCount(addPeriodBillReq.getGenerateCount());
        periodBillDO.setBillType(billTypeEnum.name());
        periodBillDO.setProduceWay(billProduceWayTypeEnum == null ? null : billProduceWayTypeEnum.name());
        periodBillDO.setAmount(new BigDecimal(String.valueOf(addPeriodBillReq.getAmount())));
        periodBillDO.setDescription(addPeriodBillReq.getDesc());
        periodBillDO.setCategory(addPeriodBillReq.getType());
        periodBillDO.setNextAddTime(cale.getTime());
        return periodBillDO;
    }

    private BillDO convertBillDO(BillExcelDTO billExcelDTO, Long userId) {
        BillProduceWayTypeEnum billProduceWayTypeEnum = BillProduceWayTypeEnum.getByDesc(billExcelDTO.getProductWay());
        if (billProduceWayTypeEnum == null) {
            billProduceWayTypeEnum = BillProduceWayTypeEnum.ALI_PAY;
        }
        Boolean isLargeCargo = billExcelDTO.getLargeCargo() != null && billExcelDTO.getLargeCargo().equals("1");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        BillDO billDO = new BillDO();
        billDO.setUserId(userId);
        billDO.setCreator(userId.toString());
        billDO.setModifier(userId.toString());
        try {
            billDO.setProduceTime(format.parse(billExcelDTO.getProduceTime()));
        } catch (ParseException e) {
            log.warn("parse bill failed");
            throw new CustomException(BackEnum.DATA_ERROR);
        }
        billDO.setBillType(BillTypeEnum.CONSUME.name());
        billDO.setProduceWay(billProduceWayTypeEnum.name()); // BillProduceWayTypeEnum
        billDO.setAmount(new BigDecimal(billExcelDTO.getAmount()));
        billDO.setDescription(billExcelDTO.getDescription());
        billDO.setCategory(StringUtils.isEmpty(billExcelDTO.getCategory()) ? "其他" : billExcelDTO.getCategory());
        billDO.setLargeItem(isLargeCargo);
        return billDO;
    }

    private BillDTO convertBillDTO(BillDO billDO) {
        if (billDO == null) {
            return null;
        }
        BillExtraDTO billExtraDTO = BillExtraDTO.convert(billDO.getExtra());

        BillDTO billDTO = new BillDTO();
        billDTO.setBillId(billDO.getId());
        billDTO.setProduceTime(billDO.getProduceTime());
        billDTO.setBillType(billDO.getBillType());
        billDTO.setProduceWayType(billDO.getProduceWay());
        billDTO.setAmount(billDO.getAmount());
        billDTO.setDesc(billDO.getDescription());
        billDTO.setCategory(billDO.getCategory());
        billDTO.setLargeItem(billDO.getLargeItem());
        billDTO.setPeriodBill(billExtraDTO != null && billExtraDTO.getPeriodBill());
        return billDTO;
    }


    private BillDO convertBillDO(PeriodBillDO periodBillDO) {
        BillExtraDTO billExtraDTO = new BillExtraDTO();
        billExtraDTO.setPeriodBill(true);

        BillDO billDO = new BillDO();
        billDO.setUserId(periodBillDO.getUserId());
        billDO.setCreator("system");
        billDO.setModifier("system");
        billDO.setProduceTime(new Date());
        billDO.setBillType(periodBillDO.getBillType());
        billDO.setProduceWay(periodBillDO.getProduceWay());
        billDO.setAmount(periodBillDO.getAmount());
        billDO.setDescription(periodBillDO.getDescription());
        billDO.setCategory(periodBillDO.getCategory());
        billDO.setLargeItem(false);
        billDO.setExtra(BillExtraDTO.convert(billExtraDTO));
        return billDO;
    }
}
