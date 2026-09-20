package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void constructor_emptyRemark_success() {
        assertEquals("", new Remark("").value);
    }

    @Test
    public void equals() {
        Remark remark = new Remark("Likes to swim");

        assertEquals(remark, new Remark("Likes to swim"));
        assertNotEquals(remark, new Remark("Likes to run"));
        assertNotEquals(remark, null);
    }

    @Test
    public void toStringMethod() {
        assertEquals("Likes to swim", new Remark("Likes to swim").toString());
    }
}
