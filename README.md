Instructions Capture Service: Operation Guide

     1. Build and Deployment

Run the following commands sequentially from the project root directory 
1. mvn clean package -DskipTests	
2. mvn clean install -DskipTests	
3. docker compose down -v	
4. docker compose up -d	
5. docker compose up --build	

     2. API Testing and Interaction

The service endpoint is available for testing using curl.

Endpoint

Description	URL
Trade Ingestion	http://localhost:8080/trades
Swagger/OpenAPI UI	http://localhost:8080/swagger-ui.html

Curl Command

To submit the trades defined in the input.json file:
Bash

curl -X POST http://localhost:8080/trades \
     -H "Content-Type: application/json" \
     --data "@input.json"

Expected Output

If the ingestion is successful, the service is expected to return:

Successfully processed 10 trades.

     3. Log Monitoring (Kafka Flow)

Once the curl command is executed, the following strings confirm the successful end-to-end flow from the REST endpoint, through Kafka, and back to the consumer listeners.

Key Log Strings to Monitor

Log String	Stage in Flow
Producing message to instructions.inbound:	Trade is received by the controller and sent to the initial Kafka topic.
Received message from instructions.inbound -	The Kafka Stream Processor (or initial listener) picks up the raw trade.
Producing message to instructions.outbound:	The trade is transformed and sent to the final outbound topic.
Received message from instructions.outbound -	The final listener picks up the fully processed/transformed trade.

Example Log Snippets

This demonstrates the successful transformation and transfer between topics:

Producing message to instructions.inbound: {"account":"****2345","security":"DEF456","type":"B","amount":75000,"timestamp":"2025-08-05T15:45:00Z","platform_id":"ACCT125"}
Received message from instructions.inbound - {"account":"****1234","security":"ABC123","type":"B","amount":100000,"timestamp":"2025-08-04T21:15:33Z","platform_id":"ACCT123"}
Producing message to instructions.outbound: {"trade":{"account":"****1234","security":"ABC123","type":"B","amount":100000,"timestamp":"2025-08-04T21:15:33Z"},"platform_id":"ACCT123"}
Received message from instructions.outbound - {"trade":{"account":"****2345","security":"DEF456","type":"B","amount":75000,"timestamp":"2025-08-05T15:45:00Z"},"platform_id":"ACCT125"}


Gemini can make mistakes, so double-check it
