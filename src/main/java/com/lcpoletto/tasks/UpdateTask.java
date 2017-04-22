/**
 * 
 */
package com.lcpoletto.tasks;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.tasks.model.Task;

/**
 * AWS Lambda function to update a task in Dynamo DB.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class UpdateTask {

    private static final Logger logger = Logger.getLogger(UpdateTask.class);

    private DynamoDBMapper dynamoMapper;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public UpdateTask() {
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
    public UpdateTask(final AmazonDynamoDB dynamoClient) {
        dynamoMapper = new DynamoDBMapper(dynamoClient);
    }

    /**
     * AWS lambda entry point which will update a received task.
     * 
     * @param input
     *            task to be updated
     * @return the updated task with new values
     * @throws ValidationException
     *             if any validation error occurs
     */
    public Task handleRequest(Task input) throws ValidationException {
        logger.debug(String.format("Updating task: %s", input));
        validateInput(input);
        dynamoMapper.save(input);
        logger.debug(String.format("Updated with success: %s", input));
        return input;
    }

    /**
     * Validates if the task values are provided and within ranges.
     * 
     * @param input
     *            task to be validated
     * @throws ValidationException
     *             if any value is missing or out of range
     */
    private void validateInput(final Task input) throws ValidationException {
        logger.debug(String.format("Validating for update: %s", input));
        if (input == null) {
            throw new ValidationException("Input can't be null.");
        }
        if (input.getDescription() == null || input.getDescription().isEmpty()) {
            throw new ValidationException("Task description is required.");
        }
        if (input.getPriority() == null || input.getPriority() < 0 || input.getPriority() > 9) {
            throw new ValidationException("Task priority is required and must be between 0 and 9.");
        }
        if (input.getId() == null || input.getId().isEmpty()) {
            throw new ValidationException("Task id is required.");
        }
    }
}
