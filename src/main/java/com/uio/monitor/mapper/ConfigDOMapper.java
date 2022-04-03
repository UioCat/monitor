package com.uio.monitor.mapper;

import com.uio.monitor.entity.ConfigDO;
import com.uio.monitor.entity.ConfigDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConfigDOMapper {
    long countByExample(ConfigDOExample example);

    int deleteByExample(ConfigDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ConfigDO record);

    int insertSelective(ConfigDO record);

    List<ConfigDO> selectByExampleWithBLOBs(ConfigDOExample example);

    List<ConfigDO> selectByExample(ConfigDOExample example);

    ConfigDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ConfigDO record, @Param("example") ConfigDOExample example);

    int updateByExampleWithBLOBs(@Param("record") ConfigDO record, @Param("example") ConfigDOExample example);

    int updateByExample(@Param("record") ConfigDO record, @Param("example") ConfigDOExample example);

    int updateByPrimaryKeySelective(ConfigDO record);

    int updateByPrimaryKeyWithBLOBs(ConfigDO record);

    int updateByPrimaryKey(ConfigDO record);
}