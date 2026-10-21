---
  layout: default.md
  title: "Testing guide"
  pageNav: 3
---

# Testing guide

<!-- * Table of Contents -->
<page-nav-print />

<!-- -------------------------------------------------------------------------------------------------------------------- -->

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<box type="info" seamless>

**Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</box>

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`

## Subject search tests

| Test class | Coverage |
| --- | --- |
| `SubjectFindCommandParserTest` | Subject syntax, whitespace, numeric levels, duplicate prefixes, invalid keywords, mixed modes, and unchanged delegation of existing search modes. |
| `SubjectContainsKeywordsPredicateTest` | Partial and case-insensitive OR matching, multiple classes, numeric levels, unrelated fields, legacy subject-only data, and stale metadata after in-memory unenrolment. |
| `SubjectSearchIntegrationTest` | Complete-list searching, unique results, feedback counts, stable ordering, and returning to the full list. |
| `SubjectSearchLogicIntegrationTest` | Real JSON save/reload, preserved enrolments, unchanged data and results after invalid input, timetable restrictions, and deletion using displayed indices. |

Run the subject-search tests and style checks with:

```sh
./gradlew test --tests '*Subject*Test' checkstyleMain checkstyleTest
```

For the full regression suite, run:

```sh
./gradlew test checkstyleMain checkstyleTest
```

If tests pass but the separate `jacocoTestReport` task fails, inspect the coverage-report error independently.
To run tests and style checks without generating that report, append `-x jacocoTestReport` to either command.

The [Developer Guide](DeveloperGuide.md#searching-by-subject) provides manual subject-search test scenarios.

## Lesson scheduling tests

| Test class | Coverage |
| --- | --- |
| `LessonTimingTest` | Weekly schedule validation, occurrence matching, representations, ordering, and overlaps. |
| `OneOffLessonScheduleTest` | Date validation, exact-date occurrence matching, representations, ordering, equality, and the mixed-schedule overlap matrix. |
| `LessonScheduleComparatorTest` | Stable ordering of weekly and one-off schedules. |
| `JsonAdaptedLessonTest` | Legacy weekly migration, schedule-type validation, and weekly/one-off conversion. |
| `ClassCommandParserTest` | Weekly and one-off parsing for enrolment, unenrolment, deletion, and payment commands. |
| `ClassCommandsIntegrationTest` | Schedule-aware identity, cost conflicts, and overlap rejection across class operations. |
| `TimetableBuilderTest` | Weekly occurrence projection and exact-date placement of one-off classes. |

Run the focused scheduling tests and style checks with:

```sh
./gradlew test --tests '*Lesson*Test' --tests '*Class*Test' --tests '*Timetable*Test' checkstyleMain checkstyleTest
```
