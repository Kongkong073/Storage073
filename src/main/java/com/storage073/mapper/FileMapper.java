package com.storage073.mapper;

import com.storage073.entity.query.FileInfoQuery;
import com.storage073.model.FileInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface FileMapper {

    @Select("<script>"
            + "SELECT COUNT(*) FROM files "
            + "<where>"
            + "<if test='fileId != null'> AND file_id = #{fileId}</if>"
            + "<if test='userId != null'> AND user_id = #{userId}</if>"
            + "<if test='fileMd5 != null'> AND file_md5 = #{fileMd5}</if>"
            + "<if test='filePid != null'> AND file_pid = #{filePid}</if>"
            + "<if test='fileName != null'> AND file_name LIKE CONCAT(SUBSTRING_INDEX(#{fileName}, '.', 1), '%.', SUBSTRING_INDEX(#{fileName}, '.', -1))</if>"
            + "<if test='fileType != null'> AND file_type = #{fileType}</if>"
            + "<if test='delFlag != null'> AND del_flag = #{delFlag}</if>"
            + "<if test='fileCategory != null'> AND file_category = #{fileCategory}</if>"
            + "<if test='status != null'> AND status = #{status}</if>"
            + "</where>"
            + "</script>")
    int findCountByParam2(FileInfoQuery param);

    @Select("<script>"
            + "SELECT file_id AS fileId, user_id AS userId, file_md5 AS fileMd5, file_pid AS filePid, " +
            "file_size AS fileSize, file_name AS fileName, file_cover AS fileCover, file_path AS filePath, " +
            "create_time AS createTime, last_update_time AS lastUpdateTime, folder_type AS folderType, " +
            "file_category AS fileCategory, file_type AS fileType, status AS status, recovery_time AS recoveryTime, " +
            "del_flag AS delFlag from files"
            + "<where>"
            + "<if test='fileId != null'> AND file_id = #{fileId}</if>"
            + "<if test='userId != null'> AND user_id = #{userId}</if>"
            + "<if test='fileMd5 != null'> AND file_md5 = #{fileMd5}</if>"
            + "<if test='filePid != null'> AND file_pid = #{filePid}</if>"
            + "<if test='fileName != null'> AND file_name LIKE CONCAT(SUBSTRING_INDEX(#{fileName}, '.', 1), '%.', SUBSTRING_INDEX(#{fileName}, '.', -1))</if>"
            + "<if test='fileType != null'> AND file_type = #{fileType}</if>"
            + "<if test='delFlag != null'> AND del_flag = #{delFlag}</if>"
            + "<if test='fileCategory != null'> AND file_category = #{fileCategory}</if>"
            + "<if test='status != null'> AND status = #{status}</if>"
            + "</where>"
            + "<if test='orderBy != null'> ORDER BY #{orderBy}</if>"
            + "</script>")
    List<FileInfo> findListByParam2(FileInfoQuery param);

    @Update("<script>"
            + "UPDATE files "
            + "<set>"
            + "<if test='bean.fileCover != null'>"
            + "file_cover = #{bean.fileCover},"
            + "</if>"
            + "<if test='bean.status != null'>"
            + "status = #{bean.status},"
            + "</if>"
            + "</set>"
            + "WHERE file_id = #{fileId} "
            + "AND user_id = #{userId} "
            + "AND status = #{oldStatus}"
            + "</script>")
    void updateFileStatusWithOldStatus(@Param("fileId") String fileId,
                                       @Param("userId") String userId,
                                       @Param("bean") FileInfo file,
                                       @Param("oldStatus") Integer oldStatus);

    @Insert("<script>"
            + "INSERT INTO files ("
            + "file_id, user_id, file_md5, file_pid, file_size, file_name, file_cover, "
            + "file_path, create_time, last_update_time, folder_type, file_category, "
            + "file_type, status, recovery_time, del_flag"
            + ") VALUES ("
            + "#{fileId}, #{userId}, #{fileMd5}, #{filePid}, #{fileSize}, "
            + "#{fileName}, #{fileCover}, #{filePath}, #{createTime}, "
            + "#{lastUpdateTime}, #{folderType}, #{fileCategory}, #{fileType}, "
            + "#{status}, #{recoveryTime}, #{delFlag}"
            + ")"
            + "</script>")
    void insertFile(FileInfo file);


    @Select("SELECT IFNULL(SUM(file_size), 0) from files where user_id = #{userId}")
    Long getUseSpaceByUserId(@Param("userId") String userId);

    @Select("<script>"
            + "SELECT COUNT(*) FROM files "
            + "<where>"
            + "<if test='fileId != null'> AND file_id = #{fileId}</if>"
            + "<if test='userId != null'> AND user_id = #{userId}</if>"
            + "<if test='fileMd5 != null'> AND file_md5 = #{fileMd5}</if>"
            + "<if test='filePid != null'> AND file_pid = #{filePid}</if>"
            + "<if test='fileName != null'> AND file_name LIKE CONCAT('%', #{fileName}, '%')</if>"
            + "<if test='fileType != null'> AND file_type = #{fileType}</if>"
            + "<if test='delFlag != null'> AND del_flag = #{delFlag}</if>"
            + "<if test='fileCategory != null'> AND file_category = #{fileCategory}</if>"
            + "<if test='status != null'> AND status = #{status}</if>"
            + "</where>"
            + "</script>")
    int findCountByParam(FileInfoQuery param);

    @Select("<script>"
            + "SELECT file_id AS fileId, user_id AS userId, file_md5 AS fileMd5, file_pid AS filePid, " +
            "file_size AS fileSize, file_name AS fileName, file_cover AS fileCover, file_path AS filePath, " +
            "create_time AS createTime, last_update_time AS lastUpdateTime, folder_type AS folderType, " +
            "file_category AS fileCategory, file_type AS fileType, status AS status, recovery_time AS recoveryTime, " +
            "del_flag AS delFlag from files"
            + "<where>"
            + "<if test='fileId != null'> AND file_id = #{fileId}</if>"
            + "<if test='userId != null'> AND user_id = #{userId}</if>"
            + "<if test='fileMd5 != null'> AND file_md5 = #{fileMd5}</if>"
            + "<if test='filePid != null'> AND file_pid = #{filePid}</if>"
            + "<if test='fileName != null'> AND file_name LIKE CONCAT('%', #{fileName}, '%')</if>"
            + "<if test='fileType != null'> AND file_type = #{fileType}</if>"
            + "<if test='delFlag != null'> AND del_flag = #{delFlag}</if>"
            + "<if test='fileCategory != null'> AND file_category = #{fileCategory}</if>"
            + "<if test='status != null'> AND status = #{status}</if>"
            + "</where>"
            + "<if test='orderBy != null'> ORDER BY #{orderBy}</if>"
            + "LIMIT #{simplePage.start}, #{simplePage.end}"
            + "</script>")
    List<FileInfo> findListByParam(FileInfoQuery param);


    @Select("SELECT file_id AS fileId, user_id AS userId, file_md5 AS fileMd5, file_pid AS filePid, " +
            "file_size AS fileSize, file_name AS fileName, file_cover AS fileCover, file_path AS filePath, " +
            "create_time AS createTime, last_update_time AS lastUpdateTime, folder_type AS folderType, " +
            "file_category AS fileCategory, file_type AS fileType, status AS status, recovery_time AS recoveryTime, " +
            "del_flag AS delFlag " +
            "FROM files WHERE file_id = #{fileId} and user_Id = #{userId}")
    FileInfo findByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

    @Select("SELECT file_id AS fileId, user_id AS userId, file_md5 AS fileMd5, file_pid AS filePid, " +
            "file_size AS fileSize, file_name AS fileName, file_cover AS fileCover, file_path AS filePath, " +
            "create_time AS createTime, last_update_time AS lastUpdateTime, folder_type AS folderType, " +
            "file_category AS fileCategory, file_type AS fileType, status AS status, recovery_time AS recoveryTime, " +
            "del_flag AS delFlag " +
            "FROM file_info WHERE user_id = #{userId}")
    List<FileInfo> findByUserId(@Param("userId") String userId);


    @Update("UPDATE file_info SET file_name = #{fileName}, file_size = #{fileSize}, last_update_time = #{lastUpdateTime} " +
            "WHERE file_id = #{fileId}")
    void updateFile(@Param("fileId") String fileId, @Param("fileName") String fileName, @Param("fileSize") Long fileSize, @Param("lastUpdateTime") Date lastUpdateTime);

    @Delete("DELETE FROM file_info WHERE file_id = #{fileId}")
    void deleteFile(@Param("fileId") String fileId);

    @Select("SELECT status FROM file_info WHERE file_id = #{fileId}")
    Integer getStatus(@Param("fileId") String fileId);
}
