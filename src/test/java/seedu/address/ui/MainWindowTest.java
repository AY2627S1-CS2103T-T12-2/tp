package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.timetable.TimetableDay;
import seedu.address.model.timetable.TimetableEntry;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainWindowTest {

    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(initialize);
        }
        if (!ready.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX did not start within 10 seconds");
        }
    }

    @Test
    public void privacyCommands_updateExistingCardsAndStatusIndicator() throws Exception {
        runOnFxThread(() -> {
            ClassDetails classDetails = new ClassDetails("https://example.com/math", "Room 2", "Bring notes");
            Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                    new LessonTiming("Monday1800-1930")).withDetails(classDetails);
            Person person = new PersonBuilder().withPhone("91234567")
                    .withEmail("student@example.com").withAddress("123 Privacy Street")
                    .withLessons(lesson).build();
            TestLogic logic = new TestLogic(person);
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, Path.of("data", "addressbook.json"));
            mainWindow.fillInnerParts();
            stage.show();

            PersonListPanel.PersonListViewCell cell = createPersonCell(mainWindow, person);
            Label phone = getCardLabel(cell, "phone");
            Label email = getCardLabel(cell, "email");
            Label address = getCardLabel(cell, "address");
            Label classDetailsLabel = (Label) cell.getGraphic().lookup(".enrolled-class-details");
            Label privacyStatus = (Label) stage.getScene().lookup("#privacyStatus");
            TextField commandTextField = (TextField) stage.getScene().lookup("#commandTextField");

            assertNotNull(privacyStatus);
            assertNotNull(commandTextField);
            assertFalse(privacyStatus.isVisible());
            assertEquals("91234567", phone.getText());
            assertEquals("student@example.com", email.getText());
            assertEquals("123 Privacy Street", address.getText());

            execute(commandTextField, "privacy on");

            assertTrue(logic.isPrivacyModeEnabled());
            assertTrue(privacyStatus.isVisible());
            assertEquals("Phone: " + PersonCard.PRIVACY_MASK, phone.getText());
            assertEquals("Email: " + PersonCard.PRIVACY_MASK, email.getText());
            assertEquals("Address: " + PersonCard.PRIVACY_MASK, address.getText());
            assertEquals("Link: " + PersonCard.PRIVACY_MASK + " | Location: " + PersonCard.PRIVACY_MASK
                    + " | Notes: Bring notes", classDetailsLabel.getText());

            execute(commandTextField, "timetable");
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            Label timetableDetails = (Label) stage.getScene().lookup("#details");
            assertEquals("Mode: Online | Link: " + PersonCard.PRIVACY_MASK + " | Location: "
                    + PersonCard.PRIVACY_MASK + " | Notes: Bring notes", timetableDetails.getText());

            execute(commandTextField, "privacy off");

            assertFalse(logic.isPrivacyModeEnabled());
            assertFalse(privacyStatus.isVisible());
            assertEquals("91234567", phone.getText());
            assertEquals("student@example.com", email.getText());
            assertEquals("123 Privacy Street", address.getText());
            assertEquals(classDetails.toString(), timetableDetails.getText());

            execute(commandTextField, "class list");
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            Text classListDetails = (Text) stage.getScene().lookup("#details");
            execute(commandTextField, "privacy on");
            assertEquals("Mode: Online | Link: " + PersonCard.PRIVACY_MASK + " | Location: "
                    + PersonCard.PRIVACY_MASK + " | Notes: Bring notes", classListDetails.getText());

            stage.close();
        });
    }

    @Test
    public void rejectedCommands_displayFeedbackAndRetainInputWithoutLoggingPrivateValues() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Private Student").build();
            ModelManager model = new ModelManager();
            model.addPerson(person);
            Path dataFile = temporaryFolder.resolve("students.json");
            Logic logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataFile),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
            Stage stage = new Stage();
            MainWindow mainWindow = new MainWindow(stage, logic, dataFile);
            Logger logger = LogsCenter.getLogger(MainWindow.class);
            Level originalLevel = logger.getLevel();
            List<String> messages = new ArrayList<>();
            Handler capture = new Handler() {
                @Override
                public void publish(LogRecord record) {
                    messages.add(record.getMessage());
                }

                @Override
                public void flush() {
                }

                @Override
                public void close() {
                }
            };
            logger.addHandler(capture);
            logger.setLevel(Level.ALL);
            try {
                mainWindow.fillInnerParts();
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                String duplicate = "add n/Private Student p/98887777 e/private@example.com a/Secret Street";
                String invalidGuardian = duplicate + " gp/private-guardian-phone";

                execute(input, invalidGuardian);
                assertEquals("Guardian phone: " + Phone.MESSAGE_CONSTRAINTS, feedback.getText());
                assertEquals(invalidGuardian, input.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

                execute(input, duplicate);
                assertEquals(AddCommand.MESSAGE_DUPLICATE_PERSON, feedback.getText());
                assertEquals(duplicate, input.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals(List.of(person), model.getAddressBook().getPersonList());
                assertEquals(List.of("Command rejected; displaying error feedback.",
                        "Command rejected; displaying error feedback."), messages);

                execute(input, "list");
                assertEquals("", input.getText());
                assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertEquals("Listed all students.", feedback.getText());
                for (String secret : List.of("Private Student", "98887777", "private@example.com", "Secret Street",
                        "private-guardian-phone")) {
                    assertFalse(String.join("\n", messages).contains(secret));
                }
            } finally {
                logger.removeHandler(capture);
                logger.setLevel(originalLevel);
                stage.close();
            }
        });
    }

    private static PersonListPanel.PersonListViewCell createPersonCell(MainWindow mainWindow, Person person) {
        @SuppressWarnings("unchecked")
        ListView<Person> listView = (ListView<Person>) mainWindow.getPersonListPanel()
                .getRoot().lookup("#personListView");
        PersonListPanel.PersonListViewCell cell =
                (PersonListPanel.PersonListViewCell) listView.getCellFactory().call(listView);
        cell.updateItem(person, false);
        return cell;
    }

    private static Label getCardLabel(PersonListPanel.PersonListViewCell cell, String id) {
        Label label = (Label) cell.getGraphic().lookup("#" + id);
        assertNotNull(label);
        return label;
    }

    private static void execute(TextField commandTextField, String command) {
        commandTextField.setText(command);
        commandTextField.fireEvent(new ActionEvent());
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private static class TestLogic implements Logic {
        private final ObservableList<Person> persons;
        private boolean privacyModeEnabled;
        private GuiSettings guiSettings = new GuiSettings();

        TestLogic(Person person) {
            persons = FXCollections.observableArrayList(person);
        }

        @Override
        public CommandResult execute(String commandText) throws ParseException {
            switch (commandText) {
                case "privacy on" -> privacyModeEnabled = true;
                case "privacy off" -> privacyModeEnabled = false;
                case "timetable" -> {
                    return new CommandResult("Timetable opened.", CommandResult.ViewChange.TIMETABLE);
                }
                case "class list" -> {
                    return new CommandResult("Classes opened.", CommandResult.ViewChange.CLASS_LIST);
                }
                default -> throw new ParseException("Unsupported test command");
            }
            return new CommandResult("Privacy mode updated.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return persons;
        }

        @Override
        public LocalDate getCurrentDate() {
            return LocalDate.of(2026, 10, 21);
        }

        @Override
        public List<TimetableDay> getTimetable(LocalDate anchorDate) {
            Person person = persons.getFirst();
            return List.of(new TimetableDay(anchorDate, List.of(
                    new TimetableEntry(person.getLessons().iterator().next(), List.of(person)))));
        }

        @Override
        public ObservableList<TuitionClass> getTuitionClassList() {
            Lesson lesson = persons.getFirst().getLessons().iterator().next();
            return FXCollections.observableArrayList(new TuitionClass(new ClassId("C001"), lesson.getSubject(),
                    lesson.getCost(), lesson.getSchedule(), lesson.getDetails()));
        }

        @Override
        public GuiSettings getGuiSettings() {
            return guiSettings;
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            this.guiSettings = guiSettings;
        }

        @Override
        public boolean isPrivacyModeEnabled() {
            return privacyModeEnabled;
        }

        @Override
        public void setPrivacyModeEnabled(boolean privacyModeEnabled) {
            this.privacyModeEnabled = privacyModeEnabled;
        }
    }
}
