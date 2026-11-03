---
  layout: default.md
  title: "Wen Yi Liang's Project Portfolio Page"
---

# Wen Yi Liang's Project Portfolio Page

## Project: TutEasy

TutEasy is a desktop application for private tutors who manage students, classes, enrolments, and lesson schedules. It
combines a fast command-line workflow with a JavaFX interface that presents student and class information clearly.

This page summarizes my contributions as a full-stack developer. I worked across the command, model, storage, UI,
testing, and documentation layers.

## Summary of contributions

* **Code contributed:** [tP Code Dashboard](https://nus-cs2103-ay2627-s1.github.io/tp-dashboard/?search=wenyiliangg&breakdown=true)
  and [GitHub commits](https://github.com/AY2627S1-CS2103T-T12-2/tp/commits?author=wenyiliangg).

### Enhancements implemented

* **Tag management:** Implemented the original `tag` and `untag` workflows for adding, removing, and clearing student
  tags ([#53](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/53)).
  * Added commands, parsers, validation, immutable person updates, storage support, and comprehensive unit and
    integration tests.
  * Styled tags as readable labels on student cards and documented the command formats in the User Guide.
  * Improved backward compatibility by normalizing legacy tag names, loading older records above the new tag limit,
    and ensuring tag changes preserve unrelated student details
    ([#61](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/61)).

* **Enrolled-class display:** Added class badges to student cards so tutors can see each student's enrolled classes at
  a glance ([#71](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/71)).
  * Designed a deterministic colour palette that gives the same logical class a consistent colour across students.
  * Sorted displayed classes consistently and updated the FXML, CSS, Developer Guide, User Guide, and UI tests.

* **Pin feature:** Implemented `pin` and `unpin` end to end so tutors can keep frequently accessed students at the top
  of the student list ([#99](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/99)).
  * Added commands, parsers, model ordering, JSON persistence, command suggestions, a visual `PINNED` badge,
    documentation, and tests.
  * Ensured pin state survives edits and application restarts while remaining backward compatible with older data.
  * Integrated pin ordering with filtered search results and fixed regressions so pinned students remain first without
    losing relevance ordering or stable ordering for ties
    ([#150](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/150),
    [#205](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/205)).

* **Privacy mode:** Implemented the persistent `privacy on|off` feature across the logic, model, user-preference
  storage, and UI layers
  ([implementation commit](https://github.com/AY2627S1-CS2103T-T12-2/tp/commit/4f1ff37ecb3181adbb8417713e48ba4bcc064413)).
  * Added strict command parsing, saved preferences, reactive masking of contact details, a status indicator, command
    suggestions, documentation, and extensive automated tests.
  * Extended masking to command feedback and deletion results so sensitive values are not revealed outside the student
    cards ([#151](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/151),
    [#204](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/204)).

* **Contact-detail compatibility:** Improved validation for realistic names, phone numbers, and email addresses while
  retaining compatibility with legacy guardian data
  ([#238](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/238)). Added regression coverage for formatted guardian
  phone numbers ([#241](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/241)).

* **Smaller product improvements:** Added command-box format hints for tag, pin, privacy, and search workflows
  ([#160](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/160)); corrected configuration guidance
  ([#221](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/221)); and fixed the documented default student order
  ([#222](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/222)).

### Documentation contributions

* **User Guide:** Documented the tag and untag commands, pinning behavior, privacy mode, class badges, command hints,
  and updated contact-detail formats. Relevant documentation was delivered with
  [#53](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/53),
  [#159](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/159),
  [#160](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/160), and
  [#238](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/238).

* **Developer Guide:**
  * Added the project's non-functional requirements
    ([#44](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/44)).
  * Documented the enrolled-class display, pin, tag, and privacy-mode implementations.
  * Created the pin activity, tag sequence, and privacy sequence diagrams
    ([#159](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/159)).
  * Updated the UI class diagram and configured PlantUML support for contributors
    ([#116](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/116)).

### Contributions to team-based tasks

* Updated the project README to introduce TutEasy and its target users
  ([#35](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/35)).
* Maintained automated tests alongside each feature and regression fix, covering commands, parsers, models, storage,
  and JavaFX UI behavior.
* Kept feature documentation synchronized across the User Guide, Developer Guide, command summary, and implementation
  diagrams.
