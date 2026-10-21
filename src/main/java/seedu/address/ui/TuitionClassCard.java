package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.lesson.TuitionClass;

/** Card displaying one class and its stable ID. */
public class TuitionClassCard extends UiPart<Region> {

    private static final String FXML = "TuitionClassCard.fxml";
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
        String scheduleText = tuitionClass.getSchedule().toString();
        int timeSeparator = scheduleText.lastIndexOf('-');
        schedule.setText(timeSeparator < 0 ? scheduleText
                : scheduleText.substring(0, timeSeparator) + "–" + scheduleText.substring(timeSeparator + 1));
        cost.setText("Cost: " + tuitionClass.getCost());
    }
}
