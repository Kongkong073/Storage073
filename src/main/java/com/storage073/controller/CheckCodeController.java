package com.storage073.controller;

import com.storage073.annotation.GlobalInterceptor;
import com.storage073.annotation.VerifyParam;
import com.storage073.entity.*;
import com.storage073.entity.enums.VerifyEnum;
import com.storage073.entity.vo.ResponseVO;
import com.storage073.exception.BusinessException;
import com.storage073.service.CheckCodeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@Slf4j
public class CheckCodeController {


    @Autowired
    private CheckCodeService checkCodeService;


    @RequestMapping("/checkCode")
    public void generateImageCheckCode(HttpServletResponse response, HttpSession session, Integer type) throws IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        String code = vCode.getCode();

        if (type == null || type == 0) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        log.info("Image Code " + code);
        log.info("Generate image code session id: " + session.getId());

        vCode.write(response.getOutputStream());
    }

    @PostMapping("/sendEmailCode")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO sendEmailCode(HttpSession session,
                                            @VerifyParam(required = true, regex = VerifyEnum.EMAIL) String email,
                                            @VerifyParam(required = true) String checkCode
                                            ) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                throw new BusinessException("图片验证码不正确");
            }
            String code = checkCodeService.generateEmailCheckCode(email);
            session.setAttribute(Constants.EMAIL_CHECK_CODE, code);
            session.setAttribute(Constants.CODE_SENT_EMAIL_ADDRESS, email);
            log.info("check code sent: " + code);
            log.info("Session ID: " + session.getId());
            return ResponseVO.success(null,"验证信息发送至: " + email);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }


    @PostMapping("/verifyImageCode")
    public ResponseVO<Boolean> verifyImageCode(@RequestParam String inputCode, HttpSession session){
        String sessionCode = (String) session.getAttribute(Constants.CHECK_CODE_KEY);
        log.info("verify code - sessionCode: " + sessionCode);
        log.info("Verify image code session id: " + session.getId());
        if (sessionCode != null && sessionCode.equals(inputCode)) {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
            return ResponseVO.success(true, "Image code verified successfully.");
        } else {
            return ResponseVO.success(false, "Image code verification failed.");
        }
    }



    @PostMapping("/verifyCheckCode")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO<Boolean> verifyCheckCode(
            @RequestParam @VerifyParam(required = true, regex = VerifyEnum.EMAIL) String email,
            @RequestParam String inputCode,
            HttpSession session)
    {
        String vCode = (String) session.getAttribute(Constants.EMAIL_CHECK_CODE);
        log.info("verify code - : " + vCode);
        log.info("Session ID: " + session.getId());
        if (vCode != null && checkCodeService.validateCheckCode(email, inputCode) && vCode.equals(inputCode)) {
            session.removeAttribute(Constants.EMAIL_CHECK_CODE);
            return ResponseVO.success(true, "Email code verified successfully.");
        } else {
            return ResponseVO.failure(400,"Email code verification failed.");
        }
    }
}

