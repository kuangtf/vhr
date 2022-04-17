package com.vhr.ktf.web.core.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.vhr.ktf.common.exception.file.FileNameLengthLimitExceededException;
import com.vhr.ktf.common.exception.file.InvalidExtensionException;
import com.vhr.ktf.common.utils.file.FileUploadUtils;
import com.vhr.ktf.common.utils.file.MimeTypeUtils;
import com.vhr.ktf.web.core.config.QiNiuProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * @author github.com/kuangtf
 * @date 2022/2/15 14:43
 */
public class QiNiuUtil {

    private static final Logger log = LoggerFactory.getLogger(QiNiuUtil.class);

    private static final String ACCESS_KEY = QiNiuProperties.getInstance().getAccessKey();
    private static final String  SECRET_KEY = QiNiuProperties.getInstance().getSecretKey();
    private static final String  BUCKET = QiNiuProperties.getInstance().getBucketName();
    private static final String  DO_MAIN = QiNiuProperties.getInstance().getDoMain();

    /**
     * 七牛云上传文件
     *
     * @param multipartFile 文件
     * @return 返回结果
     */
    public static String fileUpload(MultipartFile multipartFile) throws IOException, InvalidExtensionException {

        int fileNameLength = Objects.requireNonNull(multipartFile.getOriginalFilename()).length();
        // 判断文件名长度
        if (fileNameLength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }
        // 文件大小校验
        FileUploadUtils.assertAllowed(multipartFile, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);

        // 构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huanan());
        UploadManager uploadManager = new UploadManager(cfg);

        //默认不指定 key 的情况下，以文件内容的 hash 值作为文件名
        String key = "avatar/" + UUID.randomUUID().toString().replaceAll("-", "");
        String result = null;

        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        String upToken = auth.uploadToken(BUCKET);

        try {
            InputStream inputStream = multipartFile.getInputStream();
            Response response = uploadManager.put(inputStream, key + multipartFile.getOriginalFilename(), upToken, null, null);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            result = DO_MAIN + "/" + putRet.key;
        } catch (QiniuException ex) {
            log.error(ex.getMessage(), ex);
        }
        return result;
    }
}
