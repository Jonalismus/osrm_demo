package com.example.osrm_demo.controller;

import com.example.osrm_demo.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Annotiert die Klasse als REST-Controller, was bedeutet, dass sie HTTP-Anfragen behandelt
@RestController
public class RoutingController {

    // Deklariert ein RoutingService-Objekt, das von Spring automatisch injiziert wird
    @Autowired
    private RoutingService routingService;

    // Mapped HTTP GET-Anfragen auf /route auf die getRoute-Methode.
    @GetMapping("/route")
    public String getRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false, defaultValue = "false") boolean includeGeometry) {


        // Ruft die calculateRoute-Methode des RoutingService auf und gibt das Ergebnis zurück.
        return routingService.calculateRoute(start, end, includeGeometry);
    }

}
