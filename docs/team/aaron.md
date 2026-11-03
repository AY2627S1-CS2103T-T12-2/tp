---
  layout: default.md
  title: "Aaron Leong's Project Portfolio Page"
---

# Aaron Leong's Project Portfolio Page

GitHub: [AaronL248](https://github.com/AaronL248)

## Project: TutEasy

TutEasy is a Java desktop application that helps private tutors manage students, classes, enrolments, lesson
schedules, and payments. It combines a command-line interface with a JavaFX graphical interface and was developed
collaboratively by our five-member team from AddressBook-Level3.

This page summarizes my contributions to TutEasy. My main focus was designing and maintaining the lesson and class
management system across the command, model, storage, UI, testing, and documentation layers.

## Summary of contributions

* **Code contributed:**
  [tP Code Dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=AaronL248&breakdown=true) and
  [GitHub commits](https://github.com/AY2627S1-CS2103T-T12-2/tp/commits/master/?author=AaronL248).

## Features and enhancements

* **Lesson scheduling foundation**
  ([PR #54](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/54)):
  Introduced the core lesson model used to represent a subject, cost, and complete weekly lesson timing.
  * Integrated lessons with student creation, searching, timetable generation, JSON storage, and legacy-data
    migration.
  * Added strict schedule parsing and validation so invalid times and incomplete lesson information are rejected
    before changing application data.
  * Added model, parser, command, storage, and integration tests for the new lesson workflow.

* **Class enrolment and management commands**
  ([PR #62](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/62),
  [PR #88](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/88)):
  Built the central class-management workflow used by tutors to manage classes independently of individual students.
  * Added the `class enrol`, `class unenrol`, and `class delete` commands and their parsers, validation, feedback, and
    integration tests.
  * Added stable class IDs and an independently stored class catalogue, together with `class add`, `class list`, and
    `class edit`.
  * Connected the catalogue to the model and JSON storage, preserving student-specific agreed rates and payment
    statuses while shared class information changes.
  * Added the graphical class-list view and retained compatible subject-and-schedule command forms for existing users.

* **One-off lesson support**
  ([PR #108](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/108)):
  Helped integrate exact-date lessons with class operations and added regression coverage across commands, class
  attributes, timetable construction, parsing, and storage. This allows tutors to manage both recurring weekly classes
  and lessons that occur on one specific date.

* **Independent class-detail creation and editing**
  ([PR #231](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/231)):
  Extended `class add` and `class edit` to support teaching mode, meeting link, location, and notes independently.
  * Reused the shared `ClassDetails` validation rules and supported partial edits, clearing optional values, and
    transitions between online and offline modes.
  * Preserved class IDs, enrolments, agreed rates, payment statuses, and fields omitted from an edit.
  * Used quote-aware parsing for free-text locations and notes, including values containing command-like prefixes.
  * Added parser and integration tests for validation failures, quoted values, mode transitions, data preservation,
    and code-coverage branches.

* **Class UI readability**
  ([PR #166](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/166),
  [PR #223](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/223)):
  Improved the class, student, and timetable cards so long subjects, contact values, meeting links, locations, and notes
  wrap within the available width instead of being clipped or forcing important status labels off-screen. Added UI
  regression tests for the responsive layout.

## Reliability and validation improvements

* Improved class-command feedback for enrolment, editing overlaps, unsupported batch ID enrolment, and multiple
  unenrolment indexes
  ([PR #162](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/162),
  [PR #165](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/165),
  [PR #177](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/177),
  [PR #184](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/184)).
* Strengthened subject, cost, and schedule validation while accepting legitimate subject punctuation and normalized
  monetary formats
  ([PR #163](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/163),
  [PR #164](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/164),
  [PR #200](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/200),
  [PR #201](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/201),
  [PR #202](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/202)).
* Preserved class-ID progression across `clear` and fixed legacy-enrolment migration issues that could cause removed
  enrolments to reappear after restarting the application
  ([PR #178](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/178),
  [PR #183](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/183),
  [PR #190](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/190)).

## Documentation and testing

* Updated the Developer Guide's target-user profile and value proposition
  ([PR #46](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/46)) and maintained UML diagrams as the class model
  evolved ([PR #107](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/107)).
* Kept the User Guide and Developer Guide aligned with implemented command syntax, validation, migration, persistence,
  storage paths, rollback behavior, and timetable ordering
  ([PR #203](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/203),
  [PR #226](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/226),
  [PR #229](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/229)).
* Added and maintained unit, integration, storage, and UI tests alongside feature and bug-fix work. I also expanded
  targeted test coverage ([PR #121](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/121)) and updated representative
  sample data ([PR #120](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/120)).

## Contributions to team-based tasks

* Reviewed teammates' implementations for batch class enrolment
  ([PR #89](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/89)), batch tag assignment
  ([PR #90](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/90)), pinning
  ([PR #99](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/99)), shared lesson schedules
  ([PR #102](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/102)), and legacy subject search
  ([PR #104](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/104)).
* Maintained regression coverage and documentation when integrating teammates' schedule, class-attribute, payment, and
  persistence changes into the shared class-management model.
