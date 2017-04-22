/**
 * 
 */
package com.lcpoletto.notes;

import static org.junit.Assert.fail;
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

    @Test(expected = PermissionException.class)
    public void testChangeNotAllowed() {
        final Note input = new Note();
        input.setId("id");
        input.setContent("content");
        input.setUpdatedBy("updatedBy");

        when(mockClient.getItem(any())).thenReturn(getChangeNotAllowed());
        try {
            lambda.handleRequest(input, null);
            fail();
        } catch (Throwable t) {
            verify(mockClient).getItem(any());
            throw t;
        }
    }

    @Test(expected = PermissionException.class)
    public void testChangeAllowedWrongUser() {
        final Note input = new Note();
        input.setId("id");
        input.setContent("content");
        input.setUpdatedBy("updatedBy");

        when(mockClient.getItem(any())).thenReturn(getChangeAllowed("otherUser"));
        try {
            lambda.handleRequest(input, null);
            fail();
        } catch (Throwable t) {
            verify(mockClient).getItem(any());
            throw t;
        }
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
    public void testOtherChangeAllowed() {
        final Note input = new Note();
        input.setId("id");
        input.setContent("content");
        input.setUpdatedBy("updatedBy");

        when(mockClient.getItem(any())).thenReturn(getChangeAllowed("updatedBy"));
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
    private GetItemResult getChangeAllowed(final String user) {
        final GetItemResult result = new GetItemResult();
        result.addItemEntry("id", new AttributeValue("id"));
        result.addItemEntry("allowChange", new AttributeValue().withN("1"));
        result.addItemEntry("owner", new AttributeValue("owner"));
        result.addItemEntry("recipient", new AttributeValue(user));
        return result;
    }
}
