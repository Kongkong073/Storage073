package com.storage073.mapper;

import com.storage073.model.User;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserMapper {

    @Update("<script>"
            + "UPDATE users "
            + "<set>"
            + "<if test='useSpace != null'>"
            + "use_space = use_space + #{useSpace},"
            + "</if>"
            + "<if test='totalSpace != null'>"
            + "total_space = total_space + #{totalSpace},"
            + "</if>"
            + "</set>"
            + "WHERE user_id = #{userId} "
            + "<if test='useSpace != null'>"
            + "AND <![CDATA[(use_space + #{useSpace}) <= total_space]]>"
            + "</if>"
            + "<if test='totalSpace != null'>"
            + "AND <![CDATA[(total_space + #{totalSpace}) >= use_space]]>"
            + "</if>"
            + "</script>")
    int updateUserSpace(@Param("userId") Integer userId,
                        @Param("useSpace") Long useSpace,
                        @Param("totalSpace") Long totalSpace);

    @Select("SELECT user_id AS userId, username, email, password_hash AS passwordHash, " +
            "created_at AS createdAt, last_login_at AS lastLoginAt, phone_number AS phoneNumber, " +
            "avatar, status, use_space AS useSpace, total_space AS totalSpace FROM users WHERE user_id = #{userId}")
    User findById(@Param("userId") Integer userId);

    @Select("SELECT user_id AS userId, username, email, password_hash AS passwordHash, " +
            "created_at AS createdAt, last_login_at AS lastLoginAt, phone_number AS phoneNumber, " +
            "avatar, status, use_space AS useSpace, total_space AS totalSpace from users WHERE email = #{email}")
    User findByEmail(@Param("email") String email);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(@Param("username") String username);


    @Select("Select status from users where email = #{email}")
    Integer getStatus(@Param("email") String email);

    @Insert("INSERT INTO users(username, email, password_hash, created_at, status, use_space, total_space) " +
            "VALUES(#{username}, #{email}, #{passwordHash}, #{createdAt}, #{status}, #{useSpace}, #{totalSpace})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    void insertUser(User user);

    @Update("UPDATE users SET password_hash = #{password} WHERE email = #{email}")
    void resetPassword(@Param("email") String email, @Param("password") String password);

    @Update("UPDATE users SET password_hash = #{password} WHERE user_id = #{userId}")
    void resetPasswordById(@Param("userId") Integer userId, @Param("password") String password);

    @Update("UPDATE users SET last_login_at = #{lastLoginAt} WHERE email = #{email}")
    void updateLastLogin(@Param("email") String email, @Param("lastLoginAt") Date lastLoginAt);

    @Update("UPDATE users SET avatar = #{avatarPath} WHERE user_id = #{userId}")
    void updateAvatar(@Param("userId") Integer userId, @Param("avatarPath") String avatarPath);

    @Select("SELECT user_id from users where email = #{email}")
    Integer findIdByEmail(@Param("email") String email);
    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    void deleteUser(@Param("userId") Long userId);

    @Select("SELECT user_id AS userId, username, email, password_hash AS passwordHash, " +
            "created_at AS createdAt, last_login_at AS lastLoginAt, phone_number AS phoneNumber, " +
            "avatar, status, use_space AS useSpace, total_space AS totalSpace FROM users")
    List<User> findAll();
}

