package com.lcpoletto.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
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
@RunWith(MockitoJUnitRunner.class)
public class RetrieveTaskTest {

    private static final DateFormat ISO8601FORMAT = new ISO8601DateFormat();

    @Mock
    private AmazonDynamoDB mockClient;

    private RetrieveTask lambda;

    @Before
    public void setup() {
        lambda = new RetrieveTask(mockClient);
    }

    @Test
    public void testEmptyResults() {
        when(mockClient.scan(any())).thenReturn(new ScanResult());
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(mockClient).scan(any());
    }

    @Test
    public void testNonEmptyResults() {
        when(mockClient.scan(any())).thenReturn(getUnorderedResults());
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(mockClient).scan(any());
    }

    /**
     * This test has to fail until part 2 is implemented.
     */
    @Test
    public void testSortedResults() {
        when(mockClient.scan(any())).thenReturn(getUnorderedResults());
        final List<Task> result = lambda.handleRequest(null);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("first", result.get(0).getId());
        assertEquals("second", result.get(1).getId());
        assertEquals("third", result.get(2).getId());
        verify(mockClient).scan(any());
    }

    /**
     * Helper method which will produce test data.
     * 
     * @return testable data
     */
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
