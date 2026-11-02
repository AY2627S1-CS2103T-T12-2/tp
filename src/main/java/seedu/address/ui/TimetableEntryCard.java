package seedu.address.ui;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.timetable.TimetableEntry;

/** A UI component that displays one class in the timetable. */
public class TimetableEntryCard extends UiPart<Region> {

    private static final String FXML = "TimetableEntryCard.fxml";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private Label time;
    @FXML
    private Label subject;
    @FXML
    private Label students;
    @FXML
    private Label details;

    /** Creates a card that displays the supplied timetable entry. */
    public TimetableEntryCard(TimetableEntry entry) {
        this(entry, new SimpleBooleanProperty(false));
    }

    /** Creates a card that displays the supplied entry with the given privacy state. */
    TimetableEntryCard(TimetableEntry entry, ObservableBooleanValue privacyModeEnabled) {
        super(FXML);
        time.setText(formatTimeRange(entry));
        subject.setText(entry.getLesson().getSubject().toString());
        students.setText(formatStudents(entry));
        details.textProperty().bind(Bindings.createStringBinding(() ->
                PersonCard.formatClassDetails(entry.getLesson().getDetails(), privacyModeEnabled.get()),
                privacyModeEnabled));
        details.setVisible(true);
        details.setManaged(details.isVisible());
    }

    static String formatTimeRange(TimetableEntry entry) {
        return entry.getLesson().getSchedule().getStartTime().format(TIME_FORMAT)
                + "-" + entry.getLesson().getSchedule().getEndTime().format(TIME_FORMAT);
    }

    static String formatStudents(TimetableEntry entry) {
        return entry.getStudents().stream()
                .map(person -> person.getName().fullName)
                .collect(Collectors.joining(", "));
    }
}
