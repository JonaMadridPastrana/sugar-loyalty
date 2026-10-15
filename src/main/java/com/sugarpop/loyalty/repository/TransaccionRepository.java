package com.sugarpop.loyalty.repository;

import com.sugarpop.loyalty.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByClienteTelefonoOrderByFechaTransaccionDesc(String telefono);
}