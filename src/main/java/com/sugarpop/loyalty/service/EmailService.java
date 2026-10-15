package com.sugarpop.loyalty.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String correoRemitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía un correo con diseño HTML de bienvenida y la tarjeta de fidelización.
     */
    public void enviarTarjetaBienvenida(String destinatarioEmail, String nombreCliente, String sistemaOperativo, String walletUrl, byte[] qrBytes) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            // true indica que es un mensaje multipart (permite HTML e imágenes adjuntas)
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(correoRemitente, "Sugar Pop Repostería");
            helper.setTo(destinatarioEmail);
            helper.setSubject("🧁 ¡Bienvenido a Sugar Pop! Aquí está tu Tarjeta de Lealtad");

            boolean esAndroid = "ANDROID".equalsIgnoreCase(sistemaOperativo);

            // Construcción del diseño HTML del correo
            StringBuilder html = new StringBuilder();
            html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #FDF2F8; padding: 25px; border-radius: 20px; border: 1px solid #FBCFE8;'>");
            html.append("<div style='text-align: center; margin-bottom: 20px;'>");
            html.append("<h1 style='color: #BE185D; margin: 0; font-size: 28px; letter-spacing: 2px;'>SUGAR POP</h1>");
            html.append("<p style='color: #9D174D; margin: 5px 0 0 0; font-size: 14px;'>Club de Lealtad & Recompensas</p>");
            html.append("</div>");

            html.append("<div style='background-color: #FFFFFF; padding: 25px; border-radius: 15px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); text-align: center;'>");
            html.append("<h2 style='color: #374151; font-size: 20px; margin-top: 0;'>¡Hola, ").append(nombreCliente).append("! 🎉</h2>");
            html.append("<p style='color: #6B7280; font-size: 14px; line-height: 1.6;'>");
            html.append("Gracias por registrarte en nuestro programa de lealtad. A partir de hoy, <strong>cada compra suma puntos</strong> para canjear tus postres y bebidas favoritas.");
            html.append("<br><span style='color: #DB2777; font-weight: bold;'>Regla: 1 punto por cada $10.00 MXN en compras.</span>");
            html.append("</p>");

            if (esAndroid && walletUrl != null && !walletUrl.isBlank()) {
                // Sección específica para usuarios Android (Botón de Google Wallet)
                html.append("<div style='margin: 30px 0;'>");
                html.append("<p style='color: #374151; font-weight: bold; font-size: 15px;'>Tu pase de Google Wallet está listo:</p>");
                html.append("<a href='").append(walletUrl).append("' target='_blank' style='display: inline-block; background-color: #000000; color: #FFFFFF; text-decoration: none; padding: 14px 28px; border-radius: 12px; font-weight: bold; font-size: 15px; box-shadow: 0 4px 10px rgba(0,0,0,0.2);'>");
                html.append("💳 Guardar en Google Wallet");
                html.append("</a>");
                html.append("<p style='color: #9CA3AF; font-size: 12px; margin-top: 10px;'>Presiona el botón para agregarla a tu app Billetera de Google.</p>");
                html.append("</div>");
            } else {
                // Sección para usuarios iOS (Código QR incrustado)
                html.append("<div style='margin: 25px 0;'>");
                html.append("<p style='color: #374151; font-weight: bold; font-size: 15px;'>Tu Tarjeta Digital (Código QR):</p>");
                html.append("<p style='color: #6B7280; font-size: 13px;'>Muestra este código QR en la caja de Sugar Pop cada vez que nos visites:</p>");
                html.append("<div style='display: inline-block; padding: 10px; background-color: #FFF1F2; border: 2px solid #FBCFE8; border-radius: 12px; margin-top: 10px;'>");
                html.append("<img src='cid:qrCliente' alt='Código QR' style='width: 220px; height: 220px; display: block;'/>");
                html.append("</div>");
                html.append("<p style='color: #9CA3AF; font-size: 12px; margin-top: 8px;'>También te lo adjuntamos como imagen para que lo guardes en tus fotos.</p>");
                html.append("</div>");
            }

            html.append("<hr style='border: none; border-top: 1px solid #F3F4F6; margin: 25px 0;'/>");
            html.append("<p style='color: #9CA3AF; font-size: 12px;'>Sugar Pop Repostería • Endulzando tus mejores momentos</p>");
            html.append("</div>");
            html.append("</div>");

            helper.setText(html.toString(), true); // true = interpretar como HTML

            // Si es iOS o no es Android, adjuntamos e incrustamos la imagen del QR
            if (!esAndroid && qrBytes != null && qrBytes.length > 0) {
                ByteArrayResource qrResource = new ByteArrayResource(qrBytes);
                // Incrustar imagen dentro del correo con 'cid:qrCliente'
                helper.addInline("qrCliente", qrResource, "image/png");
                // Adjuntar archivo descargable
                helper.addAttachment("tarjeta-sugarpop-qr.png", qrResource);
            }

            // Enviar el correo
            mailSender.send(mensaje);
            System.out.println("¡Correo de bienvenida enviado exitosamente a: " + destinatarioEmail + "!");

        } catch (Exception e) {
            System.err.println("Aviso: No se pudo enviar el correo a " + destinatarioEmail + ": " + e.getMessage());
        }
    }
}