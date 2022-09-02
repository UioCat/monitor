package com.uio.monitor.entity.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author han xun
 * Date 2022/8/31 23:08
 * Description:
 */
@Data
public class BillExtraDTO {

    /**
     * 是否为周期账单自动添加
     */
    private Boolean periodBill;

    public static BillExtraDTO convert(String extraContent) {
        if (StringUtils.isEmpty(extraContent)) {
            return null;
        }
        try {
            return JSON.parseObject(extraContent, BillExtraDTO.class);
        } catch (JSONException e) {
            return null;
        }
    }

    public static String convert(BillExtraDTO billExtraDTO) {
        if (billExtraDTO == null) {
            return null;
        }
        return JSON.toJSONString(billExtraDTO);
    }
}