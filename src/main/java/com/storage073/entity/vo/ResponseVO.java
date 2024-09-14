package com.storage073.entity.vo;

import com.storage073.entity.enums.ResponseCodeEnum;


public class ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public static <T> ResponseVO<T> success(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    public static <T> ResponseVO<T> success(T t, String message) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(message);
        responseVO.setData(t);
        return responseVO;
    }

    public static <T> ResponseVO<T> failure(String message) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_ERROR);
        responseVO.setInfo(message);
        return responseVO;
    }

    public static <T> ResponseVO<T> failure(Integer code, String message) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_ERROR);
        responseVO.setCode(code);
        responseVO.setInfo(message);
        return responseVO;
    }

    public static <T> ResponseVO<T> failure(T t, Integer code, String message) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_ERROR);
        responseVO.setCode(code);
        responseVO.setInfo(message);
        responseVO.setData(t);
        return responseVO;
    }
}
