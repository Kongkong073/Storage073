package com.storage073.entity.enums;

public enum UserStatusEnum {

    DISABLE(0, "DISABLED"),
    ENABLE(1, "ENABLED");

    private Integer status;
    private String desc;

    UserStatusEnum(Integer status, String desc){
        this.status = status;
        this.desc = desc;
    }

    public static UserStatusEnum getByStatus(Integer status){
        for (UserStatusEnum item: UserStatusEnum.values()){
            if (item.getStatus().equals(status)){
                return item;
            }
        }
        return null;
    }

    public Integer getStatus(){
        return status;
    }

    public String getDesc(){
        return desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }


}
