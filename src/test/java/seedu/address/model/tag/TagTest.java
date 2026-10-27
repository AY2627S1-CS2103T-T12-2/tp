package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertThrows(IllegalArgumentException.class, () -> new Tag("-exam-prep"));
        assertThrows(IllegalArgumentException.class, () -> new Tag("exam-prep-"));
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

    @Test
    public void isValidTagSearchKeyword_allowsHyphenBoundariesOnlyForPartialSearches() {
        assertTrue(Tag.isValidTagSearchKeyword("exam-prep"));
        assertTrue(Tag.isValidTagSearchKeyword("exam-"));
        assertTrue(Tag.isValidTagSearchKeyword("-prep"));
        assertTrue(Tag.isValidTagSearchKeyword("-exam-prep-"));
        assertTrue(Tag.isValidTagSearchKeyword("a".repeat(Tag.MAX_TAG_LENGTH)));

        assertFalse(Tag.isValidTagSearchKeyword(""));
        assertFalse(Tag.isValidTagSearchKeyword("-"));
        assertFalse(Tag.isValidTagSearchKeyword("--prep"));
        assertFalse(Tag.isValidTagSearchKeyword("exam--"));
        assertFalse(Tag.isValidTagSearchKeyword("exam--prep"));
        assertFalse(Tag.isValidTagSearchKeyword("Exam-"));
        assertFalse(Tag.isValidTagSearchKeyword("a".repeat(Tag.MAX_TAG_LENGTH + 1)));
    }

    @Test
    public void equals_hashCodeAndToString_handleBoundaryCases() {
        Tag tag = new Tag("exam-prep");

        assertTrue(tag.equals(tag));
        assertTrue(tag.equals(new Tag("exam-prep")));
        assertFalse(tag.equals(new Tag("friends")));
        assertFalse(tag.equals("exam-prep"));
        assertFalse(tag.equals(null));
        assertEquals(tag.hashCode(), new Tag("exam-prep").hashCode());
        assertEquals("[exam-prep]", tag.toString());
    }

}
