package seedu.address.ui;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.timetable.TimetableDay;

/** A UI component containing a day heading and all classes for that day. */
public class TimetableDayCard extends UiPart<Region> {

    private static final String FXML = "TimetableDayCard.fxml";

    @FXML
    private Label dayHeading;
    @FXML
    private VBox entriesContainer;
    @FXML
    private Label emptyMessage;

    /** Creates a card for the supplied timetable day. */
    public TimetableDayCard(TimetableDay timetableDay) {
        super(FXML);
        dayHeading.setText(formatDay(timetableDay.getDay()));
        timetableDay.getEntries().stream()
                .map(TimetableEntryCard::new)
                .map(TimetableEntryCard::getRoot)
                .forEach(entriesContainer.getChildren()::add);
        emptyMessage.setVisible(timetableDay.getEntries().isEmpty());
        emptyMessage.setManaged(emptyMessage.isVisible());
    }

    static String formatDay(DayOfWeek day) {
        return day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }
}
