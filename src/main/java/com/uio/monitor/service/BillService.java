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
        billManager.insert(billDO);
        return true;
    }

    /**
     * 获得账单列表 分页查询
     * @param pageNum
     * @param pageSize
     * @param userId
     * @param category
     * @param billType
     * @return
     */
    public PageInfo<BillDTO> getBillList(Integer pageNum, Integer pageSize, Long userId, String category,
        Date startTime, Date endTime,
        String billType) {
        PageInfo<BillDTO> res = new PageInfo<>();
        res.setPageNum(pageSize);
        res.setPageSize(pageNum);
        res.setTotal(0);

        List<BillDO> billDOList = billManager.queryByBillType(userId, billType, pageNum, pageSize,
                startTime, endTime, category);
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
        Set<String> resultList = new HashSet<>();
        // 查询历史提交记录
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
            resultList.add(result);
        }

        List<BillConfigDTO> billConfigDTOS = configManager.getBillConfig(userId);
        if (CollectionUtils.isEmpty(billConfigDTOS)) {
            return null;
        }
        resultList.addAll(billConfigDTOS.stream().map(item -> {
            String[] priceScopes = item.getPriceScope().split("-");
            if (priceScopes.length != 2) {
                return null;
            }
            int lowPrice = Integer.parseInt(priceScopes[0]);
            int highPrice = Integer.parseInt(priceScopes[1]);
            if (lowPrice <= Double.parseDouble(amount) && Double.parseDouble(amount) <= highPrice) {
                return item.getCategory();
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()));

        return new ArrayList<>(resultList);
    }

    public List<BillStatisticsDTO> getBillStatistics(Long userId, Date startDate, Date endDate) {
        List<BillDO> billDOList = billManager.queryByDate(userId, startDate, endDate);
        Map<String, List<BillDO>> billGroupByCategoryMap = billDOList.stream().collect(Collectors.groupingBy(BillDO::getCategory));
        Set<Map.Entry<String, List<BillDO>>> entries = billGroupByCategoryMap.entrySet();

        List<BillStatisticsDTO> res = new ArrayList<>(entries.size());
        Double totalAmount = 0.0;
        for (Map.Entry<String, List<BillDO>> entry : entries) {
            List<BillDO> billDOListInCategory = entry.getValue();
            if (CollectionUtils.isEmpty(billDOListInCategory)) {
                continue;
            }
            double sum = billDOListInCategory.stream().mapToDouble(billDO -> billDO.getAmount().doubleValue()).sum();
            totalAmount += sum;
            BillStatisticsDTO billStatisticsDTO = new BillStatisticsDTO();
            billStatisticsDTO.setCategory(entry.getKey());
            billStatisticsDTO.setAmount(sum);
            res.add(billStatisticsDTO);
        }
        BillStatisticsDTO billStatisticsDTO = new BillStatisticsDTO();
        billStatisticsDTO.setCategory(TOTAL_AMOUNT_CATEGORY);
        billStatisticsDTO.setAmount(totalAmount);
        res.add(0, billStatisticsDTO);
        return res;
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
        return billDTO;
    }

}
