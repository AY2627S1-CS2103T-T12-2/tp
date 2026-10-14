package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class SubjectSearchIntegrationTest {

    @Test
    public void search_allStudentsOnce_preservesDataAndListResetsFilter() throws Exception {
        Model model = new ModelManager();
        Person alice = new PersonBuilder().withName("Alice").withLessons(
                lesson("Math", "Monday1800-1930"), lesson("Mathematics", "Tuesday1800-1930")).build();
        Person bob = new PersonBuilder().withName("Bob").withLessons(lesson("Physics", "Friday1800-1930")).build();
        Person carol = new PersonBuilder().withName("Carol").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(carol);
        List<Person> originalStudents = List.copyOf(model.getAddressBook().getPersonList());
        AddressBookParser parser = new AddressBookParser();

        parser.parseCommand("find Carol").execute(model);
        CommandResult result = parser.parseCommand("find sub/math PHYS").execute(model);
        assertEquals(List.of(alice, bob), model.getFilteredPersonList());
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2), result.getFeedbackToUser());
        assertEquals(originalStudents, model.getAddressBook().getPersonList());

        result = parser.parseCommand("find sub/Chemistry").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0), result.getFeedbackToUser());
        parser.parseCommand("list").execute(model);
        assertEquals(originalStudents, model.getFilteredPersonList());
    }

    private Lesson lesson(String subject, String timing) {
        return new Lesson(new Subject(subject), new Cost("30"), new LessonTiming(timing));
    }
}
