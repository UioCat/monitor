package com.uio.monitor.manager;

import com.github.pagehelper.PageHelper;
import com.uio.monitor.mapper.BillDOMapper;
import com.uio.monitor.entity.BillDO;
import com.uio.monitor.entity.BillDOExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author han xun
 * Date 2022/1/2 13:07
 * Description:
 */
@Repository
public class BillManager {

    @Autowired
    private BillDOMapper billDOMapper;

    public void insert(BillDO billDO) {
        billDO.setGmtCreate(new Date());
        billDO.setGmtModify(new Date());
        billDO.setDeleted(false);
        billDOMapper.insert(billDO);
    }

    public void deleteById(Long id, String modifier) {
        BillDO billDO = new BillDO();
        billDO.setId(id);
        billDO.setGmtModify(new Date());
        billDO.setModifier(modifier);
        billDO.setDeleted(true);
        billDOMapper.updateByPrimaryKeySelective(billDO);
    }

    public List<BillDO> queryByBillType(Long userId, String billType, Integer pageNum, Integer pageSize,
        String category) {

        PageHelper.startPage(pageNum, pageSize);
        BillDOExample example = new BillDOExample();
        BillDOExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(category)) {
            criteria.andCategoryEqualTo(category);
        }
        criteria.andUserIdEqualTo(userId);
        criteria.andBillTypeEqualTo(billType);
        criteria.andDeletedEqualTo(false);
        example.setOrderByClause("gmt_create desc");
        return billDOMapper.selectByExample(example);
    }

    public List<BillDO> queryByDescAndAmount(Long userId, BigDecimal amount, String desc) {
        // 查询最近匹配的20条记录
        PageHelper.startPage(1, 20);
        BillDOExample example = new BillDOExample();
        BillDOExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        criteria.andDeletedEqualTo(false);
        if (!Objects.isNull(amount)) {
            criteria.andAmountEqualTo(amount);
        }
        criteria.andDescriptionEqualTo(desc);
        example.setOrderByClause("gmt_create desc");
        return billDOMapper.selectByExample(example);
    }

    public BillDO queryBillById(Long billId) {
        if (billId == null) {
            return null;
        }
        return billDOMapper.selectByPrimaryKey(billId);
    }

    public void updateBillById(BillDO billDO) {
        billDOMapper.updateByPrimaryKeySelective(billDO);
    }

    public Long countByType(Long userId, String billType, String category) {
        BillDOExample example = new BillDOExample();
        BillDOExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(category)) {
            criteria.andCategoryEqualTo(category);
        }
        criteria.andUserIdEqualTo(userId);
        criteria.andBillTypeEqualTo(billType);
        criteria.andDeletedEqualTo(false);
        return billDOMapper.countByExample(example);
    }

    public List<BillDO> queryByDate(Long userId, Date startDate, Date endDate) {
        BillDOExample example = new BillDOExample();
        BillDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andUserIdEqualTo(userId);
        criteria.andProduceTimeBetween(startDate, endDate);
        return billDOMapper.selectByExample(example);
    }
}
