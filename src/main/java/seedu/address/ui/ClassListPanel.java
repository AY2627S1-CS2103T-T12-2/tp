package seedu.address.ui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.lesson.TuitionClass;

/** Read-only panel showing every independently stored tuition class. */
public class ClassListPanel extends UiPart<Region> {

    private static final String FXML = "ClassListPanel.fxml";

    @FXML
    private ListView<TuitionClass> classListView;

    /** Creates a panel backed by the central class list. */
    public ClassListPanel(ObservableList<TuitionClass> classes) {
        super(FXML);
        classListView.setItems(classes);
        classListView.setCellFactory(unused -> new ClassListViewCell());
    }

    private static class ClassListViewCell extends ListCell<TuitionClass> {
        @Override
        protected void updateItem(TuitionClass tuitionClass, boolean empty) {
            super.updateItem(tuitionClass, empty);
            if (empty || tuitionClass == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new TuitionClassCard(tuitionClass).getRoot());
            }
        }
    }
}
