package seedu.address.logic;

import java.time.LocalDate;
import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;
import seedu.address.model.timetable.TimetableDay;

/**
 * API of the Logic component
 */
public interface Logic {
    /**
     * Executes the command and returns the result.
     * @param commandText The command as entered by the user.
     * @return the result of the command execution.
     * @throws CommandException If an error occurs during command execution.
     * @throws ParseException If an error occurs during parsing.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /** Returns an unmodifiable view of the filtered list of persons */
    ObservableList<Person> getFilteredPersonList();

    /** Returns the current local date according to the application clock. */
    LocalDate getCurrentDate();

    /** Returns the complete timetable week containing {@code anchorDate}. */
    List<TimetableDay> getTimetable(LocalDate anchorDate);

    /** Returns all independently stored tuition classes. */
    default ObservableList<TuitionClass> getTuitionClassList() {
        return javafx.collections.FXCollections.emptyObservableList();
    }

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Set the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /** Returns whether privacy mode is enabled. */
    boolean isPrivacyModeEnabled();

    /** Enables or disables privacy mode. */
    void setPrivacyModeEnabled(boolean privacyModeEnabled);
}
