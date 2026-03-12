package com.bank.consumer.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.bank.consumer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiService {

    private static final String POST_URL =
            "https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public boolean guardarTransaccion(Transaccion transaccion) throws Exception {

        String json = objectMapper.writeValueAsString(transaccion);
        
        System.out.println("JSON enviado al POST: " + json);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POST_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Código HTTP del POST: " + response.statusCode());
        System.out.println("Respuesta del servidor: " + response.body());

        return response.statusCode() == 200 || response.statusCode() == 201;    }
}