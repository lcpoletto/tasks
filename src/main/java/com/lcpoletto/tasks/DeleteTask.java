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
 * AWS lambda function to delete a task from Dynamo DB.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class DeleteTask {

    private static final Logger logger = Logger.getLogger(DeleteTask.class);
    private static final String SUCCESS = "SUCCESS";
    private static final String NOT_FOUND = "NOT_FOUND";

    /**
     * Deletes the received task.
     * 
     * @param taskId
     *            task to be deleted
     * @return <code>SUCCESS</code> if deleted with success
     *         <code>NOT_FOUND</code> if task didn't exist
     */
    public String handleRequest(String taskId) throws ValidationException {
        validateInput(taskId);
        logger.debug(String.format("Deleting task %s", taskId));
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        final DynamoDBMapper mapper = new DynamoDBMapper(client);
        final Task retrieved = mapper.load(Task.class, taskId);
        if (retrieved == null) {
            logger.info(String.format("Task %s not found.", taskId));
            // TODO: maybe here we could map to a 404 on the API gateway
            return NOT_FOUND;
        }
        mapper.delete(retrieved);
        // if no error is thrown by the delete above it means it was succesful
        return SUCCESS;
    }

    private void validateInput(final String taskId) throws ValidationException {
        if (taskId == null || taskId.isEmpty()) {
            throw new ValidationException("Task id is required when deleting.");
        }
    }

}
