/**
 * 
 */
package com.lcpoletto;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.lcpoletto.tasks.model.Task;

/**
 * Class which holds helper and utility methods that might be used by multiple
 * others.
 * 
 * TODO: maybe there is a better way to do this with java 8? default methods on
 * interfaces?
 * 
 * @author Luis Carlos Poletto
 *
 */
public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class);

    private static final String DEFAULT_EMAIL_FROM = "noreply@lcpoletto.com";

    /**
     * Avoids instantiation.
     */
    private Utils() {
    }

    /**
     * Helper method which will try to read configuration from environment
     * variables.
     * 
     * @return configured email source or default value
     */
    public static String getMailFrom() {
        final String result = System.getenv().getOrDefault("TASKS_MAIL_FROM", DEFAULT_EMAIL_FROM);
        logger.trace(String.format("TASKS_MAIL_FROM: %s", result));
        return result;
    }

    /**
     * Implementation of merge sort which garantees O(n*log(n)) execution time.
     * 
     * <p>
     * <strong>Disclaimer:</strong> this implementation is here for the sake of
     * completeness, in the real world I think it would be enough to rely on JVM
     * {@link Arrays#sort(Object[])} as it states on it's documentation:
     * </p>
     * 
     * <p>
     * <code>This implementation is a stable, adaptive,
     * iterative mergesort that requires far fewer than n lg(n) comparisons
     * when the input array is partially sorted, while offering the
     * performance of a traditional mergesort when the input array is
     * randomly ordered.</code>
     * </p>
     * 
     * @param array
     *            array to be sorted
     * @return sorted array
     */
    public static void mergeSort(Task[] array) {
        // if the array is null or just have 1 element it's already sorted
        if (array != null && array.length > 1) {
            // first we split the array in half
            Task[] left = new Task[array.length / 2];
            Task[] right = new Task[array.length - left.length];
            System.arraycopy(array, 0, left, 0, left.length);
            System.arraycopy(array, left.length, right, 0, right.length);

            // now we sort each half of the array
            mergeSort(left);
            mergeSort(right);

            // merge both sorted halves over the received array
            merge(left, right, array);
        }
    }

    private static void merge(Task[] left, Task[] right, Task[] result) {
        int leftIndex = 0;
        int rightIndex = 0;
        int resultIndex = 0;

        while (leftIndex < left.length && rightIndex < right.length) {
            if (left[leftIndex].compareTo(right[rightIndex]) < 0) {
                result[resultIndex] = left[leftIndex];
                leftIndex++;
            } else {
                result[resultIndex] = right[rightIndex];
                rightIndex++;
            }
            resultIndex++;
        }

        System.arraycopy(left, leftIndex, result, resultIndex, left.length - leftIndex);
        System.arraycopy(right, rightIndex, result, resultIndex, right.length - rightIndex);
    }

}
