package com.sugarpop.loyalty.controller;

import com.sugarpop.loyalty.dto.TransaccionRegistroDTO;
import com.sugarpop.loyalty.dto.TransaccionResponseDTO;
import com.sugarpop.loyalty.service.TransaccionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sugarpop.loyalty.dto.CanjeRegistroDTO;
import com.sugarpop.loyalty.dto.CanjeResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    // Endpoint 1: Registrar una nueva compra/transacción
    @PostMapping
    public ResponseEntity<TransaccionResponseDTO> registrarTransaccion(@Valid @RequestBody TransaccionRegistroDTO dto) {
        TransaccionResponseDTO nuevaTransaccion = transaccionService.registrarTransaccion(dto);
        return new ResponseEntity<>(nuevaTransaccion, HttpStatus.CREATED);
    }

    // Endpoint 2: Obtener el historial completo de compras de un cliente
    @GetMapping("/cliente/{telefono}")
    public ResponseEntity<List<TransaccionResponseDTO>> obtenerHistorialCliente(@PathVariable String telefono) {
        return ResponseEntity.ok(transaccionService.obtenerHistorialCliente(telefono));
    }

    @PostMapping("/canjear")
    public ResponseEntity<CanjeResponseDTO> procesarCanje(@Valid @RequestBody CanjeRegistroDTO dto) {
        CanjeResponseDTO respuesta = transaccionService.procesarCanje(dto);
        return ResponseEntity.ok(respuesta);
    }


}