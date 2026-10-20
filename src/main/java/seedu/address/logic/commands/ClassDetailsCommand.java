package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;

/** Updates the shared details of an existing class for every enrolled student. */
public class ClassDetailsCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "details";
    public static final String MESSAGE_USAGE = "class details sub/SUBJECT c/DAYHHmm-HHmm "
            + "[link/MEETING_URL] [location/LOCATION] [notes/NOTES]\n"
            + "Supply at least one detail. An empty detail clears that field for everyone in the class.";
    public static final String MESSAGE_NOT_FOUND = "No students are enrolled in the specified class.";
    public static final String MESSAGE_SUCCESS = "Updated details for %1$s (%2$s): %3$s";

    private final Subject subject;
    private final LessonTiming timing;
    private final Optional<String> meetingLink;
    private final Optional<String> location;
    private final Optional<String> notes;

    /** Creates an update. Absent fields are retained; present empty strings clear a field. */
    public ClassDetailsCommand(Subject subject, LessonTiming timing, Optional<String> meetingLink,
            Optional<String> location, Optional<String> notes) {
        requireAllNonNull(subject, timing, meetingLink, location, notes);
        if (meetingLink.isEmpty() && location.isEmpty() && notes.isEmpty()) {
            throw new IllegalArgumentException("At least one class detail must be supplied.");
        }
        new ClassDetails(meetingLink.orElse(""), location.orElse(""), notes.orElse(""));
        this.subject = subject;
        this.timing = timing;
        this.meetingLink = meetingLink;
        this.location = location;
        this.notes = notes;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireAllNonNull(model);
        List<Person> students = List.copyOf(model.getAddressBook().getPersonList());
        Lesson existing = students.stream().flatMap(person -> person.getLessons().stream())
                .filter(lesson -> lesson.isSameClass(subject, timing)).findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_NOT_FOUND));
        ClassDetails oldDetails = existing.getDetails();
        ClassDetails updatedDetails = new ClassDetails(meetingLink.orElse(oldDetails.meetingLink()),
                location.orElse(oldDetails.location()), notes.orElse(oldDetails.notes()));
        for (Person person : students) {
            if (person.getLessons().stream().anyMatch(lesson -> lesson.isSameClass(subject, timing))) {
                Set<Lesson> updatedLessons = person.getLessons().stream()
                        .map(lesson -> lesson.isSameClass(subject, timing)
                                ? lesson.withDetails(updatedDetails) : lesson)
                        .collect(Collectors.toSet());
                model.setPerson(person, person.withLessons(updatedLessons));
            }
        }
        String description = updatedDetails.isEmpty() ? "No details" : updatedDetails.toString();
        return new CommandResult(String.format(MESSAGE_SUCCESS, subject, timing, description));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassDetailsCommand command
                && subject.equals(command.subject) && timing.equals(command.timing)
                && meetingLink.equals(command.meetingLink) && location.equals(command.location)
                && notes.equals(command.notes);
    }
}
