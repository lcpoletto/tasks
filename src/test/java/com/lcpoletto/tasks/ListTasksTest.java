/**
 * 
 */
package com.lcpoletto.tasks;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.lcpoletto.tasks.model.Task;

/**
 * @author Luis.Poletto
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClientBuilder.class, ListTasks.class, PaginatedList.class })
public class ListTasksTest {

    private static final DateFormat ISO8601FORMAT = new ISO8601DateFormat();

    @Mock
    private AmazonDynamoDB mockClient;

    /**
     * Every test we do here we are calling these mocked methods at least once,
     * thus make sense to have it on the setup method.
     */
    @Before
    public void setup() {
        PowerMock.mockStatic(AmazonDynamoDBClientBuilder.class);
        EasyMock.expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);
    }

    /**
     * We have to verify at least one call on
     * {@link AmazonDynamoDBClientBuilder}.
     */
    @After
    public void tearDown() {
        PowerMock.verifyAll();
    }

    @Test
    public void testEmptyResults() {
        EasyMock.expect(mockClient.scan(EasyMock.anyObject())).andReturn(getEmptyResults());
        EasyMock.replay(mockClient);
        PowerMock.replayAll();

        final ListTasks lambda = new ListTasks();
        final List<Task> result = lambda.handleRequest(null);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
        EasyMock.verify(mockClient);
    }

    @Test
    public void testNonEmptyResults() {
        EasyMock.expect(mockClient.scan(EasyMock.anyObject())).andReturn(getUnorderedResults());
        EasyMock.replay(mockClient);
        PowerMock.replayAll();

        final ListTasks lambda = new ListTasks();
        final List<Task> result = lambda.handleRequest(null);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
        EasyMock.verify(mockClient);
    }

    /**
     * This test has to fail until I implement part 2 of assignment.
     */
    @Test
    public void testSortedResults() {
        EasyMock.expect(mockClient.scan(EasyMock.anyObject())).andReturn(getUnorderedResults());
        EasyMock.replay(mockClient);
        PowerMock.replayAll();

        final ListTasks lambda = new ListTasks();
        final List<Task> result = lambda.handleRequest(null);
        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("first", result.get(0).getId());
        Assert.assertEquals("second", result.get(1).getId());
        Assert.assertEquals("third", result.get(2).getId());
        EasyMock.verify(mockClient);
    }

    private ScanResult getUnorderedResults() {
        final ScanResult result = new ScanResult();
        final Collection<Map<String, AttributeValue>> items = new ArrayList<>();
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", new AttributeValue("second"));
        item.put("user", new AttributeValue("second@second.com"));
        item.put("description", new AttributeValue("second description"));
        item.put("priority", new AttributeValue().withN("8"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("third"));
        item.put("user", new AttributeValue("third@third.com"));
        item.put("description", new AttributeValue("third description"));
        item.put("priority", new AttributeValue().withN("5"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("first"));
        item.put("user", new AttributeValue("first@first.com"));
        item.put("description", new AttributeValue("first description"));
        item.put("priority", new AttributeValue().withN("5"));
        item.put("completed", new AttributeValue(ISO8601FORMAT.format(new Date())));
        items.add(item);

        result.setItems(items);
        return result;
    }

    private ScanResult getEmptyResults() {
        final ScanResult result = new ScanResult();
        return result;
    }

}
