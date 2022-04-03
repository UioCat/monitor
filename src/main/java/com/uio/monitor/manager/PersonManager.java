package com.uio.monitor.manager;

import com.uio.monitor.mapper.PersonDOMapper;
import com.uio.monitor.entity.PersonDO;
import com.uio.monitor.entity.PersonDOExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author han xun
 * Date 2021/10/12 22:28
 * Description:
 */
@Component
public class PersonManager {

    @Autowired
    private PersonDOMapper personDOMapper;

    public List<PersonDO> getPersonWithEffect() {
        PersonDOExample example = new PersonDOExample();
        PersonDOExample.Criteria criteria = example.createCriteria();
        criteria.andEffectEqualTo(true);
        return personDOMapper.selectByExample(example);
    }



}
