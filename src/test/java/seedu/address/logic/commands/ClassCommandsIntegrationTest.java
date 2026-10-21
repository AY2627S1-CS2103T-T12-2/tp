package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStart;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;

public class ClassCommandsIntegrationTest {

    private static final Lesson LESSON = new Lesson(new Subject("Math"), new Cost("30"),
            new LessonTiming("Monday1800-1930"));
    private static final Lesson OTHER_LESSON = new Lesson(new Subject("English"), new Cost("25"),
            new LessonTiming("Wednesday1600-1730"));
    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void enrol_validClass_addsClassAndReturnsSuccessMessage() throws CommandException {
        Person originalPerson = model.getFilteredPersonList().get(0);

        CommandResult result = new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        Person updatedPerson = model.getAddressBook().getPersonList().get(0);

        assertEquals(String.format(ClassEnrolCommand.MESSAGE_SUCCESS, originalPerson.getName(), LESSON),
                result.getFeedbackToUser());
        assertTrue(updatedPerson.getLessons().contains(LESSON));
        assertEquals(originalPerson.getName(), updatedPerson.getName());
        assertEquals(originalPerson.getPhone(), updatedPerson.getPhone());
        assertEquals(originalPerson.getEmail(), updatedPerson.getEmail());
        assertEquals(originalPerson.getAddress(), updatedPerson.getAddress());
        assertEquals(originalPerson.getTags(), updatedPerson.getTags());
    }

    @Test
    public void enrol_oneOffClass_addsClassAndConflictsWithWeeklyOccurrence() throws CommandException {
        Lesson oneOff = new Lesson(new Subject("Math"), new Cost("30"),
                new OneOffLessonSchedule("2026-10-20T1800-1930"));
        new ClassEnrolCommand(Index.fromOneBased(1), oneOff).execute(model);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(oneOff));

