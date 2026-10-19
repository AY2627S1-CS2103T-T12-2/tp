package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;

/**
 * A UI component that displays a person's contact and student details.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final String ENROLLED_CLASS_ROW_STYLE_CLASS = "enrolled-class-row";
    private static final String ENROLLED_CLASS_DETAILS_STYLE_CLASS = "enrolled-class-details";
    private static final String PAYMENT_STATUS_STYLE_CLASS = "payment-status";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Comparator<Lesson> LESSON_COMPARATOR = Comparator
            .comparingInt((Lesson enrolledClass) -> enrolledClass.getTiming().getDay().getValue())
            .thenComparing(enrolledClass -> enrolledClass.getTiming().getStartTime())
            .thenComparing(enrolledClass -> enrolledClass.getTiming().getEndTime())
            .thenComparing(enrolledClass -> enrolledClass.getSubject().toString(),
                    String.CASE_INSENSITIVE_ORDER);

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
    private VBox classes;
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
        this.person = requireNonNull(person);

        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);

        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));

        person.getLessons().stream()
                .sorted(LESSON_COMPARATOR)
                .map(PersonCard::createClassRow)
                .forEach(classes.getChildren()::add);
        boolean hasEnrolledClasses = !classes.getChildren().isEmpty();
        classes.setVisible(hasEnrolledClasses);
        classes.setManaged(hasEnrolledClasses);

        String subjectText = person.getSubjects().stream()
                .map(Subject::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        subjects.setText("Subjects: " + subjectText);
        subjects.setVisible(!hasEnrolledClasses && !person.getSubjects().isEmpty());
        subjects.setManaged(subjects.isVisible());

        cost.setText(person.getCost()
                .map(value -> "Cost/hour: " + value)
                .orElse(""));
        cost.setVisible(!hasEnrolledClasses && person.getCost().isPresent());
        cost.setManaged(cost.isVisible());

        lesson.setText(person.getLesson()
                .map(value -> "Lesson: " + value)
                .orElse(""));
        lesson.setVisible(!hasEnrolledClasses && person.getLesson().isPresent());
        lesson.setManaged(lesson.isVisible());
    }

    private static HBox createClassRow(Lesson enrolledClass) {
        HBox classRow = new HBox(8);
        classRow.setAlignment(Pos.CENTER_LEFT);
        classRow.getStyleClass().add(ENROLLED_CLASS_ROW_STYLE_CLASS);
        Label classDetails = new Label(formatClassBadge(enrolledClass));
        classDetails.getStyleClass().addAll("cell_small_label", ENROLLED_CLASS_DETAILS_STYLE_CLASS);
        Label paymentStatus = new Label(enrolledClass.getPaymentStatus().toDisplayString());
        paymentStatus.getStyleClass().addAll(PAYMENT_STATUS_STYLE_CLASS,
                PAYMENT_STATUS_STYLE_CLASS + "-" + enrolledClass.getPaymentStatus().toString());
        classRow.getChildren().addAll(classDetails, paymentStatus);
        return classRow;
    }

    static String formatClassBadge(Lesson enrolledClass) {
        requireNonNull(enrolledClass);
        String day = enrolledClass.getTiming().getDay().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String start = enrolledClass.getTiming().getStartTime().format(TIME_FORMAT);
        String end = enrolledClass.getTiming().getEndTime().format(TIME_FORMAT);
        return enrolledClass.getSubject() + " · " + day + " " + start + "–" + end;
    }
}
