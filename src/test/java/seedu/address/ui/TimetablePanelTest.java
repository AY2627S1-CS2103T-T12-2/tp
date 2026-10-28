package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.layout.HBox;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.timetable.TimetableDay;
import seedu.address.model.timetable.TimetableEntry;
import seedu.address.model.timetable.WeekRange;
import seedu.address.testutil.PersonBuilder;

public class TimetablePanelTest {

    @BeforeAll
    public static void startJavaFx() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(initialize);
        }
        if (!ready.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX did not start within 10 seconds");
        }
    }

    @Test
    public void constructor_callbacks_populatesCurrentWeekAndAccessibleHeader() throws Exception {
        List<LocalDate> requestedDates = new ArrayList<>();

        runOnFxThread(() -> {
            TimetablePanel panel = new TimetablePanel(() -> LocalDate.of(2026, 10, 20),
                    anchorDate -> loadWeek(anchorDate, requestedDates));
            @SuppressWarnings("unchecked")
            ListView<TimetableDay> listView = (ListView<TimetableDay>) panel.getRoot()
                    .lookup("#timetableDayListView");
            Label dateRange = (Label) panel.getRoot().lookup("#dateRangeLabel");
            Button previous = (Button) panel.getRoot().lookup("#previousWeekButton");
            Button today = (Button) panel.getRoot().lookup("#todayButton");
            Button next = (Button) panel.getRoot().lookup("#nextWeekButton");
            HBox header = (HBox) panel.getRoot().lookup("#navigationHeader");

            assertNotNull(listView);
            assertEquals(List.of(LocalDate.of(2026, 10, 18)), requestedDates);
            assertEquals(WeekRange.containing(LocalDate.of(2026, 10, 20)).getDates(),
                    listView.getItems().stream().map(TimetableDay::getDate).toList());
            assertEquals("18–24 Oct 2026", dateRange.getText());
            assertEquals("Show previous week", previous.getAccessibleText());
            assertEquals("Show current week", today.getAccessibleText());
            assertEquals("Show next week", next.getAccessibleText());
            assertFalse(today.isDisabled());

            List<String> focusOrder = header.getChildren().stream()
                    .filter(Node::isFocusTraversable)
                    .map(Node::getId)
                    .toList();
            assertEquals(List.of("previousWeekButton", "todayButton", "nextWeekButton"), focusOrder);
            assertInstanceOf(Button.class, previous);
            assertInstanceOf(Button.class, today);
            assertInstanceOf(Button.class, next);

            ListCell<TimetableDay> cell = listView.getCellFactory().call(listView);
            assertNotNull(cell);
            cell.updateIndex(0);
        });
    }

    @Test
    public void previousAndNext_fire_loadAdjacentSundayAnchors() throws Exception {
        List<LocalDate> requestedDates = new ArrayList<>();

        runOnFxThread(() -> {
            TimetablePanel panel = new TimetablePanel(() -> LocalDate.of(2026, 10, 20),
                    anchorDate -> loadWeek(anchorDate, requestedDates));
            Button previous = (Button) panel.getRoot().lookup("#previousWeekButton");
            Button next = (Button) panel.getRoot().lookup("#nextWeekButton");
            Label dateRange = (Label) panel.getRoot().lookup("#dateRangeLabel");

            requestedDates.clear();
            previous.fire();
            next.fire();
            next.fire();

            assertEquals(List.of(
                    LocalDate.of(2026, 10, 11),
                    LocalDate.of(2026, 10, 18),
                    LocalDate.of(2026, 10, 25)), requestedDates);
            assertEquals("25–31 Oct 2026", dateRange.getText());
        });
    }

    @Test
    public void today_fireAlwaysRecalculatesAndReloads() throws Exception {
        AtomicReference<LocalDate> suppliedDate = new AtomicReference<>(LocalDate.of(2026, 10, 20));
        AtomicInteger dateRequests = new AtomicInteger();
        List<LocalDate> requestedDates = new ArrayList<>();

        runOnFxThread(() -> {
            TimetablePanel panel = new TimetablePanel(() -> {
                dateRequests.incrementAndGet();
                return suppliedDate.get();
            },
                    anchorDate -> loadWeek(anchorDate, requestedDates));
            Button today = (Button) panel.getRoot().lookup("#todayButton");
            Label dateRange = (Label) panel.getRoot().lookup("#dateRangeLabel");

            today.fire();
            suppliedDate.set(LocalDate.of(2026, 10, 25));
            today.fire();

            assertFalse(today.isDisabled());
            assertEquals(3, dateRequests.get());
            assertEquals(List.of(
                    LocalDate.of(2026, 10, 18),
                    LocalDate.of(2026, 10, 18),
                    LocalDate.of(2026, 10, 25)), requestedDates);
            assertEquals("25–31 Oct 2026", dateRange.getText());
        });
    }

    @Test
    public void formatDateRange_monthAndYearTransitions_returnsExpectedFormats() {
        assertEquals("18–24 Oct 2026",
                TimetablePanel.formatDateRange(WeekRange.containing(LocalDate.of(2026, 10, 20))));
        assertEquals("29 Nov – 5 Dec 2026",
                TimetablePanel.formatDateRange(WeekRange.containing(LocalDate.of(2026, 12, 1))));
        assertEquals("27 Dec 2026 – 2 Jan 2027",
                TimetablePanel.formatDateRange(WeekRange.containing(LocalDate.of(2026, 12, 31))));
    }

    @Test
    public void constructor_boundaryWeeks_disableUnavailableNavigationOnly() throws Exception {
        LocalDate earliestSunday = LocalDate.MIN.plusDays(
                Math.floorMod(DayOfWeek.SUNDAY.getValue() - LocalDate.MIN.getDayOfWeek().getValue(), 7));
        LocalDate latestSaturday = LocalDate.MAX.minusDays(
                Math.floorMod(LocalDate.MAX.getDayOfWeek().getValue() - DayOfWeek.SATURDAY.getValue(), 7));

        runOnFxThread(() -> {
            TimetablePanel earliestPanel = new TimetablePanel(() -> earliestSunday,
                    TimetablePanelTest::daysForWeek);
            TimetablePanel latestPanel = new TimetablePanel(() -> latestSaturday,
                    TimetablePanelTest::daysForWeek);

            assertTrue(((Button) earliestPanel.getRoot().lookup("#previousWeekButton")).isDisabled());
            assertFalse(((Button) earliestPanel.getRoot().lookup("#nextWeekButton")).isDisabled());
            assertFalse(((Button) earliestPanel.getRoot().lookup("#todayButton")).isDisabled());
            assertFalse(((Button) latestPanel.getRoot().lookup("#previousWeekButton")).isDisabled());
            assertTrue(((Button) latestPanel.getRoot().lookup("#nextWeekButton")).isDisabled());
            assertFalse(((Button) latestPanel.getRoot().lookup("#todayButton")).isDisabled());
        });
    }

    @Test
    public void currentAndOtherWeeks_scrollToTodayAndSunday() throws Exception {
        AtomicReference<LocalDate> suppliedDate = new AtomicReference<>(LocalDate.of(2026, 10, 20));
        AtomicReference<TimetablePanel> panelReference = new AtomicReference<>();
        AtomicInteger scrollTarget = new AtomicInteger(-1);

        runOnFxThread(() -> {
            TimetablePanel panel = new TimetablePanel(suppliedDate::get, TimetablePanelTest::daysForWeek);
            panelReference.set(panel);
            new Scene(panel.getRoot());
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            @SuppressWarnings("unchecked")
            ListView<TimetableDay> listView = (ListView<TimetableDay>) panel.getRoot()
                    .lookup("#timetableDayListView");
            listView.addEventHandler(ScrollToEvent.scrollToTopIndex(),
                    event -> scrollTarget.set(event.getScrollTarget()));
        });
        runOnFxThread(() -> assertEquals(2, scrollTarget.get()));

        runOnFxThread(() -> {
            scrollTarget.set(-1);
            Button previous = (Button) panelReference.get().getRoot().lookup("#previousWeekButton");
            previous.fire();
        });
        runOnFxThread(() -> assertEquals(0, scrollTarget.get()));

        runOnFxThread(() -> {
            scrollTarget.set(-1);
            suppliedDate.set(LocalDate.of(2026, 10, 22));
            Button today = (Button) panelReference.get().getRoot().lookup("#todayButton");
            today.fire();
        });
        runOnFxThread(() -> assertEquals(4, scrollTarget.get()));
    }

    @Test
    public void isToday_matchingAndDifferentDates_returnsExpectedResult() {
        TimetableDay tuesday = new TimetableDay(LocalDate.of(2026, 10, 20), List.of());

        assertTrue(TimetablePanel.isToday(tuesday, LocalDate.of(2026, 10, 20)));
        assertFalse(TimetablePanel.isToday(tuesday, LocalDate.of(2026, 10, 27)));
    }

    @Test
    public void renderedTimetable_maxLengthNote_wrapsAcrossMultipleLines() throws Exception {
        LocalDate currentDate = LocalDate.of(2026, 10, 20);
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Tuesday1800-1930"))
                .withDetails(new ClassDetails("", "", "x".repeat(500)));
        TimetableEntry longNoteEntry = new TimetableEntry(lesson,
                List.of(new PersonBuilder().build()));

        runOnFxThread(() -> {
            TimetablePanel panel = new TimetablePanel(() -> currentDate, anchorDate ->
                    WeekRange.containing(anchorDate).getDates().stream()
                            .map(date -> new TimetableDay(date,
                                    date.equals(currentDate) ? List.of(longNoteEntry) : List.of()))
                            .toList());
            new Scene(panel.getRoot(), 400, 600);

            panel.getRoot().applyCss();
            panel.getRoot().layout();

            Label details = (Label) panel.getRoot().lookup("#details");
            assertNotNull(details);
            assertTrue(details.getHeight() > details.getFont().getSize() * 2,
                    "A 500-character note should occupy multiple lines");
        });
    }

    private static List<TimetableDay> loadWeek(LocalDate anchorDate, List<LocalDate> requestedDates) {
        requestedDates.add(anchorDate);
        return daysForWeek(anchorDate);
    }

    private static List<TimetableDay> daysForWeek(LocalDate anchorDate) {
        return WeekRange.containing(anchorDate).getDates().stream()
                .map(date -> new TimetableDay(date, List.of()))
                .toList();
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
