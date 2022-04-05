package com.uio.monitor.mapper;

import com.uio.monitor.entity.TimingMessageDO;
import com.uio.monitor.entity.TimingMessageDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TimingMessageDOMapper {
    long countByExample(TimingMessageDOExample example);

    int deleteByExample(TimingMessageDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TimingMessageDO record);

    int insertSelective(TimingMessageDO record);

    List<TimingMessageDO> selectByExample(TimingMessageDOExample example);

    TimingMessageDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TimingMessageDO record, @Param("example") TimingMessageDOExample example);

    int updateByExample(@Param("record") TimingMessageDO record, @Param("example") TimingMessageDOExample example);

    int updateByPrimaryKeySelective(TimingMessageDO record);

    int updateByPrimaryKey(TimingMessageDO record);
}