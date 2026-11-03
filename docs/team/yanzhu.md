---
  layout: default.md
  title: "Yanzhu's Project Portfolio Page"
---

# Yanzhu's Project Portfolio Page

GitHub: [yanzhu9](https://github.com/yanzhu9)

## Project: TutEasy

TutEasy is a Java desktop application for private tutors to manage students, guardians, classes, lesson schedules, and
payments. It combines a command-line interface with a JavaFX interface and was built collaboratively by our team.

The following sections summarize my contributions, with links to the corresponding merged pull requests.

## Features and enhancements

* **Student search** ([PR #52](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/52),
  [PR #59](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/59)):
  Extended `find` to search by tags and lesson details, then added partial, case-insensitive matching and relevance
  ordering. Follow-up fixes improved lesson-result ranking and ensured repeated keywords do not distort results
  ([PR #207](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/207),
  [PR #208](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/208)).

* **Payment tracking and enrolment data** ([PR #82](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/82),
  [PR #83](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/83),
  [PR #114](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/114)):
  Added per-student payment status for class enrolments and displayed outstanding balances. Refactored enrolments to
  retain stable identities, agreed rates, and payment data, with migration support for older saved data.

* **Batch operations** ([PR #89](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/89),
  [PR #90](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/90)):
  Added batch enrolment in an existing class and batch tag assignment. Both workflows validate the selected students
  and report skipped or ineligible entries; later feedback also clarified the single-tag limit
  ([PR #147](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/147)).

* **Privacy and reliability**:
  Masked class meeting links and locations in student, class-list, and timetable views, and improved feedback when the
  requested privacy state was already active ([PR #243](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/243)). I also
  added rollback behavior when saving a payment change fails
  ([PR #136](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/136)).

## Documentation and testing

I wrote MVP use cases for the Developer Guide and aligned UML diagrams with the implemented data model
([PR #43](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/43),
[PR #118](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/118)). I also documented search, payment, and batch
workflows in the user and developer guides ([PR #140](https://github.com/AY2627S1-CS2103T-T12-2/tp/pull/140)).
Regression tests were added alongside the feature and bug-fix work described above.

The alpha-test assignment listed me as primary author for find, payment status, batch tag assignment, and batch class
enrolment. It also assigned me to test lesson scheduling, privacy mode, timetable views, and guardian and class
attributes.

My authored changes can be explored in the
[project commit history](https://github.com/AY2627S1-CS2103T-T12-2/tp/commits/master/?author=yanzhu9).