        Lesson overlappingWeekly = new Lesson(new Subject("Physics"), new Cost("40"),
                new LessonTiming("Tuesday1900-2000"));
        String expectedMessage = "Cannot add Physics (Tuesday 19:00-20:00) because it overlaps with:"
                + System.lineSeparator() + "- Math (20 Oct 2026 18:00-19:30)"
                + System.lineSeparator() + "Choose a non-overlapping time.";
        assertCommandFailure(new ClassEnrolCommand(Index.fromOneBased(2), overlappingWeekly), model,
                expectedMessage);
    }

    @Test
    public void enrol_sameClassCanBelongToMultipleStudents() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void enrolMultiple_existingClass_addsClassWithExistingCostToEverySelectedStudent()
            throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        Person secondStudent = model.getFilteredPersonList().get(1);
        Person thirdStudent = model.getFilteredPersonList().get(2);

        ClassEnrolCommand command = new ClassEnrolCommand(
                List.of(Index.fromOneBased(2), Index.fromOneBased(3)),
                new Subject("mAtH"), LESSON.getSchedule());
        CommandResult result = command.execute(model);

        assertEquals(String.format(ClassEnrolCommand.MESSAGE_BATCH_SUCCESS,
                secondStudent.getName() + ", " + thirdStudent.getName(),
                LESSON.getSubject(), LESSON.getSchedule()), result.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(2).getLessons().contains(LESSON));
    }

    @Test
    public void enrolMultiple_emptyStoredClass_findsClassThroughModelAndEnrolsStudents()
            throws CommandException {
        ClassId classId = model.allocateClassId();
        model.addTuitionClass(new TuitionClass(classId, LESSON.getSubject(), LESSON.getCost(), LESSON.getSchedule()));
        Person firstStudent = model.getFilteredPersonList().get(0);
        Person secondStudent = model.getFilteredPersonList().get(1);

        CommandResult result = new ClassEnrolCommand(
                List.of(Index.fromOneBased(1), Index.fromOneBased(2)),
                new Subject("mAtH"), LESSON.getSchedule()).execute(model);

        assertEquals(String.format(ClassEnrolCommand.MESSAGE_BATCH_SUCCESS,
                firstStudent.getName() + ", " + secondStudent.getName(),
                LESSON.getSubject(), LESSON.getSchedule()), result.getFeedbackToUser());
        for (int personIndex = 0; personIndex < 2; personIndex++) {
            Lesson enrollment = model.getAddressBook().getPersonList().get(personIndex).getLessons().stream()
                    .filter(lesson -> classId.equals(lesson.getClassId()))
                    .findFirst()
                    .orElseThrow();
            assertEquals(LESSON.getCost(), enrollment.getCost());
            assertEquals(PaymentStatus.UNPAID, enrollment.getPaymentStatus());
        }
    }

    @Test
    public void enrolMultiple_studentWithExistingLessons_preservesLessonOrder() throws CommandException {
        Lesson secondExistingLesson = new Lesson(new Subject("Science"), new Cost("35"),
                new LessonTiming("Thursday1400-1530"));
        Person originalStudent = model.getFilteredPersonList().get(0);
        Set<Lesson> existingLessons = new LinkedHashSet<>(List.of(OTHER_LESSON, secondExistingLesson));
        model.setPerson(originalStudent, originalStudent.withLessons(existingLessons));
        new ClassEnrolCommand(Index.fromOneBased(2), LESSON).execute(model);

        new ClassEnrolCommand(List.of(Index.fromOneBased(1)),
                LESSON.getSubject(), LESSON.getSchedule()).execute(model);

        Person enrolledStudent = model.getAddressBook().getPersonList().get(0);
        assertEquals(List.of(OTHER_LESSON, secondExistingLesson, LESSON),
                enrolledStudent.getLessons().stream().toList());
    }

    @Test
    public void enrolMultiple_filteredListFindsExistingClassAndUsesDisplayedIndex() throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        showPersonAtIndex(model, Index.fromOneBased(2));

        new ClassEnrolCommand(List.of(Index.fromOneBased(1)),
                LESSON.getSubject(), LESSON.getSchedule()).execute(model);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void enrolMultiple_missingClass_failureAndDoesNotModifyStudents() {
        ClassEnrolCommand command = new ClassEnrolCommand(
                List.of(Index.fromOneBased(1), Index.fromOneBased(2)),
                LESSON.getSubject(), LESSON.getSchedule());

        assertCommandFailure(command, model, ClassEnrolCommand.MESSAGE_EXISTING_CLASS_NOT_FOUND);
    }

    @Test
    public void enrolMultiple_invalidIndex_failureAndDoesNotModifyStudents() throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        ClassEnrolCommand command = new ClassEnrolCommand(
                List.of(Index.fromOneBased(2), invalidIndex), LESSON.getSubject(), LESSON.getSchedule());

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void enrolMultiple_someStudentsAlreadyEnrolled_skipsThemAndEnrolsRemainingStudents()
            throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(2), LESSON).execute(model);
        Person studentToEnrol = model.getFilteredPersonList().get(0);
        Person alreadyEnrolled = model.getFilteredPersonList().get(1);
        ClassEnrolCommand command = new ClassEnrolCommand(
                List.of(Index.fromOneBased(1), Index.fromOneBased(2)),
                LESSON.getSubject(), LESSON.getSchedule());

        CommandResult result = command.execute(model);

        assertEquals(String.format(ClassEnrolCommand.MESSAGE_BATCH_PARTIAL_SUCCESS,
                studentToEnrol.getName(), LESSON.getSubject(), LESSON.getSchedule(), alreadyEnrolled.getName()),
                result.getFeedbackToUser());
        Person enrolledStudent = model.getAddressBook().getPersonList().get(0);
        assertTrue(enrolledStudent.getLessons().contains(LESSON));
        assertEquals(PaymentStatus.UNPAID, getPaymentStatus(enrolledStudent, LESSON));
        assertEquals(alreadyEnrolled, model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void enrolMultiple_allStudentsAlreadyEnrolled_returnsNoChangeMessage() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person firstStudent = model.getFilteredPersonList().get(0);
        Person secondStudent = model.getFilteredPersonList().get(1);
        ClassEnrolCommand command = new ClassEnrolCommand(
                List.of(Index.fromOneBased(1), Index.fromOneBased(2)),
                LESSON.getSubject(), LESSON.getSchedule());

        CommandResult result = command.execute(model);

        assertEquals(String.format(ClassEnrolCommand.MESSAGE_BATCH_ALL_ALREADY_ENROLLED,
                LESSON.getSubject(), LESSON.getSchedule(), firstStudent.getName() + ", " + secondStudent.getName()),
                result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    @Test
    public void enrol_filteredList_usesDisplayedIndex() throws CommandException {
        showPersonAtIndex(model, Index.fromOneBased(2));

        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);

        assertFalse(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void findByClassStart_listsStudentsWithClass() throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        new FindCommand(new LessonStart("Monday1800")).execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
    }

    @Test
    public void enrol_sameClassWithDifferentCost_failure() throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        Lesson conflictingLesson = new Lesson(new Subject("Math"), new Cost("40"),
                new LessonTiming("Monday1800-1930"));

        assertCommandFailure(new ClassEnrolCommand(Index.fromOneBased(2), conflictingLesson), model,
                ClassEnrolCommand.MESSAGE_CLASS_COST_CONFLICT);
    }

    @Test
    public void enrol_overlappingClass_failure() throws CommandException {
        new ClassEnrolCommand(Index.fromOneBased(1), LESSON).execute(model);
        Lesson overlappingLesson = new Lesson(new Subject("Physics"), new Cost("40"),
                new LessonTiming("Monday1900-2000"));
        String expectedMessage = "Cannot add Physics (Monday 19:00-20:00) because it overlaps with:"
                + System.lineSeparator() + "- Math (Monday 18:00-19:30)"
                + System.lineSeparator() + "Choose a non-overlapping time.";

        assertCommandFailure(new ClassEnrolCommand(Index.fromOneBased(2), overlappingLesson), model,
                expectedMessage);
    }

    @Test
    public void enrol_invalidPersonIndex_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ClassEnrolCommand(invalidIndex, LESSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void enrol_sameClassForSameStudent_failure() throws CommandException {
        ClassEnrolCommand command = new ClassEnrolCommand(Index.fromOneBased(1), LESSON);
        command.execute(model);

        assertCommandFailure(command, model, ClassEnrolCommand.MESSAGE_DUPLICATE_CLASS);
    }

    @Test
    public void enrolCommand_equals() {
        ClassEnrolCommand command = new ClassEnrolCommand(Index.fromOneBased(1), LESSON);
        Lesson differentLesson = new Lesson(new Subject("Physics"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));

        assertEquals(command, command);
        assertEquals(command, new ClassEnrolCommand(Index.fromOneBased(1), LESSON));
        assertNotEquals(command, new ClassEnrolCommand(Index.fromOneBased(2), LESSON));
        assertNotEquals(command, new ClassEnrolCommand(Index.fromOneBased(1), differentLesson));
        assertNotEquals(command, new ClassEnrolCommand(List.of(Index.fromOneBased(1)),
                LESSON.getSubject(), LESSON.getSchedule()));
        assertNotEquals(command, null);
    }

    @Test
    public void enrolMultipleCommand_equals() {
        List<Index> indexes = List.of(Index.fromOneBased(1), Index.fromOneBased(2));
        ClassEnrolCommand command = new ClassEnrolCommand(indexes, LESSON.getSubject(), LESSON.getSchedule());

        assertEquals(command, command);
        assertEquals(command, new ClassEnrolCommand(indexes,
                new Subject("math"), LESSON.getSchedule()));
        assertNotEquals(command, new ClassEnrolCommand(List.of(Index.fromOneBased(2), Index.fromOneBased(1)),
                LESSON.getSubject(), LESSON.getSchedule()));
        assertNotEquals(command, new ClassEnrolCommand(indexes,
                OTHER_LESSON.getSubject(), LESSON.getSchedule()));
        assertNotEquals(command, new ClassEnrolCommand(indexes,
                LESSON.getSubject(), OTHER_LESSON.getSchedule()));
        assertNotEquals(command, null);
    }

    @Test
    public void enrolMultipleCommand_emptyIndexes_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ClassEnrolCommand(
                List.of(), LESSON.getSubject(), LESSON.getSchedule()));
    }

    @Test
    public void enrolMultipleCommand_duplicateIndexes_throwsIllegalArgumentException() {
        Index repeatedIndex = Index.fromOneBased(1);

        assertThrows(IllegalArgumentException.class, () -> new ClassEnrolCommand(
                List.of(repeatedIndex, repeatedIndex), LESSON.getSubject(), LESSON.getSchedule()));
    }

    @Test
    public void enrolMultipleCommand_nullArguments_throwsNullPointerException() {
        List<Index> indexesWithNull = new ArrayList<>();
        indexesWithNull.add(null);

        assertThrows(NullPointerException.class, () -> new ClassEnrolCommand(
                (List<Index>) null, LESSON.getSubject(), LESSON.getSchedule()));
        assertThrows(NullPointerException.class, () -> new ClassEnrolCommand(
                indexesWithNull, LESSON.getSubject(), LESSON.getSchedule()));
        assertThrows(NullPointerException.class, () -> new ClassEnrolCommand(
                List.of(Index.fromOneBased(1)), null, LESSON.getSchedule()));
        assertThrows(NullPointerException.class, () -> new ClassEnrolCommand(
                List.of(Index.fromOneBased(1)), LESSON.getSubject(), null));
    }

    @Test
    public void enrolMultipleCommand_mutableIndexes_defensivelyCopiesIndexes() {
        List<Index> mutableIndexes = new ArrayList<>(List.of(Index.fromOneBased(1)));
        ClassEnrolCommand command = new ClassEnrolCommand(
                mutableIndexes, LESSON.getSubject(), LESSON.getSchedule());

        mutableIndexes.add(Index.fromOneBased(2));

        assertEquals(new ClassEnrolCommand(List.of(Index.fromOneBased(1)),
                LESSON.getSubject(), LESSON.getSchedule()), command);
    }

    @Test
    public void enrolMultipleCommand_nullModel_throwsNullPointerException() {
        ClassEnrolCommand command = new ClassEnrolCommand(
                List.of(Index.fromOneBased(1)), LESSON.getSubject(), LESSON.getSchedule());

        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void unenrol_existingClass_removesOnlySelectedStudent() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);

        CommandResult result = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getSchedule()).execute(model);

        assertEquals(String.format(ClassUnenrolCommand.MESSAGE_SUCCESS,
                model.getAddressBook().getPersonList().get(0).getName(), LESSON.getSubject(), LESSON.getSchedule()),
                result.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void unenrol_studentWithOtherClass_preservesOtherClass() throws CommandException {
        enrolStudentsInLesson(LESSON, 1);
        enrolStudentsInLesson(OTHER_LESSON, 1);

        new ClassUnenrolCommand(Index.fromOneBased(1),
                new Subject("mAtH"), LESSON.getSchedule()).execute(model);

        assertEquals(Set.of(OTHER_LESSON), model.getAddressBook().getPersonList().get(0).getLessons());
    }

    @Test
    public void unenrol_filteredList_usesDisplayedIndex() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        showPersonAtIndex(model, Index.fromOneBased(2));

        new ClassUnenrolCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getSchedule()).execute(model);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertFalse(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void unenrol_invalidPersonIndex_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ClassUnenrolCommand(invalidIndex, LESSON.getSubject(), LESSON.getSchedule()), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void unenrol_studentNotEnrolled_failure() {
        ClassUnenrolCommand command = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getSchedule());

        assertCommandFailure(command, model, ClassUnenrolCommand.MESSAGE_NOT_ENROLLED);
    }

    @Test
    public void unenrolCommand_equals() {
        ClassUnenrolCommand command = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getSchedule());

        assertEquals(command, command);
        assertEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), new Subject("math"), LESSON.getSchedule()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(2), LESSON.getSubject(), LESSON.getSchedule()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), OTHER_LESSON.getSubject(), LESSON.getSchedule()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), OTHER_LESSON.getSchedule()));
        assertNotEquals(command, null);
    }

    @Test
    public void payment_existingEnrolment_updatesOnlySelectedStudentAndClass() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        enrolStudentsInLesson(OTHER_LESSON, 1);

        CommandResult result = new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getSchedule(), PaymentStatus.PAID).execute(model);

        Person firstStudent = model.getAddressBook().getPersonList().get(0);
        Person secondStudent = model.getAddressBook().getPersonList().get(1);
        assertEquals(String.format(ClassPaymentCommand.MESSAGE_SUCCESS, firstStudent.getName(),
                LESSON.getSubject(), LESSON.getSchedule(), "paid"), result.getFeedbackToUser());
        assertEquals(PaymentStatus.PAID, getPaymentStatus(firstStudent, LESSON));
        assertEquals(PaymentStatus.UNPAID, getPaymentStatus(firstStudent, OTHER_LESSON));
        assertEquals(PaymentStatus.UNPAID, getPaymentStatus(secondStudent, LESSON));
    }

    @Test
    public void payment_paidEnrolment_canBeMarkedUnpaid() throws CommandException {
        enrolStudentsInLesson(LESSON, 1);
        new ClassPaymentCommand(Index.fromOneBased(1), LESSON.getSubject(), LESSON.getSchedule(),
                PaymentStatus.PAID).execute(model);

        new ClassPaymentCommand(Index.fromOneBased(1), LESSON.getSubject(), LESSON.getSchedule(),
                PaymentStatus.UNPAID).execute(model);

        assertEquals(PaymentStatus.UNPAID,
                getPaymentStatus(model.getAddressBook().getPersonList().get(0), LESSON));
    }

    @Test
    public void payment_filteredList_usesDisplayedIndex() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        showPersonAtIndex(model, Index.fromOneBased(2));

        new ClassPaymentCommand(Index.fromOneBased(1), LESSON.getSubject(), LESSON.getSchedule(),
                PaymentStatus.PAID).execute(model);

        assertEquals(PaymentStatus.UNPAID,
                getPaymentStatus(model.getAddressBook().getPersonList().get(0), LESSON));
        assertEquals(PaymentStatus.PAID,
                getPaymentStatus(model.getAddressBook().getPersonList().get(1), LESSON));
    }

    @Test
    public void payment_invalidPersonIndex_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new ClassPaymentCommand(invalidIndex, LESSON.getSubject(),
                LESSON.getSchedule(), PaymentStatus.PAID), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void payment_missingEnrolment_failureAndDoesNotChangeModel() {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        ClassPaymentCommand command = new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getSchedule(), PaymentStatus.PAID);

        assertCommandFailure(command, model, String.format(ClassPaymentCommand.MESSAGE_NOT_ENROLLED,
                LESSON.getSubject(), LESSON.getSchedule()));
        assertEquals(expectedModel, model);
    }

    @Test
    public void payment_statusAlreadySet_returnsNoOpMessage() throws CommandException {
        enrolStudentsInLesson(LESSON, 1);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person person = model.getFilteredPersonList().get(0);

        CommandResult result = new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getSchedule(), PaymentStatus.UNPAID).execute(model);

        assertEquals(String.format(ClassPaymentCommand.MESSAGE_ALREADY_MARKED, person.getName(),
                LESSON.getSubject(), LESSON.getSchedule(), "unpaid"), result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    @Test
    public void paymentCommand_equals() {
        ClassPaymentCommand command = new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getSchedule(), PaymentStatus.PAID);

        assertEquals(command, command);
        assertEquals(command, new ClassPaymentCommand(Index.fromOneBased(1),
                new Subject("math"), LESSON.getSchedule(), PaymentStatus.PAID));
        assertNotEquals(command, new ClassPaymentCommand(Index.fromOneBased(2),
                LESSON.getSubject(), LESSON.getSchedule(), PaymentStatus.PAID));
        assertNotEquals(command, new ClassPaymentCommand(Index.fromOneBased(1),
                OTHER_LESSON.getSubject(), LESSON.getSchedule(), PaymentStatus.PAID));
        assertNotEquals(command, new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), OTHER_LESSON.getSchedule(), PaymentStatus.PAID));
        assertNotEquals(command, new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getSchedule(), PaymentStatus.UNPAID));
        assertNotEquals(command, null);
    }

    @Test
    public void delete_existingClass_removesEveryEnrolmentIncludingFilteredOutStudents() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        showPersonAtIndex(model, Index.fromOneBased(1));

        CommandResult result = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getSchedule()).execute(model);

        assertEquals(String.format(ClassDeleteCommand.MESSAGE_SUCCESS,
                LESSON.getSubject(), LESSON.getSchedule(), 2, "students"), result.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> person.getLessons().contains(LESSON)));
    }

    @Test
    public void delete_onlyMatchingClass_preservesOtherClassesAndUnenrolledStudents() throws CommandException {
        Person untouchedPerson = model.getAddressBook().getPersonList().get(1);
        enrolStudentsInLesson(LESSON, 1);
        enrolStudentsInLesson(OTHER_LESSON, 1);

        CommandResult result = new ClassDeleteCommand(
                new Subject("MATH"), LESSON.getSchedule()).execute(model);

        assertEquals(String.format(ClassDeleteCommand.MESSAGE_SUCCESS,
                new Subject("MATH"), LESSON.getSchedule(), 1, "student"), result.getFeedbackToUser());
        assertEquals(Set.of(OTHER_LESSON), model.getAddressBook().getPersonList().get(0).getLessons());
        assertEquals(untouchedPerson, model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void delete_missingClass_failure() {
        ClassDeleteCommand command = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getSchedule());

        assertCommandFailure(command, model, ClassDeleteCommand.MESSAGE_CLASS_NOT_FOUND);
    }

    @Test
    public void deleteCommand_equals() {
        ClassDeleteCommand command = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getSchedule());

        assertEquals(command, command);
        assertEquals(command, new ClassDeleteCommand(new Subject("math"), LESSON.getSchedule()));
        assertNotEquals(command, new ClassDeleteCommand(OTHER_LESSON.getSubject(), LESSON.getSchedule()));
        assertNotEquals(command, new ClassDeleteCommand(LESSON.getSubject(), OTHER_LESSON.getSchedule()));
        assertNotEquals(command, null);
    }

    @Test
    public void timetable_switchesViewWithoutChangingModel() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        CommandResult result = new TimetableCommand().execute(model);

        assertEquals(TimetableCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(CommandResult.ViewChange.TIMETABLE, result.getViewChange());
        assertEquals(expectedModel, model);
    }

    @Test
    public void addAndList_independentClass_allocatesStableIdAndSwitchesView() throws CommandException {
        CommandResult addResult = new ClassAddCommand(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930")).execute(model);

        assertEquals(1, model.getTuitionClassList().size());
        assertEquals(new ClassId("C001"), model.getTuitionClassList().get(0).getClassId());
        assertTrue(addResult.getFeedbackToUser().contains("C001"));

        CommandResult listResult = new ClassListCommand().execute(model);
        assertEquals(CommandResult.ViewChange.CLASS_LIST, listResult.getViewChange());
    }

    @Test
    public void enrolAndEdit_byStableId_updatesSharedDetailsAndPreservesPayment() throws CommandException {
        ClassId classId = model.allocateClassId();
        TuitionClass original = new TuitionClass(classId, new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        model.addTuitionClass(original);
        new ClassEnrolCommand(Index.fromOneBased(1), classId).execute(model);
        new ClassPaymentCommand(Index.fromOneBased(1), classId, PaymentStatus.PAID).execute(model);

        new ClassEditCommand(classId, java.util.Optional.of(new Subject("Advanced Math")),
                java.util.Optional.of(new Cost("35")), java.util.Optional.empty()).execute(model);

        TuitionClass edited = model.getTuitionClass(classId);
        Lesson enrollment = model.getAddressBook().getPersonList().get(0).getLessons().iterator().next();
        assertEquals(new Subject("Advanced Math"), edited.getSubject());
        assertEquals(new Cost("35"), enrollment.getCost());
        assertEquals(classId, enrollment.getClassId());
        assertEquals(PaymentStatus.PAID, enrollment.getPaymentStatus());
    }

    @Test
    public void enrol_missingStableId_failure() {
        ClassId missingClassId = new ClassId("C999");

        assertCommandFailure(new ClassEnrolCommand(Index.fromOneBased(1), missingClassId), model,
                String.format(ClassEnrolCommand.MESSAGE_CLASS_NOT_FOUND, missingClassId));
    }

    @Test
    public void deleteThenAdd_classIdsAreNotReused() throws CommandException {
        new ClassAddCommand(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930")).execute(model);
        new ClassDeleteCommand(new ClassId("C001")).execute(model);
        new ClassAddCommand(new Subject("Physics"), new Cost("35"),
                new LessonTiming("Tuesday1800-1930")).execute(model);

        assertEquals(new ClassId("C002"), model.getTuitionClassList().get(0).getClassId());
    }

    @Test
    public void add_duplicateAndOverlappingClasses_failWithoutConsumingId() throws CommandException {
        ClassAddCommand addMath = new ClassAddCommand(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        addMath.execute(model);

        assertCommandFailure(new ClassAddCommand(new Subject("math"), new Cost("99"),
                new LessonTiming("Monday1800-1930")), model, ClassAddCommand.MESSAGE_DUPLICATE);
        Lesson overlap = new Lesson(new Subject("Physics"), new Cost("35"),
                new LessonTiming("Monday1900-2000"));
        assertCommandFailure(new ClassAddCommand(overlap.getSubject(), overlap.getCost(), overlap.getSchedule()), model,
                Messages.getClassOverlapMessage(overlap, List.of(LESSON)));

        new ClassAddCommand(new Subject("English"), new Cost("25"),
                new LessonTiming("Tuesday1800-1930")).execute(model);
        assertEquals(new ClassId("C002"), model.getTuitionClassList().get(1).getClassId());
    }

    @Test
    public void edit_invalidCases_failWithoutMutation() throws CommandException {
        new ClassAddCommand(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930")).execute(model);
        new ClassAddCommand(new Subject("Physics"), new Cost("35"),
                new LessonTiming("Tuesday1800-1930")).execute(model);

        assertCommandFailure(new ClassEditCommand(new ClassId("C099"), java.util.Optional.empty(),
                java.util.Optional.of(new Cost("40")), java.util.Optional.empty()), model,
                String.format(ClassEditCommand.MESSAGE_NOT_FOUND, "C099"));
        assertCommandFailure(new ClassEditCommand(new ClassId("C001"), java.util.Optional.empty(),
                java.util.Optional.of(new Cost("30")), java.util.Optional.empty()), model,
                ClassEditCommand.MESSAGE_NO_CHANGE);
        assertCommandFailure(new ClassEditCommand(new ClassId("C001"),
                java.util.Optional.of(new Subject("Physics")), java.util.Optional.empty(),
                java.util.Optional.of(new LessonTiming("Tuesday1800-1930"))), model,
                ClassEditCommand.MESSAGE_DUPLICATE);

        Lesson overlap = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Tuesday1900-2000"));
        Lesson physics = new Lesson(new ClassId("C002"), new Subject("Physics"), new Cost("35"),
                new LessonTiming("Tuesday1800-1930"), PaymentStatus.UNPAID);
        assertCommandFailure(new ClassEditCommand(new ClassId("C001"), java.util.Optional.empty(),
                java.util.Optional.empty(), java.util.Optional.of(overlap.getSchedule())), model,
                Messages.getClassOverlapMessage(overlap, List.of(physics)));
    }

    @Test
    public void unenrol_byStableId_successAndMissingFailure() throws CommandException {
        ClassId id = model.allocateClassId();
        model.addTuitionClass(new TuitionClass(id, new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930")));
        new ClassEnrolCommand(Index.fromOneBased(1), id).execute(model);

        Person person = model.getFilteredPersonList().get(0);
        CommandResult result = new ClassUnenrolCommand(Index.fromOneBased(1), id).execute(model);

        assertEquals(String.format(ClassUnenrolCommand.MESSAGE_SUCCESS_BY_ID, person.getName(), id),
                result.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().isEmpty());
        assertCommandFailure(new ClassUnenrolCommand(Index.fromOneBased(1), id), model,
                ClassUnenrolCommand.MESSAGE_NOT_ENROLLED);
    }

    @Test
    public void payment_byStableId_usesIdSpecificMessages() throws CommandException {
        ClassId id = model.allocateClassId();
        model.addTuitionClass(new TuitionClass(id, new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930")));
        new ClassEnrolCommand(Index.fromOneBased(1), id).execute(model);
        Person person = model.getFilteredPersonList().get(0);

        CommandResult success = new ClassPaymentCommand(
                Index.fromOneBased(1), id, PaymentStatus.PAID).execute(model);
        assertEquals(String.format(ClassPaymentCommand.MESSAGE_SUCCESS_BY_ID,
                person.getName(), id, "paid"), success.getFeedbackToUser());

        CommandResult alreadyMarked = new ClassPaymentCommand(
                Index.fromOneBased(1), id, PaymentStatus.PAID).execute(model);
        assertEquals(String.format(ClassPaymentCommand.MESSAGE_ALREADY_MARKED_BY_ID,
                person.getName(), id, "paid"), alreadyMarked.getFeedbackToUser());

        assertCommandFailure(new ClassPaymentCommand(
                Index.fromOneBased(2), id, PaymentStatus.PAID), model,
                String.format(ClassPaymentCommand.MESSAGE_NOT_ENROLLED_BY_ID, id));
    }

    @Test
    public void newClassCommands_equals_coverDifferentFieldsAndTypes() {
        ClassAddCommand add = new ClassAddCommand(new Subject("Math"), new Cost("30"), LESSON.getSchedule());
        assertEquals(add, add);
        assertEquals(add, new ClassAddCommand(new Subject("math"), new Cost("30.00"), LESSON.getSchedule()));
        assertNotEquals(add, new ClassAddCommand(new Subject("Physics"), new Cost("30"), LESSON.getSchedule()));
        assertNotEquals(add, new ClassAddCommand(new Subject("Math"), new Cost("31"), LESSON.getSchedule()));
        assertNotEquals(add, new ClassAddCommand(new Subject("Math"), new Cost("30"), OTHER_LESSON.getSchedule()));
        assertNotEquals(add, null);

        ClassEditCommand edit = new ClassEditCommand(new ClassId("C001"), java.util.Optional.empty(),
                java.util.Optional.of(new Cost("31")), java.util.Optional.empty());
        assertEquals(edit, edit);
        assertEquals(edit, new ClassEditCommand(new ClassId("C001"), java.util.Optional.empty(),
                java.util.Optional.of(new Cost("31")), java.util.Optional.empty()));
        assertNotEquals(edit, new ClassEditCommand(new ClassId("C002"), java.util.Optional.empty(),
                java.util.Optional.of(new Cost("31")), java.util.Optional.empty()));
        assertNotEquals(edit, new ClassEditCommand(new ClassId("C001"),
                java.util.Optional.of(new Subject("Math")), java.util.Optional.of(new Cost("31")),
                java.util.Optional.empty()));
        assertNotEquals(edit, null);
        assertEquals(new ClassListCommand(), new ClassListCommand());
        assertNotEquals(new ClassListCommand(), null);
    }

    private void enrolStudentsInLesson(Lesson lesson, int... oneBasedIndexes) throws CommandException {
        for (int oneBasedIndex : oneBasedIndexes) {
            new ClassEnrolCommand(Index.fromOneBased(oneBasedIndex), lesson).execute(model);
        }
    }

    private static PaymentStatus getPaymentStatus(Person person, Lesson targetClass) {
        return person.getLessons().stream()
                .filter(lesson -> lesson.isSameClass(targetClass))
                .findFirst()
                .orElseThrow()
                .getPaymentStatus();
    }
}
