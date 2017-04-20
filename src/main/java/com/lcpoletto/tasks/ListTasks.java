package com.lcpoletto.tasks;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lcpoletto.tasks.model.Task;

/**
 * AWS Lambda function to list tasks based on input data.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class ListTasks implements RequestHandler<String, List<Task>> {

	private static final Logger logger = Logger.getLogger(ListTasks.class);

	@Override
	public List<Task> handleRequest(String input, Context context) {
		logger.debug("Listing tasks from persistence layer.");
		final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		final DynamoDBMapper mapper = new DynamoDBMapper(client);
		/*
		 * as dynamo db returns pages of results when doing blanket scans we
		 * will need to iterate on them to make sure they're all available to
		 * return
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