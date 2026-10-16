package com.sugarpop.loyalty.service;

import com.sugarpop.loyalty.dto.ClienteRegistroDTO;
import com.sugarpop.loyalty.dto.ClienteResponseDTO;
import com.sugarpop.loyalty.model.Cliente;
import com.sugarpop.loyalty.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // 1. REGISTRAR CLIENTE USANDO DTO
    public ClienteResponseDTO registrarCliente(ClienteRegistroDTO dto) {
        if (clienteRepository.existsByTelefono(dto.getTelefono())) {
            throw new IllegalArgumentException("Ya existe un cliente registrado con el teléfono: " + dto.getTelefono());
        }

        Cliente cliente = new Cliente();
        String nombreCompleto = dto.getNombre().trim() + " " + dto.getApellido().trim();
        cliente.setNombre(nombreCompleto);
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setFechaNacimiento(dto.getFechaNacimiento());
        cliente.setSistemaOperativo("QR");
        cliente.setPuntosActuales(0); // Puntos iniciales por defecto

        Cliente clienteGuardado = clienteRepository.save(cliente);
        return mapearAResponseDTO(clienteGuardado);
    }

    // 2. OBTENER TODOS LOS CLIENTES
    public List<ClienteResponseDTO> obtenerTodosLosClientes() {
        return clienteRepository.findAll()
                .stream()
                .map(this::mapearAResponseDTO)
                .toList();
    }

    // 3. BUSCAR POR TELÉFONO
    public ClienteResponseDTO obtenerPorTelefono(String telefono) {
        Cliente cliente = clienteRepository.findByTelefono(telefono)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cliente con el teléfono: " + telefono));
        return mapearAResponseDTO(cliente);
    }

    // 4. ACUMULAR PUNTOS
    public ClienteResponseDTO acumularPuntos(String telefono, Integer puntosASumar) {
        Cliente cliente = clienteRepository.findByTelefono(telefono)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cliente con el teléfono: " + telefono));

        cliente.setPuntosActuales(cliente.getPuntosActuales() + puntosASumar);
        Cliente clienteActualizado = clienteRepository.save(cliente);
        return mapearAResponseDTO(clienteActualizado);
    }

    // ELIMINAR CLIENTE POR TELÉFONO
    @Transactional
    public void eliminarPorTelefono(String telefono) {
        Cliente cliente = clienteRepository.findByTelefono(telefono)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cliente con el teléfono: " + telefono));
        clienteRepository.delete(cliente);
    }

    @Transactional
    public ClienteResponseDTO reiniciarPuntosCliente(String telefono, Integer nuevosPuntos) {
        // 1. Buscar al cliente por su teléfono
        Cliente cliente = clienteRepository.findByTelefono(telefono)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cliente con el teléfono: " + telefono));

        // 2. Asignar el nuevo saldo de puntos (ej. 0)
        cliente.setPuntosActuales(nuevosPuntos);

        // 3. Guardar el cambio en la base de datos
        Cliente clienteActualizado = clienteRepository.save(cliente);

        // 4. Retornar el DTO mapeado
        return mapearAResponseDTO(clienteActualizado);
    }

    // Metodo auxiliar de mapeo (Entity -> DTO)
    private ClienteResponseDTO mapearAResponseDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setFechaNacimiento(cliente.getFechaNacimiento());
        dto.setPuntosActuales(cliente.getPuntosActuales());
        dto.setFechaRegistro(cliente.getFechaRegistro());
        return dto;
    }

    public ClienteResponseDTO obtenerPorEmail(String email) {
        Cliente cliente = clienteRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ningún cliente con el correo: " + email));
                
        return mapearAResponseDTO(cliente);
    }
}