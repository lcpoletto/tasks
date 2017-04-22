package com.lcpoletto.tasks;

import static org.junit.Assert.assertEquals;
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
    public void testNullInput() throws ValidationException {
        lambda.handleRequest(null);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyInput() throws ValidationException {
        lambda.handleRequest("");
    }

    @Test
    public void testNotFound() throws ValidationException {
        when(mockClient.getItem(any())).thenReturn(new GetItemResult());
        final String result = lambda.handleRequest("non_existing");
        assertEquals("NOT_FOUND", result);
        verify(mockClient).getItem(any());
    }

    @Test
    public void testSuccess() throws ValidationException {
        final GetItemResult itemResult = new GetItemResult();
        itemResult.addItemEntry("id", new AttributeValue("this_id_exists"));

        when(mockClient.getItem(any())).thenReturn(itemResult);
        when(mockClient.deleteItem(any())).thenReturn(null);

        final String result = lambda.handleRequest("this_id_exists");
        assertEquals("SUCCESS", result);
        verify(mockClient).getItem(any());
        verify(mockClient).deleteItem(any());
    }

}
