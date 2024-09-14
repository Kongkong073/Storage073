package com.storage073.model;

import lombok.Data;

import java.util.Date;

@Data
public class EmailCheckCode {
    private String email;
    private String checkcode;
    private Date createdAt;
    private Date expireAt;
    private Byte status;

    // Getters and Setters
}

