package seedu.address.ui;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.timetable.TimetableDay;

/** Panel containing the timetable groups from the current day through Saturday. */
public class TimetablePanel extends UiPart<Region> {

    private static final String FXML = "TimetablePanel.fxml";

    @FXML
    private ListView<TimetableDay> timetableDayListView;

    /** Creates a scrollable timetable panel from the supplied day groups. */
    public TimetablePanel(List<TimetableDay> timetableDays) {
        super(FXML);
        timetableDayListView.setItems(FXCollections.observableArrayList(timetableDays));
        timetableDayListView.setCellFactory(listView -> new TimetableDayListViewCell());
    }

    /** A cell that displays one complete timetable day. */
    private static class TimetableDayListViewCell extends ListCell<TimetableDay> {
        @Override
        protected void updateItem(TimetableDay timetableDay, boolean empty) {
            super.updateItem(timetableDay, empty);
            if (empty || timetableDay == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new TimetableDayCard(timetableDay).getRoot());
            }
        }
    }
}
