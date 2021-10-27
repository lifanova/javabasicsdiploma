package ru.netology.graphics.image.impl;

import ru.netology.graphics.image.BadImageSizeException;
import ru.netology.graphics.image.TextColorSchema;
import ru.netology.graphics.image.TextGraphicsConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class TextGraphicsConverterImpl implements TextGraphicsConverter {
    private int maxWidth;
    private int maxHeight;
    private double maxRatio;
    private TextColorSchema schema;

    @Override
    public String convert(String url) throws IOException, BadImageSizeException {
        if (url == null || url.isEmpty()) {
            System.out.println("[Error]: URL картинки пуст!");
            return null;
        }

        // Забираем изоображение по урлу
        BufferedImage img = ImageIO.read(new URL(url));

        // Делаем проверку на max допустимое соотношение сторон картинки
        int width = img.getWidth();
        int height = img.getHeight();
        //System.out.println("width = " + width + ", height = " + height);

        double ratio = width / height;
        //System.out.println("ratio = " + ratio + ", maxRatio = " + maxRatio);
        if (ratio > maxRatio) {
            throw new BadImageSizeException(ratio, maxRatio);
        }

        // Выставлены max ширина и высота, =>, меняем ширину и высоту картинки, если она не подходит под max размеры
        int newWidth = width;
        int newHeight = height;
        if (width > maxWidth) {
            int k = width / maxWidth;
            newWidth = maxWidth;
            newHeight = height / k;
        }

        if (height > maxHeight) {
            int k = height / maxHeight;
            newHeight = maxHeight;
            newWidth = width / k;
        }

        // Масштабируем
        Image scaledImage = img.getScaledInstance(newWidth, newHeight, BufferedImage.SCALE_SMOOTH);
        // Переводим в ч/б
        BufferedImage bwImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);

        // Попросим у этой картинки инструмент для рисования на ней:
        Graphics2D graphics = bwImg.createGraphics();
        // А этому инструменту скажем, чтобы он скопировал содержимое из нашей суженной картинки:
        graphics.drawImage(scaledImage, 0, 0, null);

        ImageIO.write(bwImg, "png", new File("out.png"));

        WritableRaster bwRaster = bwImg.getRaster();

        if (schema == null) {
            schema = new TextColorSchemaImpl();
        }

        StringBuilder builder = new StringBuilder();
        for (int h = 0; h < newHeight; h++) {
            for (int w = 0; w < newWidth; w++) {
                int color = bwRaster.getPixel(w, h, new int[3])[0];
                char c = schema.convert(color);
                //запоминаем символ c, например, в двумерном массиве или как-то ещё на ваше усмотрение
                builder.append(c);
            }

            builder.append("\n");
        }

        // Возвращаем собранный текст
        return builder.toString();
    }

    @Override
    public void setMaxWidth(int width) {
        this.maxWidth = width;
    }

    @Override
    public void setMaxHeight(int height) {
        this.maxHeight = height;
    }

    @Override
    public void setMaxRatio(double maxRatio) {
        this.maxRatio = maxRatio;
    }

    @Override
    public void setTextColorSchema(TextColorSchema schema) {
        this.schema = schema;
    }
}
