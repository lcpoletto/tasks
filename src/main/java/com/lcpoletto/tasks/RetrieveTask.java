package com.lcpoletto.tasks;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.lcpoletto.tasks.model.Task;

/**
 * AWS Lambda function to list tasks based on input data.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class RetrieveTask {

    private static final Logger logger = Logger.getLogger(RetrieveTask.class);

    public List<Task> handleRequest(String input) {
        logger.debug("Listing tasks from persistence layer.");
        final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        final DynamoDBMapper mapper = new DynamoDBMapper(client);
        /*
         * After testing looks like if we use eager loading dynamo db takes way
         * more time to return the results, thus I'm iterating on results to
         * make sure I loaded all of them
         */
        final List<Task> paginatedTasks = mapper.scan(Task.class, new DynamoDBScanExpression());
        /*
         * using a linked list because we don't know the size of the results and
         * we won't do any type of sorting as of now, thus it will be more
         * efficient than resizing arrays on an ArrayList
         */
        final List<Task> result = new LinkedList<>();
        if (paginatedTasks != null && !paginatedTasks.isEmpty()) {
            for (final Task task : paginatedTasks) {
                result.add(task);
            }
        }
        logger.debug(String.format("Found %d tasks.", result.size()));
        return result;
    }
}