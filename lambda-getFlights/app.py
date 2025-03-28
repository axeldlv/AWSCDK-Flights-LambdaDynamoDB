import boto3
import os
import json


region = "us-west-1"
localstackurl = "https://localhost.localstack.cloud:4566"

dynamoDBClient = boto3.client('dynamodb', 
             endpoint_url=localstackurl,
             region_name=region)

dynamodb = boto3.resource('dynamodb', endpoint_url=localstackurl, region_name=region)

def lambda_handler(event, context):
    item_id = event.get('pathParameters', {}).get('bookingId')
    order_id = event.get('pathParameters', {}).get('orderId')

    print("Start PYTHON lambda handler with ")
    print("item_id : ", item_id)
    print("order_id : ", order_id)

    if not item_id:
        return {
            'statusCode': 400,
            'body': json.dumps({'error': 'ID not provided in path parameters'})
        }

    table_name = "FlightTable"
    if not table_name:
        raise KeyError("TABLEName environment variable is not set")
    
    print("GOT Table name : ", table_name)
    table = dynamodb.Table(table_name)

    # fetch todo from the database
    response = table.get_item(Key={'orderId': str(order_id), 'bookingId': str(item_id)})
    print("GOT response : ", response)    
    item = response.get('Item', {})
    print("GOT item : ", item)    
        
    print("Fin de lambda PYTHON handler with context", context)

    return {
        'statusCode': 200,
        'body': item
    }
