package com.sugarpop.loyalty.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sugarpop.loyalty.service.RecompensaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;

import com.sugarpop.loyalty.dto.RecompensaRequestDTO;
import com.sugarpop.loyalty.model.Recompensa;

@RestController
@RequestMapping("/api/recompensas")
public class RecompensaController {

    private final RecompensaService recompensaService;


    public RecompensaController(RecompensaService recompensaService) {
        this.recompensaService = recompensaService;
    }
// 1. Obtener todas las recompensas
    @GetMapping
    public ResponseEntity<List<Recompensa>> obtenerTodasLasRecompensas() {
        List<Recompensa> recompensas = recompensaService.obtenerTodasLasRecompensas();
        return ResponseEntity.ok(recompensas);
    }

// 2. Crear nueva recompensa
    @PostMapping
    public ResponseEntity<Recompensa> crearRecompensa(@Valid @RequestBody RecompensaRequestDTO dto) {
        Recompensa nuevaRecompensa = recompensaService.crearRecompensa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaRecompensa);
    }

// 3. Actualizar recompensa existente
    @PutMapping("/{id}")
    public ResponseEntity<Recompensa> actualizarRecompensa(@PathVariable Long id, @Valid @RequestBody RecompensaRequestDTO dto) {
        Recompensa recompensaActualizada = recompensaService.actualizarRecompensa(id, dto);
        return ResponseEntity.ok(recompensaActualizada);
    }

// 4. Desactivar recompensa
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarRecompensa(@PathVariable Long id) {
        recompensaService.desactivarRecompensa(id);
        return ResponseEntity.noContent().build();
    }
}