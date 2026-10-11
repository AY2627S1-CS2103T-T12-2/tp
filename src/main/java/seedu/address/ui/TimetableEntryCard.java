package seedu.address.ui;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

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

    /** Creates a card that displays the supplied timetable entry. */
    public TimetableEntryCard(TimetableEntry entry) {
        super(FXML);
        time.setText(formatTimeRange(entry));
        subject.setText(entry.getLesson().getSubject().toString());
        students.setText(formatStudents(entry));
    }

    static String formatTimeRange(TimetableEntry entry) {
        return entry.getLesson().getTiming().getStartTime().format(TIME_FORMAT)
                + "-" + entry.getLesson().getTiming().getEndTime().format(TIME_FORMAT);
    }

    static String formatStudents(TimetableEntry entry) {
        return entry.getStudents().stream()
                .map(person -> person.getName().fullName)
                .collect(Collectors.joining(", "));
    }
}
