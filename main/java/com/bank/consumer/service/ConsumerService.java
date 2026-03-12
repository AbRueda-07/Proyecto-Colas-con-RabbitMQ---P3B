package com.bank.consumer.service;

import java.nio.charset.StandardCharsets;

import com.bank.consumer.config.RabbitMQConfig;
import com.bank.consumer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

public class ConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiService apiService = new ApiService();

    private boolean guardarConUnReintento(Transaccion transaccion) {
        try {
            boolean guardado = apiService.guardarTransaccion(transaccion);

            if (guardado) {
                return true;
            }

            System.out.println("Primer intento fallido. Reintentando una vez...");

            return apiService.guardarTransaccion(transaccion);

        } catch (Exception e) {
            System.out.println("Error en primer intento de POST. Reintentando una vez... " + e.getMessage());

            try {
                return apiService.guardarTransaccion(transaccion);
            } catch (Exception ex) {
                System.out.println("El reintento también falló: " + ex.getMessage());
                return false;
            }
        }
    }
    
    public void consumirCola(String nombreCola) {
        try {
            Connection connection = RabbitMQConfig.createConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(nombreCola, true, false, false, null);
            channel.basicQos(1);

            System.out.println("Escuchando cola: " + nombreCola);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String json = new String(delivery.getBody(), StandardCharsets.UTF_8);

                try {
                    System.out.println("Mensaje recibido desde cola " + nombreCola + ": " + json);

                    Transaccion transaccion = objectMapper.readValue(json, Transaccion.class);

                    transaccion.setNombre("Aranza Rueda");
                    transaccion.setCarnet("0905-24-7854");
                    transaccion.setCorreo("aruedaa@miumg.edu.gt");
                    
                    boolean guardado = guardarConUnReintento(transaccion);
                    if (guardado) {
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println("Mensaje confirmado (ACK) para la cola " + nombreCola + ".");
                    } else {
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                        System.out.println("POST no exitoso. Mensaje reenviado a la cola " + nombreCola + ".");
                    }

                } catch (Exception e) {
                    System.out.println("Error procesando mensaje de la cola " + nombreCola + ": " + e.getMessage());
                    e.printStackTrace();
                }
            };

            channel.basicConsume(nombreCola, false, deliverCallback, consumerTag -> { });

        } catch (Exception e) {
            System.out.println("Error al consumir la cola " + nombreCola + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void consumirColas(String[] nombresColas) {
        if (nombresColas == null || nombresColas.length == 0) {
            System.out.println("No se proporcionaron colas para consumir.");
            return;
        }

        for (String nombreCola : nombresColas) {
            consumirCola(nombreCola);
        }
    }
}