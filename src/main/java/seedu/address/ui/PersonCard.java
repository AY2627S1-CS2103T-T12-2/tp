package seedu.address.ui;

import java.util.Comparator;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * A UI component that displays a person's contact and student details.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private FlowPane tags;
    @FXML
    private Label subjects;
    @FXML
    private Label cost;
    @FXML
    private Label lesson;

    /**
     * Creates a PersonCard displaying the given person and index.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;

        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);

        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));

        String subjectText = person.getSubjects().stream()
                .map(Subject::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        subjects.setText("Subjects: " + subjectText);
        subjects.setVisible(!person.getSubjects().isEmpty());
        subjects.setManaged(subjects.isVisible());

        cost.setText(person.getCost()
                .map(value -> "Cost/hour: " + value)
                .orElse(""));
        cost.setVisible(person.getCost().isPresent());
        cost.setManaged(cost.isVisible());

        lesson.setText(person.getLesson()
                .map(value -> "Lesson: " + value)
                .orElse(""));
        lesson.setVisible(person.getLesson().isPresent());
        lesson.setManaged(lesson.isVisible());
    }
}
