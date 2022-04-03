package com.uio.monitor.manager;

import com.uio.monitor.mapper.GetHomeDOMapper;
import com.uio.monitor.entity.GetHomeDO;
import com.uio.monitor.entity.GetHomeDOExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author han xun
 * Date 2021/10/12 22:28
 * Description:
 */
@Component
public class GetHomeManager {

    @Autowired
    private GetHomeDOMapper getHomeDOMapper;

    public void insertOrUpdate(GetHomeDO getHomeDO) {
        if (getHomeDO == null) {
            return;
        }
        if (getHomeDO.getId() == null) {
            getHomeDO.setGmtCreate(new Date());
            getHomeDO.setGmtModify(new Date());
            getHomeDO.setDeleteTag(false);
            if (getHomeDO.getCreator() == null) {
                getHomeDO.setCreator("system");
            }
            getHomeDOMapper.insert(getHomeDO);
        } else {
            getHomeDO.setGmtModify(new Date());
            getHomeDOMapper.updateByPrimaryKeySelective(getHomeDO);
        }
    }

    public List<GetHomeDO> getTodayRecord(Date dayStart, Date dayEnd) {
        GetHomeDOExample example = new GetHomeDOExample();
        GetHomeDOExample.Criteria criteria = example.createCriteria();
        criteria.andArriveTimeBetween(dayStart, dayEnd);
        return getHomeDOMapper.selectByExample(example);
    }
}
