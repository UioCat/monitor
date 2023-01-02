package com.uio.monitor;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.controller.resp.BillConfigDTO;
import com.uio.monitor.entity.BillDO;
import com.uio.monitor.entity.PeriodBillDO;
import com.uio.monitor.manager.BillManager;
import com.uio.monitor.manager.ConfigManager;
import com.uio.monitor.manager.PeriodBillManager;
import com.uio.monitor.service.BillService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author han xun
 * @date 2022-03-23 14:44
 */
@Slf4j
@SpringBootTest
public class BillServiceTest {

    private BillService billService = new BillService();
    @Autowired
    private PeriodBillManager periodBillManager;

    @Test
    public void getConsumptionTypeService() {
        initBillService(billService);

        List<String> resultList = billService.getConsumptionTypeService(null, "10", "test");
        log.info("resultList:{}", JSON.toJSONString(resultList));
        Assert.isTrue(JSON.toJSONString(resultList).equals("[\"种类10-20\"]"),
            "根据配置文件获取类型case1失败");

        resultList = billService.getConsumptionTypeService(null, "35", "test");
        log.info("resultList:{}", JSON.toJSONString(resultList));
        Assert.isTrue(JSON.toJSONString(resultList).equals("[\"种类30-40\",\"种类35-50\"]"),
            "根据配置文件获取类型case2失败");

        resultList = billService.getConsumptionTypeService(null, "35", "test");
        log.info("resultList:{}", JSON.toJSONString(resultList));
        Assert.isTrue(JSON.toJSONString(resultList).equals("[\"种类30-40\",\"种类35-50\"]"),
            "根据配置文件获取类型case3失败");

        resultList = billService.getConsumptionTypeService(null, "0", "测试描述0");
        log.info("resultList:{}", JSON.toJSONString(resultList));
        Assert.isTrue(JSON.toJSONString(resultList).equals("[\"种类-测试描述0-金额0\"]"),
            "DB数据测试case失败");

        resultList = billService.getConsumptionTypeService(null, "35", "种类35-50");
        log.info("resultList:{}", JSON.toJSONString(resultList));
        Assert.isTrue(JSON.toJSONString(resultList).equals("[\"种类3-测试描述数量2\",\"种类30-40\",\"种类35-50\"]"),
            "DB数据以及配置数据测试case失败");
    }

    @Test
    public void queryExpirePeriodBillTest() {
        List<PeriodBillDO> periodBillDOS = periodBillManager.queryExpirePeriodBill();
        log.info("result:{}", JSON.toJSONString(periodBillDOS));
    }

    /**
     * 初始化billService并为其依赖数据进行Mock
     * @param classInstance
     */
    private void initBillService(Object classInstance) {
        BillManager billManager = Mockito.mock(BillManager.class);
        Mockito.doReturn(this.mockBillList0()).when(billManager).queryByDescAndAmount(
            null, new BigDecimal(0), "测试描述0");
        Mockito.doReturn(this.mockBillList1()).when(billManager).queryByDescAndAmount(
            null, new BigDecimal("35"), "种类35-50");

        ConfigManager configManager = Mockito.mock(ConfigManager.class);
        Mockito.doReturn(this.mockBillConfigDTOList()).when(configManager).getBillConfig(Mockito.any());

        Class<?> clazz = classInstance.getClass();
        Field field = null;
        try {
            field = clazz.getDeclaredField("billManager");
            field.setAccessible(true);
            field.set(classInstance, billManager);

            field = clazz.getDeclaredField("configManager");
            field.setAccessible(true);
            field.set(classInstance, configManager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("exception happen,", e);
        }
    }

    private List<BillConfigDTO> mockBillConfigDTOList() {
        BillConfigDTO billConfigDTO1 = new BillConfigDTO();
        billConfigDTO1.setCategory("种类10-20");
        billConfigDTO1.setPriceScope("10-20");

        BillConfigDTO billConfigDTO2 = new BillConfigDTO();
        billConfigDTO2.setCategory("种类20-30");
        billConfigDTO2.setPriceScope("20-30");

        BillConfigDTO billConfigDTO3 = new BillConfigDTO();
        billConfigDTO3.setCategory("种类30-40");
        billConfigDTO3.setPriceScope("30-40");

        BillConfigDTO billConfigDTO4 = new BillConfigDTO();
        billConfigDTO4.setCategory("种类35-50");
        billConfigDTO4.setPriceScope("35-50");

        List<BillConfigDTO> billConfigDTOList = new ArrayList<>();
        billConfigDTOList.add(billConfigDTO1);
        billConfigDTOList.add(billConfigDTO2);
        billConfigDTOList.add(billConfigDTO3);
        billConfigDTOList.add(billConfigDTO4);
        return billConfigDTOList;
    }

    /**
     * mock billList数据
     * @return
     */
    private List<BillDO> mockBillList0() {
        BillDO billDO0 = new BillDO();

        billDO0.setAmount(new BigDecimal(0));
        billDO0.setDescription("测试描述0");
        billDO0.setCategory("种类-测试描述0-金额0");


        List<BillDO> billDOList = new ArrayList<>();
        billDOList.add(billDO0);
        return billDOList;
    }

    private List<BillDO> mockBillList1() {
        List<BillDO> billDOList = new ArrayList<>();

        BillDO billDO1 = new BillDO();
        BillDO billDO2 = new BillDO();
        BillDO billDO3 = new BillDO();
        BillDO billDO4 = new BillDO();

        billDO1.setAmount(new BigDecimal(10));
        billDO1.setDescription("种类35-50");
        billDO1.setCategory("种类1-测试描述数量1");

        billDO2.setAmount(new BigDecimal(20));
        billDO2.setDescription("种类35-50");
        billDO2.setCategory("种类2-测试描述数量1");

        billDO3.setAmount(new BigDecimal(40));
        billDO3.setDescription("种类35-50");
        billDO3.setCategory("种类3-测试描述数量2");

        billDO4.setAmount(new BigDecimal(40));
        billDO4.setDescription("种类35-50");
        billDO4.setCategory("种类3-测试描述数量2");
        billDOList.add(billDO1);
        billDOList.add(billDO2);
        billDOList.add(billDO3);
        billDOList.add(billDO4);
        return billDOList;
    }
}
