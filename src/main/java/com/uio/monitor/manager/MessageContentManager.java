package com.uio.monitor.manager;

import com.uio.monitor.entity.MessageContentDO;
import com.uio.monitor.entity.MessageContentDOExample;
import com.uio.monitor.mapper.MessageContentDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * @author han xun
 * Date 2022/5/5 14:05
 * Description:
 */
@Repository
public class MessageContentManager {

    @Autowired
    private MessageContentDOMapper messageContentDOMapper;

    public void insert(MessageContentDO messageContentDO) {
        messageContentDO.setGmtCreate(new Date());
        messageContentDO.setGmtModify(new Date());
        messageContentDO.setDeleted(false);
        messageContentDOMapper.insert(messageContentDO);
    }

    public MessageContentDO queryBySourceId(String source, String sourceId) {
        MessageContentDOExample example = new MessageContentDOExample();
        MessageContentDOExample.Criteria criteria = example.createCriteria();
        criteria.andSourceEqualTo(source);
        criteria.andSourceIdEqualTo(sourceId);
        return messageContentDOMapper.selectByExample(example).stream().findFirst().orElse(null);
    }
}
