package com.uio.monitor.manager;

import com.alibaba.fastjson.JSON;
import com.uio.monitor.controller.resp.BillConfigDTO;
import com.uio.monitor.entity.ConfigDO;
import com.uio.monitor.entity.ConfigDOExample;
import com.uio.monitor.mapper.ConfigDOMapper;
import com.uio.monitor.vo.CityAdCodeDTO;
import com.uio.monitor.vo.ServerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author han xun
 * Date 2021/11/22 09:52
 * Description:
 */
@Component
public class ConfigManager {

    @Autowired
    private ConfigDOMapper configDOMapper;

    /**
     * 服务器数据KEY
     */
    private final static String SERVER_LIST_KEY = "server_list_key";
    /**
     * 本地服务器数据KEY
     */
    private final static String LOCAL_SERVER_LIST_KEY = "local_server_list_key";
    /**
     * 账单配置前缀
     */
    private static final String BILL_CONFIG = "bill_config";
    /**
     * Mac屏幕亮度参数
     */
    private static final String MAC_SCREEN_BRIGHTNESS = "mac_screen_brightness";

    private static final String HOT_CITY_ADCODE = "hot_city_adcode";
    private static final String CITY_ADCODE = "city_adcode";
    private static final String WECHAT_ROBOT_HELP = "WECHAT_ROBOT_HELP";
    /**
     * iphone12 mini Wi-Fi 固定IP
     */
    private static final String IPHONE_HOME_IP = "IPHONE_HOME_IP";
    /**
     * 需要发送的邮箱列表
     */
    private static final String SEND_MAIL_LIST = "SEND_MAIL_LIST";

    public List<String> getSendMailList() {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(SEND_MAIL_LIST);
        criteria.andDeletedEqualTo(false);

        ConfigDO configDO = configDOMapper.selectByExampleWithBLOBs(example).stream().findFirst().orElse(null);
        return configDO == null ? new ArrayList<>(0) : JSON.parseArray(configDO.getConfigValue(), String.class);
    }

    /**
     * iphone12 mini 家庭固定IP
     *
     * @return
     */
    public String getIPhoneHomeIp() {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(IPHONE_HOME_IP);
        criteria.andDeletedEqualTo(false);
        ConfigDO configDO = configDOMapper.selectByExampleWithBLOBs(example).stream().findFirst().orElse(null);
        return configDO == null ? null : configDO.getConfigValue();
    }

    /**
     * 新增一个带监测的服务器数据
     *
     * @param serverMessage
     */
    public void addServer(ServerMessage serverMessage) {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(SERVER_LIST_KEY);
        criteria.andDeletedEqualTo(false);
        ConfigDO configDO = configDOMapper.selectByExample(example).stream().findFirst().orElse(null);
        List<ServerMessage> value = null;
        ConfigDO newConfigDO = new ConfigDO();

        if (configDO == null || configDO.getConfigValue() == null) {
            value = new ArrayList<>(1);
        } else {
            value = JSON.parseArray(configDO.getConfigValue(), ServerMessage.class);
            newConfigDO.setModifier("han xun");
            newConfigDO.setId(configDO.getId());
        }
        value.add(serverMessage);
        newConfigDO.setGmtCreate(new Date());
        newConfigDO.setGmtModify(new Date());
        newConfigDO.setCreator("system");
        newConfigDO.setConfigKey(SERVER_LIST_KEY);
        newConfigDO.setConfigValue(JSON.toJSONString(value));
        newConfigDO.setDeleted(false);
        this.insertOrUpdate(newConfigDO);
    }

    /**
     * 获取服务器信息列表
     *
     * @return
     */
    public List<ServerMessage> getServerList() {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(SERVER_LIST_KEY);
        criteria.andDeletedEqualTo(false);
        ConfigDO configDO = configDOMapper.selectByExampleWithBLOBs(example).stream().findFirst().orElse(null);
        if (configDO == null || configDO.getConfigValue() == null) {
            return new ArrayList<>(0);
        }
        return JSON.parseArray(configDO.getConfigValue(), ServerMessage.class);
    }

    /**
     * 获取本地服务器信息列表
     *
     * @return
     */
    public List<ServerMessage> getLocalServerList() {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(LOCAL_SERVER_LIST_KEY);
        criteria.andDeletedEqualTo(false);
        ConfigDO configDO = configDOMapper.selectByExampleWithBLOBs(example).stream().findFirst().orElse(null);
        if (configDO == null || configDO.getConfigValue() == null) {
            return new ArrayList<>(0);
        }
        return JSON.parseArray(configDO.getConfigValue(), ServerMessage.class);
    }

