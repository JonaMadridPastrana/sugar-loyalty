package com.sugarpop.loyalty.service;

import com.sugarpop.loyalty.dto.ClienteResponseDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

// @Component le dice a Spring que gestione esta clase automáticamente.
// Es similar a @Service pero se usa para componentes de infraestructura
// (tareas, listeners, etc.) que no contienen lógica de negocio directamente.
@Component
public class CumpleanosScheduler {

    // Inyectamos los dos servicios que necesitamos:
    // ClienteService para obtener la lista de cumpleañeros
    // EmailService para enviar los correos
    private final ClienteService clienteService;
    private final EmailService emailService;

    // Constructor: Spring inyecta automáticamente las dependencias
    public CumpleanosScheduler(ClienteService clienteService, EmailService emailService) {
        this.clienteService = clienteService;
        this.emailService = emailService;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAREA AUTOMÁTICA: se ejecuta el día 1 de cada mes a las 8:00 AM
    // ─────────────────────────────────────────────────────────────────────────
    // @Scheduled(cron = "...") programa la ejecución automática.
    // La expresión "0 0 8 1 * *" significa:
    //   segundo=0, minuto=0, hora=8, día del mes=1, cualquier mes, cualquier día semana
    // Resultado: se dispara el 1 de Enero, 1 de Febrero, 1 de Marzo... a las 8AM
    @Scheduled(cron = "0 0 8 1 * *")
    public void enviarCorreosCumpleanosMensual() {
        System.out.println("⏰ [Scheduler] Iniciando envío de correos de cumpleaños del mes...");
        // Llamamos al método compartido que hace el trabajo real
        ejecutarEnvioCumpleanos();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MÉTODO COMPARTIDO: contiene la lógica real del envío
    // ─────────────────────────────────────────────────────────────────────────
    // Lo separamos en su propio método para poder llamarlo desde dos lugares:
    //   1. Desde la tarea automática (@Scheduled)
    //   2. Desde el endpoint de disparo manual (en el Controller)
    // Así no repetimos código (principio DRY: Don't Repeat Yourself)
    public void ejecutarEnvioCumpleanos() {

        // Obtenemos la fecha de hoy para saber el mes y nombre del mes
        LocalDate hoy = LocalDate.now();

        // getMonthValue() → número del mes (Octubre = 10)
        int mes = hoy.getMonthValue();

        // TextStyle.FULL_STANDALONE con Locale("es") → nombre en español con mayúscula
        // Ejemplo: "Octubre", "Noviembre", "Diciembre"
        String nombreMes = hoy.getMonth()
                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.of("es"));

        // Ponemos primera letra en mayúscula por seguridad
        // (en algunos sistemas puede venir en minúscula)
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);

        // Le pedimos al ClienteService la lista de cumpleañeros del mes actual
        List<ClienteResponseDTO> cumpleaneros = clienteService.obtenerCumpleanerosMesActual();

        System.out.println("🎂 Se encontraron " + cumpleaneros.size()
                + " cumpleañeros para el mes de " + nombreMes);

        // Recorremos la lista y enviamos un correo a cada uno
        for (ClienteResponseDTO cliente : cumpleaneros) {

            // Solo enviamos si el cliente tiene correo registrado
            // (algunos podrían no tenerlo)
            if (cliente.getEmail() != null && !cliente.getEmail().isBlank()) {

                // Obtenemos el DÍA del cumpleaños desde la fecha de nacimiento
                // Ejemplo: si nació el 1995-10-24, getDayOfMonth() = 24
                int diaCumpleanos = cliente.getFechaNacimiento().getDayOfMonth();

                // Llamamos al EmailService para enviar el correo festivo
                emailService.enviarCorreoCumpleanos(
                        cliente.getEmail(),    // destinatario
                        cliente.getNombre(),   // nombre para personalizar el saludo
                        diaCumpleanos,         // día (ej: 24)
                        nombreMes              // nombre del mes (ej: "Octubre")
                );
            } else {
                // Si no tiene email, solo lo registramos en la consola
                System.out.println("⚠️ El cliente " + cliente.getNombre()
                        + " no tiene correo registrado, se omite.");
            }
        }

        System.out.println("✅ [Scheduler] Proceso de correos de cumpleaños completado.");
    }
}