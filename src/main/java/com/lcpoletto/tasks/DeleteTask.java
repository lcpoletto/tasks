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
 * AWS lambda function to delete a task from Dynamo DB.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class DeleteTask implements RequestHandler<String, String> {

    private static final Logger logger = Logger.getLogger(DeleteTask.class);
    private static final String SUCCESS = "SUCCESS";

    private DynamoDBMapper dynamoMapper;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public DeleteTask() {
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
    public DeleteTask(final AmazonDynamoDB dynamoClient) {
        dynamoMapper = new DynamoDBMapper(dynamoClient);
    }

    /**
     * Deletes the received task.
     * 
     * @param taskId
     *            task to be deleted
     * @param context
     *            aws lambda context
     * @return <code>SUCCESS</code> if deleted with success
     *         <code>NOT_FOUND</code> if task didn't exist
     */
    @Override
    public String handleRequest(String taskId, Context context) {
        logger.debug(String.format("Deleting task %s", taskId));
        validateInput(taskId);
        final Task retrieved = dynamoMapper.load(Task.class, taskId);
        if (retrieved == null) {
            throw new ObjectNotFoundException("Task %s not found.", taskId);
        }
        dynamoMapper.delete(retrieved);
        // if no error is thrown by the delete above it means it was succesful
        return SUCCESS;
    }

    /**
     * Validates if the received id is not empty or <code>null</code>.
     * 
     * @param taskId
     *            task id to validate
     * @throws ValidationException
     *             if the input is empty or <code>null</code>
     */
    private void validateInput(final String taskId) throws ValidationException {
        logger.debug(String.format("Validating for delete: %s", taskId));
        if (taskId == null || taskId.isEmpty()) {
            throw new ValidationException("Task id is required when deleting.");
        }
    }

}
