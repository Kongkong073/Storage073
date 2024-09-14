package com.storage073.service;

import com.storage073.component.RedisComponent;
import com.storage073.entity.Constants;
import com.storage073.entity.FileTools;
import com.storage073.entity.StringTools;
import com.storage073.entity.dto.SessionWebUserDto;
import com.storage073.entity.dto.UploadResultDto;
import com.storage073.entity.dto.UserSpaceDto;
import com.storage073.entity.enums.*;
import com.storage073.entity.query.FileInfoQuery;
import com.storage073.entity.query.SimplePage;
import com.storage073.entity.vo.PaginationResultVO;
import com.storage073.exception.BusinessException;
import com.storage073.mapper.FileMapper;
import com.storage073.mapper.UserMapper;
import com.storage073.model.FileInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Stack;

@Service
@Slf4j
public class FileService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private RedisComponent redisComponent;

    @Value("${project.folder}")
    private String projectFolder;

    @Resource
    @Lazy
    private FileService fileService;


    public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param) {
        int count = fileMapper.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSizeEnum.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<FileInfo> list = fileMapper.findListByParam(param);
        PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName,
                                      String filePid, String fileMd5, Integer chunkIndex, Integer chunks){
        File tempFolder = null;
        Boolean uploadSuccess = true;
        // 返回值包含字段 - FileId, status
        UploadResultDto resultDto = new UploadResultDto();
        try{
            if (StringTools.isEmpty(fileId)){
                fileId = StringTools.getRandomString(Constants.FILEID_Length);
            }
            // FileId加入返回对象
            resultDto.setFileId(fileId);
            // 文件创建时间
            Date createTime = new Date();
            //用户空间
            UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId().toString());

            //当第一个chunk时
            if(chunkIndex == 0){
                log.info("第一个分片到达..................");
                FileInfoQuery fileInfoQuery = new FileInfoQuery();
                fileInfoQuery.setFileMd5(fileMd5);
                fileInfoQuery.setSimplePage(new SimplePage(0,1)); //只需要一个
                fileInfoQuery.setStatus(FileStatusEnums.USING.getStatus());
                List<FileInfo> list = fileMapper.findListByParam(fileInfoQuery);

                //秒传
                if(!list.isEmpty()){
                    log.info("数据库中存在该文件，秒传.......................");
                    FileInfo fileInfo = list.get(0);

                    // 空间不足
                    if(fileInfo.getFileSize() + spaceDto.getUseSpace() > spaceDto.getTotalSpace()){
                        throw new BusinessException(ResponseCodeEnum.CODE_904);
                    }
                    fileInfo.setFileId(fileId);
                    fileInfo.setFilePid(filePid);
                    fileInfo.setUserId(webUserDto.getUserId().toString());
                    fileInfo.setFileMd5(fileMd5);
                    fileInfo.setCreateTime(createTime);
                    fileInfo.setStatus(FileStatusEnums.USING.getStatus());
                    fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
                    //文件重命名
                    fileName = autoRename(filePid, webUserDto.getUserId().toString(), fileName);
                    fileInfo.setFileName(fileName);
                    fileMapper.insertFile(fileInfo);
                    resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());
                    //更新用户使用空间
                    updateUserSpace(webUserDto, fileInfo.getFileSize(), null);

                    return resultDto;
                }
            }
            //暂存目录 /file/temp/userId-FileId
            String fileFolder = projectFolder + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_TEMP + webUserDto.getUserId().toString() + '-' + fileId;
            tempFolder = new File(fileFolder);
            if (!tempFolder.exists()){
                tempFolder.mkdirs();
            }

            // 检查网盘空间容量是否充足
            Long curTempSize = redisComponent.getFileTempSize(webUserDto.getUserId().toString(), fileId);
            if(file.getSize() + curTempSize + spaceDto.getUseSpace() > spaceDto.getTotalSpace()){
                throw new BusinessException(ResponseCodeEnum.CODE_904);
            }

            //创建分片临时文件
            File chunkFile = new File(tempFolder.getPath() + '/' + chunkIndex);
            file.transferTo(chunkFile);
            //redis保存临时文件大小
            redisComponent.saveFileTempSize(webUserDto.getUserId().toString(), fileId, file.getSize());
            // 不是最后一个分片
            if (chunkIndex < chunks-1){
                resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
                return resultDto;
            }

            //最后一个分片
            log.info("上传最后一个分片：" + chunkIndex + ".......................");
            if (chunkIndex == chunks - 1){
                String month = FileTools.dateFormat(DateTimePatternEnum.YYYY_MM);
                String fileSuffix = StringTools.getFileSuffix(fileName);
                String realFileName = webUserDto.getUserId() + "-" + fileId + fileSuffix;
                // 根据扩展名判断文件类型
                FileTypeEnums fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
                // 重命名文件
                fileName = autoRename(filePid, webUserDto.getUserId().toString(), fileName);
                //上传数据库
                FileInfo newfile = new FileInfo();

                newfile.setFileId(fileId);
                newfile.setUserId(webUserDto.getUserId().toString());
                newfile.setFileMd5(fileMd5);
                newfile.setFilePid(filePid);
                curTempSize = redisComponent.getFileTempSize(webUserDto.getUserId().toString(), fileId); //文件大小
                newfile.setFileSize(curTempSize);
                newfile.setFileName(fileName);
                newfile.setFilePath(month + "/" + realFileName);
                Date time = new Date();  //时间
                newfile.setCreateTime(time);
                newfile.setLastUpdateTime(time);
                newfile.setFolderType(FileFolderTypeEnums.FILE.getType());
                newfile.setFileCategory(fileTypeEnums.getCategory().getCategory());
                newfile.setFileType(fileTypeEnums.getType());
                newfile.setStatus(FileStatusEnums.TRANSFER.getStatus());
                newfile.setDelFlag(FileDelFlagEnums.USING.getFlag());

                fileMapper.insertFile(newfile);

                // 更新用户使用空间
                updateUserSpace(webUserDto, curTempSize,null);
                // 设置响应status为上传完成
                resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());

                //事务提交后调用异步方法
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("事务提交完成，准备合并分片...............................");
                        fileService.transferFile(newfile.getFileId(), webUserDto);
                    }
                });

                return resultDto;
            }

        }catch (BusinessException e){
            uploadSuccess = false;
            log.error("文件上传失败 - 1", e);
        }catch (Exception e){
            uploadSuccess = false;
            log.error("文件上传失败 - 2", e);
            throw new BusinessException("文件上传失败 - 2");
        } finally {
            // 临时目录不为空 和 上传失败 时删除临时目录
            if (tempFolder != null && !uploadSuccess){
                try {
                    FileUtils.deleteDirectory(tempFolder);
                } catch (IOException e){
                    log.error("删除临时目录失败");
                }
            }
        }

        return resultDto;
    }


    private void updateUserSpace(SessionWebUserDto webUserDto, Long useSize, Long totalSpace) {
        Integer count = userMapper.updateUserSpace(webUserDto.getUserId(), useSize, totalSpace);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId().toString());
        spaceDto.setUseSpace(spaceDto.getUseSpace() + useSize);
        redisComponent.saveUserSpaceUse(webUserDto.getUserId().toString(), spaceDto);
    }
    private String autoRename(String filePid, String userId, String fileName) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfoQuery.setFileName(fileName);
        int count = fileMapper.findCountByParam2(fileInfoQuery);

        List<FileInfo> list = fileMapper.findListByParam2(fileInfoQuery);
        for (FileInfo f : list){
            log.info(f.toString());
        }

        log.info("autoRename - count: " + count);
        if (count > 0) {
            return StringTools.rename(fileName, count);
        }

        return fileName;
    }

    @Async("taskExecutor")
    public void transferFile(String fileId, SessionWebUserDto webUserDto){
        log.info("进入transferFile...............................");
        Boolean transferSuccess = true;
        String targetFilePath = null;
        String cover = null;
        FileTypeEnums fileTypeEnum = null;
        FileInfo file = fileMapper.findByFileIdAndUserId(fileId, webUserDto.getUserId().toString());
        try{
            if (file == null || !FileStatusEnums.TRANSFER.getStatus().equals(file.getStatus())){
                log.info("transferFile - 找不到文件。");
                return;
            }
            String userFileName = webUserDto.getUserId().toString() + "-" + fileId;
            // 临时目录
            String tempFileFolder = projectFolder + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_TEMP + userFileName;
            File tempFolder = new File(tempFileFolder);
            log.info("临时目录： " + tempFileFolder);
            // 月份和文件后缀
            String month = FileTools.dateFormat(DateTimePatternEnum.YYYY_MM);
            String fileSuffix = StringTools.getFileSuffix(file.getFileName());
            // 持久化文件存储位置
            String fileFolderPersist = projectFolder + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_PERSIST;
            String targetFolder = fileFolderPersist + month;
            File fileFolder = new File(targetFolder);
            if (!fileFolder.exists()){
                fileFolder.mkdirs();
            }
            // 持久文件名
            String realFileName = userFileName + fileSuffix;
            // 持久文件路径
            targetFilePath = fileFolder.getPath() + "/" + realFileName;
            log.info("持久化路径： " + targetFilePath);
            // 合并
            union(tempFolder.getPath(), targetFilePath, file.getFileName(), true);



        }catch (Exception e){
            log.error("文件转码失败，文件Id:{},userId:{}", fileId, webUserDto.getUserId(), e);
            transferSuccess = false;
        }finally {
            FileInfo updateInfo = new FileInfo();
            updateInfo.setFileCover(cover);
            updateInfo.setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
            fileMapper.updateFileStatusWithOldStatus(fileId, webUserDto.getUserId().toString(), updateInfo, FileStatusEnums.TRANSFER.getStatus());
            // 如果转码不成功，返还用户空间
            if (! transferSuccess){
                updateUserSpace(webUserDto, file.getFileSize() * (-1), null);
            }
        }
    }

    public static void union(String dirPath, String toFilePath, String fileName, boolean delSource) throws BusinessException {
        log.info("开始合并分片...............................");
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException("目录不存在");
        }
        File fileList[] = dir.listFiles();
        File targetFile = new File(toFilePath);
        RandomAccessFile writeFile = null;
        try {
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len;
                //创建读块文件的对象
                File chunkFile = new File(dirPath + File.separator + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败", e);
                    throw new BusinessException("合并文件失败");
                } finally {
                    readFile.close();
                }
            }
        } catch (Exception e) {
            log.error("合并文件:{}失败", fileName, e);
            throw new BusinessException("合并文件" + fileName + "出错了");
        } finally {
            try {
                if (null != writeFile) {
                    writeFile.close();
                }
            } catch (IOException e) {
                log.error("关闭流失败", e);
            }
            if (delSource) {
                if (dir.exists()) {
                    try {
                        FileUtils.deleteDirectory(dir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
