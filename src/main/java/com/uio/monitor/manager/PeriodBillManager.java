package com.uio.monitor.manager;

import com.uio.monitor.entity.PeriodBillDO;
import com.uio.monitor.entity.PeriodBillDOExample;
import com.uio.monitor.mapper.PeriodBillDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author han xun
 * Date 2022/8/31 00:16
 * Description:
 */
@Repository
public class PeriodBillManager {

    @Autowired
    private PeriodBillDOMapper periodBillDOMapper;

    public void insert(PeriodBillDO periodBillDO) {
        periodBillDO.setGmtModify(new Date());
        periodBillDO.setGmtCreate(new Date());
        periodBillDO.setDeleted(false);
        periodBillDOMapper.insert(periodBillDO);
    }

    public List<PeriodBillDO> queryPeriodBillListByUserId(Long userId) {
        PeriodBillDOExample example = new PeriodBillDOExample();
        PeriodBillDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andUserIdEqualTo(userId);
        return periodBillDOMapper.selectByExample(example);
    }

    /**
     * 查询当天要生成的账单
     * @return
     */
    public List<PeriodBillDO> queryExpirePeriodBill() {
        PeriodBillDOExample example = new PeriodBillDOExample();
        PeriodBillDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andGenerateCountGreaterThan(0);
        criteria.andNextAddTimeLessThan(new Date());
        return periodBillDOMapper.selectByExample(example);
    }

    public List<PeriodBillDO> queryDisablePeriodBillByUserId(Long userId) {
        PeriodBillDOExample example = new PeriodBillDOExample();
        PeriodBillDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andGenerateCountEqualTo(0);
        criteria.andUserIdEqualTo(userId);
        return periodBillDOMapper.selectByExample(example);
    }

    public int updateGenerateCountAndGenerateNextDate(Integer oldCount, Long id, Long userId, Date curNextGenerateDate) {
        PeriodBillDOExample example = new PeriodBillDOExample();
        PeriodBillDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andIdEqualTo(id);
        criteria.andGenerateCountEqualTo(oldCount);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(curNextGenerateDate);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);

        PeriodBillDO periodBillDO = new PeriodBillDO();
        periodBillDO.setGmtModify(new Date());
        periodBillDO.setModifier(userId.toString());
        periodBillDO.setGenerateCount(--oldCount);
        periodBillDO.setNextAddTime(calendar.getTime());

        return periodBillDOMapper.updateByExampleSelective(periodBillDO, example);
    }

    public void delete(Long id, Long userId) {
        PeriodBillDO periodBillDO = new PeriodBillDO();
        periodBillDO.setId(id);
        periodBillDO.setGmtModify(new Date());
        periodBillDO.setModifier(userId.toString());
        periodBillDO.setDeleted(true);
        periodBillDOMapper.updateByPrimaryKeySelective(periodBillDO);
    }
}
