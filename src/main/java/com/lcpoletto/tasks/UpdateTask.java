/**
 * 
 */
package com.lcpoletto.tasks;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lcpoletto.tasks.model.Task;

/**
 * AWS Lambda function to update a task in Dynamo DB.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class UpdateTask implements RequestHandler<Task, Task> {

    private static final Logger logger = Logger.getLogger(UpdateTask.class);

    @Override
    public Task handleRequest(Task input, Context context) {
        // TODO: Validate if the received input has ID.
        logger.debug(String.format("Updating task: %s", input));
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        final DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(input);
        logger.debug(String.format("Updated with success: %s", input));
        return input;
    }

}
