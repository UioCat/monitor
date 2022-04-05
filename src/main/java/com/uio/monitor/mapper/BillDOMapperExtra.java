package com.uio.monitor.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author han xun
 * Date 2022/4/5 17:04
 * Description:
 */
@Mapper
public interface BillDOMapperExtra {

    List<String> queryUserCategoryInBill(Long userId);
}
