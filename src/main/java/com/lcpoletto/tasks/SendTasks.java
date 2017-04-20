/**
 * 
 */
package com.lcpoletto.tasks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.lcpoletto.tasks.model.Task;

/**
 * <p>
 * AWS lambda function to send e-mails to users with uncompleted tasks.
 * </p>
 * <p>
 * Parameterized types here really don't matter, maybe only output to give some
 * level of debugging info.
 * </p>
 * 
 * @author Luis Carlos Poletto
 */
public class SendTasks implements RequestHandler<String, String> {

	private static final Logger logger = Logger.getLogger(SendTasks.class);
	private static final String SUCCESS = "SUCCESS";
	private static final String DEFAULT_EMAIL_FROM = "noreply@tasks.com";

	@Override
	public String handleRequest(String input, Context context) {
		logger.debug("Sending tasks reminder to users.");
		// TODO: check if sending a request to the list API endpoint makes sense
		// here
		final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
		final DynamoDBMapper mapper = new DynamoDBMapper(client);
		final DynamoDBScanExpression expression = new DynamoDBScanExpression()
				.withFilterExpression("attribute_not_exists(completed)");
		/*
		 * as dynamo db returns pages of results when doing blanket scans we
		 * will need to iterate on them to make sure they're all available to
		 * return
		 */
		final List<Task> paginatedTasks = mapper.scan(Task.class, expression);
		// TODO: validate if the result tasks are empty or null
		final Map<String, List<Task>> uncompleted = new HashMap<>();
		/*
		 * Here we're generating a map with the user id and all the tasks
		 * he/she's supposed to work on.
		 */
		for (final Task task : paginatedTasks) {
			List<Task> userTasks = uncompleted.get(task.getUser());
			if (userTasks == null) {
				userTasks = new LinkedList<>();
				uncompleted.put(task.getUser(), userTasks);
			}
			userTasks.add(task);
		}
		logger.debug(String.format("Sending notifications to %d users.", uncompleted.size()));
		/*
		 * Now that we have a map with all tasks per user we can start
		 * generating aggregated e-mails for each of them.
		 */
		for (Map.Entry<String, List<Task>> userTasks : uncompleted.entrySet()) {
			sendMail(userTasks.getKey(), userTasks.getValue());
		}
		// TODO: check for any error scenario and what we want to return
		return SUCCESS;
	}

	/**
	 * Helper method which will send the e-mail using SES.
	 * 
	 * TODO: Should we use multi-threading here?
	 * 
	 * @param email
	 *            destination
	 * @param tasks
	 *            list of uncompleted tasks
	 */
	private void sendMail(final String email, List<Task> tasks) {
		logger.debug(String.format("Sending %d tasks to %s.", tasks.size(), email));
		final Destination destination = new Destination().withToAddresses(email);
		final Content subject = new Content().withData("Uncompleted tasks reminder");

		final StringBuilder text = new StringBuilder();
		text.append("This is a friendly reminder for your uncompleted tasks below: \n\n");
		for (final Task task : tasks) {
			text.append(String.format("Task: %s\nPriority: %d\n\n", task.getDescription(), task.getPriority()));
		}
		final Content textBody = new Content().withData(text.toString());
		final Body body = new Body().withText(textBody);
		final Message message = new Message().withSubject(subject).withBody(body);
		final SendEmailRequest request = new SendEmailRequest().withSource(getMailFrom()).withDestination(destination)
				.withMessage(message);

		try {
			final AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.defaultClient();
			client.sendEmail(request);
			logger.debug(String.format("Mail to %s sent with %d tasks.", email, tasks.size()));
		} catch (Exception e) {
			logger.error("Error sending e-mail.", e);
		}
	}

	/**
	 * Helper method which will try to read configuration from environment
	 * variables.
	 * 
	 * @return configured email source or default value
	 */
	private String getMailFrom() {
		final String result = System.getenv("TASKS_MAIL_FROM");
		logger.trace(String.format("TASKS_MAIL_FROM: %s", result));
		if (result == null || result.isEmpty()) {
			return DEFAULT_EMAIL_FROM;
		}
		return result;
	}

}
