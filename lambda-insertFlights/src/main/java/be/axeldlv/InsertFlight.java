package be.axeldlv;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class InsertFlight implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    String region = "us-west-1";
    String localStackUrl = "https://localhost.localstack.cloud:4566";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        context.getLogger().log("Lambda function started...");

        SecretsManagerClient secretsManagerClient = createSecretsManagerClient();
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId("table")
                .build();
        GetSecretValueResponse secretValue = secretsManagerClient.getSecretValue(getSecretValueRequest);
        DynamoDbClient dynamoDbClient = createDynamoDbClient();

        String tableName = secretValue.secretString();
        context.getLogger().log("secretValue: " + tableName);

        String requestBody = apiGatewayProxyRequestEvent.getBody();
        context.getLogger().log("requestBody " + requestBody);
        String bookingId = apiGatewayProxyRequestEvent.getPathParameters().get("bookingId");
        context.getLogger().log("bookingId " + bookingId);
        String orderId = apiGatewayProxyRequestEvent.getPathParameters().get("orderId");
        context.getLogger().log("orderId " + orderId);

        context.getLogger().log("requestBody " + requestBody);
        PutItemResponse response = insertItemToDynamoDb(dynamoDbClient, tableName, requestBody, bookingId, orderId);

        context.getLogger().log("Data saved successfully to DynamoDB: " + response);
        return createAPIResponse(requestBody);
    }

    private DynamoDbClient createDynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(localStackUrl))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region))
                .build();
    }

    private PutItemResponse insertItemToDynamoDb(DynamoDbClient dynamoDbClient, String tableName, String requestBody, String bookingId, String orderId) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("bookingId", AttributeValue.builder().s(bookingId).build());
        item.put("orderId", AttributeValue.builder().s(orderId).build());
        item.put("requestBody", AttributeValue.builder().s(requestBody).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        return dynamoDbClient.putItem(putItemRequest);
    }

    private APIGatewayProxyResponseEvent createAPIResponse( String body) {
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        responseEvent.setBody( body );
        responseEvent.setStatusCode( 201 );
        return responseEvent;
    }

    private SecretsManagerClient createSecretsManagerClient() {
        return SecretsManagerClient.builder()
                .endpointOverride(URI.create(localStackUrl))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region))
                .build();
    }
}