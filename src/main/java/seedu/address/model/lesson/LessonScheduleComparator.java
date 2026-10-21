package seedu.address.model.lesson;

import java.util.Comparator;

/** Provides the stable display order for weekly and one-off lesson schedules. */
public final class LessonScheduleComparator implements Comparator<LessonSchedule> {

    public static final LessonScheduleComparator INSTANCE = new LessonScheduleComparator();

    private LessonScheduleComparator() {
    }

    @Override
    public int compare(LessonSchedule first, LessonSchedule second) {
        int typeComparison = first.getType().compareTo(second.getType());
        if (typeComparison != 0) {
            return typeComparison;
        }

        int dateComparison = first.getDate().orElse(java.time.LocalDate.MIN)
                .compareTo(second.getDate().orElse(java.time.LocalDate.MIN));
        if (dateComparison != 0) {
            return dateComparison;
        }

        int dayComparison = first.getDay().compareTo(second.getDay());
        if (dayComparison != 0) {
            return dayComparison;
        }

        int startComparison = first.getStartTime().compareTo(second.getStartTime());
        return startComparison != 0 ? startComparison : first.getEndTime().compareTo(second.getEndTime());
    }
}
