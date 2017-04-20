/**
 * 
 */
package com.lcpoletto.tasks;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
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
        PowerMock.mockStatic(AmazonDynamoDBClientBuilder.class);
        EasyMock.expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);
        EasyMock.expect(mockClient.getItem(EasyMock.anyObject())).andReturn(new GetItemResult());
        EasyMock.replay(mockClient);
        PowerMock.replay(AmazonDynamoDBClientBuilder.class);

        final String result = lambda.handleRequest("Should receive back a not found.");
        Assert.assertEquals("NOT_FOUND", result);
        PowerMock.verify(AmazonDynamoDBClientBuilder.class);
        EasyMock.verify(mockClient);
    }

    @Test
    public void testSuccess() throws ValidationException {
        final DeleteTask lambda = new DeleteTask();
        PowerMock.mockStatic(AmazonDynamoDBClientBuilder.class);
        EasyMock.expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);

        final Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue("this_id_exists"));
        final GetItemResult itemResult = new GetItemResult();
        itemResult.setItem(item);

        EasyMock.expect(mockClient.getItem(EasyMock.anyObject())).andReturn(itemResult);
        EasyMock.expect(mockClient.deleteItem(EasyMock.anyObject())).andReturn(null);
        EasyMock.replay(mockClient);
        PowerMock.replay(AmazonDynamoDBClientBuilder.class);

        final String result = lambda.handleRequest("this_id_exists");
        Assert.assertEquals("SUCCESS", result);
        PowerMock.verify(AmazonDynamoDBClientBuilder.class);
        EasyMock.verify(mockClient);
    }

}
