package com.lcpoletto.notes;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.notes.model.Note;

/**
 * Test fixture for {@link CreateNote}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateNoteTest {

    @Mock
    private AmazonDynamoDB mockClient;

    private CreateNote lambda;

    @Before
    public void setup() {
        lambda = new CreateNote(mockClient);
    }

    @Test(expected = ValidationException.class)
    public void testNull() throws ValidationException {
        lambda.handleRequest(null);
    }

    @Test(expected = ValidationException.class)
    public void testEmpty() throws ValidationException {
        lambda.handleRequest(new Note());
    }

    @Test(expected = ValidationException.class)
    public void testWithId() throws ValidationException {
        final Note input = populateNote();
        lambda.handleRequest(input);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutAllowChange() throws ValidationException {
        final Note input = populateNote();
        input.setAllowChange(null);
        lambda.handleRequest(input);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutContent() throws ValidationException {
        final Note input = populateNote();
        input.setContent(null);
        lambda.handleRequest(input);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutOwner() throws ValidationException {
        final Note input = populateNote();
        input.setOwner(null);
        lambda.handleRequest(input);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutRecipient() throws ValidationException {
        final Note input = populateNote();
        input.setRecipient(null);
        lambda.handleRequest(input);
    }

    @Test
    public void testSuccess() throws ValidationException {
        when(mockClient.putItem(any())).thenReturn(new PutItemResult());
        final Note input = populateNote();
        input.setId(null);
        final Note created = lambda.handleRequest(input);
        assertNotNull(created.getId());
        verify(mockClient).putItem(any());
    }

    /**
     * Creates a note with all fields populated.
     * 
     * @return populated note
     */
    private Note populateNote() {
        final Note result = new Note();
        result.setAllowChange(Boolean.FALSE);
        result.setContent("content");
        result.setId("id");
        result.setOwner("owner");
        result.setRecipient("recipient");
        result.setUpdatedBy("updatedBy");
        return result;
    }

}
