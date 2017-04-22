/**
 * 
 */
package com.lcpoletto.notes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.lcpoletto.exceptions.PermissionException;
import com.lcpoletto.exceptions.ValidationException;
import com.lcpoletto.notes.model.Note;

/**
 * Test fixture for {@link UpdateNote}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateNoteTest {

    @Mock
    private AmazonDynamoDB mockClient;

    private UpdateNote lambda;

    @Before
    public void setup() {
        lambda = new UpdateNote(mockClient);
    }

    @Test(expected = ValidationException.class)
    public void testNull() {
        lambda.handleRequest(null, null);
    }

    @Test(expected = ValidationException.class)
    public void testEmpty() {
        lambda.handleRequest(new Note(), null);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutId() {
        final Note input = new Note();
        input.setContent("content");
        input.setUpdatedBy("updatedBy");
        lambda.handleRequest(input, null);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutUpdatedBy() {
        final Note input = new Note();
        input.setId("id");
        input.setContent("content");
        lambda.handleRequest(input, null);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutContent() {
        final Note input = new Note();
        input.setId("id");
        input.setOwner("owner");
        input.setUpdatedBy("updatedBy");
        lambda.handleRequest(input, null);
    }

    @Test(expected = PermissionException.class)
    public void testChangeNotAllowed() {
        final Note input = new Note();
        input.setId("id");
        input.setContent("content");
        input.setUpdatedBy("updatedBy");

        when(mockClient.getItem(any())).thenReturn(getChangeNotAllowed());
        lambda.handleRequest(input, null);
        verify(mockClient).getItem(any());
    }

    @Test
    public void testChangeByOwner() {
        final Note input = new Note();
        input.setId("id");
        input.setContent("content");
        input.setUpdatedBy("owner");

        when(mockClient.getItem(any())).thenReturn(getChangeNotAllowed());
        when(mockClient.updateItem(any())).thenReturn(new UpdateItemResult());
        lambda.handleRequest(input, null);
        verify(mockClient).getItem(any());
        verify(mockClient).updateItem(any());
    }

    @Test
    public void testChangeAllowed() {
        final Note input = new Note();
        input.setId("id");
        input.setContent("content");
        input.setUpdatedBy("updatedBy");

        when(mockClient.getItem(any())).thenReturn(getChangeAllowed());
        when(mockClient.updateItem(any())).thenReturn(new UpdateItemResult());
        lambda.handleRequest(input, null);
        verify(mockClient).getItem(any());
        verify(mockClient).updateItem(any());
    }

    /**
     * Helper method which will produce a Note that does not allow other users
     * to change it.
     * 
     * @return note that can't be changed
     */
    private GetItemResult getChangeNotAllowed() {
        final GetItemResult result = new GetItemResult();
        result.addItemEntry("id", new AttributeValue("id"));
        result.addItemEntry("allowChange", new AttributeValue().withN("0"));
        result.addItemEntry("owner", new AttributeValue("owner"));
        return result;
    }

    /**
     * Helper method which will produce a Note that does allow other users to
     * change it.
     * 
     * @return note that can be changed
     */
    private GetItemResult getChangeAllowed() {
        final GetItemResult result = new GetItemResult();
        result.addItemEntry("id", new AttributeValue("id"));
        result.addItemEntry("allowChange", new AttributeValue().withN("1"));
        result.addItemEntry("owner", new AttributeValue("owner"));
        result.addItemEntry("recipient", new AttributeValue("updatedBy"));
        return result;
    }
}
