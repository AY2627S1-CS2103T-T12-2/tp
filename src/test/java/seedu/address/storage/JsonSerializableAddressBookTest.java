package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
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
    public void toModelType_legacyPersonsOnlyConstructor_success() throws Exception {
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of());

        AddressBook restored = data.toModelType();

        assertEquals(List.of(), restored.getPersonList());
        assertEquals(List.of(), restored.getTuitionClassList());
        assertEquals(1, restored.getNextClassSequence());
    }

    @Test
    public void jsonRoundTrip_lessonAndEnrolments_success() throws Exception {
        AddressBook original = TypicalPersons.getTypicalAddressBook();
        ClassId classId = original.allocateClassId();
        TuitionClass tuitionClass = new TuitionClass(classId, new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        original.addTuitionClass(tuitionClass);
        Lesson lesson = new Enrollment(tuitionClass, PaymentStatus.PAID);
        Person firstPerson = original.getPersonList().get(0);
        original.setPerson(firstPerson, new PersonBuilder(firstPerson).withLessons(lesson).build());

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));
        JsonSerializableAddressBook restoredData = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        assertEquals(original, restoredData.toModelType());
    }

    @Test
    public void jsonRoundTrip_independentClassAndNextId_success() throws Exception {
        AddressBook original = new AddressBook();
        ClassId firstId = original.allocateClassId();
        TuitionClass tuitionClass = new TuitionClass(firstId, new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        original.addTuitionClass(tuitionClass);

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();

        assertEquals(java.util.List.of(tuitionClass), restored.getTuitionClassList());
        assertEquals(new ClassId("C002"), restored.allocateClassId());
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

    @Test
    public void toModelType_onePersonHasSameClassWithDifferentCosts_throwsIllegalValueException() {
        AddressBook addressBook = TypicalPersons.getTypicalAddressBook();
        Lesson firstCost = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson secondCost = new Lesson(new Subject("Math"), new Cost("40"),
                new LessonTiming("Monday1800-1930"));
        Person firstPerson = addressBook.getPersonList().get(0);
        addressBook.setPerson(firstPerson,
                new PersonBuilder(firstPerson).withLessons(firstCost, secondCost).build());

        JsonSerializableAddressBook data = new JsonSerializableAddressBook(addressBook);
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_CLASS_COST_CONFLICT,
                data::toModelType);
    }

    @Test
    public void toModelType_onePersonHasDuplicateClassWithDifferentStatuses_throwsIllegalValueException() {
        AddressBook addressBook = TypicalPersons.getTypicalAddressBook();
        Lesson unpaid = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson paid = unpaid.withPaymentStatus(PaymentStatus.PAID);
        Person firstPerson = addressBook.getPersonList().get(0);
        addressBook.setPerson(firstPerson,
                new PersonBuilder(firstPerson).withLessons(unpaid, paid).build());

        JsonSerializableAddressBook data = new JsonSerializableAddressBook(addressBook);
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_ENROLMENT,
                data::toModelType);
    }

    @Test
    public void toModelType_duplicateStoredClassId_throwsIllegalValueException() {
        JsonAdaptedTuitionClass first = new JsonAdaptedTuitionClass("C001", "Math", "30",
                "Monday1800-1930");
        JsonAdaptedTuitionClass second = new JsonAdaptedTuitionClass("C001", "Physics", "40",
                "Tuesday1800-1930");
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of(first, second), 2);

        assertThrows(IllegalValueException.class, data::toModelType);
    }

    @Test
    public void toModelType_duplicateStoredClassDetails_throwsIllegalValueException() {
        JsonAdaptedTuitionClass first = new JsonAdaptedTuitionClass("C001", "Math", "30",
                "Monday1800-1930");
        JsonAdaptedTuitionClass second = new JsonAdaptedTuitionClass("C002", "math", "30.00",
                "Monday1800-1930");
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(), List.of(first, second), 3);

        assertThrows(IllegalValueException.class, data::toModelType);
    }

    @Test
    public void toModelType_enrolmentWithStoredClassId_linksToStoredClass() throws Exception {
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson lesson = new Lesson(new ClassId("C001"), new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"), PaymentStatus.PAID);
        Person person = new PersonBuilder().withLessons(lesson).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(person)),
                List.of(new JsonAdaptedTuitionClass(tuitionClass)), 8);

        AddressBook restored = data.toModelType();

        Lesson restoredLesson = restored.getPersonList().get(0).getLessons().iterator().next();
        assertEquals(new ClassId("C001"), restoredLesson.getClassId());
        assertEquals(PaymentStatus.PAID, restoredLesson.getPaymentStatus());
        assertEquals(8, restored.getNextClassSequence());
    }

    @Test
    public void toModelType_legacyEnrolmentMatchingStoredDetails_linksToStoredClass() throws Exception {
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C004"), new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person person = new PersonBuilder().withLessons(new Lesson(new Subject("math"), new Cost("30.00"),
                new LessonTiming("Monday1800-1930"))).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(person)),
                List.of(new JsonAdaptedTuitionClass(tuitionClass)), null);

        AddressBook restored = data.toModelType();

        assertEquals(new ClassId("C004"),
                restored.getPersonList().get(0).getLessons().iterator().next().getClassId());
        assertEquals(5, restored.getNextClassSequence());
    }

    @Test
    public void toModelType_enrolmentForUnknownClass_createsClassUsingPersistedId() throws Exception {
        Lesson lesson = new Lesson(new ClassId("C007"), new Subject("Chemistry"), new Cost("45"),
                new LessonTiming("Friday1600-1730"), PaymentStatus.UNPAID);
        Person person = new PersonBuilder().withLessons(lesson).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(person)), null, 2);

        AddressBook restored = data.toModelType();

        assertEquals(new ClassId("C007"), restored.getTuitionClassList().get(0).getClassId());
        assertEquals(8, restored.getNextClassSequence());
    }

    @Test
    public void toModelType_storedClassAndEnrolmentCostsDiffer_throwsIllegalValueException() {
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson lesson = new Lesson(new ClassId("C001"), new Subject("Math"), new Cost("40"),
                new LessonTiming("Monday1800-1930"), PaymentStatus.UNPAID);
        Person person = new PersonBuilder().withLessons(lesson).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(person)),
                List.of(new JsonAdaptedTuitionClass(tuitionClass)), null);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_CLASS_COST_CONFLICT,
                data::toModelType);
    }

}
