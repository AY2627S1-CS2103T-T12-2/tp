package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

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
    public void enrol_sameClassCanBelongToMultipleStudents() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void enrol_sameSubjectAtDifferentTime_createsSeparateClass() throws CommandException {
        Lesson tuesdayMath = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Tuesday1800-1930"));
        new ClassEnrolCommand(Index.fromOneBased(1), tuesdayMath).execute(model);

        new ClassEnrolCommand(Index.fromOneBased(2), LESSON).execute(model);

        assertEquals(2, model.getTuitionClassList().size());
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
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
    public void enrol_missingClassId_failure() {
        ClassId missingId = new ClassId("C099");

        assertCommandFailure(new ClassEnrolCommand(Index.fromOneBased(1), missingId), model,
                String.format(ClassEnrolCommand.MESSAGE_CLASS_NOT_FOUND, missingId));
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
        assertNotEquals(command, null);

        ClassEnrolCommand idCommand = new ClassEnrolCommand(Index.fromOneBased(1), new ClassId("C001"));
        assertEquals(idCommand, new ClassEnrolCommand(Index.fromOneBased(1), new ClassId("C001")));
        assertNotEquals(idCommand, new ClassEnrolCommand(Index.fromOneBased(1), new ClassId("C002")));
        assertNotEquals(idCommand, command);
    }

    @Test
    public void unenrol_existingClass_removesOnlySelectedStudent() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);

        CommandResult result = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming()).execute(model);

        assertEquals(String.format(ClassUnenrolCommand.MESSAGE_SUCCESS,
                model.getAddressBook().getPersonList().get(0).getName(), LESSON.getSubject(), LESSON.getTiming()),
                result.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertTrue(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void unenrol_studentWithOtherClass_preservesOtherClass() throws CommandException {
        enrolStudentsInLesson(LESSON, 1);
        enrolStudentsInLesson(OTHER_LESSON, 1);

        new ClassUnenrolCommand(Index.fromOneBased(1),
                new Subject("mAtH"), LESSON.getTiming()).execute(model);

        assertEquals(Set.of(OTHER_LESSON), model.getAddressBook().getPersonList().get(0).getLessons());
    }

    @Test
    public void unenrol_filteredList_usesDisplayedIndex() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        showPersonAtIndex(model, Index.fromOneBased(2));

        new ClassUnenrolCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getTiming()).execute(model);

        assertTrue(model.getAddressBook().getPersonList().get(0).getLessons().contains(LESSON));
        assertFalse(model.getAddressBook().getPersonList().get(1).getLessons().contains(LESSON));
    }

    @Test
    public void unenrol_invalidPersonIndex_failure() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new ClassUnenrolCommand(invalidIndex, LESSON.getSubject(), LESSON.getTiming()), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void unenrol_studentNotEnrolled_failure() {
        ClassUnenrolCommand command = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming());

        assertCommandFailure(command, model, ClassUnenrolCommand.MESSAGE_NOT_ENROLLED);
    }

    @Test
    public void unenrolCommand_equals() {
        ClassUnenrolCommand command = new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming());

        assertEquals(command, command);
        assertEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), new Subject("math"), LESSON.getTiming()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(2), LESSON.getSubject(), LESSON.getTiming()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), OTHER_LESSON.getSubject(), LESSON.getTiming()));
        assertNotEquals(command, new ClassUnenrolCommand(
                Index.fromOneBased(1), LESSON.getSubject(), OTHER_LESSON.getTiming()));
        assertNotEquals(command, null);
    }

    @Test
    public void payment_existingEnrolment_updatesOnlySelectedStudentAndClass() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        enrolStudentsInLesson(OTHER_LESSON, 1);

        CommandResult result = new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getTiming(), PaymentStatus.PAID).execute(model);

        Person firstStudent = model.getAddressBook().getPersonList().get(0);
        Person secondStudent = model.getAddressBook().getPersonList().get(1);
        assertEquals(String.format(ClassPaymentCommand.MESSAGE_SUCCESS, firstStudent.getName(),
                LESSON.getSubject(), LESSON.getTiming(), "paid"), result.getFeedbackToUser());
        assertEquals(PaymentStatus.PAID, getPaymentStatus(firstStudent, LESSON));
        assertEquals(PaymentStatus.UNPAID, getPaymentStatus(firstStudent, OTHER_LESSON));
        assertEquals(PaymentStatus.UNPAID, getPaymentStatus(secondStudent, LESSON));
    }

    @Test
    public void payment_paidEnrolment_canBeMarkedUnpaid() throws CommandException {
        enrolStudentsInLesson(LESSON, 1);
        new ClassPaymentCommand(Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming(),
                PaymentStatus.PAID).execute(model);

        new ClassPaymentCommand(Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming(),
                PaymentStatus.UNPAID).execute(model);

        assertEquals(PaymentStatus.UNPAID,
                getPaymentStatus(model.getAddressBook().getPersonList().get(0), LESSON));
    }

    @Test
    public void payment_filteredList_usesDisplayedIndex() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        showPersonAtIndex(model, Index.fromOneBased(2));

        new ClassPaymentCommand(Index.fromOneBased(1), LESSON.getSubject(), LESSON.getTiming(),
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
                LESSON.getTiming(), PaymentStatus.PAID), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void payment_missingEnrolment_failureAndDoesNotChangeModel() {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        ClassPaymentCommand command = new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getTiming(), PaymentStatus.PAID);

        assertCommandFailure(command, model, String.format(ClassPaymentCommand.MESSAGE_NOT_ENROLLED,
                LESSON.getSubject(), LESSON.getTiming()));
        assertEquals(expectedModel, model);
    }

    @Test
    public void payment_statusAlreadySet_returnsNoOpMessage() throws CommandException {
        enrolStudentsInLesson(LESSON, 1);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        Person person = model.getFilteredPersonList().get(0);

        CommandResult result = new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getTiming(), PaymentStatus.UNPAID).execute(model);

        assertEquals(String.format(ClassPaymentCommand.MESSAGE_ALREADY_MARKED, person.getName(),
                LESSON.getSubject(), LESSON.getTiming(), "unpaid"), result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    @Test
    public void paymentCommand_equals() {
        ClassPaymentCommand command = new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getTiming(), PaymentStatus.PAID);

        assertEquals(command, command);
        assertEquals(command, new ClassPaymentCommand(Index.fromOneBased(1),
                new Subject("math"), LESSON.getTiming(), PaymentStatus.PAID));
        assertNotEquals(command, new ClassPaymentCommand(Index.fromOneBased(2),
                LESSON.getSubject(), LESSON.getTiming(), PaymentStatus.PAID));
        assertNotEquals(command, new ClassPaymentCommand(Index.fromOneBased(1),
                OTHER_LESSON.getSubject(), LESSON.getTiming(), PaymentStatus.PAID));
        assertNotEquals(command, new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), OTHER_LESSON.getTiming(), PaymentStatus.PAID));
        assertNotEquals(command, new ClassPaymentCommand(Index.fromOneBased(1),
                LESSON.getSubject(), LESSON.getTiming(), PaymentStatus.UNPAID));
        assertNotEquals(command, null);
    }

    @Test
    public void delete_existingClass_removesEveryEnrolmentIncludingFilteredOutStudents() throws CommandException {
        enrolStudentsInLesson(LESSON, 1, 2);
        showPersonAtIndex(model, Index.fromOneBased(1));

        CommandResult result = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getTiming()).execute(model);

        assertEquals(String.format(ClassDeleteCommand.MESSAGE_SUCCESS,
                LESSON.getSubject(), LESSON.getTiming(), 2, "students"), result.getFeedbackToUser());
        assertFalse(model.getAddressBook().getPersonList().stream()
                .anyMatch(person -> person.getLessons().contains(LESSON)));
    }

    @Test
    public void delete_onlyMatchingClass_preservesOtherClassesAndUnenrolledStudents() throws CommandException {
        Person untouchedPerson = model.getAddressBook().getPersonList().get(1);
        enrolStudentsInLesson(LESSON, 1);
        enrolStudentsInLesson(OTHER_LESSON, 1);

        CommandResult result = new ClassDeleteCommand(
                new Subject("MATH"), LESSON.getTiming()).execute(model);

        assertEquals(String.format(ClassDeleteCommand.MESSAGE_SUCCESS,
                new Subject("MATH"), LESSON.getTiming(), 1, "student"), result.getFeedbackToUser());
        assertEquals(Set.of(OTHER_LESSON), model.getAddressBook().getPersonList().get(0).getLessons());
        assertEquals(untouchedPerson, model.getAddressBook().getPersonList().get(1));
    }

    @Test
    public void delete_missingClass_failure() {
        ClassDeleteCommand command = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getTiming());

        assertCommandFailure(command, model, ClassDeleteCommand.MESSAGE_CLASS_NOT_FOUND);
    }

    @Test
    public void deleteCommand_equals() {
        ClassDeleteCommand command = new ClassDeleteCommand(LESSON.getSubject(), LESSON.getTiming());

        assertEquals(command, command);
        assertEquals(command, new ClassDeleteCommand(new Subject("math"), LESSON.getTiming()));
        assertNotEquals(command, new ClassDeleteCommand(OTHER_LESSON.getSubject(), LESSON.getTiming()));
        assertNotEquals(command, new ClassDeleteCommand(LESSON.getSubject(), OTHER_LESSON.getTiming()));
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
        assertCommandFailure(new ClassAddCommand(overlap.getSubject(), overlap.getCost(), overlap.getTiming()), model,
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
                java.util.Optional.empty(), java.util.Optional.of(overlap.getTiming())), model,
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
        ClassAddCommand add = new ClassAddCommand(new Subject("Math"), new Cost("30"), LESSON.getTiming());
        assertEquals(add, add);
        assertEquals(add, new ClassAddCommand(new Subject("math"), new Cost("30.00"), LESSON.getTiming()));
        assertNotEquals(add, new ClassAddCommand(new Subject("Physics"), new Cost("30"), LESSON.getTiming()));
        assertNotEquals(add, new ClassAddCommand(new Subject("Math"), new Cost("31"), LESSON.getTiming()));
        assertNotEquals(add, new ClassAddCommand(new Subject("Math"), new Cost("30"), OTHER_LESSON.getTiming()));
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
