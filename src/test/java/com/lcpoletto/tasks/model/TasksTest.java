/**
 * 
 */
package com.lcpoletto.tasks.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.lcpoletto.Utils;
import com.lcpoletto.exceptions.ValidationException;

/**
 * Test fixture for {@link Task}.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class TasksTest {

    private Task[] tasks;

    @Before
    public void setup() {
        tasks = populateUnorderedTasks();
    }

    @Test
    public void testComparable() {
        Arrays.sort(tasks);
        assertOrdered();
    }

    @Test
    public void testMergeSort() {
        Utils.mergeSort(tasks);
        assertOrdered();
    }

    @Test(expected = ValidationException.class)
    public void testEmpty() {
        new Task().validate(true);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutPriority() {
        final Task task = new Task();
        task.setDescription("Test description.");
        task.validate(true);
    }

    @Test(expected = ValidationException.class)
    public void testLowerBoundPriority() {
        final Task task = new Task();
        task.setDescription("Test description.");
        task.setPriority(-1);
        task.validate(true);
    }

    @Test(expected = ValidationException.class)
    public void testUpperBoundPriority() {
        final Task task = new Task();
        task.setDescription("Test description.");
        task.setPriority(10);
        task.validate(true);
    }

    @Test(expected = ValidationException.class)
    public void testUpdateWithoutId() {
        final Task task = new Task();
        task.setDescription("Test description.");
        task.setPriority(8);
        task.validate(true);
    }

    @Test
    public void testValidInsert() {
        final Task task = new Task();
        task.setDescription("Test description.");
        task.setPriority(8);
        try {
            task.validate(false);
        } catch (ValidationException e) {
            fail();
        }
    }

    @Test
    public void testValidUpdate() {
        final Task task = new Task();
        task.setDescription("Test description.");
        task.setPriority(8);
        task.setId("id");
        try {
            task.validate(false);
        } catch (ValidationException e) {
            fail();
        }
    }

    /**
     * Helper function to assert all items are ordered.
     */
    private void assertOrdered() {
        for (int i = 0; i < tasks.length; i++) {
            assertEquals("" + i, tasks[i].getId());
        }
    }

    /**
     * Produces an unordered array where the task ids should match their final
     * position on the array after sorting.
     * 
     * @return unordered array of tasks.
     */
    private Task[] populateUnorderedTasks() {
        final Date commonDate = LocalDate.now().minusDays(1).toDate();
        final Task[] result = new Task[5];
        Task task = new Task();
        task.setId("4");
        task.setCompleted(commonDate);
        task.setPriority(3);
        result[0] = task;

        task = new Task();
        task.setId("3");
        task.setCompleted(commonDate);
        task.setPriority(0);
        result[1] = task;

        task = new Task();
        task.setId("1");
        task.setPriority(4);
        result[2] = task;

        task = new Task();
        task.setId("0");
        task.setPriority(0);
        result[3] = task;

        task = new Task();
        task.setId("2");
        task.setPriority(10);
        task.setCompleted(new Date());
        result[4] = task;

        return result;
    }
}
