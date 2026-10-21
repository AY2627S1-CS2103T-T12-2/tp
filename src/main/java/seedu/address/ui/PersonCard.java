package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
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

    public static final String PRIVACY_MASK = "*******";

    private static final String FXML = "PersonListCard.fxml";
    private static final String ENROLLED_CLASS_GROUP_STYLE_CLASS = "enrolled-class-group";
    private static final String ENROLLED_CLASS_BADGE_STYLE_CLASS = "enrolled-class-badge";
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
    private Label pinned;
    @FXML
    private Label balance;
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
        this(person, displayedIndex, false);
    }

    /**
     * Creates a PersonCard displaying the given person and index with the supplied privacy state.
     */
    public PersonCard(Person person, int displayedIndex, boolean privacyModeEnabled) {
        this(person, displayedIndex, new ClassColorPalette(), new SimpleBooleanProperty(privacyModeEnabled));
    }

    /**
     * Creates a PersonCard using {@code classColorPalette} for its enrolled-class badges.
     */
    PersonCard(Person person, int displayedIndex, ClassColorPalette classColorPalette) {
        this(person, displayedIndex, classColorPalette, new SimpleBooleanProperty(false));
    }

    /**
     * Creates a PersonCard using {@code classColorPalette} and the supplied observable privacy state.
     */
    PersonCard(Person person, int displayedIndex, ClassColorPalette classColorPalette,
            ObservableBooleanValue privacyModeEnabled) {
        super(FXML);
        this.person = requireNonNull(person);
        requireNonNull(classColorPalette);
        requireNonNull(privacyModeEnabled);

        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        pinned.setVisible(person.isPinned());
        pinned.setManaged(person.isPinned());
        balance.setText("Balance: " + person.getOutstandingBalance().toPlainString());
        bindContactDetail(phone, "Phone", person.getPhone().value, privacyModeEnabled);
        bindContactDetail(email, "Email", person.getEmail().value, privacyModeEnabled);
        bindContactDetail(address, "Address", person.getAddress().value, privacyModeEnabled);

        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));

        person.getLessons().stream()
                .sorted(LESSON_COMPARATOR)
                .map(enrolledClass -> createClassGroup(enrolledClass, classColorPalette))
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

    private static void bindContactDetail(Label label, String fieldName, String value,
            ObservableBooleanValue privacyModeEnabled) {
        String maskedValue = fieldName + ": " + PRIVACY_MASK;
        label.textProperty().bind(Bindings.when(privacyModeEnabled).then(maskedValue).otherwise(value));
    }

    private static HBox createClassGroup(Lesson enrolledClass, ClassColorPalette classColorPalette) {
        HBox classGroup = new HBox(5);
        classGroup.setAlignment(Pos.CENTER_LEFT);
        classGroup.getStyleClass().add(ENROLLED_CLASS_GROUP_STYLE_CLASS);
        Label classBadge = new Label(formatClassBadge(enrolledClass));
        classBadge.getStyleClass().addAll(ENROLLED_CLASS_BADGE_STYLE_CLASS,
                classColorPalette.getStyleClass(enrolledClass));
        Label paymentStatus = new Label(enrolledClass.getPaymentStatus().toDisplayString());
        paymentStatus.getStyleClass().addAll(PAYMENT_STATUS_STYLE_CLASS,
                PAYMENT_STATUS_STYLE_CLASS + "-" + enrolledClass.getPaymentStatus().toString());
        classGroup.getChildren().addAll(classBadge, paymentStatus);
        return classGroup;
    }

    static String formatClassBadge(Lesson enrolledClass) {
        requireNonNull(enrolledClass);
        String day = enrolledClass.getTiming().getDay().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String start = enrolledClass.getTiming().getStartTime().format(TIME_FORMAT);
        String end = enrolledClass.getTiming().getEndTime().format(TIME_FORMAT);
        return enrolledClass.getSubject() + " · " + day + " " + start + "–" + end;
    }
}
