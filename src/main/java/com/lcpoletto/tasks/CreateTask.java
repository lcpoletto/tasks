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
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.tasks.model.Task;

/**
 * Lambda function to insert a new task into Dynamo DB.
 * 
 * @author Luis Carlos Poletto
 * 
 */
public class CreateTask implements RequestHandler<Task, Task> {

    private static final Logger logger = Logger.getLogger(CreateTask.class);

    private DynamoDBMapper dynamoMapper;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public CreateTask() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    /**
     * Overloaded constructor which received the AWS dynamo db client. This
     * constructor was created mainly to make it easier to mock external
     * dependencies on unit tests.
     * 
     * @param dynamoClient
     *            dynamo db client to be used
     */
    public CreateTask(final AmazonDynamoDB dynamoClient) {
        dynamoMapper = new DynamoDBMapper(dynamoClient);
    }

    /**
     * Lambda entry point which will create a new task in dynamo DB.
     * 
     * @param input
     *            task to be created
     * @param context
     *            aws lambda context
     * @return created task with the generated task id
     * @throws ValidationException
     *             if any validation error happens
     */
    @Override
    public Task handleRequest(final Task input, final Context context) {
        logger.debug(String.format("Adding task: %s", input));
        validateInput(input);
        dynamoMapper.save(input);
        input.setResourceUri(String.format("%s/%s", input.getResourceUri(), input.getId()));
        logger.debug(String.format("Inserted with success: %s", input));
        return input;
    }

    private void validateInput(final Task input) {
        logger.debug(String.format("Validating for create: %s", input));
        if (input == null) {
            throw new ValidationException("Task can't be null.");
        }
        input.validate(false);
    }
}
