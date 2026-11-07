package com.sugarpop.loyalty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // Sirve la PWA de clientes en la ruta principal /
    @GetMapping("/")
    public String paginaInicio() {
        return "forward:/index.html";
    }

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