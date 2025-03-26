package be.axeldlv.infra;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;

public class CDKLambdaDynamoDBStackStack extends Stack {
    public CDKLambdaDynamoDBStackStack(final Construct scope, final String id) {
        this(scope, id, null);
    }
    public CDKLambdaDynamoDBStackStack(final Construct scope, final String name, final StackProps props) {
        super(scope, name, props);

        Table flightTable = Table.Builder.create(this, "FlightTable")
                .tableName("FlightTable")
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .sortKey(Attribute.builder().name("orderId").type(AttributeType.STRING).build())
                .partitionKey(Attribute.builder().name("bookingId").type(AttributeType.STRING).build()).build();

        Function insertFlightLambda = Function.Builder.create(this, "InsertFlightLambda")
                .functionName("InsertFlightLambda")
                .runtime(Runtime.JAVA_21)
                .handler("be.axeldlv.InsertFlight::handleRequest")
                .code(Code.fromAsset("../lambda-insertFlights/jar/sendtodynamodb-1.0-SNAPSHOT.jar"))
                .memorySize(1024)
                .timeout(Duration.seconds(60))
                .description("Lambda Function to insert data to DynamoDB")
                .tracing(Tracing.ACTIVE)
                .build();

        Function getFlightLambda = Function.Builder.create(this, "getFlightLambda")
                .functionName("getFlightLambda")
                .runtime(Runtime.PYTHON_3_13)
                .handler("app.lambda_handler")
                .code(Code.fromAsset("../lambda-getFlights"))
                .memorySize(1024)
                .timeout(Duration.seconds(60))
                .description("Lambda function to retrieve data from DynamoDB")
                .tracing(Tracing.ACTIVE)
                .build();

        Secret addTableNameSecret = Secret.Builder.create(this, "addTableNameSecret")
                .secretName("table")
                .secretStringValue(SecretValue.unsafePlainText(flightTable.getTableName()))
                .build();

        addTableNameSecret.grantRead(insertFlightLambda);
        flightTable.grantReadWriteData(insertFlightLambda);
        addTableNameSecret.grantRead(getFlightLambda);
        flightTable.grantReadData(getFlightLambda);
    }
}
