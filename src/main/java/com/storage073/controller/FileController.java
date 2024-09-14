package com.storage073.controller;


import com.storage073.annotation.GlobalInterceptor;
import com.storage073.annotation.VerifyParam;
import com.storage073.entity.Constants;
import com.storage073.entity.dto.SessionWebUserDto;
import com.storage073.entity.dto.UploadResultDto;
import com.storage073.entity.enums.FileCategoryEnums;
import com.storage073.entity.enums.FileDelFlagEnums;
import com.storage073.entity.query.FileInfoQuery;
import com.storage073.entity.vo.FileInfoVO;
import com.storage073.entity.vo.PaginationResultVO;
import com.storage073.entity.vo.ResponseVO;
import com.storage073.service.FileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.storage073.entity.FileTools.convert2PaginationVO;

@RestController("fileInfoController")
@RequestMapping("/file")
@Slf4j
public class FileController {
    @Resource
    private FileService fileService;

    protected SessionWebUserDto getUserInfoFromSession(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        return sessionWebUserDto;
    }

    @RequestMapping("/loadDataList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<PaginationResultVO> loadDataList(HttpSession session, FileInfoQuery query, String category) {
        // e.g. category = "music"
        FileCategoryEnums categoryEnum = FileCategoryEnums.getByCode(category);
        if (null != categoryEnum) {
            query.setFileCategory(categoryEnum.getCategory());
        }
        query.setUserId(getUserInfoFromSession(session).getUserId().toString());
        query.setOrderBy("last_update_time desc");
        // FileDelFlagEnums.USING.getFlag() = 0/1/2
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        // result类型为PaginationResultVO<FileInfo>
        PaginationResultVO result = fileService.findListByPage(query);
        // 返回类行为PaginationResultVO<FileInfoVO>的结果
        // FileInfo简化不需要的field为FileInfoVO
        return ResponseVO.success(convert2PaginationVO(result, FileInfoVO.class));
    }

    @RequestMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<UploadResultDto> uploadFile(
            HttpSession session,
            String fileId,
            MultipartFile file,
            @VerifyParam(required = true) String fileName,
            @VerifyParam(required = true) String filePid,
            @VerifyParam(required = true) String fileMd5,
            @VerifyParam(required = true) Integer chunkIndex,
            @VerifyParam(required = true) Integer chunks

    ){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        UploadResultDto uploadResultDto = fileService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
        return ResponseVO.success(uploadResultDto);
    }


}

