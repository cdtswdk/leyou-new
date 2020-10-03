package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {
    private static final List<String> contentTypes = Arrays.asList("image/jpg","image/jpeg","image/gif","image/png");
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient storageClient;
    public String uploadImage(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();
        // 校验文件的类型
        String contentType = file.getContentType();
        if(!contentTypes.contains(contentType)){
            LOGGER.info("文件类型不合法：{}",originalFilename);
            return null;
        }

        try {
            // 校验文件的内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if(bufferedImage == null){
                LOGGER.info("文件内容不合法：{}",originalFilename);
                return null;
            }
            // 保存到服务器
            //file.transferTo(new File("E:\\代码\\Java\\黑马Java\\11-乐优商城\\image\\"+originalFilename));
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            String fullPath = storePath.getFullPath();
            // 生成url地址，返回
            return "http://image.leyou.com/"+fullPath;
        } catch (Exception e) {
            LOGGER.info("服务器内部错误：{}",originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
