package com.uio.monitor.timer;

import com.uio.monitor.common.CacheService;
import com.uio.monitor.constant.MonitorConstant;
import com.uio.monitor.constant.RedisConstant;
import com.uio.monitor.entity.BillDO;
import com.uio.monitor.entity.PeriodBillDO;
import com.uio.monitor.entity.dto.BillExtraDTO;
import com.uio.monitor.manager.BillManager;
import com.uio.monitor.manager.PeriodBillManager;
import com.uio.monitor.service.BillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author han xun
 * Date 2022/8/31 00:40
 * Description:
 */
@Component
@EnableScheduling
@Slf4j
public class PeriodBillTimer {

    @Autowired
    private BillService billService;
    @Autowired
    private PeriodBillManager periodBillManager;
    @Autowired
    private CacheService cacheService;

    /**
     * 一分钟
     */
    private final Long PERIOD_BILL_LOCK_TIME =  1000 * 60L;

    @Scheduled(cron = MonitorConstant.SCAN_PERIOD_BILL_CRON)
    public void autoGenerateBillTimer() {
        log.info("autoGenerateBillTimer trigger");
        List<PeriodBillDO> periodBillDOS = periodBillManager.queryExpirePeriodBill();
        String requestId = "";
        if (!CollectionUtils.isEmpty(periodBillDOS)) {
            for (PeriodBillDO periodBillDO : periodBillDOS) {
                requestId = UUID.randomUUID().toString();
                // 分布式锁
                String lockName = RedisConstant.getPeriodBillLock(periodBillDO.getId().toString());
                try {
                    boolean lock = cacheService.lock(lockName, requestId, String.valueOf(PERIOD_BILL_LOCK_TIME),
                            3);
                    if (lock) {
                        billService.addBillByPeriodBill(periodBillDO);
                    }
                } catch (Throwable t) {
                    log.error("addBillByPeriodBill exception, ", t);
                } finally {
                    cacheService.unLock(lockName, requestId);
                }
            }
        }
    }
}
