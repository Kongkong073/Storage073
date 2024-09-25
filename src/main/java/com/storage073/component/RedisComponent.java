package com.storage073.component;


import com.aliyun.oss.model.PartETag;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storage073.entity.dto.SysSettingsDto;
import com.storage073.entity.dto.UserSpaceDto;
import com.storage073.mapper.FileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.storage073.entity.Constants;
import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;


/*
* Redis中存放的对象
* */
@Component("redisComponent")
@Slf4j
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private FileMapper fileMapper;

    @Autowired
    private ObjectMapper objectMapper1;
    /**
     * 获取系统设置
     *
     * @return
     */
    public SysSettingsDto getSysSettingsDto() {
        SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingsDto == null) {
            sysSettingsDto = new SysSettingsDto();
            saveSysSettingsDto(sysSettingsDto);
        }
        return sysSettingsDto;
    }

    /**
     * 保存设置
     *
     * @param sysSettingsDto
     */
    public void saveSysSettingsDto(SysSettingsDto sysSettingsDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
    }



    /**
     * 保存已使用的空间
     *
     * @param userId
     */
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
    }

//    public UserSpaceDto getUserSpaceUse(String userId) {
//        UserSpaceDto spaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
//        if (null == spaceDto) {
//            log.info("进入null == spaceDto");
//            spaceDto = new UserSpaceDto();
//            Long useSpace = fileMapper.getUseSpaceByUserId(userId);
//            spaceDto.setUseSpace(useSpace);
//            spaceDto.setTotalSpace(getSysSettingsDto().getUserInitUseSpace() * Constants.MB);
//            redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, spaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
//        }
////        log.info("userSpaceDto - Redis: " + spaceDto.toString());
//        return spaceDto;
//    }

    public UserSpaceDto getUserSpaceUse(String userId) {
        String key = Constants.REDIS_KEY_USER_SPACE_USE + userId;
        UserSpaceDto spaceDto = (UserSpaceDto) redisUtils.get(key, UserSpaceDto.class);  // 指定反序列化为 UserSpaceDto
        if (spaceDto == null) {
            log.info("进入null == spaceDto");
            spaceDto = new UserSpaceDto();
            Long useSpace = fileMapper.getUseSpaceByUserId(userId);
            spaceDto.setUseSpace(useSpace);
            spaceDto.setTotalSpace(getSysSettingsDto().getUserInitUseSpace() * Constants.MB);
            redisUtils.setex(key, spaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
        }
        return spaceDto;
    }


    public Long getFileTempSize(String userId, String fileId){
        Long fileTempSIze = getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId+fileId);
        return fileTempSIze;
    }

    public void saveFileTempSize(String userId, String fileId, Long tempSize){
        Long curTempSize = getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId+fileId);
        Long updateTempsize = curTempSize + tempSize;
        redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId+fileId, updateTempsize, Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
    }

    private Long getFileSizeFromRedis(String key) {
        Object sizeObj = redisUtils.get(key);
        if (sizeObj == null) {
            return 0L;
        }
        if (sizeObj instanceof Integer) {
            return ((Integer) sizeObj).longValue();
        } else if (sizeObj instanceof Long) {
            return (Long) sizeObj;
        }

        return 0L;
    }

    public String getUploadId(String userId, String fileId) {
        String key = Constants.REDIS_KEY_UPLOAD_ID + userId + ":" + fileId;
        return (String) redisUtils.get(key);
    }

    public void saveUploadId(String userId, String fileId, String uploadId) {
        String key = Constants.REDIS_KEY_UPLOAD_ID + userId + ":" + fileId;
        redisUtils.setex(key, uploadId, Constants.REDIS_KEY_EXPIRES_ONE_HOUR); // 设置过期时间为 1 小时
    }

    public void deleteUploadId(String userId, String fileId) {
        String key = Constants.REDIS_KEY_UPLOAD_ID + userId + ":" + fileId;
        redisUtils.delete(key);
    }


    public void savePartETag(String userId, String fileId, Integer chunkIndex, PartETag partETag) {
        String key = Constants.REDIS_KEY_PART_ETAG + userId + ":" + fileId;
        List<PartETag> partETags = (ArrayList<PartETag>) redisUtils.get(key, new TypeReference<List<PartETag>>() {});
        // 获取当前的 PartETag 列表
//        List<Object> partETags = (ArrayList<Object>) redisUtils.get(key, new TypeReference<List<Object>>() {});

        if (partETags == null) {
            partETags = new ArrayList<>();
        }

        // 如果分片索引已经存在，替换对应的分片信息
        if (chunkIndex < partETags.size()) {
            partETags.set(chunkIndex, partETag);
        } else {
            partETags.add(partETag);
        }

        redisUtils.setex(key, partETags, Constants.REDIS_KEY_EXPIRES_ONE_HOUR); // 设置过期时间
    }


    public List<PartETag> getPartETags(String userId, String fileId) {
        String key = Constants.REDIS_KEY_PART_ETAG + userId + ":" + fileId;
        List<PartETag> result = (ArrayList<PartETag>) redisUtils.get(key, new TypeReference<List<PartETag>>() {});
//        List<Object> partETags = (ArrayList<Object>) redisUtils.get(key, new TypeReference<List<Object>>() {});
//        // 新建List<PartETag>，待返回
//        List<PartETag> result = new ArrayList<>();
//
//        for(Object e: partETags){
//            result.add(objectMapper1.convertValue(e, PartETag.class));
//        }
        return result;
    }

}
