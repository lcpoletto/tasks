package com.lcpoletto.notes;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.notes.model.Note;

/**
 * AWS lambda function to insert a new Note into dynamo DB.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class CreateNote implements RequestHandler<Note, Note> {

    private static final Logger logger = Logger.getLogger(CreateNote.class);

    private DynamoDBMapper dynamoMapper;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public CreateNote() {
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
    public CreateNote(final AmazonDynamoDB dynamoClient) {
        dynamoMapper = new DynamoDBMapper(dynamoClient);
    }

    /**
     * AWS lambda entry point which will create a note into dynamo db.
     * 
     * @param input
     *            record to be inserted
     * @param context
     *            aws lambda context
     * @return the inserted record with generated id
     * @throws ValidationException
     *             if any input validation error happens
     */
    public Note handleRequest(final Note input, final Context context) {
        logger.debug(String.format("Inserting %s into persistence layer.", input));
        validateInput(input);
        dynamoMapper.save(input);
        input.setResourceUri(String.format("%s/%s", input.getResourceUri(), input.getId()));
        logger.debug(String.format("Inserted with success: %s", input));
        return input;
    }

    /**
     * Validates if all the required fields are present on the received note.
     * 
     * @param input
     *            note to be validate
     * @throws ValidationException
     *             if any required field is missing
     */
    public void validateInput(final Note input) {
        logger.debug(String.format("Validating for insert: %s", input));
        if (input == null) {
            throw new ValidationException("Note to be created is required.");
        }
        if (input.getId() != null && !input.getId().isEmpty()) {
            throw new ValidationException("Note to be created can't have an id set.");
        }
        if (input.getAllowChange() == null) {
            throw new ValidationException("Note allowChange is required.");
        }
        if (input.getContent() == null || input.getContent().isEmpty()) {
            throw new ValidationException("Note content is required.");
        }
        if (input.getOwner() == null || input.getOwner().isEmpty()) {
            throw new ValidationException("Note owner is required.");
        }
        if (input.getRecipient() == null || input.getRecipient().isEmpty()) {
            throw new ValidationException("Note recipient is required.");
        }
    }
}
