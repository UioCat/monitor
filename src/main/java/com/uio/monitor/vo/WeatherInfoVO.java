package com.uio.monitor.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * @author han xun
 * Date 2022/6/13 03:06
 * Description:
 */
@Data
public class WeatherInfoVO {

    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 邮政编码
     */
    @JsonAlias("adcode")
    private String adCode;
    /**
     * 天气
     */
    private String weather;
    /**
     * 温度
     */
    private String temperature;
    /**
     * 风向
     */
    @JsonAlias("winddirection")
    private String windDirection;
    /**
     * 风力
     */
    @JsonAlias("windpower")
    private String windPower;
    /**
     * 湿度
     */
    private String humidity;
    /**
     * 报告时间
     */
    @JsonAlias("reporttime")
    private String reportTime;
}
