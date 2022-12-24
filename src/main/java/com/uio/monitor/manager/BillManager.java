package com.uio.monitor.manager;

import com.github.pagehelper.PageHelper;
import com.uio.monitor.mapper.BillDOMapper;
import com.uio.monitor.entity.BillDO;
import com.uio.monitor.entity.BillDOExample;
import com.uio.monitor.mapper.BillDOMapperExtra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
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
    @Autowired
    private BillDOMapperExtra billDOMapperExtra;

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

    public void updateBillById(BillDO billDO) {
        billDOMapper.updateByPrimaryKeySelective(billDO);
    }

    /**
     * 根据类别、种类 分页查询
     * @param userId
     * @param billType
     * @param pageNum
     * @param pageSize
     * @param category
     * @return
     */
    public List<BillDO> queryByBillType(Long userId, String billType, Integer pageNum, Integer pageSize,
        Date startTime, Date endTime, Boolean largeItem,
        String category) {

        PageHelper.startPage(pageNum, pageSize);
        BillDOExample example = new BillDOExample();
        BillDOExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(category)) {
            criteria.andCategoryEqualTo(category);
        }
        if (startTime != null && endTime != null) {
            criteria.andProduceTimeBetween(startTime, endTime);
        }
        if (largeItem != null) {
            criteria.andLargeItemEqualTo(largeItem);
        }
        if (!StringUtils.isEmpty(billType)) {
            criteria.andBillTypeEqualTo(billType);
        }
        criteria.andUserIdEqualTo(userId);
        criteria.andDeletedEqualTo(false);
        example.setOrderByClause("gmt_create desc");
        return billDOMapper.selectByExample(example);
    }

    /**
     * 查询用户的账单数量
     * @param userId
     * @param billType
     * @param category
     * @return
     */
    public Long countByType(Long userId, String billType,
                            Date startTime, Date endTime,
                            String category) {
        BillDOExample example = new BillDOExample();
        BillDOExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(category)) {
            criteria.andCategoryEqualTo(category);
        }
        if (startTime != null && endTime != null) {
            criteria.andProduceTimeBetween(startTime, endTime);
        }
        if (!StringUtils.isEmpty(billType)) {
            criteria.andBillTypeEqualTo(billType);
        }
        criteria.andDeletedEqualTo(false);
        criteria.andUserIdEqualTo(userId);
        return billDOMapper.countByExample(example);
    }

    /**
     * 查询最近的20条记录
     * @param userId
     * @param amount
     * @param desc
     * @return
     */
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

    /**
     * 根据ID查询
     * @param billId
     * @return
     */
    public BillDO queryBillById(Long billId) {
        if (billId == null) {
            return null;
        }
        return billDOMapper.selectByPrimaryKey(billId);
    }

    /**
     * 根据日期查询账单
     * @param userId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<BillDO> queryByDate(Long userId, Date startDate, Date endDate, String billType) {
        BillDOExample example = new BillDOExample();
        BillDOExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        if (startDate != null || endDate != null) {
            criteria.andProduceTimeBetween(startDate, endDate);
        }
        if (!StringUtils.isEmpty(billType)) {
            criteria.andBillTypeEqualTo(billType);
        }
        criteria.andDeletedEqualTo(false);
        example.setOrderByClause("produce_time ASC");
        return billDOMapper.selectByExample(example);
    }

    public List<String> queryAllCategoryByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return billDOMapperExtra.queryUserCategoryInBill(userId);
    }
}
