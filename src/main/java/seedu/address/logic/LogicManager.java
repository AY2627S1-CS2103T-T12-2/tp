package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.ClassListCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.PrivacyCommand;
import seedu.address.logic.commands.TimetableCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;
import seedu.address.model.timetable.TimetableDay;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    public static final String MESSAGE_TIMETABLE_READ_ONLY = "Timetable is read-only. Use \"list\" to return to "
            + "the student view. Only list, class list, privacy, help, and exit are available.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private final Clock clock;
    private CommandResult.ViewChange currentView = CommandResult.ViewChange.PERSON_LIST;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, Clock.systemDefaultZone());
    }

    /**
     * Constructs a {@code LogicManager} using the supplied clock for date-dependent views.
     */
    public LogicManager(Model model, Storage storage, Clock clock) {
        this.model = requireNonNull(model);
        this.storage = requireNonNull(storage);
        this.clock = requireNonNull(clock);
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        if (currentView == CommandResult.ViewChange.TIMETABLE && !isAllowedInTimetable(command)) {
            throw new CommandException(MESSAGE_TIMETABLE_READ_ONLY);
        }
        if (currentView == CommandResult.ViewChange.CLASS_LIST && !isAllowedInClassList(command)) {
            throw new CommandException("Class list is read-only. Use \"list\" to return to the student view. "
                    + "Only list, timetable, class list, privacy, help, and exit are available.");
        }
        ReadOnlyAddressBook addressBookBeforeCommand = new AddressBook(model.getAddressBook());
        GuiSettings guiSettingsBeforeCommand = model.getGuiSettings();
        boolean privacyModeBeforeCommand = model.isPrivacyModeEnabled();
        Model.FilteredPersonListState filteredPersonListStateBeforeCommand = model.getFilteredPersonListState();

        try {
            commandResult = command.execute(model);
            storage.saveAddressBook(model.getAddressBook());
            if (command instanceof PrivacyCommand) {
                storage.saveUserPrefs(model.getUserPrefs());
            }
        } catch (CommandException e) {
            restoreModelState(addressBookBeforeCommand, guiSettingsBeforeCommand, privacyModeBeforeCommand,
                    filteredPersonListStateBeforeCommand);
            throw e;
        } catch (AccessDeniedException e) {
            restoreModelState(addressBookBeforeCommand, guiSettingsBeforeCommand, privacyModeBeforeCommand,
                    filteredPersonListStateBeforeCommand);
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            restoreModelState(addressBookBeforeCommand, guiSettingsBeforeCommand, privacyModeBeforeCommand,
                    filteredPersonListStateBeforeCommand);
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        if (commandResult.getViewChange() != CommandResult.ViewChange.NONE) {
            currentView = commandResult.getViewChange();
        }

        return commandResult;
    }

    /** Restores in-memory state when command execution or persistence fails. */
    private void restoreModelState(ReadOnlyAddressBook addressBook, GuiSettings guiSettings,
            boolean privacyModeEnabled, Model.FilteredPersonListState filteredPersonListState) {
        model.setAddressBook(addressBook);
        model.setGuiSettings(guiSettings);
        model.setPrivacyModeEnabled(privacyModeEnabled);
        model.restoreFilteredPersonListState(filteredPersonListState);
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public LocalDate getCurrentDate() {
        return LocalDate.now(clock);
    }

    @Override
    public List<TimetableDay> getTimetable(LocalDate anchorDate) {
        requireNonNull(anchorDate);
        return model.getTimetable(anchorDate);
    }

    @Override
    public ObservableList<TuitionClass> getTuitionClassList() {
        return model.getTuitionClassList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }

    @Override
    public boolean isPrivacyModeEnabled() {
        return model.isPrivacyModeEnabled();
    }

    @Override
    public void setPrivacyModeEnabled(boolean privacyModeEnabled) {
        model.setPrivacyModeEnabled(privacyModeEnabled);
    }

    private boolean isAllowedInTimetable(Command command) {
        return command instanceof ListCommand || command instanceof ClassListCommand
                || command instanceof PrivacyCommand || command instanceof HelpCommand
                || command instanceof ExitCommand;
    }

    private boolean isAllowedInClassList(Command command) {
        return command instanceof ListCommand || command instanceof TimetableCommand
                || command instanceof ClassListCommand || command instanceof PrivacyCommand
                || command instanceof HelpCommand
                || command instanceof ExitCommand;
    }
}
