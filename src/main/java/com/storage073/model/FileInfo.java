package com.storage073.model;

import lombok.Data;
import java.util.Date;

@Data
public class FileInfo {

    private String fileId; // 文件ID
    private String userId; // 用户ID
    private String fileMd5; // 文件MD5值
    private String filePid; // 父级ID
    private Long fileSize; // 文件大小
    private String fileName; // 文件名
    private String fileCover; // 封面
    private String filePath; // 文件路径
    private Date createTime; // 创建时间
    private Date lastUpdateTime; // 最后更新时间
    private Integer folderType; // 0:文件 1:目录
    private Integer fileCategory; // 文件分类 1:视频 2:音频 3:图片 4:文档 5:其他
    private Integer fileType; // 文件类型 1:视频 2:音频 3:图片 4:pdf 5:doc 6:excel 7:txt 8:code 9:zip 10:其他
    private Integer status; // 0:转码中 1:转码失败 2:转码成功
    private Date recoveryTime; // 进入回收站时间
    private Integer delFlag; // 标记删除 0:删除 1:回收站 2:正常
}

