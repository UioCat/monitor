package com.uio.monitor.mapper;

import com.uio.monitor.entity.PushMessageDO;
import com.uio.monitor.entity.PushMessageDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PushMessageDOMapper {
    long countByExample(PushMessageDOExample example);

    int deleteByExample(PushMessageDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PushMessageDO record);

    int insertSelective(PushMessageDO record);

    List<PushMessageDO> selectByExample(PushMessageDOExample example);

    PushMessageDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PushMessageDO record, @Param("example") PushMessageDOExample example);

    int updateByExample(@Param("record") PushMessageDO record, @Param("example") PushMessageDOExample example);

    int updateByPrimaryKeySelective(PushMessageDO record);

    int updateByPrimaryKey(PushMessageDO record);
}