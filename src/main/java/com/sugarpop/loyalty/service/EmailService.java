package com.sugarpop.loyalty.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.sugarpop.loyalty.model.Recompensa;
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
    public void enviarTarjetaBienvenida(String destinatarioEmail, String nombreCliente, byte[] qrBytes) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            // true indica que es un mensaje multipart (permite HTML e imágenes adjuntas)
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(correoRemitente, "Sugar Pop Repostería");
            helper.setTo(destinatarioEmail);
            helper.setSubject("🧁 ¡Bienvenido a Sugar Pop! Aquí está tu Tarjeta de Lealtad");

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

            html.append("<div style='margin: 25px 0;'>");
            html.append("<p style='color: #374151; font-weight: bold; font-size: 15px;'>Tu Tarjeta Digital (Código QR):</p>");
            html.append("<p style='color: #6B7280; font-size: 13px;'>Muestra este código QR en la caja de Sugar Pop cada vez que nos visites:</p>");
            html.append("<div style='display: inline-block; padding: 10px; background-color: #FFF1F2; border: 2px solid #FBCFE8; border-radius: 12px; margin-top: 10px;'>");
            html.append("<img src='cid:qrCliente' alt='Código QR' style='width: 220px; height: 220px; display: block;'/>");
            html.append("</div>");
            html.append("<p style='color: #9CA3AF; font-size: 12px; margin-top: 8px;'>También te lo adjuntamos como imagen para que lo guardes en tus fotos.</p>");
            html.append("</div>");

            html.append("<hr style='border: none; border-top: 1px solid #F3F4F6; margin: 25px 0;'/>");
            html.append("<p style='color: #9CA3AF; font-size: 12px;'>Sugar Pop Repostería • Endulzando tus mejores momentos</p>");
            html.append("</div>");
            html.append("</div>");

            helper.setText(html.toString(), true); // true = interpretar como HTML

            if (qrBytes != null && qrBytes.length > 0) {
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

    /**
     * Envía un correo con diseño HTML de agradecimiento por compra, detallando:
     * 1. Puntos sumados en la visita.
     * 2. Saldo total acumulado.
     * 3. Recompensas activas que el cliente ya puede canjear.
     * 4. Próximas recompensas del catálogo y cuántos puntos le faltan para alcanzarlas.
     */
    public void enviarNotificacionCompra(
            String destinatarioEmail,
            String nombreCliente,
            BigDecimal montoCompra,
            int puntosGanados,
            int puntosTotales,
            List<Recompensa> recompensasActivas) {

        // Si el cliente no cuenta con correo registrado, salimos limpiamente
        if (destinatarioEmail == null || destinatarioEmail.trim().isEmpty()) {
            return;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(correoRemitente, "Sugar Pop Repostería");
            helper.setTo(destinatarioEmail);
            helper.setSubject("🧁 ¡Gracias por tu compra en Sugar Pop! Has sumado +" + puntosGanados + " puntos");

            // Clasificamos las recompensas entre:
            // a) Alcanzables (puntosRequeridos <= saldo total)
            // b) Próximas metas (puntosRequeridos > saldo total)
            List<Recompensa> alcanzables = new ArrayList<>();
            List<Recompensa> proximas = new ArrayList<>();

            if (recompensasActivas != null) {
                for (Recompensa r : recompensasActivas) {
                    if (r.getPuntosRequeridos() <= puntosTotales) {
                        alcanzables.add(r);
                    } else {
                        proximas.add(r);
                    }
                }
            }

            // Construcción del correo HTML
            StringBuilder html = new StringBuilder();
            html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #FDF2F8; padding: 25px; border-radius: 20px; border: 1px solid #FBCFE8;'>");

            // Encabezado con logotipo y título de la marca
            html.append("<div style='text-align: center; margin-bottom: 20px;'>");
            html.append("<h1 style='color: #BE185D; margin: 0; font-size: 28px; letter-spacing: 2px;'>SUGAR POP</h1>");
            html.append("<p style='color: #9D174D; margin: 5px 0 0 0; font-size: 14px;'>Club de Lealtad & Recompensas</p>");
            html.append("</div>");

            // Tarjeta principal con fondo blanco
            html.append("<div style='background-color: #FFFFFF; padding: 25px; border-radius: 15px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>");
            html.append("<h2 style='color: #374151; font-size: 20px; margin-top: 0; text-align: center;'>¡Gracias por tu compra, ").append(nombreCliente).append("! 🍰</h2>");
            html.append("<p style='color: #6B7280; font-size: 14px; line-height: 1.6; text-align: center;'>");
            html.append("Agradecemos tu preferencia. Tu compra por un monto de <strong>$").append(String.format("%.2f", montoCompra)).append(" MXN</strong> ha generado nuevos puntos para tu cuenta.");
            html.append("</p>");

            // Tarjeta central de puntos: Puntos ganados y Saldo total
            html.append("<div style='background: linear-gradient(135deg, #FCE7F3 0%, #FBCFE8 100%); padding: 18px; border-radius: 12px; margin: 20px 0; text-align: center; border: 1px solid #F472B6;'>");
            html.append("<div style='display: inline-block; margin: 0 15px;'>");
            html.append("<span style='font-size: 12px; color: #9D174D; font-weight: bold; text-transform: uppercase;'>Puntos Ganados</span>");
            html.append("<p style='margin: 4px 0 0; font-size: 26px; font-weight: 900; color: #059669;'>+").append(puntosGanados).append("</p>");
            html.append("</div>");
            html.append("<div style='display: inline-block; margin: 0 15px; border-left: 2px solid #F9A8D4; padding-left: 20px;'>");
            html.append("<span style='font-size: 12px; color: #9D174D; font-weight: bold; text-transform: uppercase;'>Saldo Total</span>");
            html.append("<p style='margin: 4px 0 0; font-size: 26px; font-weight: 900; color: #BE185D;'>").append(puntosTotales).append(" pts</p>");
            html.append("</div>");
            html.append("</div>");

            // SECCIÓN 1: Recompensas que ya puede canjear
            if (!alcanzables.isEmpty()) {
                html.append("<div style='margin-top: 25px;'>");
                html.append("<h3 style='color: #059669; font-size: 15px; margin: 0 0 10px 0;'>");
                html.append("🎉 ¡Recompensas que YA puedes canjear hoy!");
                html.append("</h3>");
                html.append("<div style='background-color: #ECFDF5; border: 1px solid #A7F3D0; border-radius: 10px; padding: 12px;'>");

                for (Recompensa r : alcanzables) {
                    html.append("<div style='display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px dashed #A7F3D0;'>");
                    html.append("<span style='color: #065F46; font-size: 13px; font-weight: bold;'>🧁 ").append(r.getNombre()).append("</span>");
                    html.append("<span style='background-color: #059669; color: #FFFFFF; font-size: 11px; font-weight: bold; padding: 3px 8px; border-radius: 6px;'>").append(r.getPuntosRequeridos()).append(" pts</span>");
                    html.append("</div>");
                }
                html.append("<p style='color: #047857; font-size: 11px; margin: 8px 0 0; text-align: center;'>Pide cualquiera de estos beneficios en la caja en tu próxima visita.</p>");
                html.append("</div>");
                html.append("</div>");
            }

            // SECCIÓN 2: Próximas recompensas del catálogo
            if (!proximas.isEmpty()) {
                html.append("<div style='margin-top: 25px;'>");
                html.append("<h3 style='color: #BE185D; font-size: 15px; margin: 0 0 10px 0;'>");
                html.append("⭐ Próximas metas del catálogo:");
                html.append("</h3>");
                html.append("<div style='background-color: #FFF1F2; border: 1px solid #FECDD3; border-radius: 10px; padding: 12px;'>");

                for (Recompensa r : proximas) {
                    int faltantes = r.getPuntosRequeridos() - puntosTotales;
                    html.append("<div style='display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px dashed #FECDD3;'>");
                    html.append("<div>");
                    html.append("<span style='color: #881337; font-size: 13px; font-weight: bold;'>🍰 ").append(r.getNombre()).append("</span>");
                    html.append("<span style='color: #9F1239; font-size: 11px; margin-left: 6px;'>(").append(r.getPuntosRequeridos()).append(" pts)</span>");
                    html.append("</div>");
                    html.append("<span style='color: #E11D48; font-size: 11px; font-weight: bold;'>Te faltan ").append(faltantes).append(" pts</span>");
                    html.append("</div>");
                }
                html.append("</div>");
                html.append("</div>");
            }

            // Pie de página del correo
            html.append("<hr style='border: none; border-top: 1px solid #F3F4F6; margin: 25px 0;'/>");
            html.append("<p style='color: #9CA3AF; font-size: 12px; text-align: center; margin: 0;'>Sugar Pop Repostería • Endulzando tus mejores momentos</p>");
            html.append("</div>");
            html.append("</div>");

            helper.setText(html.toString(), true);
            mailSender.send(mensaje);
            System.out.println("¡Notificación de compra enviada exitosamente a: " + destinatarioEmail + "!");

        } catch (Exception e) {
            // Manejamos cualquier error del servidor SMTP para que la venta en el POS no falle
            System.err.println("Aviso: No se pudo enviar el correo de compra a " + destinatarioEmail + ": " + e.getMessage());
        }
    }

        /**
     * Envía un correo HTML festivo de cumpleaños al cliente indicado,
     * avisándole que tiene un postre gratis esperándolo en Sugar Pop
     * durante todo el mes de su cumpleaños.
     *
     * @param destinatarioEmail  Correo electrónico del cliente
     * @param nombreCliente      Nombre completo del cliente (para personalizar el saludo)
     * @param diaCumpleanos      Día del mes en que cumple años (ej: 24)
     * @param mesNombre          Nombre del mes en español (ej: "Octubre")
     */
    public void enviarCorreoCumpleanos(String destinatarioEmail, String nombreCliente,
                                       int diaCumpleanos, String mesNombre) {
        try {
            // Creamos el objeto mensaje usando el mailSender que ya existe en la clase
            MimeMessage mensaje = mailSender.createMimeMessage();

            // MimeMessageHelper facilita agregar HTML, adjuntos, etc.
            // El segundo parámetro "true" indica que es un mensaje multipart (con HTML)
            // El tercer parámetro define la codificación para soportar acentos y ñ
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            // Configuramos remitente, destinatario y asunto del correo
            helper.setFrom(correoRemitente, "Sugar Pop Repostería");
            helper.setTo(destinatarioEmail);
            helper.setSubject("🎂 ¡Feliz Cumpleaños de parte de Sugar Pop! Tu postre gratis te espera 🧁");

            // ── Construcción del cuerpo HTML del correo ──────────────────────────
            StringBuilder html = new StringBuilder();

            // Contenedor principal con fondo rosado festivo
            html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #FDF2F8; padding: 30px; border-radius: 20px; border: 2px solid #F9A8D4;'>");

            // Encabezado con logo y nombre de la marca
            html.append("<div style='text-align: center; margin-bottom: 20px;'>");
            html.append("<h1 style='color: #BE185D; margin: 0; font-size: 32px; letter-spacing: 2px;'>SUGAR POP</h1>");
            html.append("<p style='color: #9D174D; margin: 5px 0 0 0; font-size: 14px;'>🧁 Repostería & Club de Lealtad</p>");
            html.append("</div>");

            // Banner festivo de cumpleaños
            html.append("<div style='background: linear-gradient(135deg, #DB2777, #9333EA); padding: 30px; border-radius: 16px; text-align: center; margin-bottom: 25px;'>");
            html.append("<p style='color: #FDE68A; font-size: 48px; margin: 0;'>🎂🎉🎈</p>");
            html.append("<h2 style='color: #FFFFFF; font-size: 28px; margin: 10px 0 5px 0;'>¡Feliz Cumpleaños,</h2>");
            // Aquí insertamos el nombre real del cliente con Java, no HTML estático
            html.append("<h2 style='color: #FDE68A; font-size: 30px; margin: 0;'>").append(nombreCliente).append("! 🎁</h2>");
            // Mostramos el día y mes de su cumpleaños
            html.append("<p style='color: #F3E8FF; font-size: 15px; margin-top: 10px;'>")
                .append(diaCumpleanos).append(" de ").append(mesNombre)
                .append("</p>");
            html.append("</div>");

            // Sección del regalo: el postre gratis
            html.append("<div style='background-color: #FFFFFF; padding: 25px; border-radius: 16px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); text-align: center; margin-bottom: 20px;'>");
            html.append("<p style='font-size: 40px; margin: 0;'>🎁</p>");
            html.append("<h3 style='color: #BE185D; font-size: 22px; margin: 10px 0 5px 0;'>¡Tienes un POSTRE GRATIS!</h3>");
            html.append("<p style='color: #6B7280; font-size: 14px; line-height: 1.7; margin: 0;'>");
            html.append("En Sugar Pop celebramos tu día especial con un regalo dulce. ");
            html.append("Durante <strong>todo el mes de ").append(mesNombre).append("</strong>, ");
            html.append("tienes disponible este cupón para un postre de cortesía. Solo muestra este correo en caja. 🧁");
            html.append("</p>");
            // Resaltado visual del beneficio
            html.append("<div style='margin-top: 20px; padding: 15px; background-color: #FDF4FF; border: 2px dashed #D946EF; border-radius: 12px;'>");
            html.append("<p style='color: #7E22CE; font-size: 16px; font-weight: bold; margin: 0;'>✨ 1 Postre Gratis • Válido todo ").append(mesNombre).append("</p>");
            html.append("<p style='color: #9CA3AF; font-size: 12px; margin: 5px 0 0 0;'>Presenta este correo o menciónalo en caja al pagar</p>");
            html.append("</div>");
            html.append("</div>");

            // Mensaje motivacional de cierre
            html.append("<div style='text-align: center; padding: 15px;'>");
            html.append("<p style='color: #DB2777; font-size: 15px; font-weight: bold;'>¡Te esperamos para celebrar juntos! 🎊</p>");
            html.append("<p style='color: #9CA3AF; font-size: 12px;'>Sugar Pop Repostería • Endulzando tus mejores momentos</p>");
            html.append("</div>");

            // Cierre del contenedor principal
            html.append("</div>");
            // ─────────────────────────────────────────────────────────────────────

            // Le indicamos al helper que el cuerpo del correo es HTML (true = HTML)
            helper.setText(html.toString(), true);

            // Enviamos el correo
            mailSender.send(mensaje);
            System.out.println("✅ Correo de cumpleaños enviado a: " + destinatarioEmail);

        } catch (Exception e) {
            // Si falla el envío, solo registramos el error.
            // No lanzamos excepción para que los demás correos del lote sigan enviándose.
            System.err.println("⚠️ No se pudo enviar correo de cumpleaños a "
                    + destinatarioEmail + ": " + e.getMessage());
        }
    }
}