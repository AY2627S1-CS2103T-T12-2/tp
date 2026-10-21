package seedu.address.ui;

import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.lesson.TuitionClass;

/** Card displaying one class and its stable ID. */
public class TuitionClassCard extends UiPart<Region> {

    private static final String FXML = "TuitionClassCard.fxml";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private Label classId;
    @FXML
    private Label subject;
    @FXML
    private Label schedule;
    @FXML
    private Label cost;

    /** Creates a card for the supplied class. */
    public TuitionClassCard(TuitionClass tuitionClass) {
        super(FXML);
        classId.setText(tuitionClass.getClassId().toString());
        subject.setText(tuitionClass.getSubject().toString());
        String day = tuitionClass.getTiming().getDay().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        schedule.setText(day + " " + tuitionClass.getTiming().getStartTime().format(TIME_FORMAT)
                + "–" + tuitionClass.getTiming().getEndTime().format(TIME_FORMAT));
        cost.setText("Cost: " + tuitionClass.getCost());
    }
}
