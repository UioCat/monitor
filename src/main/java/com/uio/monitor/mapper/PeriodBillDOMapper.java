package com.uio.monitor.mapper;

import com.uio.monitor.entity.PeriodBillDO;
import com.uio.monitor.entity.PeriodBillDOExample;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PeriodBillDOMapper {
    long countByExample(PeriodBillDOExample example);

    int deleteByExample(PeriodBillDOExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PeriodBillDO record);

    int insertSelective(PeriodBillDO record);

    List<PeriodBillDO> selectByExample(PeriodBillDOExample example);

    PeriodBillDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PeriodBillDO record, @Param("example") PeriodBillDOExample example);

    int updateByExample(@Param("record") PeriodBillDO record, @Param("example") PeriodBillDOExample example);

    int updateByPrimaryKeySelective(PeriodBillDO record);

    int updateByPrimaryKey(PeriodBillDO record);
}