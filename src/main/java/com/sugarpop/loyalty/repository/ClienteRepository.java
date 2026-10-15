package com.sugarpop.loyalty.repository;

import com.sugarpop.loyalty.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;


public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Spring Data JPA lee este nombre de metodo y genera la consulta automáticamente:
    // SELECT * FROM clientes WHERE telefono = ?
    Optional<Cliente> findByTelefono(String telefono);

    // Spring Data JPA lee este nombre de metodo y genera la consulta automáticamente:
    Optional<Cliente> findByEmailIgnoreCase(String email);

    // Metodo para verificar si ya existe un cliente registrado con ese número de teléfono
    boolean existsByTelefono(String telefono);
}