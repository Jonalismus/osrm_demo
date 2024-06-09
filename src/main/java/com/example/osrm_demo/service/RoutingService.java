package com.example.osrm_demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

// Annotiert die Klasse als Service, was bedeutet, dass es eine von Spring verwaltete Komponente ist
@Service
public class RoutingService {

    // Deklariert ein WebClient-Objekt für HTTP-Anfragen und ein ObjectMapper-Objekt für die JSON-Verarbeitung
    private final WebClient osrmClient;
    private final ObjectMapper objectMapper;

    // Konstruktor, der die OSRM-Server-URL aus der "application.properties" liest und einen WebClient mit dieser URL erstellt.
    public RoutingService(@Value("${osrm.server.url}") String osrmServerUrl) {
        // osrmServerUrl wird hier mit dem Wert "http://osrm-backend:5000" gefüllt
        this.osrmClient = WebClient.create(osrmServerUrl);
        this.objectMapper = new ObjectMapper();
    }

    // Methode zur Berechnung der Route basierend auf Start- und Endpunkten und ob die Geometrie mit angezeigt werden soll.
    public String calculateRoute(String start, String end, boolean includeGeometry) {
        // Erstellt die URL für die OSRM-Anfrage basierend auf den Start- und Endkoordinaten.
        String url = String.format("/route/v1/driving/%s;%s", start, end); // /route/v1/driving = der verwendete OSRM-Endpoint

        // Erstellt die HTTP GET-Anfrage und fügt die erforderlichen Parameter hinzu.
        Mono<String> response = osrmClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("overview", includeGeometry ? "full" : "false")
                        .queryParam("geometries", "geojson")
                        .build())
                .retrieve()
                .bodyToMono(String.class);

        try {
            // Blockiert den Mono-Stream und konvertiert die Antwort in einen JSON-Baum
            JsonNode jsonNode = objectMapper.readTree(response.block());
            // Extrahiert das erste Route-Objekt aus der JSON-Antwort
            JsonNode routes = jsonNode.get("routes").get(0);

            // Extrahiert Dauer und Entfernung aus der Route
            double durationInSeconds = routes.get("duration").asDouble();
            double distanceInMeters = routes.get("distance").asDouble();
            // Extrahiert die Geometrie, wenn includeGeometry auf true gesetzt ist
            String geometry = includeGeometry ? routes.get("geometry").toString() : null;

            // Berechnet Stunden und Minuten aus der Dauer in Sekunden
            long hours = (long) (durationInSeconds / 3600);
            long minutes = (long) ((durationInSeconds % 3600) / 60);
            // Konvertiert die Entfernung in Kilometer
            double distanceInKilometers = distanceInMeters / 1000;

            // Erstellt einen Ergebnis String mit Dauer und Entfernung
            String result = String.format("Die Fahrzeit beträgt %d Stunden und %d Minuten bei einer Distanz von %.1f Kilometern.", hours, minutes, distanceInKilometers);

            // Fügt die Geometrie zur Ergebniszeichenfolge hinzu, wenn includeGeometry auf true gesetzt ist.
            if (includeGeometry) {
                result += " Geometrie: " + geometry;
            }

            return result;

        } catch (WebClientResponseException e) {
            // Faengt Fehler bei der HTTP-Anfrage ab und gibt eine entsprechende Fehlermeldung zurück
            return "Fehler bei der Routenberechnung: Ungültige Koordinaten oder Route nicht erreichbar.";
        } catch (Exception e) {
            // Fangt allgemeine Fehler ab und gibt die Fehlermeldung zurück
            return "Fehler bei der Routenberechnung: " + e.getMessage();
        }
    }
}
