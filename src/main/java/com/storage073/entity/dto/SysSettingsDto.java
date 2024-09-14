package com.storage073.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

// 在将 JSON 数据反序列化为 Java 对象时忽略 JSON 中的未知字段
@JsonIgnoreProperties(ignoreUnknown = true)
public class SysSettingsDto implements Serializable {

    /**
     * 用户初始化空间大小 5M
     */
    private Integer userInitUseSpace = 5;

    public Integer getUserInitUseSpace() {
        return userInitUseSpace;
    }

    public void setUserInitUseSpace(Integer userInitUseSpace) {
        this.userInitUseSpace = userInitUseSpace;
    }
}
