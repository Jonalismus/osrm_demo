package com.example.osrm_demo.controller;

import com.example.osrm_demo.service.RoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoutingController {

    @Autowired
    private RoutingService routingService;

    @GetMapping("/route")
    public String getRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false, defaultValue = "false") boolean includeGeometry) {

        return routingService.calculateRoute(start, end, includeGeometry);
    }

}
