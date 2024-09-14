package com.storage073.mapper;

import com.storage073.model.EmailCheckCode;
import org.apache.ibatis.annotations.*;

@Mapper
public interface EmailCheckCodeMapper {

    @Insert("INSERT INTO email_checkcode(email, checkcode, created_at, expire_at, status) " +
            "VALUES(#{email}, #{checkcode}, #{createdAt}, #{expireAt}, #{status})")
    void insertCheckCode(EmailCheckCode emailCheckCode);

    @Select("SELECT * FROM email_checkcode WHERE email = #{email} AND checkcode = #{checkcode} AND status = 1")
    EmailCheckCode findByEmailAndCheckcode(@Param("email") String email, @Param("checkcode") String checkcode);

    @Update("UPDATE email_checkcode SET status = 0 WHERE email = #{email} AND checkcode != #{checkcode}")
    void invalidatePreviousCheckCodes(@Param("email") String email, @Param("checkcode") String currentCheckcode);

    @Update("UPDATE email_checkcode SET status = 0 WHERE email = #{email} AND checkcode = #{checkcode}")
    void updateStatus(@Param("email") String email, @Param("checkcode") String checkcode, @Param("status") Byte status);

    @Update("UPDATE email_checkcode SET status = 0 WHERE email = #{email} AND expire_at < NOW()")
    void invalidateExpiredCheckCodes(@Param("email") String email);

    @Delete("DELETE FROM email_checkcode WHERE expire_at < NOW()")
    void deleteExpiredCheckCodes();
}
