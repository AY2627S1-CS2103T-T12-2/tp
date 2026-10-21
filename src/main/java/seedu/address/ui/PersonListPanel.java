package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);
    private final ClassColorPalette classColorPalette;
    private final BooleanProperty privacyModeEnabled = new SimpleBooleanProperty();

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        this(personList, false);
    }

    /**
     * Creates a {@code PersonListPanel} with the given list and privacy state.
     */
    public PersonListPanel(ObservableList<Person> personList, boolean privacyModeEnabled) {
        super(FXML);
        this.privacyModeEnabled.set(privacyModeEnabled);
        classColorPalette = new ClassColorPalette(personList);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
    }

    /**
     * Updates the privacy state and refreshes all displayed person cards.
     */
    public void setPrivacyModeEnabled(boolean privacyModeEnabled) {
        if (this.privacyModeEnabled.get() == privacyModeEnabled) {
            logger.finer("Privacy mode display state is unchanged");
            return;
        }
        logger.fine("Updating person cards for privacy mode: "
                + (privacyModeEnabled ? "enabled" : "disabled"));
        this.privacyModeEnabled.set(privacyModeEnabled);
        personListView.refresh();
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1, classColorPalette,
                        privacyModeEnabled).getRoot());
            }
        }
    }

}
