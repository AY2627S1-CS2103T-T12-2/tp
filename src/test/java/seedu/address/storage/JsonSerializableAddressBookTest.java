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
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.EnrollmentId;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
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
        Person firstPerson = original.getPersonList().get(0);
        Enrollment enrollment = new Enrollment(firstPerson.getStudentId(), tuitionClass,
                tuitionClass.getCost(), PaymentStatus.PAID);
        original.setPerson(firstPerson, new PersonBuilder(firstPerson).withEnrollments(enrollment).build());

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(original));
        JsonSerializableAddressBook restoredData = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);

        assertEquals(original, restoredData.toModelType());
    }

    @Test
    public void toModelType_mixedAssociationAndLegacyLesson_migratesBoth() throws Exception {
        String studentId = "S-00000000-0000-0000-0000-000000000001";
        JsonAdaptedLesson association = new JsonAdaptedLesson(null, "35", null, "PAID",
                null, null, null, null, "C001", null, null,
                "E-00000000-0000-0000-0000-000000000001", studentId);
        JsonAdaptedLesson legacyLesson = new JsonAdaptedLesson("Physics", "40",
                "Tuesday1800-1930", "UNPAID", "https://example.com/physics", "", "", "ONLINE");
        JsonAdaptedPerson student = new JsonAdaptedPerson("Alice", "91234567",
                "alice@example.com", "One Street", List.of(), List.of(association, legacyLesson),
                null, null, null, null, null, null, false, studentId);
        JsonAdaptedTuitionClass tuitionClass = new JsonAdaptedTuitionClass(
                "C001", "Math", "30", "Monday1800-1930");
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(student), List.of(tuitionClass), 2);

        AddressBook restored = data.toModelType();

        assertEquals(2, restored.getPersonList().getFirst().getEnrollments().size());
        assertEquals(2, restored.getTuitionClassList().size());
    }

    @Test
    public void jsonRoundTrip_oneOffLesson_success() throws Exception {
        AddressBook original = TypicalPersons.getTypicalAddressBook();
        ClassId classId = original.allocateClassId();
        TuitionClass tuitionClass = new TuitionClass(classId, new Subject("Math"), new Cost("30"),
                new OneOffLessonSchedule("2026-10-20T1800-1930"));
        original.addTuitionClass(tuitionClass);
        Person firstPerson = original.getPersonList().get(0);
        Enrollment enrollment = new Enrollment(firstPerson.getStudentId(), tuitionClass);
        original.setPerson(firstPerson, new PersonBuilder(firstPerson).withEnrollments(enrollment).build());

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
    public void toModelType_sameClassWithDifferentCosts_preservesAgreedRates() throws Exception {
        AddressBook addressBook = TypicalPersons.getTypicalAddressBook();
        Lesson firstCost = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson secondCost = new Lesson(new Subject("Math"), new Cost("40"),
                new LessonTiming("Monday1800-1930"));
        Person firstPerson = addressBook.getPersonList().get(0);
        Person secondPerson = addressBook.getPersonList().get(1);
        addressBook.setPerson(firstPerson, new PersonBuilder(firstPerson).withLessons(firstCost).build());
        addressBook.setPerson(secondPerson, new PersonBuilder(secondPerson).withLessons(secondCost).build());

        AddressBook restored = new JsonSerializableAddressBook(addressBook).toModelType();

        assertEquals(new Cost("30"), restored.getPersonList().get(0)
                .getEnrollments().iterator().next().getAgreedRate());
        assertEquals(new Cost("40"), restored.getPersonList().get(1)
                .getEnrollments().iterator().next().getAgreedRate());
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
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_ENROLMENT,
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
    public void toModelType_storedClassAndEnrolmentCostsDiffer_preservesAgreedRate() throws Exception {
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson lesson = new Lesson(new ClassId("C001"), new Subject("Math"), new Cost("40"),
                new LessonTiming("Monday1800-1930"), PaymentStatus.UNPAID);
        Person person = new PersonBuilder().withLessons(lesson).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(new JsonAdaptedPerson(person)),
                List.of(new JsonAdaptedTuitionClass(tuitionClass)), null);

        AddressBook restored = data.toModelType();

        assertEquals(new Cost("30"), restored.getTuitionClassList().getFirst().getCost());
        assertEquals(new Cost("40"), restored.getPersonList().getFirst()
                .getEnrollments().iterator().next().getAgreedRate());
    }

    @Test
    public void toModelType_associationReferencesMissingClass_throwsIllegalValueException() {
        AddressBook addressBook = new AddressBook();
        Person person = new PersonBuilder().build();
        Enrollment enrollment = new Enrollment(
                new EnrollmentId("E-00000000-0000-0000-0000-000000000001"),
                person.getStudentId(), new ClassId("C999"), new Cost("30"), PaymentStatus.UNPAID);
        addressBook.addPerson(person.withEnrollments(java.util.Set.of(enrollment)));

        JsonSerializableAddressBook data = new JsonSerializableAddressBook(addressBook);

        assertThrows(IllegalValueException.class, data::toModelType);
    }

    @Test
    public void toModelType_duplicateEnrollmentIds_throwsIllegalValueException() {
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"),
                new Cost("30"), new LessonTiming("Monday1800-1930"));
        String enrollmentId = "E-00000000-0000-0000-0000-000000000001";
        String firstStudentId = "S-00000000-0000-0000-0000-000000000001";
        String secondStudentId = "S-00000000-0000-0000-0000-000000000002";
        JsonAdaptedLesson firstEnrollment = new JsonAdaptedLesson(null, "30", null, "UNPAID",
                null, null, null, null, "C001", null, null, enrollmentId, firstStudentId);
        JsonAdaptedLesson secondEnrollment = new JsonAdaptedLesson(null, "30", null, "UNPAID",
                null, null, null, null, "C001", null, null, enrollmentId, secondStudentId);
        JsonAdaptedPerson first = new JsonAdaptedPerson("Alice", "91234567", "alice@example.com", "One Street",
                List.of(), List.of(firstEnrollment), null, null, null, null, null, null, false, firstStudentId);
        JsonAdaptedPerson second = new JsonAdaptedPerson("Bob", "92345678", "bob@example.com", "Two Street",
                List.of(), List.of(secondEnrollment), null, null, null, null, null, null, false, secondStudentId);
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(List.of(first, second),
                List.of(new JsonAdaptedTuitionClass(tuitionClass)), 2);

        assertThrows(IllegalValueException.class, data::toModelType);
    }

    @Test
    public void toModelType_duplicateStudentIds_throwsIllegalValueException() {
        String duplicateStudentId = "S-00000000-0000-0000-0000-000000000001";
        JsonAdaptedPerson alice = new JsonAdaptedPerson("Alice", "91234567",
                "alice@example.com", "One Street", List.of(), null, null, null, null,
                null, null, null, false, duplicateStudentId);
        JsonAdaptedPerson bob = new JsonAdaptedPerson("Bob", "92345678",
                "bob@example.com", "Two Street", List.of(), null, null, null, null,
                null, null, null, false, duplicateStudentId);
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(alice, bob), List.of(), 1);

        assertThrows(IllegalValueException.class,
                JsonSerializableAddressBook.MESSAGE_DUPLICATE_STUDENT_ID, data::toModelType);
    }

    @Test
    public void toModelType_legacyEnrollmentMatchesSecondStoredClass() throws Exception {
        TuitionClass earlierPhysics = new TuitionClass(new ClassId("C001"), new Subject("Physics"),
                new Cost("30"), new LessonTiming("Monday1800-1930"));
        TuitionClass physics = new TuitionClass(new ClassId("C002"), new Subject("Physics"),
                new Cost("40"), new LessonTiming("Tuesday1800-1930"));
        Person person = new PersonBuilder().withLessons(new Lesson(new Subject("physics"),
                new Cost("35"), physics.getSchedule())).build();
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                List.of(new JsonAdaptedPerson(person)),
                List.of(new JsonAdaptedTuitionClass(earlierPhysics),
                        new JsonAdaptedTuitionClass(physics)), 3);

        AddressBook restored = data.toModelType();

        assertEquals(physics.getClassId(), restored.getPersonList().getFirst()
                .getEnrollments().iterator().next().getClassId());
        assertEquals(new Cost("35"), restored.getPersonList().getFirst()
                .getEnrollments().iterator().next().getAgreedRate());
    }

    @Test
    public void toModelType_explicitDetailsForDifferentClasses_doNotConflict() throws Exception {
        ClassDetails online = new ClassDetails(ClassMode.ONLINE, "https://example.com", "", "");
        Person first = new PersonBuilder().withName("Alice").withLessons(new Lesson(new Subject("Math"),
                new Cost("30"), new LessonTiming("Monday1800-1930"), PaymentStatus.UNPAID, online)).build();
        Person second = new PersonBuilder().withName("Bob").withLessons(new Lesson(new Subject("Physics"),
                new Cost("40"), new LessonTiming("Tuesday1800-1930"), PaymentStatus.UNPAID, online)).build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(first);
        addressBook.addPerson(second);

        AddressBook restored = new JsonSerializableAddressBook(addressBook).toModelType();

        assertEquals(2, restored.getTuitionClassList().size());
    }

    @Test
    public void toModelType_occupiedEmptyClassRejectsLaterDetailReplacement() {
        AddressBook addressBook = new AddressBook();
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"),
                new Cost("30"), new LessonTiming("Monday1800-1930"));
        addressBook.addTuitionClass(tuitionClass);
        Person first = new PersonBuilder().withName("Alice").build();
        Enrollment firstEnrollment = new Enrollment(first.getStudentId(), tuitionClass);
        Person second = new PersonBuilder().withName("Bob").withLessons(new Lesson(new Subject("Math"),
                new Cost("35"), tuitionClass.getSchedule(), PaymentStatus.UNPAID,
                new ClassDetails(ClassMode.OFFLINE, "", "Room 2", ""))).build();
        addressBook.addPerson(first.withEnrollments(java.util.Set.of(firstEnrollment)));
        addressBook.addPerson(second);

        JsonSerializableAddressBook data = new JsonSerializableAddressBook(addressBook);

        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_CLASS_DETAILS_CONFLICT,
                data::toModelType);
    }

}
