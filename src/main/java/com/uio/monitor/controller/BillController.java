package com.uio.monitor.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.uio.monitor.common.*;
import com.uio.monitor.controller.base.BaseController;
import com.uio.monitor.controller.req.AddBillReq;
import com.uio.monitor.controller.req.AddPeriodBillReq;
import com.uio.monitor.controller.req.DeleteConfigReq;
import com.uio.monitor.controller.req.UpdateBillReq;
import com.uio.monitor.controller.resp.BillConfigDTO;
import com.uio.monitor.controller.resp.BillDTO;
import com.uio.monitor.controller.resp.BillStatisticsDTO;
import com.uio.monitor.controller.resp.PeriodBillDTO;
import com.uio.monitor.manager.BillManager;
import com.uio.monitor.manager.ConfigManager;
import com.uio.monitor.service.BillService;
import com.uio.monitor.vo.BillExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author han xun
 * Date 2022/1/2 14:02
 * Description:
 */
@RestController
@RequestMapping("/bill")
@Slf4j
public class BillController extends BaseController {

    @Autowired
    private BillService billService;
    @Autowired
    private ConfigManager configManager;
    @Autowired
    private BillManager billManager;


    /**
     * 获得账单类型
     * @return
     */
    @GetMapping("/getBillProduceWayType")
    public BackMessage<List<JSONObject>> getBillProduceWayType() {
        Long userId = super.getUserId();
        List<JSONObject> res = new ArrayList<>();
        for (BillProduceWayTypeEnum billProduceWayTypeEnum : BillProduceWayTypeEnum.values()) {
            JSONObject item = new JSONObject();
            item.put("value", billProduceWayTypeEnum.name());
            item.put("name", billProduceWayTypeEnum.getDesc());
        }
        return BackMessage.success(res);
    }


    /**
     * 添加一笔账单 - 收入/支出都支持
     * @param addBillReq
     * @return
     */
    @PostMapping("/addBill")
    public BackMessage<Boolean> addBill(@RequestBody @Valid AddBillReq addBillReq) {
        BillTypeEnum billType = BillTypeEnum.getByName(addBillReq.getBillType());
        if (billType == null) {
            throw new CustomException(BackEnum.PARAM_ERROR);
        }
        Boolean res = billService.addBill(addBillReq, super.getUserId());
        return BackMessage.success(res);
    }

    /**
     * 添加一笔周期性账单 - 收入/支出都支持
     * @param addPeriodBillReq
     * @return
     */
    @PostMapping("/addPeriodBill")
    public BackMessage<Void> addPeriodBill(@RequestBody @Valid AddPeriodBillReq addPeriodBillReq) {
        Long userId = super.getUserId();
        billService.addPeriodBill(addPeriodBillReq, userId);
        return BackMessage.success();
    }

    /**
     * 查询周期性账单列表 - 收入/支出都支持
     * @return
     */
    @GetMapping("/queryPeriodBillList")
    BackMessage<List<PeriodBillDTO>> queryPeriodBillList() {
        Long userId = super.getUserId();
        List<PeriodBillDTO> periodBillListByUserId = billService.getPeriodBillListByUserId(userId);
        return BackMessage.success(periodBillListByUserId);
    }

    /**
     * 插入/更新 bill config
     * @param billConfigDTO
     * @return
     */
    @PostMapping("/addBillConfig")
    public BackMessage<Boolean> addBillConfig(@RequestBody @Valid BillConfigDTO billConfigDTO) {
        Long userId = super.getUserId();
        if (userId == null) {
            throw new CustomException(BackEnum.UNAUTHORIZED);
        }
        configManager.insertBillConfig(super.getUserId(), billConfigDTO);
        return BackMessage.success(true);
    }

    /**
     * 获取用户bill配置列表
     * @return
     */
    @GetMapping("/getBillConfigList")
    public BackMessage<List<BillConfigDTO>> getBillConfig() {
        Long userId = super.getUserId();
        List<BillConfigDTO> billConfigDTOS = configManager.getBillConfig(userId);
        return BackMessage.success(billConfigDTOS);
    }

