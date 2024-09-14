package com.storage073.entity;

public class Constants {

    //验证码
    public static final String CHECK_CODE_KEY = "check_code_key";
    public static final String CHECK_CODE_KEY_EMAIL = "check_code_key_email";
    public static final String EMAIL_CHECK_CODE = "email_check_code";
    public static final int EMAIL_CHECK_CODE_EXPIRE_TIME = 3;
    public static final String CODE_SENT_EMAIL_ADDRESS = "email_address";


    // Session
    public static final String SESSION_KEY = "session_key";



    // Redis
    public static final String REDIS_KEY_SYS_SETTING = "storage073:syssetting:";
    public static final String REDIS_KEY_USER_SPACE_USE = "storage073:user:spaceuse:";
    public static final String REDIS_KEY_USER_FILE_TEMP_SIZE = "easypan:user:file:temp:";
    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60;
    public static final Integer REDIS_KEY_EXPIRES_ONE_HOUR = REDIS_KEY_EXPIRES_ONE_MIN * 60;
    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;



    // 配置相关
    public static final Long MB = 1024 * 1024L;


    //文件路径相关
    public static final String FILE_FOLDER_AVATAR_NAME = "avatar/";
    public static final String FILE_FOLDER_FILE = "/file/";
    public static final String FILE_FOLDER_TEMP = "/temp/";
    public static final String FILE_FOLDER_PERSIST = "/persist/";


    //头像
    public static final String AVATAR_SUFFIX = ".jpg";
    public static final String AVATAR_DEFUALT = "default_avatar.jpg";
    public static final int AVATAR_SIZE= 100;

    //文件相关
    public static final int FILEID_Length= 10;
}
