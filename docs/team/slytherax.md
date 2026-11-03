---
  layout: default.md
  title: "Laxshan's Project Portfolio Page"
---

# Laxshan's Project Portfolio Page

GitHub: [Slytherax](https://github.com/Slytherax)

## Project: TutEasy

TutEasy is a Java desktop application that helps private tutors manage students, guardian contacts, classes,
lesson schedules, and individual payment statuses. It combines a command-line interface with a JavaFX graphical
interface. The project builds on AddressBook-Level3 and was developed collaboratively by our five-member team.

The following sections describe my contributions to TutEasy, with links to the corresponding pull requests.

## Features and enhancements

* **Subject search** ([PR #70](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/70)):
  Added subject-based student search so tutors can find students by the subjects they study instead of searching
  only by name. I also corrected searching for older records whose incomplete lesson information could not be
  migrated into current class enrolments
  ([PR #104](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/104)). This preserves access to useful information
  when tutors upgrade from an earlier data format.

* **Guardian contacts and class attributes**
  ([PR #103](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/103)):
  Added optional guardian contact information and class attributes, including teaching mode, meeting link,
  location, and notes. These details let tutors retain contact information for a student's guardian and practical
  information about attending a lesson. Subsequent fixes preserved shared class details and handled free-text
  notes and locations safely
  ([PR #181](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/181)).

* **Guardian editing** ([PR #233](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/233)):
  Extended `edit` to update guardian name, phone, and email independently or together. Omitted fields remain
  unchanged, while an empty guardian prefix clears that field. The change carries optional values through the
  parser and edit descriptor into the replacement student, while preserving their identity, enrolments, and pin
  state. Regression tests cover malformed values, duplicate prefixes, clearing fields, privacy feedback, and
  saved-data round trips.

* **Persistence and command reliability**:
  Hardened command parsing and made JSON saves atomic
  ([PR #191](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/191)). I also corrected packaged-app storage paths,
  prevented read-only commands from unnecessarily saving the address book, and added natural ordering for student
  names in timetable entries
  ([PR #228](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/228)). These changes help preserve data across
  launches and allow navigation when the data location is temporarily unwritable.

## Documentation

I updated the Developer Guide glossary
([PR #47](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/47)) and the model diagram for contacts and enrolments
([PR #119](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/119)). I also aligned the User Guide and Developer Guide
with current guardian validation and class-editing behavior, clarified search boundaries, and added manual regression
scenarios ([PR #244](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/244)).

## Team contributions and release work

I reviewed teammates' changes, including identifying a backward-compatibility problem in guardian-name validation
that could prevent older saved contacts from loading
([PR #238](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/238)). I also reviewed clear-command validation and its
documentation ([PR #237](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/237)).

I updated the application version
([PR #209](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/209)) and published the
[TutEasy v1.5 release](https://github.com/AY2627S1-CS2103T-T12-2/tp/releases/tag/v1.5), including the downloadable JAR.

My authored changes can be explored in the
[project commit history](https://github.com/AY2627S1-CS2103T-T12-2/tp/commits/master/?author=Slytherax).
