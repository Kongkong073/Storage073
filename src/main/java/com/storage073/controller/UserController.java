package com.storage073.controller;

import com.storage073.annotation.GlobalInterceptor;
import com.storage073.annotation.VerifyParam;
import com.storage073.component.RedisComponent;
import com.storage073.entity.Constants;
import com.storage073.entity.AvatarTools;
import com.storage073.entity.dto.SessionWebUserDto;
import com.storage073.entity.dto.UserSpaceDto;
import com.storage073.entity.enums.VerifyEnum;
import com.storage073.entity.vo.ResponseVO;
import com.storage073.exception.BusinessException;
import com.storage073.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Value("${project.folder}")
    private String projectFolder;

    @Resource
    private RedisComponent redisComponent;

    @PostMapping("/register")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO<Boolean> register(HttpSession session,
                                        @VerifyParam(required = true, regex = VerifyEnum.EMAIL) String email,
                                        @VerifyParam(required = true) String nickName,
                                        @VerifyParam(required = true, regex = VerifyEnum.PASSWORD) String password,
                                        @VerifyParam(required = true) String checkCode,
                                        @VerifyParam(required = true) Integer emailCode) {

        log.info("Session ID: " + session.getId());

        try{

            if( !session.getAttribute(Constants.CODE_SENT_EMAIL_ADDRESS).equals(email)){
                return ResponseVO.failure(400, "注册邮箱与发送验证码邮箱不符，重新填写。");
            }
            // 验证邮箱是否已经存在
            if (userService.emailExists(email)) {
                return ResponseVO.failure(400, "邮箱已注册。");
            }

            // 验证用户名是否已经存在
            if (userService.userNameExists(nickName)) {
                return ResponseVO.failure(400, "用户名已存在。");
            }

            // 从session中获取验证码
            String storedCheckCode = (String) session.getAttribute(Constants.CHECK_CODE_KEY);
            String storedEmailCode = (String) session.getAttribute(Constants.EMAIL_CHECK_CODE);

            // 验证图片
            if (!checkCode.equalsIgnoreCase(storedCheckCode)) {
                log.info("storedCheckCode" + storedCheckCode);
                return ResponseVO.failure(400, "图片验证码无效或已过期。");
            }

            //验证邮箱验证码
            if (!(emailCode.toString()).equals(storedEmailCode)) {
                log.info("storedEmailCode: " + storedEmailCode);
                return ResponseVO.failure(400, "邮箱验证码无效或已过期。");
            }

            // 创建用户
            userService.createUser(email, nickName, password);

            return ResponseVO.success(true, "注册成功！");

        }

        finally {
            // 注册成功后清除 session 中的验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY);
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
            session.removeAttribute(Constants.CODE_SENT_EMAIL_ADDRESS);
        }
    }


    @RequestMapping("/login")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO<SessionWebUserDto> login(HttpSession session,
                              @VerifyParam(required = true) String email,
                              @VerifyParam(required = true) String password,
                              @VerifyParam(required = true) String checkCode){
        try{
            log.info("Session ID: " + session.getId());
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))){
                throw new BusinessException("图片验证码不正确。");
            }
            SessionWebUserDto sessionWebUserDto = userService.login(email,password);
            session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
            return ResponseVO.success(sessionWebUserDto);
        }finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @RequestMapping("/resetPwd")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO resetPassword(
            HttpSession session,
            @VerifyParam(required = true, regex = VerifyEnum.EMAIL) String email,
            @VerifyParam(required = true, regex = VerifyEnum.PASSWORD) String password,
            @VerifyParam(required = true) String checkCode,
            @VerifyParam(required = true) String emailCode

    ){
        try{
            if (! (session.getAttribute(Constants.EMAIL_CHECK_CODE)).equals(emailCode)){
                throw new BusinessException("邮箱验证码错误。");
            }
            if (! ((String) session.getAttribute(Constants.CHECK_CODE_KEY)).equalsIgnoreCase(checkCode)){
                throw new BusinessException("图片验证码错误。");
            }
            userService.resetPassword(email, password);
            return ResponseVO.success(null);
        }
        finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }


    @RequestMapping("/getAvatar/{userId}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void getAvatar(
            HttpServletResponse response,
            @VerifyParam(required = true) @PathVariable("userId") String userId
    ){
        // 头像文件夹
        String avatarFolderName = projectFolder + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        File folder = new File(avatarFolderName);
        if (!folder.exists()){
            folder.mkdirs();
        }
        log.info("Avatar folder path:" + avatarFolderName);

        // 头像路径
        String avatarPath = avatarFolderName + userId + Constants.AVATAR_SUFFIX;
        log.info("Avatar path:" + avatarPath);
        File file = new File(avatarPath);
        if (!file.exists()) {
            avatarPath = avatarFolderName + Constants.AVATAR_DEFUALT;
            if (!new File(avatarPath).exists()) {
                log.info("Default avatar path:" + avatarPath);
                AvatarTools.printNoDefaultImage(response);
                return;
            }
        }

        // 返回圆形头像，大小为100x100
        AvatarTools.sendCircularAvatar(response, avatarPath, Constants.AVATAR_SIZE);
    }


    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO<SessionWebUserDto> getUserInfo(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        return ResponseVO.success(sessionWebUserDto);
    }

    protected SessionWebUserDto getUserInfoFromSession(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        return sessionWebUserDto;
    }

    @RequestMapping("/getUseSpace")
    @GlobalInterceptor
    public ResponseVO<UserSpaceDto> getUseSpace(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        log.info("sessionWebUserDto: " + sessionWebUserDto.toString());
        UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(sessionWebUserDto.getUserId().toString());
        log.info("userSpaceDto: " + userSpaceDto.toString());
        return ResponseVO.success(userSpaceDto);
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpSession session) {
        session.invalidate();
        return ResponseVO.success(null);
    }


    @RequestMapping("/updateUserAvatar")
    @GlobalInterceptor
    public ResponseVO updateUserAvatar(HttpSession session,
                                       MultipartFile avatar) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String baseFolder = projectFolder + Constants.FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if (!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        String avatarPath = targetFileFolder.getPath() + "/" + webUserDto.getUserId() + Constants.AVATAR_SUFFIX;
        File targetFile = new File(avatarPath);
        try {
            avatar.transferTo(targetFile);
        } catch (Exception e) {
            log.error("上传头像失败", e);
        }

        webUserDto.setAvatar(avatarPath);
        session.setAttribute(Constants.SESSION_KEY, webUserDto);
//        userService.updateAvatar(webUserDto.getUserId(), avatarPath);
        return ResponseVO.success(null);
    }

    @RequestMapping("/updatePassword")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updatePassword(HttpSession session,
                                     @VerifyParam(required = true, regex = VerifyEnum.PASSWORD, min = 8, max = 18) String password) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);
        userService.updatePassword(sessionWebUserDto.getUserId(), password);
        return ResponseVO.success(null);
    }

}
