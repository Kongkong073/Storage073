package com.storage073.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CreateImageCode {

    private int width;    // 验证码图片的宽度
    private int height;   // 验证码图片的高度
    private int codeCount;   // 验证码字符个数
    private int lineCount;   // 干扰线数量
    private String code = null; // 生成的验证码
    private BufferedImage bufferedImage = null; // 生成的验证码图片

    private char[] codeSequence = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9' };

    public CreateImageCode(int width, int height, int codeCount, int lineCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        this.lineCount = lineCount;
        createImage();
    }

    public void createImage() {
        int x = 0, fontHeight = 0, codeY = 0;
        int red = 0, green = 0, blue = 0;

        x = width / (codeCount + 2); // 每个字符的宽度
        fontHeight = height - 2; // 字体的高度
        codeY = height - 4;

        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();
        Random random = new Random();

        graphics.setColor(Color.WHITE); // 背景色
        graphics.fillRect(0, 0, width, height);

        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        graphics.setFont(font);

        for (int i = 0; i < lineCount; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);

            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            graphics.setColor(new Color(red, green, blue));
            graphics.drawLine(x1, y1, x2, y2);
        }

        StringBuffer randomCode = new StringBuffer();

        for (int i = 0; i < codeCount; i++) {
            String strRand = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            graphics.setColor(new Color(red, green, blue));
            graphics.drawString(strRand, (i + 1) * x, codeY);
            randomCode.append(strRand);
        }

        code = randomCode.toString();
    }

    public void write(OutputStream os) throws IOException {
        ImageIO.write(bufferedImage, "jpeg", os);
        os.close();
    }

    public String getCode() {
        return code;
    }
}
