package com.sugarpop.loyalty.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    /**
     * Genera la imagen de un código QR en formato de arreglo de bytes (PNG)
     * @param contenido Texto o identificador que se codificará (ej. teléfono del cliente)
     * @param ancho Ancho de la imagen en px
     * @param alto Alto de la imagen en px
     * @return Arreglo de bytes con la imagen PNG
     */
    public byte[] generarQrCliente(String contenido, int ancho, int alto) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el código QR para el contenido: " + contenido, e);
        }
    }
}