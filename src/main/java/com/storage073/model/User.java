package com.storage073.model;

import lombok.Data;

import java.util.Date;

@Data
public class User{
    private Integer userId;

    private String username;
    private String email;
    private String passwordHash;
    private Date createdAt;
    private Date lastLoginAt;
    private String phoneNumber;
    private String avatar;
    private Integer status;
    private Long useSpace;
    private Long totalSpace;



}

