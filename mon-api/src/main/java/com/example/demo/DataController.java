package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200") // Indispensable pour Angular !
public class DataController {

    @GetMapping("/message")
    public Map<String, String> getMessage() {
        return Map.of("content", "Salut Karlie ! Ton Backend Java fonctionne !");
    }
}