Run following commands one by one to start kafka and spring boot service:
mvn clean package -DskipTests
mvn clean install -DskipTests
docker compose down -v
docker compose up -d
docker compose up --build

Following is the curl command(input.json is present in instructions-capture-service folder)
curl -X POST http://localhost:8080/trades \
     -H "Content-Type: application/json" \
     --data "@input.json"

swagger is configured here:
http://localhost:8080/swagger-ui.html

the above curl command will return following:
Successfully processed 10 trades.

Once the Kafka server is up and running. Look for following string in logs.
Screenshots are attached in this folder instructions-capture-service/screenshots
Received message from instructions.inbound - 
Received message from instructions.outbound - 
Producing message to instructions.outbound:
Producing message to instructions.inbound:

here is example of what i received in logs:
Producing message to instructions.outbound: {"trade":{"account":"****1234","security":"ABC123","type":"B","amount":100000,"timestamp":"2025-08-04T21:15:33Z"},"platform_id":"ACCT123"}
Received message from instructions.outbound - {"trade":{"account":"****2345","security":"DEF456","type":"B","amount":75000,"timestamp":"2025-08-05T15:45:00Z"},"platform_id":"ACCT125"}
Producing message to instructions.inbound: {"account":"****2345","security":"DEF456","type":"B","amount":75000,"timestamp":"2025-08-05T15:45:00Z","platform_id":"ACCT125"}
Received message from instructions.inbound - {"account":"****1234","security":"ABC123","type":"B","amount":100000,"timestamp":"2025-08-04T21:15:33Z","platform_id":"ACCT123"}
