package com.sugarpop.loyalty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // Redirige la URL /registro a la pantalla de registro del cliente
    @GetMapping("/registro")
    public String paginaRegistro() {
        return "registro"; // Busca un archivo llamado registro.html
    }

    // Redirige la URL /pos a la pantalla del punto de venta para caja
    @GetMapping("/pos")
    public String paginaPos() {
        return "pos"; // Busca un archivo llamado pos.html
    }
}