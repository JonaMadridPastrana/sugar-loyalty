package com.sugarpop.loyalty.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import com.sugarpop.loyalty.repository.RecompensaRepository;
import com.sugarpop.loyalty.dto.RecompensaRequestDTO;
import com.sugarpop.loyalty.model.Recompensa;
import java.util.List;

@Service 
public class RecompensaService {
    
    private final RecompensaRepository recompensaRepository;

    public RecompensaService(RecompensaRepository recompensaRepository) {
        this.recompensaRepository = recompensaRepository;
    }

    // 1. OBTENER TODAS LAS RECOMPENSAS
    public List<Recompensa> obtenerTodasLasRecompensas() {
        return recompensaRepository.findByActivaTrueOrderByPuntosRequeridosAsc();
    }

    // 2. CREAR NUEVA RECOMPENSA
    public Recompensa crearRecompensa(RecompensaRequestDTO dto) {
        Recompensa recompensa = new Recompensa();
        recompensa.setNombre(dto.getNombre());
        recompensa.setPuntosRequeridos(dto.getPuntosRequeridos());

        return recompensaRepository.save(recompensa);
    }

    // 3. ACTUALIZAR RECOMPENSA EXISTENTE
    public Recompensa actualizarRecompensa(Long id, RecompensaRequestDTO dto) {
        Recompensa recompensaExistente = recompensaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada con id: " + id));

        recompensaExistente.setNombre(dto.getNombre());
        recompensaExistente.setPuntosRequeridos(dto.getPuntosRequeridos());

        return recompensaRepository.save(recompensaExistente);
    }

    // 4. DESACTIVAR RECOMPENSA
    public void desactivarRecompensa(Long id) {
        Recompensa recompensaExistente = recompensaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada con id: " + id));

        recompensaExistente.setActiva(false);
        recompensaRepository.save(recompensaExistente);
    }
}