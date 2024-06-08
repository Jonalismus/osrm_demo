package com.example.osrm_demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
public class RoutingService {


    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public RoutingService() {
        this.webClient = WebClient.create("http://localhost:5000");
        this.objectMapper = new ObjectMapper();
    }

    public String calculateRoute(String start, String end, boolean includeGeometry) {
        String url = String.format("/route/v1/driving/%s;%s", start, end);

        Mono<String> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("overview", includeGeometry ? "full" : "false")
                        .queryParam("geometries", "geojson")
                        .build())
                .retrieve()
                .bodyToMono(String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response.block());
            JsonNode routes = jsonNode.get("routes").get(0);

            double durationInSeconds = routes.get("duration").asDouble();
            double distanceInMeters = routes.get("distance").asDouble();
            String geometry = includeGeometry ? routes.get("geometry").toString() : null;

            long hours = (long) (durationInSeconds / 3600);
            long minutes = (long) ((durationInSeconds % 3600) / 60);
            double distanceInKilometers = distanceInMeters / 1000;

            String result = String.format("Die Fahrzeit beträgt %d Stunden und %d Minuten bei einer Distanz von %.1f Kilometern.", hours, minutes, distanceInKilometers);

            if (includeGeometry) {
                result += " Geometrie: " + geometry;
            }

            return result;

        } catch (WebClientResponseException e) {
            return "Fehler bei der Routenberechnung: Ungültige Koordinaten oder Route nicht erreichbar.";
        } catch (Exception e) {
            return "Fehler bei der Routenberechnung: " + e.getMessage();
        }
    }

}
