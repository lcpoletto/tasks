package com.lcpoletto.tasks;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.lcpoletto.tasks.model.Task;

/**
 * Test fixture for {@link RetrieveTask}.
 * 
 * @author Luis.Poletto
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ AmazonDynamoDBClientBuilder.class })
public class RetrieveTaskTest {

    private static final DateFormat ISO8601FORMAT = new ISO8601DateFormat();

    @Mock
    private AmazonDynamoDB mockClient;

    /**
     * Every test we do here we are calling these mocked methods at least once,
     * thus make sense to have it on the setup method.
     */
    @Before
    public void setup() {
        mockStatic(AmazonDynamoDBClientBuilder.class);
        expect(AmazonDynamoDBClientBuilder.defaultClient()).andReturn(mockClient);
    }

    /**
     * We have to verify at least one call on
     * {@link AmazonDynamoDBClientBuilder}.
     */
    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testEmptyResults() {
        expect(mockClient.scan(EasyMock.anyObject())).andReturn(new ScanResult());
        replay(mockClient);
        replayAll();

        final RetrieveTask lambda = new RetrieveTask();
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(mockClient);
    }

    @Test
    public void testNonEmptyResults() {
        expect(mockClient.scan(EasyMock.anyObject())).andReturn(getUnorderedResults());
        replay(mockClient);
        replayAll();

        final RetrieveTask lambda = new RetrieveTask();
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(mockClient);
    }

    /**
     * This test has to fail until part 2 is implemented.
     */
    @Test
    public void testSortedResults() {
        expect(mockClient.scan(EasyMock.anyObject())).andReturn(getUnorderedResults());
        replay(mockClient);
        replayAll();

        final RetrieveTask lambda = new RetrieveTask();
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("first", result.get(0).getId());
        assertEquals("second", result.get(1).getId());
        assertEquals("third", result.get(2).getId());
        verify(mockClient);
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

}
