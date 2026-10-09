package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));

        // invalid tag names
        assertFalse(Tag.isValidTagName(""));
        assertFalse(Tag.isValidTagName("ExamPrep"));
        assertFalse(Tag.isValidTagName("exam prep"));
        assertFalse(Tag.isValidTagName("exam_prep"));
        assertFalse(Tag.isValidTagName("-exam-prep"));
        assertFalse(Tag.isValidTagName("exam-prep-"));
        assertFalse(Tag.isValidTagName("exam--prep"));
        assertFalse(Tag.isValidTagName("a".repeat(Tag.MAX_TAG_LENGTH + 1)));

        // valid tag names
        assertTrue(Tag.isValidTagName("exam-prep"));
        assertTrue(Tag.isValidTagName("primary-5"));
        assertTrue(Tag.isValidTagName("a".repeat(Tag.MAX_TAG_LENGTH)));
    }

}
