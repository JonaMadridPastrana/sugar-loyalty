package com.sugarpop.loyalty.controller;

import com.sugarpop.loyalty.dto.ClienteLoginDTO;
import com.sugarpop.loyalty.dto.ClienteRegistroDTO;
import com.sugarpop.loyalty.dto.ClienteResponseDTO;
import com.sugarpop.loyalty.dto.PuntosAjusteDTO;
import com.sugarpop.loyalty.service.ClienteService;
import com.sugarpop.loyalty.service.CumpleanosScheduler;
import com.sugarpop.loyalty.service.EmailService;
import com.sugarpop.loyalty.service.QrCodeService;
import com.sugarpop.loyalty.service.RecompensaService;
import com.sugarpop.loyalty.service.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final QrCodeService qrCodeService;
    private final EmailService emailService;
    private final CumpleanosScheduler cumpleanosScheduler;
    private final RecompensaService recompensaService;
    private final TransaccionService transaccionService;

    public ClienteController(
            ClienteService clienteService,
            QrCodeService qrCodeService,
            EmailService emailService,
            CumpleanosScheduler cumpleanosScheduler,
            RecompensaService recompensaService,
            TransaccionService transaccionService) {
        this.clienteService = clienteService;
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
        this.cumpleanosScheduler = cumpleanosScheduler;
        this.recompensaService = recompensaService;
        this.transaccionService = transaccionService;
    }

    // 1. Registrar cliente y entregar su código QR
    @PostMapping
    public ResponseEntity<?> registrarCliente(@Valid @RequestBody ClienteRegistroDTO dto) {

        // Guardamos el cliente en la base de datos
        ClienteResponseDTO nuevoCliente = clienteService.registrarCliente(dto);

        // Enviar el mismo código QR a todos los clientes, independientemente del dispositivo
        try {
            byte[] qrBytes = qrCodeService.generarQrCliente(dto.getTelefono(), 300, 300);
            emailService.enviarTarjetaBienvenida(
                    dto.getEmail(),
                    dto.getNombre(),
                    qrBytes
            );
        } catch (Exception e) {
            System.err.println("Aviso al intentar enviar correo al cliente: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "mensaje", "¡Cliente registrado con éxito!",
                "cliente", nuevoCliente
        ));
    }

    // 1.B Iniciar sesión segura del cliente desde la PWA (valida teléfono + correo en el servidor)
    @PostMapping("/login-cliente")
    public ResponseEntity<?> loginCliente(@Valid @RequestBody ClienteLoginDTO dto) {
        ClienteResponseDTO cliente = clienteService.autenticarCliente(dto.getTelefono(), dto.getEmail());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "cliente", cliente,
                "recompensas", recompensaService.obtenerTodasLasRecompensas(),
                "transacciones", transaccionService.obtenerHistorialCliente(cliente.getTelefono())
        ));
    }

    // 2. Obtener todos los clientes
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(clienteService.obtenerTodosLosClientes());
    }

    // 3. Buscar por teléfono (Protegido para el cajero POS)
    @GetMapping("/{telefono}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorTelefono(@PathVariable String telefono) {
        return ResponseEntity.ok(clienteService.obtenerPorTelefono(telefono));
    }

    // 4. Eliminar por teléfono
    @DeleteMapping("/{telefono}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable String telefono) {
        clienteService.eliminarPorTelefono(telefono);
        return ResponseEntity.noContent().build();
    }

    // 5. Reiniciar puntos de cliente
    @PatchMapping("/{telefono}/puntos")
    public ResponseEntity<ClienteResponseDTO> reiniciarPuntos(
            @PathVariable String telefono,
            @Valid @RequestBody PuntosAjusteDTO dto) {
        ClienteResponseDTO respuesta = clienteService.reiniciarPuntosCliente(telefono, dto.getPuntos());
        return ResponseEntity.ok(respuesta);
    }

    // 6. Generar e identificar código QR del cliente
    @GetMapping(value = "/{telefono}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> obtenerQrCliente(@PathVariable String telefono) {
        byte[] qrBytes = qrCodeService.generarQrCliente(telefono, 300, 300);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-" + telefono + ".png\"")
                .body(qrBytes);
    }

    @GetMapping(value = "/{telefono}/qr/descargar", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> descargarQrCliente(@PathVariable String telefono) {
        byte[] qrBytes = qrCodeService.generarQrCliente(telefono, 300, 300);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"qr-" + telefono + ".png\"")
                .body(qrBytes);
    }

    // 7. Buscar por correo electrónico
    @GetMapping("/buscar-por-email")
    public ResponseEntity<ClienteResponseDTO> obtenerPorEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(clienteService.obtenerPorEmail(email));
    }

    // 8. Obtener lista de clientes que cumplen años en el mes actual
    @GetMapping("/cumpleaneros")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerCumpleaneros() {
        List<ClienteResponseDTO> cumpleaneros = clienteService.obtenerCumpleanerosMesActual();
        return ResponseEntity.ok(cumpleaneros);
    }

    // 9. Disparar manualmente el envío de correos de cumpleaños del mes
    @PostMapping("/cumpleaneros/enviar-correos")
    public ResponseEntity<Map<String, String>> enviarCorreosCumpleanosManual() {
        cumpleanosScheduler.ejecutarEnvioCumpleanos();
        return ResponseEntity.ok(Map.of(
                "mensaje", "✅ Correos de cumpleaños del mes enviados correctamente."
        ));
    }
}
