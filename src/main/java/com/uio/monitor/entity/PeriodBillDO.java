package com.uio.monitor.entity;

import java.math.BigDecimal;
import java.util.Date;

public class PeriodBillDO {
    private Long id;

    private Long userId;

    private Date gmtCreate;

    private Date gmtModify;

    private String creator;

    private String modifier;

    private Boolean deleted;

    private Integer generateDay;

    private Integer generateCount;

    private String billType;

    private String produceWay;

    private BigDecimal amount;

    private String description;

    private String category;

    private Date addTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModify() {
        return gmtModify;
    }

    public void setGmtModify(Date gmtModify) {
        this.gmtModify = gmtModify;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getGenerateDay() {
        return generateDay;
    }

    public void setGenerateDay(Integer generateDay) {
        this.generateDay = generateDay;
    }

    public Integer getGenerateCount() {
        return generateCount;
    }

    public void setGenerateCount(Integer generateCount) {
        this.generateCount = generateCount;
    }

    public String getBillType() {
        return billType;
    }

    public void setBillType(String billType) {
        this.billType = billType;
    }

    public String getProduceWay() {
        return produceWay;
    }

    public void setProduceWay(String produceWay) {
        this.produceWay = produceWay;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }
}