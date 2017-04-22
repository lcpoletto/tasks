package com.lcpoletto.tasks;

import static org.junit.Assert.assertEquals;
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
import com.lcpoletto.exceptions.ObjectNotFoundException;
import com.lcpoletto.exceptions.ValidationException;

/**
 * Test fixture for {@link DeleteTask}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteTaskTest {

    @Mock
    private AmazonDynamoDB mockClient;

    private DeleteTask lambda;

    @Before
    public void setup() {
        lambda = new DeleteTask(mockClient);
    }

    @Test(expected = ValidationException.class)
    public void testNullInput() {
        lambda.handleRequest(null, null);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyInput() {
        lambda.handleRequest("", null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testNotFound() {
        when(mockClient.getItem(any())).thenReturn(new GetItemResult());
        try {
            lambda.handleRequest("non_existing", null);
            fail();
        } catch (Throwable t) {
            verify(mockClient).getItem(any());
            throw t;
        }

    }

    @Test
    public void testSuccess() {
        final GetItemResult itemResult = new GetItemResult();
        itemResult.addItemEntry("id", new AttributeValue("this_id_exists"));

        when(mockClient.getItem(any())).thenReturn(itemResult);
        when(mockClient.deleteItem(any())).thenReturn(null);

        final String result = lambda.handleRequest("this_id_exists", null);
        assertEquals("SUCCESS", result);
        verify(mockClient).getItem(any());
        verify(mockClient).deleteItem(any());
    }

}
