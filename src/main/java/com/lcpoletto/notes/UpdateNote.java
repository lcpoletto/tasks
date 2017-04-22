/**
 * 
 */
package com.lcpoletto.notes;

import org.apache.log4j.Logger;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.lcpoletto.exceptions.ObjectNotFoundException;
import com.lcpoletto.exceptions.PermissionException;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.notes.model.Note;

/**
 * AWS Lambda function to update notes on dynamo db.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class UpdateNote implements RequestHandler<Note, String> {

    private static final Logger logger = Logger.getLogger(CreateNote.class);

    private DynamoDBMapper dynamoMapper;

    /**
     * Default constructor which will use AWS static helpers to instantiate
     * class properties.
     */
    public UpdateNote() {
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
    public UpdateNote(final AmazonDynamoDB dynamoClient) {
        dynamoMapper = new DynamoDBMapper(dynamoClient);
    }

    /**
     * AWS lambda entry point which will update a note into dynamo db.
     * 
     * @param input
     *            record to be inserted
     * @param context
     *            aws lambda context
     * @return the inserted record with generated id
     * @throws ValidationException
     *             if any input validation error happens
     */
    public String handleRequest(final Note input, final Context context) {
        logger.debug(String.format("Inserting into persistence layer: %s", input));
        validateInput(input);
        final Note retrieved = validateAllowChange(input);
        retrieved.setContent(input.getContent());
        // we store the last user which updated the record because we're going
        // to use this information when sending the notification to original
        // owner of the note.
        retrieved.setUpdatedBy(input.getUpdatedBy());
        dynamoMapper.save(input);
        logger.info(String.format("Inserted with success: %s", input));
        return "SUCCESS";
    }

    /**
     * Validate all the required fields on the input.
     * 
     * @param input
     *            note to be validated
     * @throws ValidationException
     *             if any required field is missing
     */
    private void validateInput(final Note input) {
        logger.debug(String.format("Validating for update: %s", input));
        if (input == null) {
            throw new ValidationException("Note to be updated is required.");
        }
        if (input.getId() == null || input.getId().isEmpty()) {
            throw new ValidationException("Note id is required.");
        }
        if (input.getUpdatedBy() == null || input.getUpdatedBy().isEmpty()) {
            throw new ValidationException("User updating the note is required.");
        }
        if (input.getContent() == null || input.getContent().isEmpty()) {
            throw new ValidationException("Note content is required.");
        }
    }

    /**
     * Retrieves the correspondig note from the database and validate if the
     * user making the change is allowed to do so.
     * 
     * @param input
     *            note to be validated
     * @return the retrieved note from the database
     * @throws ValidationException
     *             if the user is not the owner of the note and the note is not
     *             marked to allow changes
     * @throws ObjectNotFoundException
     *             if the note is not present on persistence layer
     */
    private Note validateAllowChange(final Note input) {
        logger.debug(String.format("Checking change allowed: %s", input));
        final Note retrieved = dynamoMapper.load(input);
        if (retrieved == null) {
            throw new ObjectNotFoundException("Note %s not found.", input.getId());
        }
        // if this update was not triggered by the owner we need
        // to verify it's permissions
        if (!retrieved.getOwner().equals(input.getUpdatedBy())) {
            if (Boolean.FALSE.equals(retrieved.getAllowChange())) {
                throw new PermissionException("User %s is not allowed to change this note.", input.getUpdatedBy());
            }
        }
        return retrieved;
    }

}
