package com.sugarpop.loyalty.repository;

import com.sugarpop.loyalty.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // Busca todos los clientes cuyo mes de nacimiento coincide con el mes indicado.
    // @Query le dice a Spring que ejecute esta consulta personalizada.
    // "c" es un alias para la tabla de clientes (como en SQL: FROM clientes c).
    // :mes es un parámetro que pasaremos cuando llamemos a este método.
    @Query("SELECT c FROM Cliente c WHERE EXTRACT(MONTH FROM c.fechaNacimiento) = :mes")
    List<Cliente> findByMesDeNacimiento(@Param("mes") int mes);
}