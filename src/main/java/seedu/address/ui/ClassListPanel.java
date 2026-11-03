package seedu.address.ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
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
        this(classes, new SimpleBooleanProperty(false));
    }

    /** Creates a panel backed by the central class list and supplied privacy state. */
    public ClassListPanel(ObservableList<TuitionClass> classes, ObservableBooleanValue privacyModeEnabled) {
        super(FXML);
        classListView.setItems(classes);
        classListView.setCellFactory(unused -> new ClassListViewCell(privacyModeEnabled));
    }

    private static class ClassListViewCell extends ListCell<TuitionClass> {
        private final ObservableBooleanValue privacyModeEnabled;

        ClassListViewCell(ObservableBooleanValue privacyModeEnabled) {
            this.privacyModeEnabled = privacyModeEnabled;
        }

        @Override
        protected void updateItem(TuitionClass tuitionClass, boolean empty) {
            super.updateItem(tuitionClass, empty);
            if (empty || tuitionClass == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new TuitionClassCard(tuitionClass, privacyModeEnabled).getRoot());
            }
        }
    }
}
