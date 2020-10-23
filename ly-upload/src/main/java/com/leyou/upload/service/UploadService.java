package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.constant.LyConstant;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class UploadService {
    @Autowired
    private OSS client;

    @Autowired
    private OSSProperties prop;


    /**
     * 允许的图片类型
     */
    private static final List ALLOW_IMG_TYPE = Arrays.asList("image/jpeg");

    public String uploadImgToNginx(MultipartFile file) {
        //获取文件的mime类型
        String imgType = file.getContentType();
        //判断文件mime类型是否是允许的类型
        if(!ALLOW_IMG_TYPE.contains(imgType)){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        BufferedImage buffer = null;
        try {
            buffer = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        if(buffer==null){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        //获取到要上传的文件夹对象
        File imgFile = new File(LyConstant.BRAND_IMG_FILE);
        //设置图片在文件夹中的名称
        String fileName = UUID.randomUUID()+file.getOriginalFilename();
        //上传文件
        try {
            file.transferTo(new File(imgFile, fileName));
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
        return LyConstant.BRAND_IMG_URL+fileName;
    }

    public Map<String, Object> getSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<String, Object>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
}