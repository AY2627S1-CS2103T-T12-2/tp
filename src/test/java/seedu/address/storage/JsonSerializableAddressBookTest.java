package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void jsonRoundTrip_lessonAndEnrolments_success() throws Exception {
        AddressBook original = TypicalPersons.getTypicalAddressBook();
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person firstPerson = original.getPersonList().get(0);
        original.setPerson(firstPerson, new PersonBuilder(firstPerson).withLessons(lesson).build());

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));
        JsonSerializableAddressBook restoredData = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        assertEquals(original, restoredData.toModelType());
    }

    @Test
    public void toModelType_sameClassWithDifferentCosts_throwsIllegalValueException() {
        AddressBook addressBook = TypicalPersons.getTypicalAddressBook();
        Lesson firstCost = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson secondCost = new Lesson(new Subject("Math"), new Cost("40"),
                new LessonTiming("Monday1800-1930"));
        Person firstPerson = addressBook.getPersonList().get(0);
        Person secondPerson = addressBook.getPersonList().get(1);
        addressBook.setPerson(firstPerson, new PersonBuilder(firstPerson).withLessons(firstCost).build());
        addressBook.setPerson(secondPerson, new PersonBuilder(secondPerson).withLessons(secondCost).build());

        JsonSerializableAddressBook data = new JsonSerializableAddressBook(addressBook);
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_CLASS_COST_CONFLICT,
                data::toModelType);
    }

}
