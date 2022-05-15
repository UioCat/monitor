package com.uio.monitor.mapper;

import com.uio.monitor.entity.MessageContentDO;
import com.uio.monitor.entity.MessageContentDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MessageContentDOMapper {
    long countByExample(MessageContentDOExample example);

    int deleteByExample(MessageContentDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MessageContentDO record);

    int insertSelective(MessageContentDO record);

    List<MessageContentDO> selectByExampleWithBLOBs(MessageContentDOExample example);

    List<MessageContentDO> selectByExample(MessageContentDOExample example);

    MessageContentDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MessageContentDO record, @Param("example") MessageContentDOExample example);

    int updateByExampleWithBLOBs(@Param("record") MessageContentDO record, @Param("example") MessageContentDOExample example);

    int updateByExample(@Param("record") MessageContentDO record, @Param("example") MessageContentDOExample example);

    int updateByPrimaryKeySelective(MessageContentDO record);

    int updateByPrimaryKeyWithBLOBs(MessageContentDO record);

    int updateByPrimaryKey(MessageContentDO record);
}