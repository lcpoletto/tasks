package com.lcpoletto.notes.model;

import org.junit.Test;

import com.lcpoletto.exceptions.ValidationException;

/**
 * Test fixture for {@link Note}.
 * 
 * @author Luis Carlos Poletto
 *
 */
public class NoteTest {

    @Test(expected = ValidationException.class)
    public void testEmpty() {
        new Note().validate(false);
    }

    @Test(expected = ValidationException.class)
    public void testWithId() {
        final Note note = populateNote();
        note.validate(false);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutAllowChange() {
        final Note note = populateNote();
        note.setAllowChange(null);
        note.validate(false);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutContent() {
        final Note note = populateNote();
        note.setContent(null);
        note.validate(false);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutOwner() {
        final Note note = populateNote();
        note.setOwner(null);
        note.validate(false);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutRecipient() {
        final Note note = populateNote();
        note.setRecipient(null);
        note.validate(false);
    }
    
    @Test(expected = ValidationException.class)
    public void testWithoutId() {
        final Note note = new Note();
        note.setContent("content");
        note.setUpdatedBy("updatedBy");
        note.validate(true);
    }

    @Test(expected = ValidationException.class)
    public void testWithoutUpdatedBy() {
        final Note note = new Note();
        note.setId("id");
        note.setContent("content");
        note.validate(true);
    }

    /**
     * Creates a note with all fields populated.
     * 
     * @return populated note
     */
    private Note populateNote() {
        final Note result = new Note();
        result.setAllowChange(Boolean.FALSE);
        result.setContent("content");
        result.setId("id");
        result.setOwner("owner");
        result.setRecipient("recipient");
        result.setUpdatedBy("updatedBy");
        return result;
    }

}
