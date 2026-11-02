package seedu.address.ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
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
    @FXML
    private Text details;

    /** Creates a card for the supplied class. */
    public TuitionClassCard(TuitionClass tuitionClass) {
        this(tuitionClass, new SimpleBooleanProperty(false));
    }

    /** Creates a card for the supplied class and privacy state. */
    TuitionClassCard(TuitionClass tuitionClass, ObservableBooleanValue privacyModeEnabled) {
        super(FXML);
        classId.setText(tuitionClass.getClassId().toString());
        subject.setText(tuitionClass.getSubject().toString());
        String scheduleText = tuitionClass.getSchedule().toString();
        int timeSeparator = scheduleText.lastIndexOf('-');
        schedule.setText(timeSeparator < 0 ? scheduleText
                : scheduleText.substring(0, timeSeparator) + "–" + scheduleText.substring(timeSeparator + 1));
        cost.setText("Cost: " + tuitionClass.getCost());
        details.textProperty().bind(Bindings.createStringBinding(() ->
                PersonCard.formatClassDetails(tuitionClass.getDetails(), privacyModeEnabled.get()),
                privacyModeEnabled));
    }
}
