 package com;

 import be.axeldlv.infra.CDKLambdaDynamoDBStackStack;
 import org.junit.jupiter.api.Test;
 import software.amazon.awscdk.App;
 import software.amazon.awscdk.assertions.Template;

 import java.io.IOException;
 import java.util.Map;

 public class InsertFlightLambdaAppTest {

     @Test
     public void testLambdaFunctionCreated() throws IOException {
         App app = new App();
         CDKLambdaDynamoDBStackStack stack = new CDKLambdaDynamoDBStackStack(app, "InsertFlightLambdaStack");

         Template template = Template.fromStack(stack);

         template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                 "Runtime", "java21",
                 "Handler", "be.axeldlv.InsertFlight::handleRequest",
                 "MemorySize", 1024
         ));

         template.hasResourceProperties("AWS::Lambda::Function", Map.of(
                 "Runtime", "python3.13",
                 "Handler", "app.lambda_handler",
                 "MemorySize", 1024
         ));
     }

     @Test
     void testDynamoDBTableCreated() {
         App app = new App();
         CDKLambdaDynamoDBStackStack stack = new CDKLambdaDynamoDBStackStack(app, "InsertFlightLambdaStack");

         Template template = Template.fromStack(stack);

         // Assert that a DynamoDB table exists
         template.hasResourceProperties("AWS::DynamoDB::Table", Map.of(
                 "TableName", "FlightTable",
                 "BillingMode", "PAY_PER_REQUEST"
         ));
     }
 }
