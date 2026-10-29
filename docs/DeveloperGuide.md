---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# TutEasy Developer Guide

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

TutEasy is based on the [AddressBook-Level3 project](https://se-education.org/addressbook-level3/) created by the
[SE-EDU initiative](https://se-education.org). It uses JavaFX for its desktop interface, Jackson for JSON
serialization, JUnit for testing, Gradle for builds, and MarkBind for this documentation site.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

TutEasy requires JDK 25. Fork and clone the repository, import it into IntelliJ IDEA as a Gradle project, and configure
the project SDK as JDK 25. Run `seedu.address.Main` to launch the application, and run `./gradlew test` (macOS/Linux)
or `gradlew.bat test` (Windows) to verify the setup. See
[_Setting up and getting started_](SettingUp.md) for the full IDE, code-style, and CI instructions.

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" alt="High-level architecture of TutEasy" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/AY2627S1-CS2103T-T12-2/tp/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/AY2627S1-CS2103T-T12-2/tp/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" alt="Interaction between architecture components" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete manager class that implements the corresponding API interface, such
  as `LogicManager` implementing `Logic`.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" alt="Component APIs and manager implementations" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/AY2627S1-CS2103T-T12-2/tp/tree/master/src/main/java/seedu/address/ui/Ui.java).

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`,
`TimetablePanel`, `ClassListPanel`, and `StatusBarFooter`. `MainWindow` switches the main content placeholder among the
student list, read-only timetable, and read-only class list according to the `ViewChange` carried by a successful
`CommandResult`. These UI classes inherit from the abstract `UiPart` class, which captures common behavior among
classes that represent visible GUI parts.

`MainWindow` constructs `TimetablePanel` with callbacks for obtaining the application date and loading a timetable for
an anchor date. The panel owns only transient navigation state and does not access `Model` or storage directly.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/AY2627S1-CS2103T-T12-2/tp/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/AY2627S1-CS2103T-T12-2/tp/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

`PersonCard` renders contact details, tags, enrolled-class groups, outstanding balance, and pin state for one student.
`PersonListPanel` supplies the shared display state needed across cards. The
[enrolled-class display](#enrolled-class-display), [pin feature](#pin-feature), and
[privacy mode](#privacy-mode) sections describe these behaviors in detail.

`MainWindow` passes the privacy preference from `Logic` to `PersonListPanel` and `StatusBarFooter`. The panel owns an
observable privacy state shared by its `PersonCard` instances. Each card binds its phone, email, address, and guardian
labels to that state, so toggling privacy mode replaces each non-empty contact field with a fixed seven-asterisk mask
without modifying the underlying `Person`. The panel also refreshes its `ListView` to ensure newly created and
currently displayed cells use the same state. `StatusBarFooter` keeps a visible privacy indicator active across the
student-list, timetable, and class-list views.

### Logic component

**API**: [`Logic.java`](https://github.com/AY2627S1-CS2103T-T12-2/tp/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, the lifeline continues till the end of diagram.
</box>


How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a student).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

`LogicManager` also tracks whether the student list, timetable, or class list is active. In the read-only timetable and
class-list views, it rejects modifying commands before execution while allowing navigation, `privacy`, `help`, and
`exit`. `timetable`, `class list`, and `list` return view-change instructions that keep this state synchronized with
`MainWindow`.

`Logic#getCurrentDate()` reads `LocalDate.now(clock)`, keeping date-dependent UI behavior testable with a fixed clock.
`Logic#getTimetable(LocalDate)` delegates a requested anchor date to `Model` without executing a command or saving data.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API**: [`Model.java`](https://github.com/AY2627S1-CS2103T-T12-2/tp/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores all students as `Person` objects in a `UniquePersonList`.
* stores independent `TuitionClass` objects in a `UniqueTuitionClassList`. Each class has a stable `ClassId`, subject,
  cost, and complete lesson timing.
* represents the student-to-class association using an `Enrollment` value with stable `EnrollmentId`, `StudentId`, and
  `ClassId` references. The association owns the student-specific agreed rate and `PaymentStatus`.
* stores each student's optional `GuardianContact` separately from shared class details. Immutable person copies retain
  guardian fields, stable IDs, enrolments and pin state unless the operation explicitly changes them.
* provides `Model#hasNameConflict` for new-student and rename validation. `Name#hasSameIdentityAs` ignores case and
  repeated spaces; `Model#hasNameConflict` excludes the record with the same `StudentId` during a rename. Display
  spelling and exact equality remain unchanged, so older
  case/spacing variants can still load through the stored collection's exact-name uniqueness rule.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s GUI settings and privacy preference. This is exposed to the
  outside as a `ReadOnlyUserPrefs` object.
* derives immutable, date-backed timetable day and class entries from the central class catalogue and associates
  enrolled students from the complete student list. The timetable is not stored separately and does not use the
  filtered student list.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API**: [`Storage.java`](https://github.com/AY2627S1-CS2103T-T12-2/tp/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both TutEasy data and user preferences in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)
* reconstructs shared classes before resolving enrolment references. `JsonAdaptedPerson` owns guardian fields;
  `JsonAdaptedTuitionClass` owns mode, link, location and notes. Missing older fields use the defaults described under
  [attribute migration](#storage-and-legacy-migration).
* retains already-saved case/spacing name variants without merging students, and logs a warning without their names.
  Exact duplicate names, duplicate IDs and invalid association references are still rejected.

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

`LogsCenter` supplies the loggers used by student creation, shared-detail resolution, search and migration. These log
event categories and counts rather than raw input or contact values. `ParserUtil` is a shared parsing utility in Logic:
it trims inputs and constructs domain values, translating validation failures to `ParseException`. The immutable
values `GuardianContact`, `ClassDetails`, `ClassMode`, `StudentId`, `ClassId` and `Enrollment` belong to Model, even
though parser, storage and presentation code use them.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Lesson scheduling model

`LessonSchedule` is the common immutable contract for when a class occurs. `LessonTiming` implements it for a weekly
schedule containing a `DayOfWeek`, start time, and end time. `OneOffLessonSchedule` implements it for an exact
`LocalDate` and time range. Both reject null fields, invalid times, equal start and end times, and overnight ranges.

Commands retain the existing weekly representation `c/Monday1800-1930` and accept one-off schedules in the form
`c/2026-10-20T1800-1930`. `ParserUtil#parseLessonSchedule` selects the matching immutable schedule type. A `Lesson`
stores the resulting `LessonSchedule`; its subject and complete schedule form its logical class identity, while cost and
payment status remain enrolment properties excluded by `Lesson#isSameClass`.

Schedule overlap is date-aware. Two weekly schedules conflict on the same weekday, two one-off schedules conflict only
on the same date, and a weekly schedule conflicts with a one-off schedule when the date has the weekly schedule's
weekday. In all three cases, their time ranges must overlap; ranges meeting only at an endpoint do not conflict.

`JsonAdaptedLesson` stores `scheduleType` as `WEEKLY` or `ONE_OFF` alongside the compact `timing` value. Saved lessons
without `scheduleType` are interpreted as weekly schedules, so existing address-book files load without manual
migration. Weekly occurrences remain derived and are never expanded into persisted dated records.

### Enrolled-class display

`PersonListPanel` owns one `ClassColorPalette` and passes it to every `PersonCard`. The palette maps the same logical
class to the same CSS colour across students. Its `ClassIdentity` normalizes the subject to lowercase and combines it
with the complete stored schedule, matching `Lesson#isSameClass`; agreed rate and payment status are intentionally
excluded because they can differ between enrolments in one class. Eight colour styles are available, and deterministic
hash-based reuse keeps the mapping stable when there are more classes than colours.

`PersonCard` sorts a student's resolved lessons by schedule and then case-insensitive subject. For each lesson, it
creates an `HBox` containing the coloured subject-and-timing badge, teaching-mode label, and payment-status label. A
surrounding `VBox` adds a separate wrapped line only when a meeting link, location, or note exists. Keeping payment and
mode outside the coloured badge prevents those changing values from altering class identity or colour.

The muted balance badge beside the student's name displays `Person#getOutstandingBalance`, which derives the sum of
unpaid agreed rates rather than storing another value. This design lets payment updates refresh both the status label
and balance from the same enrolment state while leaving the class colour unchanged.

### Pin feature

The activity diagram below summarizes the common flow for `pin INDEX` and `unpin INDEX`.

<puml src="diagrams/PinActivityDiagram.puml" alt="Validation and list reordering for pin and unpin commands" />

Each `Person` stores a `pinned` boolean. `JsonAdaptedPerson` writes this value to the address-book data file and treats
a missing value as `false`, allowing older files to remain compatible. Copy operations such as editing tags or lessons
retain the existing pin state.

`PinCommand` and `UnpinCommand` resolve their target against the displayed student list and replace that student with
an immutable copy containing the new pin state. Repeating the existing state is rejected with an actionable error.

`ModelManager` exposes a `SortedList` that places pinned students before unpinned students. This comparator is applied
both to the complete list and to filtered lists. When a command supplies another comparator, such as the relevance
comparator used by `find`, pinned state remains the primary key and the supplied comparator orders students within
each pin group. Filters still control which students are included.

`PersonListPanel` observes this sorted list, so pinning or unpinning immediately reorders the existing list and updates
displayed indexes. `PersonCard` reads the pin state when rendering and shows the `PINNED` badge for pinned students.
`CommandBox` suggests the `pin INDEX` and `unpin INDEX` formats while either command is being entered.

### Find feature

The `find` feature is implemented across `FindCommandParser`, `FindCommand`, the search predicates, and `CommandBox`.

Supported modes are mutually exclusive:

* `find KEYWORD [MORE_KEYWORDS]` -> `NameContainsKeywordsPredicate`
* `find t/TAG_KEYWORD [MORE_TAG_KEYWORDS]` -> `TagContainsKeywordsPredicate`
* `find sub/SUBJECT_KEYWORD [MORE_SUBJECT_KEYWORDS]` -> `SubjectContainsKeywordsPredicate`
* `find c/DAY [TIME_HHmm]`, `find c/TIME_HHmm`, or `find c/DATE [TIME_HHmm]`
  -> `LessonContainsKeywordsPredicate`

`FindCommandParser` selects the mode from the prefix immediately after `find`. It rejects mixed modes, duplicate search
prefixes, empty prefixed values, and invalid tag or lesson keywords before constructing the corresponding predicate.
For tag search, it normalizes keywords to lowercase before tag-format validation. This keeps stored tags lowercase-only
while allowing case-insensitive search input for user convenience.
Lesson keywords accept a weekday/prefix such as `Mon`, an HHmm-style time within `0000`-`2359`, or both; shorter
numeric fragments such as `18` are accepted for partial matching. An exact ISO date such as `2026-10-20` matches a
one-off schedule, while a weekday search also matches one-off schedules occurring on that weekday.
`FindCommandParserTest` covers empty and invalid search inputs, while `FindCommandTest` covers partial matching and
result ranking. Parse failures occur before command construction, so the active filter remains unchanged.

`FindCommand` applies the predicate through `Model#updateFilteredPersonList`. Predicates that implement
`ScoredPersonPredicate` also provide a relevance score, so `FindCommand` requests a sorted filtered list with stronger
matches first within each pin group and preserves the current displayed order for ties. Students not currently displayed
fall back to original address-book order. Matching pinned students are
placed before matching unpinned students.

`CommandBox` provides light-grey input-bar hints for common `find` inputs and the command formats for `pin`, `unpin`,
`tag`, `untag`, and `privacy`. After a successful `find`, it keeps the suggestion overlay visible with
`Type 'list' to return to the home page.`; the hint clears when the user starts typing or when a non-`find` command
succeeds.

### Privacy mode

The sequence below shows how `privacy on` moves from command execution to preference storage and the reactive UI.

<puml src="diagrams/PrivacySequenceDiagram.puml" alt="Interactions for enabling privacy mode" />

`privacy on` and `privacy off` update `UserPrefs#privacyModeEnabled` through the model. `LogicManager` saves the updated
preferences using the existing user-preferences storage, so the selection survives an application restart. A missing
JSON field maps to Java's default `false` value, keeping older preference files compatible.

After any successful command, `MainWindow` reads the privacy state from `Logic` and updates both `PersonListPanel` and
`StatusBarFooter`. Existing `PersonCard` contact-label bindings react immediately, while a list refresh ensures all
cells are rendered consistently. Disabling privacy mode binds the labels back to the original values held by `Person`;
neither command changes address-book data.

`PrivacyCommandParser` accepts only the exact lowercase modes `on` and `off`. `LogicManager` permits the command in the
student list and both read-only views. When privacy mode is active, `AddCommand` and `EditCommand` pass the current
privacy state to `Messages#format`, which masks phone, email, address, and non-empty guardian details in their success
feedback. `DeleteCommand` does the same before displaying the deleted student's details. Other non-contact values remain
visible.

### Timetable view

The sequence below shows how the current-week timetable is opened and how navigation reloads a date-backed projection
without modifying stored data.

<puml src="diagrams/TimetableSequenceDiagram.puml" alt="Interactions for opening and navigating the timetable" />

`TimetableCommand` does not modify model data. It returns a `CommandResult` requesting the `TIMETABLE` view.
`MainWindow` responds by replacing `PersonListPanel` with a newly built `TimetablePanel`. It supplies
`logic::getCurrentDate` and `logic::getTimetable` callbacks, so the panel can refresh date-dependent state without
reading the model directly. Creating a new panel whenever timetable mode is entered resets the view to the current week.

`ModelManager#getTimetable(LocalDate)` passes the central class list and complete student list to `TimetableBuilder`.
The builder uses `WeekRange` to normalize the supplied anchor date to a complete Sunday-to-Saturday range. `WeekRange`
performs safe epoch-day calculations, exposes optional adjacent weeks, and rejects an anchor whose complete week is
outside the supported `LocalDate` range. The builder uses `LessonSchedule#occursOn(LocalDate)` to place weekly schedules
on matching weekdays and one-off schedules only on their exact date. Independently created classes appear even without
enrolled students, while enrolments attach students to the matching class. The builder returns seven immutable,
date-backed `TimetableDay` values, including empty days, with entries ordered by start time, case-insensitive subject,
and end time.

`TimetablePanel` keeps its navigation header outside the scrolling `ListView`. Previous and Next select optional adjacent
`WeekRange` values and reload through the callback. Today always requests a fresh application date and projection, even
when the same week remains selected. The current date is highlighted and scrolled to the top where possible; other weeks
start at Sunday. `TimetableDayCard` displays the dated heading and empty state, while `TimetableEntryCard` displays a
class's time, subject, and students. Entering `list` restores the existing student panel and normal command availability.
`class list` returns a `CLASS_LIST` view change; `MainWindow` replaces the current panel with `ClassListPanel`, including
when the timetable is open.

### Independent class management

`class add` validates subject, default rate, timing, duplicates, and schedule overlap before allocating the next persisted class
ID. IDs use the form `C001`, are case-insensitive at the command boundary, and are never reused. `class edit` resolves a
class by ID and replaces the immutable central class while updating each associated enrolment without changing its
agreed rate or payment status. `class delete` removes the central class and cascades to its enrolments.

`JsonSerializableAddressBook` stores central classes and the next ID sequence. When loading an older data file without
a class collection, it groups existing enrolments by subject and timing, assigns stable IDs, and preserves payment
statuses. This migration is applied before the model is exposed to commands or UI code.

### Payment status

Every enrolment stored by a `Person` has an `EnrollmentId` and references its student and central class through
`StudentId` and `ClassId`. It contains the student's immutable agreed rate and a `PaymentStatus` of `PAID` or `UNPAID`.
Constructors that do not specify a status default to `UNPAID`, which also provides backwards compatibility for saved
enrolments created before payment tracking or independent classes were introduced.

`class payment` parses a displayed student index, stable class ID, and payment status. During execution,
`ClassPaymentCommand` resolves the student from the filtered list and locates the enrolment by class ID. It replaces
only that immutable enrolment and then replaces only the selected `Person`; other
classes and students are not modified. A missing enrolment causes the command to fail before any model mutation.

`Enrollment#equals` includes its stable IDs, agreed rate, and payment status. The shared class reference is resolved by
`ClassId` and deliberately excluded from the association value. Presentation code obtains read-only `Lesson` projections.
`TimetableBuilder` and overlap detection group those projections by logical class identity, ensuring students with
different agreed rates or payment statuses still appear in one class.

`JsonAdaptedLesson` stores the status as `paymentStatus`. A missing property is interpreted as `UNPAID`, while an
unrecognised value is rejected as invalid data.

The outstanding balance is derived rather than stored. `Person#getOutstandingBalance` sums the agreed rate of each
enrolment whose status is `UNPAID`, excluding paid enrolments. Recomputing it from the immutable associations prevents a
stored balance from becoming stale after `class payment`, enrolment, unenrolment, or class deletion operations.

### Adding class and guardian attributes

#### Input validation and student identity

`AddCommandParser` requires student name, phone, email and address. `gn/`, `gp/` and `ge/` are independent optional
guardian fields; `ParserUtil#parseGuardianContact` trims them and uses empty strings for omitted values.
`GuardianContact` accepts names starting with a letter/digit and containing letters, digits, spaces, hyphens and
straight/curly apostrophes. This rule is independent of the student `Name` validator. Non-empty guardian phones and
emails use `Phone` and `Email` validation. Every supplied guardian field is checked even when the others are absent.
Invalid values become parse errors before a command can change the model.

New `add` commands accept either none of `sub/`, `cost/` and `c/`, or all three with a complete weekly or one-off
schedule. An incomplete group, empty value or start-only schedule is rejected before command execution, preserving
students, classes, the current filter and saved data. Legacy subject/cost/start-time fields remain supported when
reading older saved records; new commands cannot create those partial legacy records.

Guardian values participate in `Person#equals`/`hashCode` and survive immutable copy operations. They are neither
part of the duplicate-name check nor shared with classmates. `AddCommand` and `EditCommand` use
`Model#hasNameConflict`: `Name#hasSameIdentityAs` derives a trimmed, single-space, lowercase key using `Locale.ROOT`.
It does not rewrite `fullName` or change exact value equality. The same stable student ID is excluded, allowing its
own display spelling to be edited without self-collision. Renames check the complete address book, not just the current
search results. Unchanged-name contact edits remain possible for legacy records whose names already conflict under
the new policy.

#### Shared class details and inheritance

A student added with a complete class can include `mode/online|offline`, `link/`, `location/`, and
`notes/`. `ClassDetails` stores an immutable `ClassMode`, meeting link, location, and notes. Online
classes may omit the link; offline classes reject non-empty links. Links must be absolute HTTP(S)
URIs with a host and no credentials. Location and notes are limited to 500 single-line characters.

`TuitionClass` owns the shared subject, schedule, default rate, and `ClassDetails`. `Enrollment` stores only stable
association, student, and class IDs together with the student's agreed rate and payment status. It resolves its
referenced class through the central collection instead of copying shared class fields. Class edits re-resolve affected
associations while retaining their IDs, agreed rates, and payment statuses. Class listing, student cards, and timetable
entries therefore display the current shared class values without duplicating them in each enrolment.

`ClassDetailsUpdate` represents optional `add` input. It resolves classes from the class catalogue,
including empty classes. New classes default to offline. An empty, undescribed catalogue class can
receive its extra details through its first student. For occupied classes or classes with recorded
details, omitted input inherits shared values and conflicting explicit values fail before adding data.

The lookup uses the case-insensitive subject and complete `LessonSchedule`. The supplied `cost/` is this student's
agreed rate and need not equal the default rate. `AddCommand` checks duplicate names and overlaps, then resolves optional
details before calling `Model#addPerson`. The model creates a class when no match exists, or links the student to the
existing class and preserves classmates' enrolments. New enrolments start unpaid. The activity below summarizes
validation and inheritance; persistence remains `LogicManager`'s responsibility.

<puml src="diagrams/AddStudentAttributesActivity.puml" width="550" />

No guardian or extra class-detail input is added to `edit`, `class enrol`, or `class edit`.
Ordinary contact edits preserve guardian data. ID enrolments inherit class attributes;
class edits preserve those attributes and payments. Unenrolling the last student retains the class
and its metadata until the class itself is deleted.

#### Storage and legacy migration

`JsonAdaptedPerson` stores the stable student ID together with `guardianName`, `guardianPhone`, and `guardianEmail`.
Canonical associations use a compact `JsonAdaptedLesson` representation containing enrolment, student, and class IDs,
the agreed rate, and payment status. Shared class fields remain only in `JsonAdaptedTuitionClass`. The same adapter still
accepts legacy lesson records containing `mode`, `meetingLink`, `location`, and `notes`; loading migrates those records to
canonical classes and associations without losing details, guardians, agreed rates, or payment statuses. Duplicate
student or enrolment IDs and missing class references are rejected. Empty classes also retain attributes in JSON.
Missing contact/text fields default to empty strings; missing modes are inferred from existing links.

Loading proceeds through `JsonSerializableAddressBook#toModelType`:

1. Validate and restore the class catalogue and its IDs.
2. Restore each student and guardian contact. Generate student IDs only for old records lacking them.
3. Resolve compact enrolments against the catalogue. For older embedded lessons, reuse a matching subject/schedule or
   create a class; preserve the individual cost and payment status. Explicit shared details must agree, except that an
   empty, undescribed class with no students may receive its first details.
4. Reject conflicting shared details, duplicate student/enrolment IDs, duplicate class enrolments or missing class
   references. Otherwise retain the student and advance the class-ID sequence beyond all restored IDs.

| Older representation / missing field | Compatibility behavior |
| --- | --- |
| No guardian fields, location or notes | Use empty values. Punctuated guardian names accepted by current validation round-trip unchanged. |
| Class has no mode | Infer online if a meeting link is present; otherwise offline. |
| Embedded lesson has no payment status / schedule type | Default to unpaid / weekly. Compact associations require an explicit payment status. |
| Old student-level subjects, cost and start-only lesson | With all three available, infer a one-hour weekly range and migrate to classes/enrolments. Cap the end at 23:59 if adding an hour would cross midnight. |
| Incomplete student-level lesson, or start at 23:59 | Do not fabricate a valid duration; retain the legacy subject fallback for searching. |
| Previously saved names differing only in case/spacing | Preserve both records and IDs, log a warning, and reject new conflicting additions/renames. Exact duplicate names remain invalid. |

`Person#legacyLessonEnd` is used by both conversion and `hasUnmigratedLegacyLesson`, keeping fallback eligibility
consistent. Once a complete legacy record has migrated, deleting its class must not reactivate retained old subject
metadata. Successful saves write the canonical catalogue and compact associations; search filters are never written.
No automatic student merge is performed because each record may have different guardians, rates and enrolments.

#### Diagnostics and verification

Creation logs indicate success, duplicate-name rejection or schedule conflict; shared-detail resolution logs inheritance
or conflict. Search logs record keyword/result counts, and storage logs record loaded/migrated counts and legacy-name
warnings. Detailed counts use `FINE`; creation/rejection events use `INFO`. Command dispatch and UI diagnostics omit raw
input and feedback so they do not reintroduce contact values, URLs, notes or search keywords into those logs. These
diagnostics do not extend privacy mode's visual behavior or encrypt saved data.

Automated coverage includes first-contact catalogue enrichment, ID enrolment, class edits, last-student
unenrolment, preservation of IDs/metadata/guardians/payments, empty-class persistence, both legacy file
formats, conflict rejection, and catalogue UI display. `StudentIdentityIntegrationTest` covers normalized conflicts,
filtered renames, existing-record compatibility and punctuated guardian-name persistence. `FeatureLoggingIntegrationTest`
checks diagnostic events and exclusion of private values at detailed log levels. See the
[manual checks below](#student-creation-guardians-and-class-details) and the
[manual-testing appendix](#appendix-instructions-for-manual-testing) for reproducible end-to-end workflows.

### Subject search

`AddressBookParser` routes `find` to `SubjectFindCommandParser`. The parser handles the `sub/` mode and delegates
name, tag, and class searches to `FindCommandParser`, preserving their existing parsing and error messages.
Subject mode requires a single `sub/` prefix, non-empty subject-format keywords, and no other search category or
name preamble. Each keyword starts with an alphanumeric character, may contain supported subject punctuation, and is
limited to 100 characters. Whitespace between keywords is normalized before parsing. Invalid input throws `ParseException`
before the model or storage is changed.

`SubjectContainsKeywordsPredicate` matches any keyword as a case-insensitive substring of a current class subject.
It reads `Person#getLessons()`. For older subject-only records that could not be migrated because a cost or lesson
start is missing, or no valid same-day range can be inferred, it reads `Person#getSubjects()` instead.
Complete legacy records already have migrated lessons;
their retained legacy metadata is not used to restore matches after a class is removed from the in-memory model.

The existing `FindCommand` applies this predicate to the complete address book. Each student appears at most once,
and subject results put pinned matches first, retaining original address-book order within each pin group.
Subject search does not implement relevance scoring. `list` clears
the filter. `LogicManager` applies the read-only timetable and class-list restrictions and saves the complete address book after a
successful command, so searching does not remove students or enrolments from storage. Filters are not persisted.

The [subject search guide](SubjectSearch.md) documents examples and input constraints.
The [testing guide](Testing.md#subject-search-tests) lists automated coverage and commands for this feature.

### Tag management

The sequence below shows the batch form `tag 1 3 t/exam-prep`. The single-student `tag` and `untag` forms follow the
same parse, immutable replacement, and save flow.

<puml src="diagrams/TagSequenceDiagram.puml" alt="Interactions for assigning a tag to multiple students" />

`TagCommandParser` selects the legacy single-student mode when no `t/` prefix is present. When `t/` is present, it
parses the whitespace-separated preamble as one or more unique displayed student indexes and parses exactly one tag
from the prefix value. This distinction preserves `tag INDEX TAG [MORE_TAGS]` while allowing numeric batch tags to be
separated unambiguously from indexes.

Before changing the model, `TagCommand` validates every displayed index and categorizes all selected students. A
student who already has the tag is skipped. A student with 10 or more tags is reported as being at the tag limit.
Only the remaining students receive immutable replacement `Person` objects containing the new tag. The command result
lists all three outcome groups, or reports that no changes were made when every selected student is skipped.

For the single-student form, `TagCommand` copies the student's existing `LinkedHashSet<Tag>`, then rejects the complete
operation if any requested tag is already present or if the resulting set would exceed `Tag#MAX_TAGS_PER_PERSON`.
`UntagCommandParser` accepts either one or more tag names or the standalone `-all` flag. `UntagCommand` similarly
copies the existing tag set and rejects the complete operation when any requested tag is absent; otherwise, it removes
the requested tags or clears the copy. Both commands call `Person#withTags`, which preserves the student's pin,
guardian, and class details, before replacing the old `Person` through the model.

Tag text is validated by `ParserUtil` before command execution. A tag contains at most 30 characters and consists of
lowercase letters or digits separated by single hyphens. `JsonAdaptedTag` applies the same rules when loading data,
while its legacy normalization keeps compatible older records readable.

### Batch class enrolment

`ClassEnrolCommandParser` parses the whitespace-separated preamble as one or more displayed student indexes. When
`cost/` is present, exactly one index is accepted and the existing single-student behaviour is preserved. When cost is
omitted, the parser constructs a command that identifies an existing class by its case-insensitive subject and complete
lesson timing.

`ClassEnrolCommand` uses `Model#findTuitionClass` to resolve the subject and timing against the independent central
class collection, rather than scanning student enrolments. This allows an empty class to be found and supplies the
canonical class ID and cost for each new `Enrollment`. Before changing the model, the command validates every displayed
index and the target class. It then skips selected students who are already enrolled and replaces only the remaining
immutable `Person` objects. Invalid or duplicate indexes and a missing class leave every student unchanged. If all
selected students are already enrolled, the command makes no changes and returns an informative message.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` -- Saves the current address book state in its history.
* `VersionedAddressBook#undo()` -- Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` -- Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.
</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.
</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.
</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …` command. This is the behavior that most modern desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

### \[Proposed\] Data archiving

Data archiving can be implemented by adding an `archived` state to students and classes while retaining their stable
IDs and enrolments in storage. The default student and class lists would filter archived records, while dedicated
commands would archive, restore, and list them. Timetable construction would exclude archived classes by default.
The storage adapters would treat a missing `archived` field as `false` to preserve compatibility with existing data.


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

TutEasy is designed for student tutors who:
* tutor multiple students across different subjects and timeslots
* conduct individual or group lessons, either online or in person
* need to manage student details and recurring lesson schedules in one place
* prefer fast, keyboard-based interactions and are comfortable using a CLI
* want to keep their tutoring schedule separate from their school or personal calendar
* use other platforms to communicate with students and parents, as TutEasy focuses on student and lesson management

**Value proposition**:

TutEasy gives private tutors one keyboard-driven desktop application for maintaining student contact details, classes,
enrolments, payment status, and weekly or one-off schedules. Search, class-list, privacy, and timetable views help tutors
retrieve the information they need while keeping the stored student and class data in one place.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …                                      | I want to …                                                        | So that I can…                                                             |
|----------|---------------------------------------------|--------------------------------------------------------------------|---------------------------------------------------------------------------|
| `* * *`  | new user                                    | see usage instructions                                             | refer to them when I forget how to use the app                            |
| `* * *`  | tutor                                       | add a student's contact details                                    | store all their information in one place                                  |
| `* * *`  | tutor                                       | edit or remove student records                                     | keep my student list accurate                                             |
| `* * *`  | tutor                                       | search for students by name, subject, or class                     | find their information quickly                                            |
| `* * *`  | tutor                                       | record a student's subject                                         | prepare suitable lesson materials                                         |
| `* * *`  | tutor                                       | create a class with its subject and weekly timeslot                | represent a teaching arrangement in my schedule                           |
| `* * *`  | tutor                                       | edit or remove a class independently of its students               | change my timetable without incorrectly altering student records          |
| `* * *`  | tutor                                       | add students to or remove students from a class                    | maintain individual and group class rosters                               |
| `* * *`  | tutor teaching a student multiple subjects  | enrol the same student in multiple classes                         | represent their different teaching arrangements accurately               |
| `* * *`  | tutor                                       | record each class's day, start time, and end time                  | know when I am teaching and identify scheduling overlaps                  |
| `* * *`  | tutor with weekly classes                   | have each weekly class recur automatically                         | avoid entering the same class every week                                  |
| `* * *`  | tutor                                       | view my tutoring classes in a weekly timetable                     | understand my schedule at a glance                                        |
| `* * *`  | tutor                                       | see the subject, students, start time, and end time for each class | view important class details immediately                                  |
| `* * *`  | tutor with a busy schedule                  | be warned when two classes overlap                                 | avoid scheduling conflicts                                                 |
| `* * *`  | tutor                                       | see the students in each class and the classes assigned to each student | understand class membership from either direction                    |
| `* * *`  | tutor collecting lesson fees                | mark each student's class enrolment as paid or unpaid               | track outstanding payments without affecting classmates                  |
| `* * *`  | tutor                                       | clear an active search, filter, or grouping                        | return to the complete student list easily                                |
| `* * *`  | keyboard-driven user                        | receive clear success messages and actionable error messages      | know whether a command worked and how to correct invalid input            |
| `* * *`  | tutor                                       | have my student and class information saved and restored automatically | avoid losing my records when the application restarts                 |
| `* * *`  | tutor                                       | have invalid contact details, costs, and class times rejected     | prevent incorrect data from entering my records or timetable              |
| `* *`    | tutor                                       | give each class a recognizable name or identifier                  | distinguish classes with similar subjects and times                       |
| `* *`    | tutor                                       | create either a one-off or recurring class with a recurrence period | schedule both ad-hoc and regular lessons                                  |
| `* *`    | tutor                                       | navigate between timetable weeks and return to the current week   | inspect past and future schedules easily                                  |
| `* *`    | tutor                                       | view all classes for a selected day                                | prepare for upcoming lessons                                               |
| `* *`    | tutor teaching online and in-person lessons | record whether a lesson is online or in person                     | know how to attend it                                                      |
| `* *`    | tutor                                       | store the meeting link or lesson address                           | access the class location quickly                                          |
| `* *`    | tutor                                       | reschedule or cancel an individual lesson                          | keep timetable changes accurate                                            |
| `* *`    | tutor changing a recurring class            | apply a change to one lesson, future lessons, or the entire series | avoid changing unrelated lessons accidentally                             |
| `* *`    | tutor                                       | mark a lesson as scheduled, completed, or cancelled                | keep my timetable and lesson history accurate                             |
| `* *`    | tutor                                       | undo an accidental add, edit, deletion, cancellation, or rescheduling | recover from mistakes quickly                                          |
| `* *`    | tutor                                       | archive inactive students and discontinued classes                | hide old records without permanently deleting their history               |
| `* *`    | tutor                                       | be warned about possible duplicate students or classes            | avoid creating redundant records accidentally                             |
| `* *`    | tutor                                       | export and restore a backup of my records                          | recover from device or file failure                                       |
| `* *`    | tutor deleting a student or class           | see which enrolments and lessons will be affected                 | avoid removing related information accidentally                           |
| `* *`    | tutor                                       | view my next few lessons when the application opens               | prepare without navigating the full timetable                             |
| `* *`    | tutor with many students                    | sort and filter my students                                        | manage a large contact list easily                                         |
| `* *`    | tutor with frequently accessed students     | pin and unpin important students above the rest                    | access their details quickly                                               |
| `* *`    | tutor                                       | add notes about each student                                       | remember their learning needs and progress                                 |
| `* *`    | tutor who conducts in-person lessons        | store a student's home address                                     | travel to their lessons easily                                             |
| `* *`    | tutor teaching minors                       | record a parent or guardian's contact details                      | contact them when necessary                                                |
| `* *`    | tutor with incomplete guardian information | save any known guardian field independently | retain contact information without inventing missing details |
| `* *`    | tutor recording a guardian's name | use common hyphens and apostrophes | preserve the person's actual name |
| `* *`    | tutor adding a student to an existing class | inherit shared mode, meeting link, location and notes | avoid re-entering inconsistent class details |
| `* *`    | tutor adding a class | record optional preparation notes and a location | know where to teach and what to bring |
| `* *`    | tutor recording shared class details | be told when supplied details conflict with an existing class | avoid changing classmates' arrangements accidentally |
| `* *`    | tutor                                       | assign class tags to students                                      | identify students from the same class easily                               |
| `* *`    | tutor                                       | view students grouped by subject                                   | plan similar lessons together                                              |
| `* *`    | tutor                                       | view students grouped by timeslot                                  | see who attends each class                                                 |
| `* *`    | tutor                                       | record what was covered in the previous lesson                     | plan the next lesson effectively                                           |
| `* *`    | tutor                                       | record homework or follow-up tasks for each student                | track their work                                                           |
| `* *`    | tutor preparing for upcoming classes        | view upcoming classes together with student notes                  | prepare before teaching                                                    |
| `* *`    | tutor                                       | record student attendance                                          | keep an accurate lesson history                                            |
| `* *`    | privacy-conscious tutor                     | hide private contact details on screen and in edit feedback        | reduce the chance of someone else seeing them accidentally                 |
| `*`      | tutor                                       | record my hourly rate for each student or class                    | know how much to charge                                                     |
| `*`      | tutor                                       | record whether a lesson has been paid for                          | track outstanding payments                                                 |
| `*`      | tutor                                       | view the amount earned from lessons over a period                  | monitor my tutoring income                                                 |
| `*`      | tutor                                       | give each student a profile picture                                | identify the student visually                                              |
| `*`      | tutor who is also a student                 | keep my tutoring timetable separate from my school calendar       | avoid confusing tutoring classes with university commitments              |

### Use cases

(For all use cases below, the **System** is the `TutEasy` and the **Actor** is the `tutor`, unless specified otherwise.
Common preconditions for all use cases below: TutEasy is running and ready to accept command inputs.)

**Use case: Add a student**

**MSS**

1. Tutor requests to add a student with required contacts and any known guardian details or scheduled class.

2. TutEasy validates the supplied fields and checks for a duplicate student name.

3. If a class is supplied, TutEasy checks schedule conflicts, resolves a matching class or prepares a new one,
   and checks or inherits its shared details.

4. TutEasy adds the student and any class enrolment, preserving existing students' information.

5. TutEasy saves the updated records and confirms success.

   Use case ends.

**Extensions**

* 2a. A required field is missing, a supplied student/guardian/class field is invalid, or a single-valued prefix repeats.

    * 2a1. TutEasy rejects the input without adding the student or changing the class.
    * 2a2. TutEasy explains the validation failure.

      Use case resumes at step 1.

* 2b. The name matches an existing student's name after ignoring case and repeated spaces.

    * 2b1. TutEasy rejects the duplicate entry and explains that the student already exists.

      Use case ends.

* 3a. A different class overlaps the supplied schedule.

    * 3a1. TutEasy rejects the addition and identifies the conflicting class time.

      Use case resumes at step 1.

* 3b. Supplied mode, link, location or notes disagree with recorded shared class details.

    * 3b1. TutEasy rejects the addition and advises omitting the fields to inherit existing details.

      Use case resumes at step 1.

* 3c. A matching class exists and optional details are omitted.

    * 3c1. TutEasy inherits shared values, including for classes with no students. An undescribed empty class may
      instead receive explicit details from its first student.

      Use case resumes at step 4.

* 5a. Saving fails.

    * 5a1. TutEasy reports the storage error. Saving/rollback recovery is a separate persistence concern; validation
      atomicity does not itself guarantee rollback after a storage failure.

      Use case ends.

**Use case: Delete a student**

**MSS**

1. Tutor requests to delete the student at a specified index in the displayed student list.

2. TutEasy checks that the given index is valid.

3. TutEasy identifies the student at the specified index.

4. TutEasy deletes the student from its student records.

5. TutEasy refreshes the student list immediately.

6. TutEasy displays a success message.

   Use case ends.

**Extensions**

* 2a. The given index is outside the displayed student list.

    * 2a1. TutEasy does not delete any student.
    * 2a2. TutEasy shows an error message.

      Use case resumes at step 1.

* 4a. The deletion cannot be saved due to a storage failure.

    * 4a1. The student's record remains in TutEasy.
    * 4a2. TutEasy shows an error message.

      Use case ends.

**Use case: Add tags to students**

**MSS**

1. Tutor requests to add one or more tags to a specific student using the displayed index.

2. TutEasy checks that the index is valid.

3. TutEasy validates each tag.

4. TutEasy checks that none of the tags already exist for that student and that the result will not exceed 10 tags.

5. TutEasy adds the tags to the student’s tag set.

6. TutEasy refreshes the tags displayed on the student card.

7. TutEasy displays a success message.

   Use case ends.

**Extensions**

* 1a. Tutor does not provide any tag.

    * 1a1. TutEasy shows an error message.

      Use case resumes at step 1.

* 1b. Tutor requests to add one tag to multiple students using unique displayed indexes.

    * 1b1. TutEasy validates every index and the tag before changing any student.
    * 1b2. TutEasy skips students who already have the tag or have reached the 10-tag limit.
    * 1b3. TutEasy adds the tag to every remaining selected student.
    * 1b4. TutEasy reports the students in the added, already-tagged, and at-limit groups.

      Use case ends.

* 2a. The given index is out of range.

    * 2a1. TutEasy shows an error message.

      Use case resumes at step 1.

* 3a. A tag contains invalid characters or invalid formatting.

    * 3a1. TutEasy rejects the tag.
    * 3a2. TutEasy shows an invalid tag format error message.

      Use case resumes at step 1.

* 4a. A tag already exists for the student.

    * 4a1. TutEasy rejects the duplicate tag.
    * 4a2. TutEasy shows a duplicate tag error message.

      Use case resumes at step 1.

* 4b. Adding the supplied tags would exceed the 10-tag limit.

    * 4b1. TutEasy rejects all supplied tags.
    * 4b2. TutEasy shows a tag limit error message.

      Use case ends.

**Use case: View timetable**

**MSS**

1. Tutor requests to view the timetable.

2. TutEasy checks the current view mode.

3. TutEasy obtains the current date from the application clock and switches the display to timetable mode.

4. TutEasy shows the complete Sunday-to-Saturday week, highlights today, and makes it visible.

   Use case ends.

**Extensions**

* 4a. Tutor selects Previous week or Next week.

    * 4a1. TutEasy loads the complete adjacent week without changing or saving address-book data.

      Use case resumes at step 4.

* 4b. Tutor selects Today.

    * 4b1. TutEasy recalculates the current date, reloads its week, and makes today visible.

      Use case resumes at step 4.

* 4c. Tutor requests to view the default student list.

    * 4c1. TutEasy switches the display back to the student list, with pinned students first and the original order
      preserved within the pinned and unpinned groups.

      Use case ends.

* 2a. Tutor enters `timetable` while already in timetable mode.

    * 2a1. TutEasy rejects the command and keeps the current view.
    * 2a2. TutEasy shows the timetable read-only message.

      Use case ends.

* 1b. Tutor enters non‑alphabetic characters that cause an invalid command.

    * 1b1. TutEasy shows an error message.

      Use case resumes at step 1.

**Use case: Find students**

**MSS**

1. Tutor requests to find students by partial name, tag, subject, or lesson timing keywords.

2. TutEasy validates the chosen search mode and search input.

3. TutEasy searches the student list.

4. For name, tag, and lesson searches, TutEasy ranks matching students by relevance. Subject search retains original
   address-book order within each pin group. All modes place pinned matches first.

5. TutEasy displays the matching students and shows the light-grey input-bar hint for returning to the home page.

   Use case ends.

**Extensions**

* 2a. The search input is empty or invalid for the chosen mode.

    * 2a1. TutEasy shows a search-mode-specific error message.

      Use case resumes at step 1.

* 3a. No student matches the search input.

    * 3a1. TutEasy displays a message to show that there are no people matched.

      Use case resumes at step 1.

* 3b. Multiple search results refer to the same student.

    * 3b1. TutEasy removes the duplicate results.
    * 3b2. TutEasy displays only the unique matching students.

      Use case resumes at step 5.

* 1a. There is no whitespace between `find` and the keyword.

    * 1a1. TutEasy shows an unknown command error message.

      Use case resumes at step 1.

### Non-Functional Requirements

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed.
2.  Should be able to hold up to 1000 students without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4.  With 1000 student records and 10,000 lesson records, each documented command should display either a success or an error message within 2 seconds on a computer with a 2 GHz dual-core processor and 8 GB of RAM.
5.  With 1000 student records and 10,000 lesson records, the application should be ready to accept a command within 5 seconds of launch on a computer with a 2 GHz dual-core processor, 8 GB of RAM, and a solid-state drive.
6.  All data-management features should remain usable when the user's computer has no network connection. Opening the
    online User Guide URL shown by `help` requires a network connection.
7.  The application should not transmit any tutor, student, or lesson data over a network.
8.  After the application reports that a data-modifying command has succeeded, the resulting data should still be present after the application is terminated and restarted immediately.
9.  If a data file cannot be read, TutEasy should start with an empty data set and leave the unreadable file unchanged
    until the next successful command triggers a save.
10. The application should be distributed as a single JAR file that requires no software other than Java `25` or above on a supported OS.
11. In a usability test, at least four out of five first-time users who match the target user profile should be able to add a student, schedule a lesson, and find that lesson within 10 minutes after reading the Quick Start section of the User Guide, without assistance.
12. At a display resolution of 1280 x 720 and 100% OS display scaling, no text or control required for a documented workflow should be clipped or overlap another UI element.

### Glossary

* **Mainstream OS**: A commonly used operating system, such as Windows, macOS or Linux.
* **Student**: An individual whose details are stored by the tutor and who can be enrolled in one or more classes.
* **Tutor**: The user of TutEasy who manages student records, classes and lessons.
* **Contact details**: Information used to contact a student or their parent or guardian, such as a phone number, email address or home address.
* **Private contact detail**: A contact detail that the tutor considers sensitive and does not want others to see.
* **Teaching mode**: Whether a lesson is conducted online or in person.
* **Location**: Optional text describing where a lesson takes place, such as a room or physical address.
* **Meeting link**: An optional HTTP(S) URL used to join an online lesson.
* **Subject**: The area of study taught in a class, such as Mathematics, Science or a specific university module.
* **Class**: A teaching arrangement for zero or more students, with an associated subject, default rate, schedule,
  teaching mode and optional location, meeting link and notes.
* **Lesson**: An individual session of a class, scheduled for a specific date and time.
* **One-off class**: A class consisting of a single scheduled lesson.
* **Recurring class**: A class whose lesson repeats weekly on a specified weekday and time.
* **Timeslot**: A period identified by its start and end times on a specified day or date.
* **Scheduling conflict**: An overlap in the scheduled times of two or more lessons.
* **Enrolment**: The association between a student and a class they attend.
* **Roster**: The list of students enrolled in a particular class.
* **Tag**: A label attached to a student record to help organise or identify students. A tag does not itself represent enrolment in a class.
* **Timetable**: A calendar view showing the tutor’s lessons and their scheduled dates and times.
* **MSS (Main Success Scenario)**: The sequence of steps through which a use case achieves its intended outcome without encountering exceptional situations.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.
</box>

### Launch and shutdown

1. Initial launch

   1. Ensure Java 25 or later is installed. Download `tuteasy.jar` and copy it into an empty folder.

   1. Open a terminal in that folder and run `java -jar tuteasy.jar`.<br>
      Expected: The TutEasy window opens with sample students and classes. The status bar shows the data-file path.

1. Exiting with a command

   1. Enter `exit`.<br>
      Expected: The TutEasy and Help windows close without an error.

   1. Relaunch with `java -jar tuteasy.jar`.<br>
      Expected: The application starts normally and previously saved data remains available.

1. Exiting with the window controls

   1. Close the main window using the operating system's close button.<br>
      Expected: TutEasy closes cleanly. Relaunching the same JAR succeeds.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app with `java -jar tuteasy.jar`.<br>
       Expected: The most recent window size and location are retained.

### Help

| Test action | Expected result |
| --- | --- |
| Enter `help` | The Help window opens and displays the TutEasy User Guide URL. |
| Press `F1` while the command box is focused | The same Help window opens or receives focus. |
| Select **Help** from the menu | The same Help window opens or receives focus. |
| Select **Copy URL** | The clipboard contains `https://ay2627s1-cs2103t-t12-2.github.io/tp/UserGuide.html`. |

### Adding, editing, listing, and deleting students

Use a disposable data folder and run `clear` before this section.

| Test input | Expected result |
| --- | --- |
| `add n/Alice Tan p/91234567 e/alice@example.com a/Clementi t/priority` | Alice is added with the supplied details and tag. |
| Repeat the previous command | Duplicate-student error; no second Alice is added. |
| `add n/Bob Tan p/81234567 e/bob@example.com a/Jurong gn/Mary Tan gp/98765432 ge/mary@example.com` | Bob is added with guardian details. |
| `add n/Cara Lim p/82223333 e/cara@example.com a/Bishan sub/Physics cost/45 c/2026-11-03T1600-1730 mode/online link/https://meet.example.com/physics location/Online notes/Bring calculator` | Cara is added and enrolled in a one-off online class with the supplied shared details. |
| Add an offline class while supplying `link/https://meet.example.com/invalid` | Validation error; neither the student nor class is added. |
| `edit 1 p/90000000 e/alice.tan@example.com` | Only Alice's phone and email change. |
| `edit 0 n/Invalid` | Invalid-index error; no student changes. |
| `list` | All students are shown. |
| `delete 1` | The first displayed student is deleted and the result identifies that student. |
| `delete 0` | Invalid-index error; no student is deleted. |

Also try `delete`, `delete x`, and an index larger than the displayed list. Each should show an actionable error and
leave all student data unchanged.

### Clearing all data

1. Add at least one student and one independent class.
1. Enter `clear`.<br>
   Expected: All students, classes, and enrolments are removed.
1. Exit and relaunch TutEasy.<br>
   Expected: The cleared state is retained.

### Searching and returning to the student list

Use students whose names, tags, subjects, weekly schedules, and one-off schedules are distinguishable.

| Test input | Expected result |
| --- | --- |
| `find ali bob` | Students whose names partially match `ali` or `bob` appear. |
| `find t/pri` | Students with a partially matching tag such as `priority` appear. |
| `find c/Mon 1800` | Students with a Monday schedule containing `1800` appear. |
| `find c/2026-10-20` | Students enrolled in a one-off class on that exact date appear. |
| `list` | The complete student list is restored. |

### Pinning and unpinning students

Use at least three students. Note their original order before running these tests.

| Test input | Expected result |
| --- | --- |
| `pin 2` | The selected student receives a `PINNED` badge and moves above unpinned students. |
| Repeat `pin 1` for that now-pinned student | Already-pinned error; ordering is unchanged. |
| `unpin 1` | The selected pinned student is unpinned and returns to the unpinned group. |
| Find at least two students, then pin a displayed unpinned result | Only matching students remain visible; the newly pinned result moves above matching unpinned results. |
| Exit and relaunch after pinning a student | The student remains pinned and appears in the pinned group. |

After each successful `pin` or `unpin`, verify that the displayed indexes are refreshed before using another
index-based command.

### Tagging and removing tags

| Test input | Expected result |
| --- | --- |
| `tag 1 exam-prep needs-follow-up` | Both tags are added to student 1. |
| Repeat `tag 1 exam-prep` | Duplicate-tag error; no tag is added or removed. |
| Give student 1 ten tags, then run `tag 1 extra` | Tag-limit error; the existing ten tags remain unchanged. |
| `untag 1 exam-prep` | Only `exam-prep` is removed. |
| `untag 1 missing-tag` | Missing-tag error; the student's tags remain unchanged. |
| `untag 1 -all` | All remaining tags are removed from student 1. |

### Privacy mode

| Test input | Expected result |
| --- | --- |
| `privacy on` | Student phone, email, and address values plus non-empty guardian contact details are masked; the status bar reports privacy mode. |
| Add, edit, or delete a student while privacy mode is active | Contact details are masked in the success feedback; non-contact details such as names and tags remain visible. |
| Navigate to `timetable` and `class list` | The privacy-mode status remains visible. |
| `list`, then `privacy off` | The original contact details reappear unchanged. |
| Enable privacy mode, exit, and relaunch | Privacy mode remains enabled. |
| `privacy ON` or `privacy on now` | Invalid-command-format error; the existing privacy state is unchanged. |

### Finding by name, tag, or lesson

Start with a disposable, empty data file and add two students:

* `add n/Amy Smith p/91234567 e/amy@example.com a/One t/exam-prep sub/Math cost/30 c/Monday1800-1930`
* `add n/Ben Lee p/81234567 e/ben@example.com a/Two t/friends sub/Physics cost/35 c/Tuesday1900-2030`

| Test input | Expected result |
| --- | --- |
| `find am` | Amy appears; the partial name match is case-insensitive. |
| `find t/EXAM` | Amy appears because the tag keyword partially matches `exam-prep`, ignoring case. |
| `find c/Mon 18` | Amy appears because the lesson matches the weekday prefix and time fragment. |
| `find`, `find t/`, or `find c/` | A validation error is shown; the displayed results and records are unchanged. |
| `find t/exam*` or `find c/Funday` | An invalid-keyword error is shown; the displayed results and records are unchanged. |
| `list` | The filter clears and both students appear. |

### Student creation, guardians and class details

Use a disposable folder with sample data and unused `QA...` names. These tests use Thursday lesson times outside the
sample timetable. Run commands individually. Record class IDs from feedback or `class list`; run `list` after class
listing to return to the writable student view. Compare student/class counts before and after rejected commands.

1. Add a complete online class and a guardian with punctuation:

   ```text
   add n/QAAlice p/91111111 e/qa.alice@example.com a/One Street gn/Mary-Anne O'Connor gp/98888888 ge/mary@example.com sub/QA Algebra cost/30 c/Thursday1000-1130 mode/online link/https://meet.example.com/qa location/Virtual room notes/Bring worksheet
   ```

   Expected: student added with all guardian fields; online class shows its link, location and notes. Record its class ID.

2. Add `QABob` to the existing class with a different agreed rate and no repeated shared details:

   ```text
   add n/QABob p/92222222 e/qa.bob@example.com a/Two Street sub/QA Algebra cost/35 c/Thursday1000-1130
   ```

   Expected: the same class ID, online mode, link, location and notes; Bob's balance is 35 and Alice's is 30.
   Guardian fields remain individual, so Bob does not inherit Alice's guardian.

3. Test phone-only, email-only and name-only guardians:

   ```text
   add n/QAPhone p/93333333 e/qa.phone@example.com a/Three Street gp/98765432
   add n/QAEmail p/94444444 e/qa.email@example.com a/Four Street ge/guardian@example.com
   add n/QANameOnly p/95550000 e/qa.nameonly@example.com a/Five Street gn/Mary-Anne O'Connor
   ```

   Expected: all three are added; each shows only its supplied guardian field.

4. Run each invalid command below. Every command should fail without adding `QAInvalid` or changing the shared class:

   | Test input | Expected reason |
   | --- | --- |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street sub/English` | Incomplete class fields; requires subject, cost and schedule together. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street cost/20` | Incomplete class fields. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street sub/English cost/20` | Incomplete class fields. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street sub/English cost/20 c/Thursday1200` | Schedule requires both start and end times. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street gn/@Mary` | Invalid guardian-name character. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street gp/12` | Guardian phone too short. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street ge/invalid` | Invalid guardian email. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street gp/123 gp/456` | Repeated single-valued prefix. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street sub/QA Link cost/30 c/Thursday1200-1300 mode/online link/javascript:alert(1)` | Invalid meeting-link scheme. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street sub/QA Link cost/30 c/Thursday1200-1300 mode/online link/https://user:pass@example.com` | Embedded link credentials. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street sub/QA Link cost/30 c/Thursday1200-1300 mode/offline link/https://example.com` | Link on an offline class. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street sub/QA Algebra cost/30 c/Thursday1000-1130 mode/offline` | Conflicts with existing shared online mode. |
   | `add n/QAInvalid p/95555555 e/qa.invalid@example.com a/Five Street sub/QA Algebra cost/30 c/Thursday1000-1130 notes/Different notes` | Conflicting shared notes. |

   For length boundaries, use a separate disposable folder and reset its data between trials. Append `notes/` followed
   by 500 letters to a new `QA Link` class command without a link: it should succeed. Repeat with 501 letters: it should
   fail without additions. Do the same for location. Return to the main fixture for the remaining steps.

5. Test normalized name conflicts and renames:

   ```text
   add n/QA Name p/96666666 e/qa.name@example.com a/Six Street
   add n/qa  name p/97777777 e/qa.other@example.com a/Seven Street
   find QABob
   edit 1 n/QA  NAME
   ```

   Expected: first add succeeds; second add and rename fail as duplicates, with records and displayed results unchanged.
   Rename `QABob` to `QABOB` instead: this succeeds and retains the same student ID and class details.

6. Use `find QAAlice`, then mark its recorded class ID paid with `class payment 1 id/CLASS_ID status/paid` (substitute
   the actual ID). Run `list`, exit and relaunch from the same folder. Expected: guardian punctuation, modes, links,
   location, notes, individual rates and payment status persist; Bob remains unpaid. Both students' details appear
   consistently in the class list and timetable. Unenrolling the last student must retain the class and shared metadata.

For legacy JSON and already-saved case/spacing variants, use the automated storage fixtures/tests rather than editing
your real file. Verify `ClassAttributesMigrationTest`, `SubjectSearchIntegrationTest` and
`StudentIdentityIntegrationTest`: missing attributes default correctly, incomplete legacy subjects stay searchable,
deleting a migrated class does not revive old subject matches, and existing name variants survive a save/reload.

### Searching by subject

Use a disposable data file with three students: Alice taking Mathematics and Physics, Bob taking Computer Science,
and Carol taking no subjects. These students should initially be unpinned, in Alice/Bob/Carol insertion order.
Use `list` first if the timetable or class list is open. Keep this fixture separate from the `QA...` attribute tests.

To construct that fixture in a separate disposable folder, use `clear` to remove the sample records, then run:

```text
add n/Alice p/91111111 e/alice@example.com a/One sub/Mathematics cost/30 c/Thursday0700-0800
add n/Bob p/92222222 e/bob@example.com a/Two sub/Computer Science cost/40 c/Thursday0800-0900
add n/Carol p/93333333 e/carol@example.com a/Three
class add sub/Physics cost/35 c/Thursday0900-1000
```

Record the Physics class ID from feedback, then run `class enrol 1 id/CLASS_ID` with that ID. Expected: Alice has both
subjects and Bob has one. `clear` deletes records, so never run this fixture setup against your working data.

| Test input | Expected result |
| --- | --- |
| `find sub/mAtH` | Alice appears once; feedback reports one result. |
| `find sub/phys computer` | Alice and Bob appear in original address-book order. |
| `find sub/math Math physics` | Alice appears once despite multiple matching keywords and classes. |
| `find Carol`, then `find sub/computer` | Bob appears; the subject search includes students outside the old results. |
| `find sub/Chemistry` | Empty results and a zero-result message. |
| `list` | All three students reappear. |
| `find sub/` or `find sub/Math!` | Error message; previous results and student records remain unchanged. |
| `find sub/Math sub/Physics` | Duplicate-prefix error; previous results remain unchanged. |
| `find sub/Math t/priority` or `find Alice sub/Math` | Mixed-mode error; previous results remain unchanged. |
| `timetable`, then `find sub/Math` | Timetable read-only error; use `list` before searching. |
| `class list`, then `find sub/Math` | Class-list read-only error; use `list` before searching. |
| `list`, `pin 2`, then `find sub/phys computer` | Bob appears before Alice because pinned matches come first; each appears once. |
| `find sub/computer`, then `delete 1` | Bob is deleted, even if he was not first in the complete list. |

After a subject search, exit and relaunch the app. All remaining students and their enrolments should be present;
the full student list should be displayed. Repeat `find sub/math` to check that saved class subjects remain searchable.

### Managing classes and individual enrolments

Use at least two students. Replace `C001` below with the class ID reported by TutEasy if it differs.

| Test input | Expected result |
| --- | --- |
| `class add sub/Math cost/30 c/Monday1800-1930` | An independent weekly class is created with a stable ID such as `C001`. |
| Repeat the previous command | Duplicate-class error; no second class is created. |
| Add an overlapping Monday class | Overlap error; no class is created. |
| `class list` | A read-only class list opens and includes the ID, subject, schedule, and default cost. |
| `class edit id/C001 sub/Mathematics cost/35` | The class keeps its ID and schedule while its subject and default cost change. |
| `list`, then `class enrol 1 id/C001` | Student 1 is enrolled at the current default cost. |
| Repeat the previous enrolment | Already-enrolled feedback; no duplicate enrolment is created. |
| `class unenrol 1 id/C001` | Student 1 is removed from the class; the independent class remains in `class list`. |
| `class enrol 1 sub/Mathematics cost/40 c/Monday1800-1930` | Student 1 is enrolled with an agreed rate of 40. |
| `class delete id/C001` | The class and all of its enrolments are removed without deleting any student. |

### Timetable view

Prepare a weekly class on the current weekday, a one-off class on today's date, classes on at least two other days,
and a class with two students whose names are not already alphabetical.

| Test action | Expected result |
| --- | --- |
| Enter `timetable` | The complete Sunday-to-Saturday week containing today appears. Today is highlighted and scrolled into view where possible. |
| Inspect the header | The displayed date range matches the selected week, including month or year boundaries where applicable. |
| Inspect the entries | Classes are chronological; student names inside a class are alphabetical. Empty days still have dated headings. |
| Inspect today's entries | Both the matching weekly class and today's one-off class appear. |
| Select **Next week** | The range advances exactly seven days. The weekly class recurs; today's one-off class is absent. |
| Select **Previous week** | The range moves back exactly seven days. |
| Select **Today** | The current week is reloaded and today is highlighted again. |
| Enter a modifying command such as `delete 1` | A read-only error appears and no data changes. |
| Enter `class list`, then `timetable` | The read-only class list opens, then the timetable opens again at the current week. |
| Enter `list` | The student list returns and modifying commands are enabled. |

Navigate away from the current week, enter `list`, and enter `timetable` again. The newly created timetable panel should
start at the current week rather than the previously selected week.

### Updating payment status

Create `Math` at `Monday1800-1930` using `class add`, note its generated ID (for example `C001`), and enrol two
students using that ID. Each student's coloured Math badge should initially be
followed by a separate, muted `Unpaid` label, and each balance badge should initially include the Math cost.

| Test input | Expected result |
| --- | --- |
| `class payment 1 id/C001 status/paid` | The first student's Math badge is followed by `Paid` and its balance decreases by the Math cost; the second student's status and balance remain unchanged. |
| Repeat the previous command | Feedback says the payment is already paid; no other data changes. |
| `class payment 1 id/C001 status/unpaid` | Only the first student's Math status returns to `Unpaid`, and its balance increases by the Math cost. |
| `class payment 1 id/C999 status/paid` | Error says the student is not enrolled in the specified class; no data changes. |
| `class payment 0 id/C001 status/paid` | Invalid-index error; no data changes. |
| `class payment 1 id/C001 status/pending` | Invalid-status error; no data changes. |

Mark one student as paid, exit the app, and relaunch it. The paid status should still appear, while the other student's
status remains unpaid. Open the timetable and verify that both students still appear together in one Math class.

### Displaying enrolled classes on student cards

Use at least two students. Create a weekly Math class and a later weekly Physics class, then enrol both students in
Math and the first student in Physics. Replace the example class IDs below with the IDs reported by TutEasy.

| Test action | Expected result |
| --- | --- |
| Inspect both student cards | Both Math badges use the same colour and show the same subject and timing. Each has separate `Offline` and `Unpaid` labels. |
| Inspect the first student's two class groups | Math and Physics appear on separate rows ordered by schedule, then subject. |
| Mark the first student's Math enrolment as paid | Its Math colour is unchanged, its status becomes `Paid`, and only that student's balance decreases. |
| Exit and relaunch TutEasy | The same classes, payment labels, balance values, order, and shared class colours are restored. |

### Assigning a tag to multiple students

Use a disposable data file with at least three displayed students and reset it between test cases. Unless a row states
otherwise, selected students do not have the target tag and have fewer than 10 tags.

| Test input | Expected result |
| --- | --- |
| `tag 1 2 3 t/exam-prep` | All three students receive `exam-prep`; feedback lists them under the added group. |
| Give student 1 `math-2026` and student 2 ten other tags, then run `tag 1 2 3 t/math-2026` | Student 1 is skipped as already tagged, student 2 is skipped at the tag limit, and student 3 receives the tag; feedback identifies each outcome. |
| Give student 1 `math-2026` and student 2 ten other tags, then run `tag 1 2 t/math-2026` | No student changes; feedback identifies student 1 as already tagged and student 2 as being at the tag limit. |
| `tag 1 1 3 t/exam-prep` | Duplicate-index error; no student changes. |
| `tag 1 999 t/exam-prep` | Invalid-index error; no student changes. |
| `tag 1 3 t/ExamPrep` | Invalid-tag error under the existing lowercase tag rules; no student changes. |
| `tag 1 2 t/exam-prep urgent` | Batch-mode error says exactly one tag is allowed; no student changes. |
| `tag 1 2 t/exam-prep Invalid` | Invalid-tag error; no student changes. |
| `tag 1 3 t/2026` | Both students receive the numeric tag `2026`. |
| `tag 3 exam-prep needs-follow-up` | The legacy format still adds both tags to student 3. |

### Batch class enrolment

First create `Math` at `Monday1800-1930` with cost 30 using `class add`. Leave the class empty initially so the first
test also verifies that batch enrolment looks up the independent class rather than scanning existing enrolments.

| Test input | Expected result |
| --- | --- |
| `class enrol 2 3 sub/Math c/Monday1800-1930` | Both displayed students are enrolled using the existing cost of 30. |
| `class enrol 2 sub/math c/Monday1800-1930` | The subject match is case-insensitive and the student is enrolled. |
| `class enrol 2 2 sub/Math c/Monday1800-1930` | Duplicate-index error; no student changes. |
| `class enrol 2 999 sub/Math c/Monday1800-1930` | Invalid-index error; no student changes. |
| `class enrol 1 2 sub/Math c/Monday1800-1930` | Student 1 is skipped because they are already enrolled; student 2 is enrolled, and the feedback identifies both outcomes. |
| With students 1 and 2 already enrolled, repeat `class enrol 1 2 sub/Math c/Monday1800-1930` | No student changes; feedback says that all selected students are already enrolled. |
| `class enrol 2 3 sub/Physics c/Monday1800-1930` | Missing-class error; no student changes. |
| `class enrol 2 3 sub/Math cost/30 c/Monday1800-1930` | Error explaining that cost cannot be supplied for multiple students. |

### Saving data

Use a disposable folder and keep backup copies outside TutEasy's `data` folder.

1. Automatic saving

   1. Add a student, class, enrolment, and payment status, then enter `exit`.
   1. Relaunch the same JAR from the same folder.<br>
      Expected: All saved values are restored. The complete student list is shown on startup.

1. Missing data file

   1. Close TutEasy and move `data/addressbook.json` to a backup location.
   1. Relaunch TutEasy.<br>
      Expected: TutEasy starts with sample data and does not fail to launch.

1. Corrupted data file

   1. Close TutEasy, back up `data/addressbook.json`, and replace its contents with invalid JSON.
   1. Relaunch TutEasy without entering any command.<br>
      Expected: TutEasy starts with an empty data set. The corrupted file remains unchanged at this point.
   1. Enter a successful command such as `add` with valid student details.<br>
      Expected: TutEasy saves the new valid data. Restore the backup after completing the test if required.

1. Read-only data location

   1. Run TutEasy from a folder where the current user cannot write, then execute a data-modifying command.<br>
      Expected: TutEasy reports that it cannot save because of insufficient permissions instead of silently losing
      the error.