    /**
     * 获得用户Bill配置
     *
     * @param userId
     * @return
     */
    public List<BillConfigDTO> getBillConfig(Long userId) {
        String key = ConfigManager.getBillConfigKey(userId);

        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(key);
        criteria.andDeletedEqualTo(false);
        List<ConfigDO> configDOList = configDOMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isEmpty(configDOList)) {
            return Collections.emptyList();
        } else {
            return configDOList.stream().map(item -> {
                BillConfigDTO billConfigDTO = JSON.parseObject(item.getConfigValue(), BillConfigDTO.class);
                billConfigDTO.setConfigId(item.getId());
                return billConfigDTO;
            }).collect(Collectors.toList());
        }
    }

    /**
     * 删除账单配置数据
     *
     * @param userId
     * @param configId
     */
    @Transactional
    public Boolean deleteBillConfig(Long userId, Long configId) {
        String key = ConfigManager.getBillConfigKey(userId);

        ConfigDO configDO = configDOMapper.selectByPrimaryKey(configId);
        if (configDO == null || !configDO.getConfigKey().equals(key)) {
            return false;
        }
        configDO.setDeleted(true);
        configDOMapper.updateByPrimaryKeySelective(configDO);
        return true;
    }

    /**
     * 插入/更新 用户账单配置
     *
     * @param userId
     * @param billConfigDTO
     */
    public void insertBillConfig(Long userId, BillConfigDTO billConfigDTO) {
        if (userId == null) {
            return;
        }
        List<BillConfigDTO> billConfigDTOS = this.getBillConfig(userId);
        String key = ConfigManager.getBillConfigKey(userId);
        ConfigDO configDO = new ConfigDO();
        if (CollectionUtils.isEmpty(billConfigDTOS) ||
                billConfigDTOS.stream().noneMatch(item -> item.getCategory().equals(billConfigDTO.getCategory()))) {
            // insert
            configDO.setGmtCreate(new Date());
            configDO.setGmtModify(new Date());
            configDO.setCreator(userId.toString());
            configDO.setModifier(userId.toString());
            configDO.setConfigKey(key);
            configDO.setConfigValue(JSON.toJSONString(billConfigDTO));
            configDO.setDeleted(false);
            configDOMapper.insert(configDO);
        }
    }

    private static String getBillConfigKey(Long userId) {
        return userId + "_" + BILL_CONFIG;
    }

    private void insertOrUpdate(ConfigDO configDO) {
        if (configDO == null) {
            return;
        }
        if (configDO.getId() == null) {
            configDOMapper.insert(configDO);
        } else {
            configDOMapper.updateByPrimaryKeySelective(configDO);
        }
    }

    /**
     * 更新屏幕亮度参数
     */
    public String queryAndUpdateMacScreenBrightness() {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(MAC_SCREEN_BRIGHTNESS);
        criteria.andDeletedEqualTo(false);
        List<ConfigDO> configDOList = configDOMapper.selectByExampleWithBLOBs(example);
        ConfigDO configDO = new ConfigDO();
        if (CollectionUtils.isEmpty(configDOList)) {
            // 插入
            configDO.setGmtCreate(new Date());
            configDO.setGmtModify(new Date());
            configDO.setCreator("system");
            configDO.setModifier("system");
            configDO.setDeleted(false);
            configDO.setConfigKey(MAC_SCREEN_BRIGHTNESS);
            configDO.setConfigValue("0");
            configDOMapper.insert(configDO);
            return "0";
        }
        {
            ConfigDO configDOInDb = configDOList.get(0);
            // 更新
            configDO.setId(configDOInDb.getId());
            configDO.setGmtModify(new Date());
            configDO.setModifier("system");
            configDO.setConfigValue(configDOInDb.getConfigValue().equals("0") ? "1" : "0");
            configDOMapper.updateByPrimaryKeySelective(configDO);
            return configDOInDb.getConfigValue();
        }
    }

    public List<CityAdCodeDTO> getHotCityCode() {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(HOT_CITY_ADCODE);
        criteria.andDeletedEqualTo(false);
        List<ConfigDO> configDOList = configDOMapper.selectByExampleWithBLOBs(example);
        ConfigDO configDO = CollectionUtils.isEmpty(configDOList) ? null : configDOList.get(0);
        if (configDO == null) {
            return null;
        }
        return configDO.getConfigValue() == null ? null : JSON.parseArray(configDO.getConfigValue(), CityAdCodeDTO.class);
    }

    public void addHotCity(CityAdCodeDTO cityAdCodeDTO) {
        if (cityAdCodeDTO == null) {
            return;
        }
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(HOT_CITY_ADCODE);
        criteria.andDeletedEqualTo(false);
        List<ConfigDO> configDOList = configDOMapper.selectByExampleWithBLOBs(example);
        ConfigDO configDO = CollectionUtils.isEmpty(configDOList) ? null : configDOList.get(0);
        List<CityAdCodeDTO> cityAdCodeDTOS = configDO.getConfigValue() == null ? new ArrayList<>() :
                JSON.parseArray(configDO.getConfigValue(), CityAdCodeDTO.class);
        for (CityAdCodeDTO item : cityAdCodeDTOS) {
            if (item.getCityName().equals(cityAdCodeDTO.getCityName())) {
                return;
            }
        }
        cityAdCodeDTOS.add(cityAdCodeDTO);
        String value = JSON.toJSONString(cityAdCodeDTOS);

        configDO.setGmtModify(new Date());
        configDO.setModifier("system");
        configDO.setConfigValue(value);
        configDOMapper.updateByPrimaryKeySelective(configDO);
    }

    public List<CityAdCodeDTO> getAllCityCode() {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(CITY_ADCODE);
        criteria.andDeletedEqualTo(false);
        List<ConfigDO> configDOList = configDOMapper.selectByExampleWithBLOBs(example);

        List<CityAdCodeDTO> res = new ArrayList<>();
        for (ConfigDO configDO : configDOList) {
            if (configDO.getConfigValue() != null) {
                res.addAll(JSON.parseArray(configDO.getConfigValue(), CityAdCodeDTO.class));
            }
        }
        return res;
    }

    public String getWechatRobotHelpConfig() {
        ConfigDOExample example = new ConfigDOExample();
        ConfigDOExample.Criteria criteria = example.createCriteria();
        criteria.andConfigKeyEqualTo(WECHAT_ROBOT_HELP);
        criteria.andDeletedEqualTo(false);
        List<ConfigDO> configDOList = configDOMapper.selectByExampleWithBLOBs(example);
        return CollectionUtils.isEmpty(configDOList) ? null : configDOList.get(0).getConfigValue();
    }

}
