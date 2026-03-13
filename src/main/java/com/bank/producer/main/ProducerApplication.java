package com.bank.producer.main;

import com.bank.producer.model.LoteTransacciones;
import com.bank.producer.service.ApiService;
import com.bank.producer.service.ProducerService;

public class ProducerApplication {

    public static void main(String[] args) {
        try {
            ApiService apiService = new ApiService();
            ProducerService producerService = new ProducerService();

            LoteTransacciones lote = apiService.obtenerLoteTransacciones();
            producerService.enviarLote(lote);

        } catch (Exception e) {
            System.out.println("Error general en ProducerApplication: " + e.getMessage());
            e.printStackTrace();
        }
    }
}