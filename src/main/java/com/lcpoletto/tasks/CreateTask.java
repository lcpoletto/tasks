/**
 * 
 */
package com.lcpoletto.tasks;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.lcpoletto.tasks.exceptions.ValidationException;
import com.lcpoletto.tasks.model.Task;

/**
 * Lambda function to insert a new task into Dynamo DB.
 * 
 * @author Luis Carlos Poletto
 * 
 */
public class CreateTask {

    private static final Logger logger = Logger.getLogger(CreateTask.class);

    public Task handleRequest(final Task input) throws ValidationException {
        validateInput(input);
        logger.debug(String.format("Adding task: %s", input));
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        final DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(input);
        logger.debug(String.format("Inserted with success: %s", input));
        return input;
    }

    // TODO: change all of this to use bean validation???
    private void validateInput(final Task input) throws ValidationException {
        final StringBuilder errors = new StringBuilder();
        if (input == null) {
            addError(errors, "Input can't be null.");
        } else {
            if (input.getDescription() == null || input.getDescription().isEmpty()) {
                addError(errors, "Task description is required.");
            }
            if (input.getPriority() == null || input.getPriority() < 0 || input.getPriority() > 10) {
                addError(errors, "Task priority is required and must be between 0 and 10.");
            }
        }
        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }

    private void addError(final StringBuilder errors, final String message) {
        if (errors.length() > 0) {
            errors.append("\n");
        }
        errors.append(message);
    }
}
