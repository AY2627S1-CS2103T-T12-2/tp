package seedu.address.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.timetable.TimetableDay;

/** A UI component containing a day heading and all classes for that day. */
public class TimetableDayCard extends UiPart<Region> {

    private static final String FXML = "TimetableDayCard.fxml";
    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.ENGLISH);

    @FXML
    private Label dayHeading;
    @FXML
    private VBox entriesContainer;
    @FXML
    private Label emptyMessage;

    /** Creates a card for the supplied timetable day. */
    public TimetableDayCard(TimetableDay timetableDay) {
        super(FXML);
        dayHeading.setText(formatDate(timetableDay.getDate()));
        timetableDay.getEntries().stream()
                .map(TimetableEntryCard::new)
                .map(TimetableEntryCard::getRoot)
                .forEach(entriesContainer.getChildren()::add);
        emptyMessage.setVisible(timetableDay.getEntries().isEmpty());
        emptyMessage.setManaged(emptyMessage.isVisible());
    }

    static String formatDate(LocalDate date) {
        return date.format(DAY_FORMAT);
    }
}
