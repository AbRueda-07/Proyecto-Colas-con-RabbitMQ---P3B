package com.bank.producer.service;

import java.util.HashSet;
import java.util.Set;


import com.bank.producer.config.RabbitMQConfig;
import com.bank.producer.model.Transaccion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.bank.producer.model.LoteTransacciones;

public class ProducerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<String> facturasVistas = new HashSet<>();
    private final Set<String> idsTransaccionVistos = new HashSet<>();
    
    private boolean publicarConCanal(Channel channel, Transaccion transaccion) throws Exception {

        if (transaccion == null) {
            System.out.println("La transacción es nula. No se enviará.");
            return false;
        }

        if (transaccion.getBancoDestino() == null || transaccion.getBancoDestino().isBlank()) {
            System.out.println("La transacción no tiene bancoDestino válido. No se enviará.");
            return false;
        }

        if (transaccion.getDetalle() == null ||
            transaccion.getDetalle().getReferencias() == null ||
            transaccion.getDetalle().getReferencias().getFactura() == null ||
            transaccion.getDetalle().getReferencias().getFactura().isBlank()) {
            System.out.println("La transacción no tiene factura válida. No se enviará.");
            return false;
        }

        if (transaccion.getIdTransaccion() == null || transaccion.getIdTransaccion().isBlank()) {
            System.out.println("La transacción no tiene idTransaccion válido. No se enviará.");
            return false;
        }

        if (!idsTransaccionVistos.add(transaccion.getIdTransaccion())) {
            System.out.println("idTransaccion duplicado detectado. No se enviará: " + transaccion.getIdTransaccion());
            return false;
        }
        
        String factura = transaccion.getDetalle().getReferencias().getFactura();

        if (!facturasVistas.add(factura)) {
            System.out.println("Factura duplicada detectada. No se enviará: " + factura);
            return false;
        }

        String nombreCola = transaccion.getBancoDestino().trim().toUpperCase();

        channel.queueDeclare(nombreCola, true, false, false, null);

        String json = objectMapper.writeValueAsString(transaccion);

        channel.basicPublish(
                "",
                nombreCola,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                json.getBytes()
        );

        System.out.println("ID transacción: " + transaccion.getIdTransaccion());
        System.out.println("Banco destino: " + nombreCola);
        System.out.println("Transacción enviada a la cola: " + nombreCola);
        System.out.println("Mensaje: " + json);

        return true;
    }

    public boolean enviarTransaccion(Transaccion transaccion) {
        try (Connection connection = RabbitMQConfig.createConnection();
             Channel channel = connection.createChannel()) {

            return publicarConCanal(channel, transaccion);

        } catch (Exception e) {
            System.out.println("Error al enviar transacción a RabbitMQ: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public void enviarLote(LoteTransacciones lote) {

        if (lote == null || lote.getTransacciones() == null || lote.getTransacciones().isEmpty()) {
            System.out.println("El lote está vacío o es nulo. No hay transacciones para enviar.");
            return;
        }

        int enviadas = 0;
        int omitidasOFallidas = 0;

        System.out.println("Procesando lote: " + lote.getLoteId());
        System.out.println("Cantidad de transacciones recibidas: " + lote.getTransacciones().size());

        try (Connection connection = RabbitMQConfig.createConnection();
             Channel channel = connection.createChannel()) {

            for (Transaccion transaccion : lote.getTransacciones()) {
                boolean enviada = publicarConCanal(channel, transaccion);

                if (enviada) {
                    enviadas++;
                } else {
                    omitidasOFallidas++;
                }
            }

        } catch (Exception e) {
            System.out.println("Error al procesar el lote en RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Resumen del lote:");
        System.out.println("Transacciones enviadas correctamente: " + enviadas);
        System.out.println("Transacciones omitidas o fallidas: " + omitidasOFallidas);
        System.out.println("Lote procesado correctamente.");
    }
}