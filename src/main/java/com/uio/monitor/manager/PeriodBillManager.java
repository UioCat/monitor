package com.uio.monitor.manager;

import com.uio.monitor.entity.PeriodBillDO;
import com.uio.monitor.entity.PeriodBillDOExample;
import com.uio.monitor.mapper.PeriodBillDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
        Calendar cal = Calendar.getInstance();
        int curDay = cal.get(Calendar.DAY_OF_MONTH);
        PeriodBillDOExample example = new PeriodBillDOExample();
        PeriodBillDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andGenerateDayLessThanOrEqualTo(curDay);
        criteria.andGenerateCountGreaterThan(0);
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

    public int updateGenerateCount(Integer oldCount, Long id, Long userId) {
        PeriodBillDOExample example = new PeriodBillDOExample();
        PeriodBillDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andIdEqualTo(id);
        criteria.andGenerateCountEqualTo(oldCount);

        PeriodBillDO periodBillDO = new PeriodBillDO();
        periodBillDO.setGmtModify(new Date());
        periodBillDO.setModifier(userId.toString());
        periodBillDO.setGenerateCount(--oldCount);

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
