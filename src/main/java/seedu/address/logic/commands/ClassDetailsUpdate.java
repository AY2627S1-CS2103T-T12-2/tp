package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TuitionClass;

/** Optional class fields supplied when adding a contact; omitted fields inherit existing class values. */
public record ClassDetailsUpdate(Optional<ClassMode> mode, Optional<String> meetingLink,
        Optional<String> location, Optional<String> notes) {

    public static final ClassDetailsUpdate NONE = new ClassDetailsUpdate(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    public static final String MESSAGE_CONFLICT = "This class already has different details. "
            + "Omit the class detail fields to inherit the existing class values.";

    /** Validates explicitly supplied fields before any model changes. */
    public ClassDetailsUpdate {
        requireAllNonNull(mode, meetingLink, location, notes);
        new ClassDetails(mode.orElse(ClassMode.ONLINE), meetingLink.orElse(""),
                location.orElse(""), notes.orElse(""));
    }

    /** Returns whether no class detail fields were supplied. */
    public boolean isEmpty() {
        return mode.isEmpty() && meetingLink.isEmpty() && location.isEmpty() && notes.isEmpty();
    }

    /** Applies fields, clearing a previous meeting link when switching to offline. */
    public ClassDetails applyTo(ClassDetails existing) {
        ClassMode updatedMode = mode.orElse(existing.mode());
        String updatedLink = meetingLink.orElse(updatedMode == ClassMode.OFFLINE ? "" : existing.meetingLink());
        return new ClassDetails(updatedMode, updatedLink,
                location.orElse(existing.location()), notes.orElse(existing.notes()));
    }

    /** Inherits an existing class's fields and rejects conflicting details on a new enrolment. */
    public Lesson forEnrolment(Model model, Lesson lesson) throws CommandException {
        Optional<TuitionClass> existing = model.getTuitionClassList().stream()
                .filter(item -> item.getSubject().equals(lesson.getSubject())
                        && item.getSchedule().equals(lesson.getSchedule())).findFirst();
        try {
            ClassDetails base = existing.map(TuitionClass::getDetails).orElse(lesson.getDetails());
            ClassDetails updated = applyTo(base);
            boolean hasStudents = model.getAddressBook().getPersonList().stream()
                    .flatMap(person -> person.getLessons().stream()).anyMatch(item -> item.isSameClass(lesson));
            boolean canFillEmptyClass = !hasStudents && base.equals(ClassDetails.EMPTY);
            if (existing.isPresent() && !updated.equals(base) && !canFillEmptyClass) {
                throw new CommandException(MESSAGE_CONFLICT);
            }
            return existing.isPresent() ? lesson.withTuitionClass(existing.get().withDetails(updated))
                    : lesson.withDetails(updated);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }
}
