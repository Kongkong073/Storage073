package com.storage073.entity;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import javax.imageio.ImageIO;
import java.awt.geom.Ellipse2D;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Slf4j
public class AvatarTools {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";

    public static void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print("请在头像目录下放置默认头像default_avatar.jpg");
            writer.close();
        } catch (Exception e) {
            log.error("输出无默认图失败", e);
        } finally {
            writer.close();
        }
    }

    // 新增的方法：将头像缩放并裁剪为圆形
    public static BufferedImage resizeAndCropToCircle(String filePath, int targetSize) {
        try {
            BufferedImage originalImage = ImageIO.read(new File(filePath));

            // 缩放图像
            Image scaledImage = originalImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
            BufferedImage bufferedScaledImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = bufferedScaledImage.createGraphics();
            g2.setClip(new Ellipse2D.Float(0, 0, targetSize, targetSize));
            g2.drawImage(scaledImage, 0, 0, null);
            g2.dispose();

            return bufferedScaledImage;
        } catch (IOException e) {
            log.error("Error while resizing and cropping avatar image", e);
            return null;
        }
    }

    // 读取并返回圆形头像
    public static void sendCircularAvatar(HttpServletResponse response, String filePath, int size) {
        BufferedImage circularImage = resizeAndCropToCircle(filePath, size);
        if (circularImage != null) {
            try {
                response.setContentType("image/png");
                ImageIO.write(circularImage, "png", response.getOutputStream());
            } catch (IOException e) {
                log.error("Error sending circular avatar image", e);
            }
        } else {
            printNoDefaultImage(response);
        }
    }

}
