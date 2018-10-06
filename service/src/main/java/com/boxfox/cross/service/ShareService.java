package com.boxfox.cross.service;

import com.boxfox.cross.service.model.ShareContent;
import com.boxfox.vertx.data.Config;
import com.boxfox.vertx.secure.AES256;
import com.boxfox.vertx.service.AbstractService;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class ShareService extends AbstractService {
    private Gson gson;

    public ShareService() {
        gson = new Gson();
    }

    public String createTransactionData(String symbol, String address, float amount) {
        ShareContent content = new ShareContent();
        content.setSymbol(symbol);
        content.setAddress(address);
        content.setAmount(amount);
        return AES256.encrypt(gson.toJson(content));
    }

    public ShareContent decodeTransactionData(String data) {
        ShareContent content = null;
        if (data != null) {
            String str = AES256.decrypt(data);
            content = gson.fromJson(str, ShareContent.class);
        }
        return content;
    }

    public String createTransactionHtml(String data) {
        final String INTENT_NAME = "cross-transaction";
        String html = "<html><script>window.location.href='INTENT_NAME://"+data+"/'</script></html>";
        return html;
    }

    public File createQRImage(String qrCodeText) {
        File file = null;
        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 200, 200, hintMap);
            int matrixWidth = byteMatrix.getWidth();
            BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);
            image.createGraphics();

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, matrixWidth, matrixWidth);
            graphics.setColor(Color.BLACK);

            for (int i = 0; i < matrixWidth; i++) {
                for (int j = 0; j < matrixWidth; j++) {
                    if (byteMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
            file = new File(Config.getDefaultInstance().getString("cachePath")+"/"+System.currentTimeMillis()+".png");
            ImageIO.write(image, "png", file);
        }catch(WriterException | IOException e) {
            e.printStackTrace();
            if(file!=null)
                file.delete();
        }
        return file;
    }
}
