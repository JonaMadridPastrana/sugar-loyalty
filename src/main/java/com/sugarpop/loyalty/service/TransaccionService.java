package com.sugarpop.loyalty.service;

import com.sugarpop.loyalty.dto.TransaccionRegistroDTO;
import com.sugarpop.loyalty.dto.TransaccionResponseDTO;
import com.sugarpop.loyalty.model.Cliente;
import com.sugarpop.loyalty.model.Transaccion;
import com.sugarpop.loyalty.repository.ClienteRepository;
import com.sugarpop.loyalty.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sugarpop.loyalty.dto.CanjeRegistroDTO;
import com.sugarpop.loyalty.dto.CanjeResponseDTO;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final ClienteRepository clienteRepository;

    public TransaccionService(TransaccionRepository transaccionRepository, ClienteRepository clienteRepository) {
        this.transaccionRepository = transaccionRepository;
        this.clienteRepository = clienteRepository;
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

        // 5. Retornar el DTO de respuesta
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

        // PASO A: Buscamos al cliente en PostgreSQL por su teléfono
        Cliente cliente = clienteRepository.findByTelefono(dto.getTelefonoCliente())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cliente con el teléfono: " + dto.getTelefonoCliente()));

        // PASO B: Verificamos si tiene puntos suficientes para la recompensa
        if (cliente.getPuntosActuales() < dto.getPuntosACanjear()) {
            throw new IllegalArgumentException("Puntos insuficientes. El cliente tiene "
                    + cliente.getPuntosActuales() + " puntos, pero la recompensa requiere " + dto.getPuntosACanjear() + " puntos.");
        }

        // PASO C: Restamos los puntos al cliente y guardamos su nuevo saldo
        cliente.setPuntosActuales(cliente.getPuntosActuales() - dto.getPuntosACanjear());
        clienteRepository.save(cliente);

        // PASO D: Creamos el historial del canje en la base de datos
        Transaccion historial = new Transaccion();
        historial.setCliente(cliente); // Ligamos este recibo a este cliente en particular
        historial.setPuntosObtenidos(-dto.getPuntosACanjear()); // Guardamos los puntos en negativo (ej. -10) para reflejar la salida
        historial.setMontoCompra(BigDecimal.ZERO); // En un canje no hay dinero gastado
        historial.setDescripcion("canje: " + dto.getNombreRecompensa());

        // Guardamos el recibo de la transacción en PostgreSQL
        transaccionRepository.save(historial);

        // PASO E: Armamos la respuesta con los datos actualizados para responderle a Postman
        CanjeResponseDTO respuesta = new CanjeResponseDTO();
        respuesta.setTelefonoCliente(cliente.getTelefono());
        respuesta.setNombreCliente(cliente.getNombre());
        respuesta.setNombreRecompensa(dto.getNombreRecompensa());
        respuesta.setPuntosCanjeados(dto.getPuntosACanjear());
        respuesta.setPuntosRestantes(cliente.getPuntosActuales());
        respuesta.setMensaje("¡Canje realizado con éxito!");

        return respuesta;
    }
    }