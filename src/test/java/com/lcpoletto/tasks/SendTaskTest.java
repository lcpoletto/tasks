package com.lcpoletto.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

/**
 * Test fixture for {@link SendTask}.
 * 
 * @author Luis Carlos Poletto
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SendTaskTest {

    @Mock
    private AmazonDynamoDB mockDynamo;

    @Mock
    private AmazonSimpleEmailService mockSimpleEmailService;

    private SendTask lambda;

    @Before
    public void setup() {
        lambda = new SendTask(mockDynamo, mockSimpleEmailService);
    }

    @Test
    public void testEmptyResults() {
        when(mockDynamo.scan(any())).thenReturn(new ScanResult());
        final String result = lambda.handleRequest(null, null);
        assertNotNull(result);
        assertEquals("SUCCESS", result);
        verify(mockDynamo).scan(any());
    }

    @Test
    public void testWithResults() {
        when(mockDynamo.scan(any())).thenReturn(getUnorderedResults());
        when(mockSimpleEmailService.sendEmail(any())).thenReturn(new SendEmailResult());
        final String result = lambda.handleRequest(null, null);
        assertNotNull(result);
        assertEquals("SUCCESS", result);
        verify(mockDynamo).scan(any());
        // we expect 2 calls as we have only 2 different users on the list
        verify(mockSimpleEmailService, times(2)).sendEmail(any());
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
        item.put("user", new AttributeValue("first@first.com"));
        item.put("description", new AttributeValue("third description"));
        item.put("priority", new AttributeValue().withN("5"));
        items.add(item);

        item = new HashMap<>();
        item.put("id", new AttributeValue("first"));
        item.put("user", new AttributeValue("first@first.com"));
        item.put("description", new AttributeValue("first description"));
        item.put("priority", new AttributeValue().withN("5"));
        items.add(item);

        result.setItems(items);
        return result;
    }

}
