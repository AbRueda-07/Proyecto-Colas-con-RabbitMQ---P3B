package com.bank.consumer.main;

import com.bank.consumer.service.ConsumerService;

public class ConsumerApplication {

    public static void main(String[] args) {
        try {
            ConsumerService consumerService = new ConsumerService();

            String[] colas = {"BAC", "BANRURAL", "BI", "GYT"};
            consumerService.consumirColas(colas);

            System.out.println("ConsumerApplication iniciada correctamente.");

        } catch (Exception e) {
            System.out.println("Error general en ConsumerApplication: " + e.getMessage());
            e.printStackTrace();
        }
    }
}