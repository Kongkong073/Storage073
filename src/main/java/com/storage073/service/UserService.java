package com.storage073.service;

import com.storage073.component.RedisComponent;
import com.storage073.entity.Constants;
import com.storage073.entity.StringTools;
import com.storage073.entity.dto.SessionWebUserDto;
import com.storage073.entity.dto.SysSettingsDto;
import com.storage073.entity.dto.UserSpaceDto;
import com.storage073.entity.enums.UserStatusEnum;
import com.storage073.exception.BusinessException;
import com.storage073.mapper.FileMapper;
import com.storage073.mapper.UserMapper;
import com.storage073.model.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisComponent redisComponent;

    @Value("${admin.email}")
    private String[] adminEmail;

    @Resource
    private FileMapper fileMapper;

    public void updateLastLogin(String email, Date lastLoginAt){
        userMapper.updateLastLogin(email, lastLoginAt);
    }

    public boolean emailExists(String email) {
        return userMapper.findByEmail(email) != null;
    }

    public boolean userNameExists(String userName) {

        return userMapper.findByUsername(userName) != null;
    }



    @Transactional(rollbackFor = Exception.class)
    public void createUser(String email, String userName, String password) {

//        // 密码哈希处理
//        String hashedPassword = passwordHash(password);

        User newUser = new User();

        newUser.setEmail(email);
        newUser.setUsername(userName);
        newUser.setPasswordHash(StringTools.encodeByMD5(password));
        newUser.setCreatedAt(new Date());
        newUser.setStatus(UserStatusEnum.ENABLE.getStatus());
        newUser.setLastLoginAt(new Date());
        newUser.setUseSpace(0L);
        SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
        newUser.setTotalSpace(sysSettingsDto.getUserInitUseSpace() * Constants.MB);
        log.info("Total space: " + sysSettingsDto.getUserInitUseSpace() * Constants.MB);
        userMapper.insertUser(newUser);
    }

    public SessionWebUserDto login(String email, String password){
        User user = userMapper.findByEmail(email);

        log.info("User: " + user.toString());

        if (!email.equalsIgnoreCase(user.getEmail()) || !user.getPasswordHash().equals(password)){
            throw new BusinessException("账号或密码错误。");
        }
        if (userMapper.getStatus(email) == 0){
            throw new BusinessException("账号禁用。");
        }

        //更新最后登录时间
        updateLastLogin(email, new Date());

        //添加返回值参数
        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setUserId(user.getUserId());
        sessionWebUserDto.setUserName(user.getUsername());
        sessionWebUserDto.setAvatar(user.getAvatar());
        if(ArrayUtils.contains(adminEmail, email)){
            sessionWebUserDto.setAdmin(true);
        }else{
            sessionWebUserDto.setAdmin(false);
        }

        //用户空间
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        Long useSpace = fileMapper.getUseSpaceByUserId(user.getUserId().toString());
        userSpaceDto.setUseSpace(useSpace);
        userSpaceDto.setTotalSpace(user.getTotalSpace());
        redisComponent.saveUserSpaceUse(user.getUserId().toString(), userSpaceDto);

        return sessionWebUserDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String password){
        User user = userMapper.findByEmail(email);

        log.info("User: " + user.toString());
        log.info("password: " + user.getPasswordHash());

        if (!email.equalsIgnoreCase(user.getEmail())){
            throw new BusinessException("找不到账号。");
        }
        //重置密码
        userMapper.resetPassword(email, StringTools.encodeByMD5(password));

    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Integer userId, String password){
        User user = userMapper.findById(userId);
        if (user==null){
            throw new BusinessException("用户不存在。");
        }
        userMapper.resetPasswordById(userId,StringTools.encodeByMD5(password));
    }

    public void updateAvatar(Integer userId, String avatarPath){
        userMapper.updateAvatar(userId, avatarPath);
    }


}

