package com.sugarpop.loyalty.service;

import com.sugarpop.loyalty.dto.TransaccionRegistroDTO;
import com.sugarpop.loyalty.dto.TransaccionResponseDTO;
import com.sugarpop.loyalty.dto.CanjeRegistroDTO;
import com.sugarpop.loyalty.dto.CanjeResponseDTO;
import com.sugarpop.loyalty.model.Cliente;
import com.sugarpop.loyalty.model.Recompensa;
import com.sugarpop.loyalty.model.Transaccion;
import com.sugarpop.loyalty.repository.ClienteRepository;
import com.sugarpop.loyalty.repository.RecompensaRepository;
import com.sugarpop.loyalty.repository.TransaccionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final ClienteRepository clienteRepository;
    private final RecompensaRepository recompensaRepository;
    private final EmailService emailService;

    public TransaccionService(
            TransaccionRepository transaccionRepository,
            ClienteRepository clienteRepository,
            RecompensaRepository recompensaRepository,
            EmailService emailService) {
        this.transaccionRepository = transaccionRepository;
        this.clienteRepository = clienteRepository;
        this.recompensaRepository = recompensaRepository;
        this.emailService = emailService;
    }

    @Transactional
    public TransaccionResponseDTO registrarTransaccion(TransaccionRegistroDTO dto) {
        // 1. Buscar al cliente por su teléfono
        Cliente cliente = clienteRepository.findByTelefono(dto.getTelefonoCliente())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cliente con el teléfono: " + dto.getTelefonoCliente()));

        // 2. Regla de negocio: 1 punto por cada $10 consumidos
        int puntosAbonados = dto.getMontoCompra().divide(new BigDecimal("10")).intValue();

        // 3. Crear y guardar la entidad Transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setMontoCompra(dto.getMontoCompra());
        transaccion.setPuntosObtenidos(puntosAbonados);
        transaccion.setCliente(cliente);

        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        // 4. Actualizar el saldo de puntos del cliente
        cliente.setPuntosActuales(cliente.getPuntosActuales() + puntosAbonados);
        clienteRepository.save(cliente);

        // 5. Enviar notificación por correo con el resumen de la compra y catálogo de recompensas
        try {
            if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
                List<Recompensa> recompensasActivas = recompensaRepository.findByActivaTrueOrderByPuntosRequeridosAsc();
                emailService.enviarNotificacionCompra(
                        cliente.getEmail(),
                        cliente.getNombre(),
                        dto.getMontoCompra(),
                        puntosAbonados,
                        cliente.getPuntosActuales(),
                        recompensasActivas
                );
            }
        } catch (Exception e) {
            System.err.println("Aviso al intentar enviar notificación de compra por correo: " + e.getMessage());
        }

        // 6. Retornar el DTO de respuesta
        return mapearAResponseDTO(transaccionGuardada);
    }

    public List<TransaccionResponseDTO> obtenerHistorialCliente(String telefono) {
        return transaccionRepository.findByClienteTelefonoOrderByFechaTransaccionDesc(telefono)
                .stream()
                .map(this::mapearAResponseDTO)
                .toList();
    }

    private TransaccionResponseDTO mapearAResponseDTO(Transaccion t) {
        TransaccionResponseDTO dto = new TransaccionResponseDTO();
        dto.setId(t.getId());
        dto.setMontoCompra(t.getMontoCompra());
        dto.setPuntosObtenidos(t.getPuntosObtenidos());
        dto.setFechaTransaccion(t.getFechaTransaccion());
        dto.setTelefonoCliente(t.getCliente().getTelefono());
        dto.setNombreCliente(t.getCliente().getNombre());
        dto.setPuntosTotalesCliente(t.getCliente().getPuntosActuales());
        return dto;
    }
    @Transactional
    public CanjeResponseDTO procesarCanje(CanjeRegistroDTO dto) {
        Cliente cliente = clienteRepository.findByTelefono(dto.getTelefonoCliente())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró ningún cliente con el teléfono: " + dto.getTelefonoCliente()));

        Recompensa recompensa = recompensaRepository.findByIdAndActivaTrue(dto.getRecompensaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "La recompensa no existe o está desactivada."));

        int puntosRequeridos = recompensa.getPuntosRequeridos();

        if (cliente.getPuntosActuales() < puntosRequeridos) {
            throw new IllegalArgumentException("Puntos insuficientes. El cliente tiene "
                    + cliente.getPuntosActuales() + " puntos, pero la recompensa requiere "
                    + puntosRequeridos + " puntos.");
        }

        cliente.setPuntosActuales(cliente.getPuntosActuales() - puntosRequeridos);
        clienteRepository.save(cliente);

        Transaccion historial = new Transaccion();
        historial.setCliente(cliente);
        historial.setPuntosObtenidos(-puntosRequeridos);
        historial.setMontoCompra(BigDecimal.ZERO);
        historial.setDescripcion("canje: " + recompensa.getNombre());
        transaccionRepository.save(historial);

        CanjeResponseDTO respuesta = new CanjeResponseDTO();
        respuesta.setTelefonoCliente(cliente.getTelefono());
        respuesta.setNombreCliente(cliente.getNombre());
        respuesta.setNombreRecompensa(recompensa.getNombre());
        respuesta.setPuntosCanjeados(puntosRequeridos);
        respuesta.setPuntosRestantes(cliente.getPuntosActuales());
        respuesta.setMensaje("¡Canje realizado con éxito!");

        return respuesta;
    }
}