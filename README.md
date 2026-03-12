# Proyecto: Procesamiento de Transacciones Bancarias con RabbitMQ y Java

## Descripción

Este proyecto implementa un sistema distribuido basado en el patrón **Producer–Consumer** utilizando **RabbitMQ** como sistema de mensajería.
El sistema procesa transacciones bancarias obtenidas desde una API externa y las distribuye automáticamente a diferentes colas según el banco destino. Posteriormente, otro componente consume estas colas y guarda las transacciones en una base de datos mediante una API REST.
---
## Arquitectura
API (GET /transacciones)
          │
          ▼
Producer (Java + Maven)
          │
          ▼
RabbitMQ
(Colas dinámicas por banco)
          │
          ▼
Consumer (Java + Maven)
          │
          ▼
API (POST /guardarTransacciones)
          │
          ▼
      DynamoDB


---

## Tecnologías utilizadas

- Java 17
- Maven
- RabbitMQ
- Docker
- Jackson (JSON)
- Java HttpClient

---

## Componentes del sistema

### Producer

Responsabilidades:

- Consumir el endpoint GET `/transacciones`
- Parsear el JSON recibido
- Recorrer el lote de transacciones
- Leer el campo `bancoDestino`
- Crear colas dinámicamente en RabbitMQ
- Publicar cada transacción como mensaje JSON

Características implementadas:

- Validación de `idTransaccion` único
- Validación de `factura` única
- Manejo de errores
- Logs informativos
- Mensajes persistentes en RabbitMQ

---

### Consumer

Responsabilidades:

- Escuchar múltiples colas de RabbitMQ
- Deserializar mensajes JSON
- Enviar cada transacción al endpoint POST `/guardarTransacciones`
- Confirmar mensaje con ACK manual solo si el POST fue exitoso

Características implementadas:

- Consumo de múltiples colas
- Reintento automático de POST
- Manejo de errores
- ACK manual

---

## Flujo de procesamiento

1. Producer consume la API GET `/transacciones`.
2. El JSON recibido es convertido a objetos Java.
3. Cada transacción se envía a RabbitMQ usando una cola cuyo nombre corresponde al `bancoDestino`.
4. El Consumer escucha las colas de los bancos.
5. Cada mensaje recibido se envía al endpoint POST `/guardarTransacciones`.
6. Si el POST responde correctamente, se confirma el mensaje con ACK.

---

## Ejecución del sistema

### 1. Iniciar RabbitMQ con Docker

docker run -d --name rabbitmq-server -p 5672:5672 -p 15672:15672 rabbitmq:3-management

##Panel de Administracion:
http://localhost:15672
usuario: guest
password: guest

## Ejecutar el Consumer
Ejecutar clase:
ConsumerApplication

El sistema comenzara a escuchar las colas:
BAC
BANRURAL
BI
GYT

## Ejecutar Producer
Ejecutar la clase:
ProducerApplication

El sistema:

-Obtendrá transacciones desde la API
-Publicará mensajes en RabbitMQ
-Creará colas automáticamente

## Ejemplo de mensaje enviado
{
  "idTransaccion": "TX-10001",
  "nombre": "Aranza Rueda",
  "carnet": "0905-24-7854",
  "correo": "aruedaa@miumg.edu.gt",
  "monto": 2500.75,
  "moneda": "GTQ",
  "cuentaOrigen": "001-123456-7",
  "bancoDestino": "BANRURAL",
  "detalle": {
    "nombreBeneficiario": "Carlos Pérez",
    "tipoTransferencia": "INTERBANCARIA",
    "descripcion": "Pago proveedor",
    "referencias": {
      "factura": "F-88991",
      "codigoInterno": "ABC123"
    }
  }
}

Casos soportados

-Creación automática de colas por banco
-Procesamiento de cientos de transacciones
-Reintento automático en caso de error en el POST
-Confirmación manual de mensajes
-Prevención de duplicados por factura e idTransaccion.

