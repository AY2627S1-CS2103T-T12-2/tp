# Lesson Implementation Handoff

## Terminology

The Java domain class is named `Lesson` and is located at:

```text
seedu.address.model.lesson.Lesson
```

The user-facing documentation and commands call it a **class**. In this implementation, a `Lesson` represents a
recurring weekly tuition class, not an individual dated occurrence.

## Ownership model

Classes cannot exist independently of students. There is no global lesson collection in `Model` or `AddressBook`.
Each `Person` owns an immutable view of their classes:

```java
private final Set<Lesson> lessons;

public Set<Lesson> getLessons();
```

`Person#getLessons()` returns an unmodifiable set. A student may have no classes or multiple classes.

Multiple students may share the same logical class by containing equal `Lesson` values. The timetable groups those
equal values and displays all associated students. No empty or orphan class is stored.

## Lesson API

```java
public class Lesson {
    private final Subject subject;
    private final Cost cost;
    private final LessonTiming timing;

    public Lesson(Subject subject, Cost cost, LessonTiming timing);

    public Subject getSubject();
    public Cost getCost();
    public LessonTiming getTiming();
    public boolean isSameClass(Lesson otherLesson);
}
```

Supporting value classes are under `seedu.address.model.lesson`:

- `Subject`: one non-blank alphanumeric subject, with case-insensitive equality.
- `Cost`: a non-negative decimal with at most two decimal places.
- `LessonTiming`: a weekly day, start time, and end time.
- `LessonStart`: a day and start time used by `find c/`.

`LessonTiming` accepts `DAYHHmm-HHmm`, for example `Monday1800-1930`. The end time must be later than the start time.

## Class identity and cost invariant

A logical class is identified by:

- subject, case-insensitively; and
- complete lesson timing: day, start time, and end time.

Cost is deliberately not part of `Lesson#isSameClass`. All students in the same logical class must have the same cost.
Commands reject a new class assignment if an existing student has the same subject and timing with a different cost.
JSON loading also rejects saved data that violates this invariant.

`Lesson#equals` includes subject, cost, and timing. Because the shared-cost invariant is enforced, equal class values
can be used safely as timetable map keys.

## Commands

### Add a student without a class

```text
add n/Alice Tan p/92345678 e/alice@gmail.com a/20 Tampines Street
```

### Add a student with their first class

```text
add n/John Davis p/91234567 e/johndavis@gmail.com a/12 Clementi Road sub/Math cost/30 c/Monday1800-1930
```

The `sub/`, `cost/`, and `c/` fields are an all-or-nothing group.

### Add another class to an existing student

```text
list
addclass 1 sub/Physics cost/35 c/Wednesday1700-1830
```

`addclass` uses the student's currently displayed index. Adding the same logical class to the same student twice is
rejected.

There is intentionally no `addlesson` command and no `enrol` command because classes may not exist without students.

### Find students by class start

```text
find c/Monday1800
```

This returns students with any class beginning on Monday at 18:00.

### Display the timetable

```text
timetable
```

`TimetableCommand` iterates through `model.getAddressBook().getPersonList()`, then through each person's lessons. It
groups equal lessons, sorts them by day and start time, and produces a textual weekly timetable in the result display.
There is currently no separate graphical calendar panel.

## Persistence

Lessons are nested under each person in JSON through `JsonAdaptedPerson` and `JsonAdaptedLesson`. There is no root-level
lesson list and no lesson ID/reference system. The previous `subjects`, `cost`, and start-only `lesson` properties are
still read and retained. When all three are present, each subject is migrated to a lesson using the stored start and a
one-hour duration (capped at 23:59). This prevents a save from silently discarding existing lesson data.

## Search modes

The supported `find` forms are:

```text
find KEYWORD [MORE_KEYWORDS]
find t/TAG
find c/DAYHHmm
```

Name, tag, and class-time modes cannot be combined in one command.

## Important implementation files

```text
src/main/java/seedu/address/model/lesson/Lesson.java
src/main/java/seedu/address/model/lesson/Subject.java
src/main/java/seedu/address/model/lesson/Cost.java
src/main/java/seedu/address/model/lesson/LessonTiming.java
src/main/java/seedu/address/model/lesson/LessonStart.java
src/main/java/seedu/address/model/person/Person.java
src/main/java/seedu/address/logic/commands/AddClassCommand.java
src/main/java/seedu/address/logic/commands/TimetableCommand.java
src/main/java/seedu/address/logic/parser/AddClassCommandParser.java
src/main/java/seedu/address/storage/JsonAdaptedLesson.java
src/main/java/seedu/address/storage/JsonAdaptedPerson.java
```

## Tests and verification

Relevant new tests cover:

- lesson equality and timing validation;
- creating a student with an initial class;
- adding classes to existing students;
- attaching the same class to multiple students;
- rejecting different costs for the same class;
- finding students by class start time;
- timetable grouping; and
- JSON round trips and inconsistent-cost rejection.

The last complete verification command was:

```text
./gradlew check
```

It passed, including tests and Checkstyle. The changes are currently present in the working tree and have not been
committed by Codex.
