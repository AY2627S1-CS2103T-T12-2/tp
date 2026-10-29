package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.timetable.TimetableDay;
import seedu.address.model.timetable.WeekRange;

/** Panel containing one navigable Sunday-to-Saturday timetable week. */
public class TimetablePanel extends UiPart<Region> {

    private static final String FXML = "TimetablePanel.fxml";
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("d", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_MONTH_FORMAT = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_MONTH_YEAR_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);

    private final Supplier<LocalDate> currentDateSupplier;
    private final Function<LocalDate, List<TimetableDay>> timetableLoader;

    private LocalDate currentDate;
    private WeekRange selectedWeek;

    @FXML
    private ListView<TimetableDay> timetableDayListView;
    @FXML
    private Button previousWeekButton;
    @FXML
    private Label dateRangeLabel;
    @FXML
    private Button todayButton;
    @FXML
    private Button nextWeekButton;

    /** Creates a scrollable timetable panel backed by the supplied date and timetable callbacks. */
    public TimetablePanel(Supplier<LocalDate> currentDateSupplier,
            Function<LocalDate, List<TimetableDay>> timetableLoader) {
        super(FXML);
        this.currentDateSupplier = requireNonNull(currentDateSupplier);
        this.timetableLoader = requireNonNull(timetableLoader);
        timetableDayListView.setCellFactory(listView -> new TimetableDayListViewCell());
        showCurrentWeek();
    }

    @FXML
    private void handlePreviousWeek() {
        selectedWeek.previous().ifPresent(this::loadWeek);
    }

    @FXML
    private void handleToday() {
        showCurrentWeek();
    }

    @FXML
    private void handleNextWeek() {
        selectedWeek.next().ifPresent(this::loadWeek);
    }

    private void showCurrentWeek() {
        currentDate = requireNonNull(currentDateSupplier.get());
        loadWeek(WeekRange.containing(currentDate));
    }

    private void loadWeek(WeekRange week) {
        selectedWeek = requireNonNull(week);
        List<TimetableDay> timetableDays = requireNonNull(timetableLoader.apply(week.getStart()));
        timetableDayListView.setItems(FXCollections.observableArrayList(timetableDays));
        dateRangeLabel.setText(formatDateRange(week));
        previousWeekButton.setDisable(week.previous().isEmpty());
        nextWeekButton.setDisable(week.next().isEmpty());
        todayButton.setDisable(false);
        timetableDayListView.refresh();
        scheduleScroll(week);
    }

    private void scheduleScroll(WeekRange week) {
        int targetIndex = week.equals(WeekRange.containing(currentDate))
                ? indexOfDate(currentDate)
                : 0;
        Platform.runLater(() -> timetableDayListView.scrollTo(Math.max(targetIndex, 0)));
    }

    private int indexOfDate(LocalDate date) {
        return IntStream.range(0, timetableDayListView.getItems().size())
                .filter(index -> timetableDayListView.getItems().get(index).getDate().equals(date))
                .findFirst()
                .orElse(-1);
    }

    static boolean isToday(TimetableDay timetableDay, LocalDate currentDate) {
        return timetableDay.getDate().equals(currentDate);
    }

    static String formatDateRange(WeekRange week) {
        LocalDate start = week.getStart();
        LocalDate end = week.getEnd();
        if (start.getYear() != end.getYear()) {
            return start.format(DAY_MONTH_YEAR_FORMAT) + " – " + end.format(DAY_MONTH_YEAR_FORMAT);
        }
        if (start.getMonth() != end.getMonth()) {
            return start.format(DAY_MONTH_FORMAT) + " – " + end.format(DAY_MONTH_YEAR_FORMAT);
        }
        return start.format(DAY_FORMAT) + "–" + end.format(DAY_MONTH_YEAR_FORMAT);
    }

    /** A cell that displays one complete timetable day. */
    private class TimetableDayListViewCell extends ListCell<TimetableDay> {
        @Override
        protected void updateItem(TimetableDay timetableDay, boolean empty) {
            super.updateItem(timetableDay, empty);
            if (empty || timetableDay == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region dayCard = new TimetableDayCard(timetableDay,
                        isToday(timetableDay, currentDate)).getRoot();
                dayCard.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> Math.max(0,
                        getWidth() - snappedLeftInset() - snappedRightInset()), widthProperty(), insetsProperty()));
                setGraphic(dayCard);
            }
        }
    }
}
