package com.hmdp.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.Result;
import com.hmdp.utils.constants.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    /**
     * 上传图片
     * @param image 上传的图片
     * @return
     */
    @PostMapping("blog")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        try {
            // 校验文件大小（最大5MB）
            if (image.getSize() > 5 * 1024 * 1024) {
                return Result.fail("文件大小不能超过5MB");
            }
            // 校验文件类型
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.fail("只允许上传图片文件");
            }
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return Result.fail("文件名不能为空");
            }
            // 校验文件后缀
            String suffix = StrUtil.subAfter(originalFilename, ".", true);
            if (!isValidImageSuffix(suffix)) {
                return Result.fail("不支持的图片格式");
            }
            // 生成新文件名
            String fileName = createNewFileName(originalFilename);
            // 保存文件
            image.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName));
            // 返回结果
            log.debug("文件上传成功，{}", fileName);
            return Result.ok(fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 删除图片
     * @param filename 文件名
     * @return
     */
    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        // 校验文件名，防止路径穿越
        if (filename == null || filename.isEmpty()) {
            return Result.fail("文件名不能为空");
        }
        // 检查是否包含路径穿越字符
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return Result.fail("非法的文件名称");
        }
        // 检查文件扩展名是否为图片
        String suffix = StrUtil.subAfter(filename, ".", true);
        if (!isValidImageSuffix(suffix)) {
            return Result.fail("只允许删除图片文件");
        }
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, filename);
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }
        // 确保文件在上传目录内
        if (!file.getAbsolutePath().startsWith(new File(SystemConstants.IMAGE_UPLOAD_DIR).getAbsolutePath())) {
            return Result.fail("非法的文件路径");
        }
        FileUtil.del(file);
        return Result.ok();
    }

    /**
     * 校验图片文件后缀
     */
    private boolean isValidImageSuffix(String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            return false;
        }
        String lowerSuffix = suffix.toLowerCase();
        return "jpg".equals(lowerSuffix) || "jpeg".equals(lowerSuffix) ||
               "png".equals(lowerSuffix) || "gif".equals(lowerSuffix) ||
               "bmp".equals(lowerSuffix) || "webp".equals(lowerSuffix);
    }

    /**
     * 生成新的文件名
     * @param originalFilename 原始文件名
     * @return 新的文件名
     */
    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        // 生成目录
        String name = UUID.randomUUID().toString();
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;
        // 判断目录是否存在
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, StrUtil.format("/blogs/{}/{}", d1, d2));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        return StrUtil.format("/blogs/{}/{}/{}.{}", d1, d2, name, suffix);
    }
}