    /**
     * 删除一条配置数据
     * @param deleteConfigReq
     * @return
     */
    @PostMapping("/deleteBillConfig")
    public BackMessage<Boolean> deleteBillConfig(@RequestBody @Valid DeleteConfigReq deleteConfigReq) {
        Boolean aBoolean = configManager.deleteBillConfig(super.getUserId(), deleteConfigReq.getConfigId());
        return BackMessage.success(aBoolean);
    }

    /**
     * 根据金额获取对应类型
     * @param amount
     * @return
     */
    @GetMapping("/getConsumptionType")
    public BackMessage<List<String>> getConsumptionType(@RequestParam("amount") String amount,
        @RequestParam("desc") String desc) {
        Long userId = super.getUserId();
        return BackMessage.success(billService.getConsumptionTypeService(userId, amount, desc));
    }

    /**
     * 获取所有类型
     * @return
     */
    @GetMapping("/getAllConsumptionType")
    public BackMessage<List<String>> getAllConsumptionType() {
        Long userId = super.getUserId();
        List<BillConfigDTO> billConfigDTOS = configManager.getBillConfig(userId);

        Set<String> categoryList = Optional.ofNullable(billConfigDTOS).orElse(Collections.emptyList()).stream()
                .map(BillConfigDTO::getCategory).collect(Collectors.toSet());
        categoryList.addAll(billManager.queryAllCategoryByUserId(userId));
        return BackMessage.success(new ArrayList<>(categoryList));
    }

    /**
     * 获取账单列表 - 收入/支出都支持
     * @param largeItem 空的时候不筛选
     */
    @GetMapping("/getBillList")
    public BackMessage<PageInfo<BillDTO>> getBillList(
        @RequestParam(value = "pageNum", required = false) Integer pageNum,
        @RequestParam(value = "pageSize", required = false) Integer pageSize,
        @RequestParam(value = "category", required = false) String category,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "startTime", required = false) Date startTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "endTime", required = false) Date endTime,
        @RequestParam(value = "largeItem", required = false) Boolean largeItem,
        @RequestParam(value = "type", required = true) String type,
        @RequestParam(value = "orderBy", required = false) String orderBy) {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
        orderBy = StringUtils.isEmpty(orderBy) ? "produce_time DESC" : orderBy;
        Long userId = super.getUserId();
        PageInfo<BillDTO> billList = billService.getBillList(pageNum, pageSize, userId, category,
                startTime, endTime, largeItem, type, orderBy);
        return BackMessage.success(billList);
    }

    /**
     * 更新账单
     * @param updateBillReq
     * @return
     */
    @PostMapping("/updateBill")
    public BackMessage<Boolean> updateBill(@RequestBody UpdateBillReq updateBillReq) {
        Long userId = super.getUserId();
        return BackMessage.success(billService.updateBill(userId, updateBillReq));
    }

    /**
     * 获取账单统计数据 - 收入/支出都支持
     * @return
     */
    @GetMapping("/getStatistics")
    public BackMessage<List<BillStatisticsDTO>> getStatistics(
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "startDate", required = false) Date startDate,
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(value = "endDate", required = false) Date endDate,
        @RequestParam(value = "largeItem", required = false) Boolean largeItem,
        @RequestParam(value = "periodBill", required = false) Boolean periodBill,
        @RequestParam(value = "type", required = false) String type) {
        if (StringUtils.isEmpty(type)) {
            type = BillTypeEnum.CONSUME.name();
        }
        List<BillStatisticsDTO> billStatistics = billService.getBillStatistics(super.getUserId(), startDate, endDate,
                largeItem, periodBill, type);
        return BackMessage.success(billStatistics);
    }

    /**
     * 从excel文件中读取账单数据
     */
    @PostMapping("/inputBillList")
    @ResponseBody
    public BackMessage<Void> upload(MultipartFile file)  {
        Long userId = super.getUserId();
        try {
            List<BillExcelDTO> billExcelDTOS = new ArrayList<>();
            EasyExcel.read(file.getInputStream(), BillExcelDTO.class, new PageReadListener<BillExcelDTO>(dataList -> {
                billExcelDTOS.addAll(dataList);
            })).sheet().doRead();
            billService.exportBillList(billExcelDTOS, userId);
            return BackMessage.success();
        } catch (IOException e) {
            log.error("read bill from excel io exception, ", e);
            throw new CustomException(BackEnum.UNKNOWN_ERROR);
        }
    }

}
