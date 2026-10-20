package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class UserPrefsTest {

    @Test
    public void constructor_defaultsPrivacyModeToDisabled() {
        assertFalse(new UserPrefs().isPrivacyModeEnabled());
    }

    @Test
    public void copyConstructor_copiesPrivacyMode() {
        UserPrefs original = new UserPrefs();
        original.setPrivacyModeEnabled(true);

        assertTrue(new UserPrefs(original).isPrivacyModeEnabled());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        UserPrefs userPref = new UserPrefs();
        assertThrows(NullPointerException.class, () -> userPref.setGuiSettings(null));
    }

    @Test
    public void setPrivacyModeEnabled_validValue_setsPrivacyMode() {
        UserPrefs userPrefs = new UserPrefs();

        userPrefs.setPrivacyModeEnabled(true);
        assertTrue(userPrefs.isPrivacyModeEnabled());

        userPrefs.setPrivacyModeEnabled(false);
        assertFalse(userPrefs.isPrivacyModeEnabled());
    }

    @Test
    public void equalsAndHashCode_privacyModeIncluded() {
        UserPrefs first = new UserPrefs();
        UserPrefs sameValues = new UserPrefs();

        assertEquals(first, first);
        assertEquals(first, sameValues);
        assertEquals(first.hashCode(), sameValues.hashCode());

        sameValues.setPrivacyModeEnabled(true);

        assertNotEquals(first, sameValues);
        assertNotEquals(first, null);
        assertNotEquals(first, new Object());
    }

}
