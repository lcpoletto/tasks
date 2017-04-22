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
import com.lcpoletto.exceptions.ObjectNotFoundException;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.tasks.model.Task;

/**
 * AWS Lambda function to update a task in Dynamo DB.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class UpdateTask implements RequestHandler<Task, String> {

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
     * @param context
     *            aws lambda context information
     * @return
     * @throws ValidationException
     *             if any validation error occurs
     * @throws ObjectNotFoundException
     *             if the object being updated is not on persistence layer
     */
    @Override
    public String handleRequest(Task input, Context context) {
        logger.debug(String.format("Updating task: %s", input));
        validateInput(input);
        if (dynamoMapper.load(input) == null) {
            throw new ObjectNotFoundException("Task %s was not found.", input.getId());
        }
        dynamoMapper.save(input);
        logger.debug(String.format("Updated with success: %s", input));
        return "SUCCESS";
    }

    /**
     * Validates if the task values are provided and within ranges.
     * 
     * @param input
     *            task to be validated
     * @throws ValidationException
     *             if any value is missing or out of range
     */
    private void validateInput(final Task input) {
        logger.debug(String.format("Validating for update: %s", input));
        if (input == null) {
            throw new ValidationException("Input can't be null.");
        }
        input.validate(true);
    }
}
