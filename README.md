Instructions Capture Service: Operation Guide

This guide outlines the build, deployment, and testing steps for the instructions-capture-service application.

1. Build and Deployment

Run the following commands sequentially from the project root directory to package the application, manage Docker containers, and start the services.
Step	Command	Description
1. Package	mvn clean package -DskipTests	Cleans the target directory and packages the Spring Boot application into a runnable JAR, skipping unit tests.
2. Install	mvn clean install -DskipTests	Installs the artifact into the local Maven repository (usually required before Docker build).
3. Clean Down	docker compose down -v	Stops and removes all containers, networks, and named volumes defined in docker-compose.yml.
4. Initial Up	docker compose up -d	Starts the services (Kafka, Zookeeper, etc.) in detached mode (-d).
5. Build & Run	docker compose up --build	Rebuilds the application image (to include the latest JAR) and starts all services in the foreground, showing logs.

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
