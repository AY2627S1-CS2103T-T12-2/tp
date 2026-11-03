---
  layout: default.md
  title: "Aarav Jayapalan's Project Portfolio Page"
---

# Aarav Jayapalan's Project Portfolio Page

GitHub: [purpleshark74](https://github.com/purpleshark74)

## Overview

TutEasy is a Java desktop application for private tutors to manage students, guardians, classes, lesson schedules, and
payments. It combines a command-line interface with a JavaFX interface and was built collaboratively by our team.

My main responsibility was developing the lesson-scheduling and timetable experience. I worked across the model,
storage, logic, UI, testing, and documentation layers to turn recurring and one-off class data into a navigable weekly
timetable.

## Summary of Contributions

* **Code contributed:** [tP Code Dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=purpleshark74&breakdown=true)
  and [GitHub commits](https://github.com/AY2627S1-CS2103T-T12-2/tp/commits/master/?author=purpleshark74).

### Enhancements implemented

* **Graphical timetable view**
  ([PR #55](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/55)):
  * Implemented the `timetable` command and its end-to-end flow through the logic, model, and JavaFX UI layers.
  * Built the timetable projection model and the panel, day-card, and entry-card UI components. The view groups
    classes by day, orders them consistently, displays enrolled students, and includes empty days so that the whole
    week remains visible.
  * Added automated tests across commands, logic, model, and UI components. This was a cross-cutting enhancement that
    changed 32 files and added dedicated timetable model and UI abstractions rather than placing scheduling logic in
    the view.

* **Recurring and one-off lesson scheduling**
  ([PR #102](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/102)):
  * Generalised the original weekly-only lesson representation into a shared immutable `LessonSchedule` abstraction,
    implemented by weekly `LessonTiming` and exact-date `OneOffLessonSchedule` values.
  * Integrated the abstraction with class creation, enrolment, payment, deletion, search, student cards, and the
    timetable. Added exact-date lesson searches while preserving the existing weekly command format.
  * Extended JSON storage with an explicit schedule type and a migration path that interprets older records as weekly
    schedules. This kept existing user data loadable without manual conversion.
  * Added focused unit and integration tests for schedule validation, persistence, migration, ordering, class
    operations, searching, and timetable projection. The completeness of this change required coordinated updates
    across more than 30 production and test files.

* **Date-backed weekly navigation**
  ([PR #105](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/105)):
  * Added a `WeekRange` model that normalises any date to a complete Sunday-to-Saturday week and safely handles the
    boundaries of Java's supported date range.
  * Added Previous week, Next week, and Today controls, dated day headings, selected-range feedback, current-day
    highlighting, and sensible initial scrolling.
  * Kept the timetable read-only by refreshing it through logic callbacks rather than mutating stored data. Injected
    clock-aware queries also made date-sensitive behaviour deterministic and testable.
  * Added extensive UI and model tests for navigation, reloading, boundary dates, highlighting, and week layout.

* **Schedule-conflict warnings**
  ([PR #69](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/69)):
  * Warned tutors when adding a student or class would create overlapping lesson times, while still allowing them to
    decide whether the real-world overlap is acceptable.
  * Covered overlap and non-overlap boundaries with tests. The logic later became date-aware through the shared
    scheduling model: weekly schedules conflict by weekday, one-off schedules by exact date, and endpoint-only
    contact is not treated as an overlap.

* **Reliability and validation improvements:**
  * Enforced bounded lengths for student and guardian contact fields, with model tests and matching User Guide rules
    ([PR #220](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/220)).
  * Made address parsing quote-aware so addresses can safely contain text that resembles command prefixes, and added
    parser regression tests for both `add` and `edit`
    ([PR #225](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/225)).
  * Tightened `clear` so it rejects unexpected arguments and preserves the user's data on malformed input
    ([PR #237](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/237)).
  * Fixed truncated timetable notes ([PR #158](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/158)) and corrected
    class-search matching and its tests ([PR #250](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/250)).

### Contributions to the User Guide

* Documented the complete `timetable` workflow, including its read-only behaviour, navigation controls, ordering,
  current-day treatment, empty classes, and the differences between weekly and one-off schedules
  ([PR #105](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/105),
  [PR #124](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/124)).
* Documented weekly and exact-date schedule formats and exact-date `find` behaviour as part of the scheduling feature
  ([PR #102](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/102)).
* Updated validation and command behaviour for contact-length limits, quoted addresses, `clear`, class search, class
  examples, enrolment feedback, and the command summary
  ([PR #155](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/155),
  [PR #156](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/156),
  [PR #157](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/157),
  [PR #220](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/220),
  [PR #225](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/225),
  [PR #237](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/237),
  [PR #246](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/246), and
  [PR #250](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/250)).

### Contributions to the Developer Guide

* Wrote the **Lesson scheduling model** implementation section, explaining the schedule abstraction, validation,
  class identity, date-aware overlap rules, JSON representation, and backward-compatible migration.
* Wrote the **Timetable view** implementation section and created the timetable sequence diagram to show how the
  command, UI, logic callbacks, model projection, and weekly navigation collaborate
  ([PR #124](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/124)).
* Added and refined project user stories
  ([PR #36](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/36),
  [PR #45](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/45)).
* Updated the parser class diagram to reflect the project's expanded command structure
  ([PR #111](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/111)) and updated the architecture diagram and
  documentation structure as the product evolved
  ([PR #113](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/113),
  [PR #124](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/124)).

### Contributions to team-based tasks

* Maintained automated tests alongside feature work, including model, storage, parser, command, logic, and JavaFX UI
  tests for the scheduling and timetable subsystems.
* Kept product terminology and behaviour aligned across the User Guide, Developer Guide, command summary, diagrams,
  and application feedback
  ([PR #64](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/64),
  [PR #246](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/246)).
* Updated application versions for milestone releases
  ([PR #72](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/72),
  [PR #73](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/73),
  [PR #251](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/251)).

### Review and integration contributions

I helped integrate teammates' work by merging pull requests across the product, including the extended
student search ([PR #52](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/52)), lesson and class foundations
([PR #54](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/54),
[PR #88](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/88)), pinning
([PR #99](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/99)), model and UML updates
([PR #118](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/118),
[PR #119](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/119)), and final privacy and documentation fixes
([PR #243](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/243),
[PR #244](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/244)). I also resolved substantial merge conflicts
while integrating the generalised scheduling model with concurrent class-management work.
