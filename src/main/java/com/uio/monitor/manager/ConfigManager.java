package com.uio.monitor.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.uio.monitor.controller.resp.BillConfigDTO;
import com.uio.monitor.entity.ConfigDO;
import com.uio.monitor.entity.ConfigDOExample;
import com.uio.monitor.mapper.ConfigDOMapper;
import com.uio.monitor.vo.ServerMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
     * 新增一个带监测的服务器数据
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
}
