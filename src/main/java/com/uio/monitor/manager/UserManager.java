package com.uio.monitor.manager;

import com.uio.monitor.entity.UserDO;
import com.uio.monitor.entity.UserDOExample;
import com.uio.monitor.mapper.UserDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author han xun
 * Date 2022/1/2 13:04
 * Description:
 */
@Repository
public class UserManager {

    @Autowired
    private UserDOMapper userDOMapper;

    public void insert(UserDO userDO) {
        userDO.setGmtCreate(new Date());
        userDO.setGmtModify(new Date());
        userDO.setDeleted(false);
        userDOMapper.insert(userDO);
    }

    public UserDO queryById(Long id) {
        return userDOMapper.selectByPrimaryKey(id);
    }

    public UserDO queryByAccount(String account) {
        UserDOExample example = new UserDOExample();
        UserDOExample.Criteria criteria = example.createCriteria();
        criteria.andAccountEqualTo(account);
        return userDOMapper.selectByExample(example).stream().findFirst().orElse(null);
    }
}
