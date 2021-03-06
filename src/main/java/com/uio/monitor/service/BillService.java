package com.uio.monitor.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.uio.monitor.common.BackEnum;
import com.uio.monitor.common.BillTypeEnum;
import com.uio.monitor.common.CustomException;
import com.uio.monitor.controller.req.AddBillReq;
import com.uio.monitor.controller.req.UpdateBillReq;
import com.uio.monitor.controller.resp.BillConfigDTO;
import com.uio.monitor.controller.resp.BillDTO;
import com.uio.monitor.controller.resp.BillStatisticsDTO;
import com.uio.monitor.entity.BillDO;
import com.uio.monitor.manager.BillManager;
import com.uio.monitor.manager.ConfigManager;
import com.uio.monitor.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

    /**
     * 插入一条账单记录
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
     * 获得账单列表 分页查询
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
     * @param userId
     * @param amount
     * @return
     */
    public List<String> getConsumptionTypeService(Long userId, String amount, String desc) {
        List<String> resultList = new ArrayList<>();

        // 从配置文件查询类型
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

        // 从历史提交记录查询对应类型
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

        return resultList;
    }

    public List<BillStatisticsDTO> getBillStatistics(Long userId, Date startDate, Date endDate, Boolean largeItem) {
        List<BillStatisticsDTO> res = new ArrayList<>();
        List<BillDO> billDOList = billManager.queryByDate(userId, startDate, endDate);
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
        } else {
            Map<String, List<BillDO>> billGroupByCategoryMap = billDOList.stream().collect(Collectors.groupingBy(BillDO::getCategory));
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

    private BillDTO convertBillDTO(BillDO billDO) {
        BillDTO billDTO = new BillDTO();
        billDTO.setBillId(billDO.getId());
        billDTO.setProduceTime(billDO.getProduceTime());
        billDTO.setBillType(billDO.getBillType());
        billDTO.setProduceWayType(billDO.getProduceWay());
        billDTO.setAmount(billDO.getAmount());
        billDTO.setDesc(billDO.getDescription());
        billDTO.setCategory(billDO.getCategory());
        billDTO.setLargeItem(billDO.getLargeItem());
        return billDTO;
    }

}
