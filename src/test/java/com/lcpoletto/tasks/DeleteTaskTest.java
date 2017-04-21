package com.lcpoletto.tasks;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.easymock.Mock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.lcpoletto.tasks.exceptions.ValidationException;

/**
 * Test fixture for {@link DeleteTask}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClientBuilder.class })
public class DeleteTaskTest {

    @Mock
    private AmazonDynamoDB mockClient;

    @Test(expected = ValidationException.class)
    public void testNullInput() throws ValidationException {
        final DeleteTask lambda = new DeleteTask();
        lambda.handleRequest(null);
    }

    @Test(expected = ValidationException.class)
    public void testEmptyInput() throws ValidationException {
        final DeleteTask lambda = new DeleteTask();
        lambda.handleRequest("");
    }

    @Test
    public void testNotFound() throws ValidationException {
        final DeleteTask lambda = new DeleteTask();
        mockStatic(AmazonDynamoDBClientBuilder.class);
        expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);
        expect(mockClient.getItem(anyObject())).andReturn(new GetItemResult());
        replay(mockClient);
        replayAll();

        final String result = lambda.handleRequest("non_existing");
        assertEquals("NOT_FOUND", result);
        verifyAll();
        verify(mockClient);
    }

    @Test
    public void testSuccess() throws ValidationException {
        final DeleteTask lambda = new DeleteTask();
        mockStatic(AmazonDynamoDBClientBuilder.class);
        expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);

        final GetItemResult itemResult = new GetItemResult();
        itemResult.addItemEntry("id", new AttributeValue("this_id_exists"));

        expect(mockClient.getItem(anyObject())).andReturn(itemResult);
        expect(mockClient.deleteItem(anyObject())).andReturn(null);
        replay(mockClient);
        replayAll();

        final String result = lambda.handleRequest("this_id_exists");
        assertEquals("SUCCESS", result);
        verifyAll();
        verify(mockClient);
    }

}
