package com.sugarpop.loyalty.repository;

import com.sugarpop.loyalty.model.Recompensa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RecompensaRepository extends JpaRepository<Recompensa, Long> {

    List<Recompensa> findByActivaTrueOrderByPuntosRequeridosAsc();
     Optional<Recompensa> findByIdAndActivaTrue(Long id);
}