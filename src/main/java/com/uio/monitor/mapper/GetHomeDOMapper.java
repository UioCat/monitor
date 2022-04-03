package com.uio.monitor.mapper;

import com.uio.monitor.entity.GetHomeDO;
import com.uio.monitor.entity.GetHomeDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GetHomeDOMapper {
    long countByExample(GetHomeDOExample example);

    int deleteByExample(GetHomeDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(GetHomeDO record);

    int insertSelective(GetHomeDO record);

    List<GetHomeDO> selectByExample(GetHomeDOExample example);

    GetHomeDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") GetHomeDO record, @Param("example") GetHomeDOExample example);

    int updateByExample(@Param("record") GetHomeDO record, @Param("example") GetHomeDOExample example);

    int updateByPrimaryKeySelective(GetHomeDO record);

    int updateByPrimaryKey(GetHomeDO record);
}