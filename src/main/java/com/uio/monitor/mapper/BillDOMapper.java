package com.uio.monitor.mapper;

import com.uio.monitor.entity.BillDO;
import com.uio.monitor.entity.BillDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BillDOMapper {
    long countByExample(BillDOExample example);

    int deleteByExample(BillDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BillDO record);

    int insertSelective(BillDO record);

    List<BillDO> selectByExample(BillDOExample example);

    BillDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BillDO record, @Param("example") BillDOExample example);

    int updateByExample(@Param("record") BillDO record, @Param("example") BillDOExample example);

    int updateByPrimaryKeySelective(BillDO record);

    int updateByPrimaryKey(BillDO record);
}