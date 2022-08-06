package com.uio.monitor.manager;

import com.uio.monitor.common.ProcessMessageContentStateEnum;
import com.uio.monitor.common.PushStateEnum;
import com.uio.monitor.entity.MessageContentDO;
import com.uio.monitor.entity.MessageContentDOExample;
import com.uio.monitor.entity.TimingMessageDOExample;
import com.uio.monitor.mapper.MessageContentDOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author han xun
 * Date 2022/5/5 14:05
 * Description:
 */
@Repository
public class MessageContentManager {

    @Autowired
    private MessageContentDOMapper messageContentDOMapper;

    /**
     * 处理超过 X分钟的消息都算处理失败，查询出来，记录数据，并重新处理
     */
    private static final Long SCAN_ABNORMAL_MESSAGE_TIME = 2 * 60 * 1000L;

    public void insert(MessageContentDO messageContentDO) {
        messageContentDO.setGmtCreate(new Date());
        messageContentDO.setGmtModify(new Date());
        messageContentDO.setDeleted(false);
        messageContentDOMapper.insert(messageContentDO);
    }

    public MessageContentDO queryBySourceId(String source, String sourceId) {
        MessageContentDOExample example = new MessageContentDOExample();
        MessageContentDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andSourceEqualTo(source);
        criteria.andSourceIdEqualTo(sourceId);
        return messageContentDOMapper.selectByExample(example).stream().findFirst().orElse(null);
    }

    public int updateState(Long id, ProcessMessageContentStateEnum originState,
                            ProcessMessageContentStateEnum expectState) {
        MessageContentDOExample example = new MessageContentDOExample();
        MessageContentDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andProcessStateEqualTo(originState.name());
        criteria.andIdEqualTo(id);

        MessageContentDO messageContentDO = new MessageContentDO();
        messageContentDO.setProcessState(expectState.name());
        messageContentDO.setGmtModify(new Date());
        messageContentDO.setModifier("system");

        return messageContentDOMapper.updateByExampleSelective(messageContentDO, example);
    }

    public List<MessageContentDO> queryProcessFailedMessage() {
        MessageContentDOExample example = new MessageContentDOExample();
        MessageContentDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andProcessStateEqualTo(ProcessMessageContentStateEnum.PROCESSING.name());
        criteria.andGmtModifyLessThan(new Date(System.currentTimeMillis() - SCAN_ABNORMAL_MESSAGE_TIME));
        return messageContentDOMapper.selectByExampleWithBLOBs(example);
    }

    public List<MessageContentDO> queryUnProcessedMessage() {
        MessageContentDOExample example = new MessageContentDOExample();
        MessageContentDOExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        criteria.andProcessStateEqualTo(ProcessMessageContentStateEnum.INIT.name());
        return messageContentDOMapper.selectByExampleWithBLOBs(example);
    }
}
