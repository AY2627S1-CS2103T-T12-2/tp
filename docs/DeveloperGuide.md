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

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
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

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`,
`TimetablePanel`, `ClassListPanel`, and `StatusBarFooter`. `MainWindow` switches the main content placeholder among the
person list, read-only timetable, and read-only class list according to the `ViewChange` carried by a successful
`CommandResult`. These UI classes inherit from the abstract `UiPart` class, which captures common behavior among
classes that represent visible GUI parts.

`MainWindow` constructs `TimetablePanel` with callbacks for obtaining the application date and loading a timetable for
an anchor date. The panel owns only transient navigation state and does not access `Model` or storage directly.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

`PersonCard` renders enrolled classes as coloured badges in a vertical list below the student's email. A smaller,
muted paid or unpaid label follows each badge without inheriting its background colour. Each badge and status are
contained in one `HBox`, while the surrounding `VBox` places every class group on its own row. A single
`ClassColorPalette` is owned by `PersonListPanel` and shared by every card, ensuring that the same logical class uses
the same colour across students. The palette identifies a class using its case-insensitive subject and complete
lesson timing, matching `Lesson#isSameClass`; cost and payment status are not part of the colour identity. A muted
balance badge beside the student's name always displays `Person#getOutstandingBalance`, including when it is zero.
Pinned students also display a `PINNED` badge beside their name. The badge is visible and participates in the layout
only when `Person#isPinned` is true.

`MainWindow` passes the privacy preference from `Logic` to `PersonListPanel` and `StatusBarFooter`. The panel owns an
observable privacy state shared by its `PersonCard` instances. Each card binds its phone, email, and address labels to
that state, so toggling privacy mode replaces each value with a fixed seven-asterisk mask without modifying the
underlying `Person`. The panel also refreshes its `ListView` to ensure newly created and currently displayed cells use
the same state. `StatusBarFooter` keeps a visible privacy indicator active across the student-list, timetable, and
class-list views.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

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
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

`LogicManager` also tracks whether the person list, timetable, or class list is active. In the read-only timetable and
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
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores independent `TuitionClass` objects in a `UniqueTuitionClassList`. Each class has a stable `ClassId`, subject,
  cost, and complete lesson timing.
* represents the student-to-class association using an `Enrollment` value with stable `EnrollmentId`, `StudentId`, and
  `ClassId` references. The association owns the student-specific agreed rate and `PaymentStatus`.
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s GUI settings and privacy preference. This is exposed to the
  outside as a `ReadOnlyUserPrefs` object.
* derives immutable, date-backed timetable day and class entries from every person's lessons. The timetable is not
  stored separately, and it uses the complete address book rather than the filtered person list.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)


<box type="info" seamless>

**Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />
</box>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

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

### Pin feature

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
* `find c/DAY [TIME_HHmm]`, `find c/TIME_HHmm`, or `find c/DATE [TIME_HHmm]`
  -> `LessonContainsKeywordsPredicate`

`FindCommandParser` selects the mode from the prefix immediately after `find`. It rejects mixed modes, duplicate search
prefixes, empty prefixed values, and invalid tag or lesson keywords before constructing the corresponding predicate.
For tag search, it normalizes keywords to lowercase before tag-format validation. This keeps stored tags lowercase-only
while allowing case-insensitive search input for user convenience.
Lesson keywords accept a weekday/prefix such as `Mon`, an HHmm-style time within `0000`-`2359`, or both; shorter
numeric fragments such as `18` are accepted for partial matching. An exact ISO date such as `2026-10-20` matches a
one-off schedule, while a weekday search also matches one-off schedules occurring on that weekday.

`FindCommand` applies the predicate through `Model#updateFilteredPersonList`. Predicates that implement
`ScoredPersonPredicate` also provide a relevance score, so `FindCommand` requests a sorted filtered list with stronger
matches first within each pin group and preserves original address-book order for ties. Matching pinned students are
placed before matching unpinned students.

`CommandBox` provides light-grey input-bar hints for common `find` inputs and the available `privacy` modes. After a
successful `find`, it keeps the suggestion overlay visible with `Type 'list' to return to the home page.`; the hint
clears when the user starts typing or when a non-`find` command succeeds.

### Privacy mode

`privacy on` and `privacy off` update `UserPrefs#privacyModeEnabled` through the model. `LogicManager` saves the updated
preferences using the existing user-preferences storage, so the selection survives an application restart. A missing
JSON field maps to Java's default `false` value, keeping older preference files compatible.

After any successful command, `MainWindow` reads the privacy state from `Logic` and updates both `PersonListPanel` and
`StatusBarFooter`. Existing `PersonCard` contact-label bindings react immediately, while a list refresh ensures all
cells are rendered consistently. Disabling privacy mode binds the labels back to the original values held by `Person`;
neither command changes address-book data.

### Timetable view

`TimetableCommand` does not modify model data. It returns a `CommandResult` requesting the `TIMETABLE` view.
`MainWindow` responds by replacing `PersonListPanel` with a newly built `TimetablePanel`. It supplies
`logic::getCurrentDate` and `logic::getTimetable` callbacks, so the panel can refresh date-dependent state without
reading the model directly. Creating a new panel whenever timetable mode is entered resets the view to the current week.

`ModelManager#getTimetable(LocalDate)` passes the central class list and complete person list to `TimetableBuilder`.
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
class's time, subject, and students. Entering `list` restores the existing person panel and normal command availability.
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

`add` accepts optional `gn/`, `gp/`, and `ge/` guardian contact fields. `GuardianContact` validates
non-empty values with the existing name, phone, and email rules. Contact fields are independent,
with empty strings representing missing values. They participate in `Person#equals` and `hashCode`,
while student identity retains its existing rules. Person-copy operations preserve the contact.

A contact added with a complete lesson can include `mode/online|offline`, `link/`, `location/`, and
`notes/`. `ClassDetails` stores an immutable `ClassMode`, meeting link, location, and notes. Online
classes may omit the link; offline classes reject non-empty links. Links must be absolute HTTP(S)
URIs with a host and no credentials. Location and notes are limited to 500 single-line characters.

`TuitionClass` owns the shared subject, schedule, default rate, and `ClassDetails`. `Enrollment` stores only stable
association, student, and class IDs together with the student's agreed rate and payment status. It resolves its
referenced class through the central collection instead of copying shared class fields. Class edits re-resolve affected
associations while retaining their IDs, agreed rates, and payment statuses. Class listing, student cards, and timetable
entries therefore display the current shared class values without duplicating them in each enrolment.

Subject search delegates legacy fallback eligibility to `Person#hasUnmigratedLegacyLesson`.
The same end-time calculation drives migration and fallback, so a valid legacy `Monday2359`
record stays searchable without restoring stale subjects after a successfully migrated class is removed.

`ClassDetailsUpdate` represents optional `add` input. It resolves classes from the class catalogue,
including empty classes. New classes default to offline. An empty, undescribed catalogue class can
receive its extra details through its first contact. For occupied classes or classes with recorded
details, omitted input inherits shared values and conflicting explicit values fail before adding data.

No new attribute input is added to `edit`, `class enrol`, or `class edit`; Aaron's class-ID commands
are retained. Ordinary contact edits preserve guardian data. ID enrolments inherit class attributes;
class edits preserve those attributes and payments. Unenrolling the last student retains the class
and its metadata until the class itself is deleted.

`JsonAdaptedPerson` stores the stable student ID together with `guardianName`, `guardianPhone`, and `guardianEmail`.
Canonical associations use a compact `JsonAdaptedLesson` representation containing enrolment, student, and class IDs,
the agreed rate, and payment status. Shared class fields remain only in `JsonAdaptedTuitionClass`. The same adapter still
accepts legacy lesson records containing `mode`, `meetingLink`, `location`, and `notes`; loading migrates those records to
canonical classes and associations without losing details, guardians, agreed rates, or payment statuses. Duplicate
student or enrolment IDs and missing class references are rejected. Empty classes also retain attributes in JSON.
Missing contact/text fields default to empty strings; missing modes are inferred from existing links.

Automated coverage includes first-contact catalogue enrichment, ID enrolment, class edits, last-student
unenrolment, preservation of IDs/metadata/guardians/payments, empty-class persistence, both legacy file
formats, conflict rejection, and catalogue UI display. See [the manual checklist](ManualTests-v1.4.md).

### Subject search

`AddressBookParser` routes `find` to `SubjectFindCommandParser`. The parser handles the `sub/` mode and delegates
name, tag, and class searches to `FindCommandParser`, preserving their existing parsing and error messages.
Subject mode requires a single `sub/` prefix, non-empty alphanumeric keywords, and no other search category or
name preamble. Whitespace between keywords is normalized before parsing. Invalid input throws `ParseException`
before the model or storage is changed.

`SubjectContainsKeywordsPredicate` matches any keyword as a case-insensitive substring of a current class subject.
It reads `Person#getLessons()`. For older subject-only records that could not be migrated because a cost or lesson
start is missing, it reads `Person#getSubjects()` instead. Complete legacy records already have migrated lessons;
their retained legacy metadata is not used to restore matches after a class is removed from the in-memory model.

The existing `FindCommand` applies this predicate to the complete address book. Each student appears at most once,
and subject results retain address-book order. Subject search does not implement relevance scoring. `list` clears
the filter. `LogicManager` applies the usual timetable restriction and saves the complete address book after a
successful command, so searching does not remove students or enrolments from storage. Filters are not persisted.

The [subject search guide](SubjectSearch.md) documents examples and input constraints.
The [testing guide](Testing.md#subject-search-tests) lists automated coverage and commands for this feature.

### Batch tag assignment

`TagCommandParser` selects the legacy single-student mode when no `t/` prefix is present. When `t/` is present, it
parses the whitespace-separated preamble as one or more unique displayed student indexes and parses exactly one tag
from the prefix value. This distinction preserves `tag INDEX TAG [MORE_TAGS]` while allowing numeric batch tags to be
separated unambiguously from indexes.

Before changing the model, `TagCommand` validates every displayed index and categorizes all selected students. A
student who already has the tag is skipped. A student with 10 or more tags is reported as being at the tag limit.
Only the remaining students receive immutable replacement `Person` objects containing the new tag. The command result
lists all three outcome groups, or reports that no changes were made when every selected student is skipped.

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

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


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
TutEasy helps tutors manage their tutoring schedules efficiently through a CLI-based desktop application. It stores student and lesson information (such as contact details, class timings, teaching modes, class timing etc.) in a central location. 

Features such as timetable view mode and search features allow tutors to organise, retrieve and display information in an organised manner. Tutors can use TutEasy to  keep track of their students' necessary information, allowing them to prepare for upcoming lessons and avoid scheduling conflicts.


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
| `* * *`  | tutor teaching a student multiple subjects  | enroll the same student in multiple classes                        | represent their different teaching arrangements accurately               |
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
| `* *`    | tutor deleting a student or class           | see which enrollments and lessons will be affected                | avoid removing related information accidentally                           |
| `* *`    | tutor                                       | view my next few lessons when the application opens               | prepare without navigating the full timetable                             |
| `* *`    | tutor with many students                    | sort and filter my students                                        | manage a large contact list easily                                         |
| `* *`    | tutor with frequently accessed students     | pin important students above the rest                              | access their details quickly                                               |
| `* *`    | tutor                                       | add notes about each student                                       | remember their learning needs and progress                                 |
| `* *`    | tutor who conducts in-person lessons        | store a student's home address                                     | travel to their lessons easily                                             |
| `* *`    | tutor teaching minors                       | record a parent or guardian's contact details                      | contact them when necessary                                                |
| `* *`    | tutor                                       | assign class tags to students                                      | identify students from the same class easily                               |
| `* *`    | tutor                                       | view students grouped by subject                                   | plan similar lessons together                                              |
| `* *`    | tutor                                       | view students grouped by timeslot                                  | see who attends each class                                                 |
| `* *`    | tutor                                       | record what was covered in the previous lesson                     | plan the next lesson effectively                                           |
| `* *`    | tutor                                       | record homework or follow-up tasks for each student                | track their work                                                           |
| `* *`    | tutor preparing for upcoming classes        | view upcoming classes together with student notes                  | prepare before teaching                                                    |
| `* *`    | tutor                                       | record student attendance                                          | keep an accurate lesson history                                            |
| `* *`    | privacy-conscious tutor                     | hide private contact details                                       | reduce the chance of someone else seeing them accidentally                 |
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

1. Tutor requests to add a student with the required details.

2. TutEasy validates the details and checks for duplicates.

3. TutEasy adds the student to the address book.

4. TutEasy updates the timetable view if the student has class timing information.

5. TutEasy displays a success message.

   Use case ends.

**Extensions**

* 1a. Tutor omits one or more required fields or uses an invalid flag.

    * 1a1. TutEasy shows an error message describing the invalid input.

      Use case resumes at step 1.

* 1b. Tutor enters an invalid phone number, email, subject, cost, or class timing.

    * 1b1. TutEasy shows an error message describing the invalid field.

      Use case resumes at step 1.

* 2a. The student already exists in the address book.

    * 2a1. TutEasy rejects the duplicate entry.
    * 2a2. TutEasy shows a duplicate student error message.

      Use case ends.

**Use case: Delete a student**

**MSS**

1. Tutor requests to delete the student at a specified index in the displayed student list.

2. TutEasy checks that the given index is valid.

3. TutEasy identifies the student at the specified index.

4. TutEasy deletes the student from the address book.

5. TutEasy refreshes the student list immediately.

6. TutEasy displays a success message.

   Use case ends.

**Extensions**

* 2a. The given index is outside the displayed student list.

    * 2a1. TutEasy does not delete any student.
    * 2a2. TutEasy shows an error message.

      Use case resumes at step 1.

* 4a. The deletion cannot be saved due to a storage failure.

    * 4a1. The student’s contact remains in the address book.
    * 4a2. TutEasy shows an error message.

      Use case ends.

* 1a. Tutor submits the same deletion while the first delete request is still being processed.

    * 1a1. TutEasy ignores the additional request.
    * 1a2. TutEasy shows a message informing the tutor that the deletion is already being processed.

      Use case resumes at step 2 for the original request.

**Use case: Tag a student**

**MSS**

1. Tutor requests to add one or more tags to a specific student by index.

2. TutEasy checks that the index is valid.

3. TutEasy validates each tag.

4. TutEasy checks that none of the tags already exist for that student.

5. TutEasy adds the tags to the student’s tag set.

6. TutEasy displays the tags next to the student’s name.

7. TutEasy displays a success message.

   Use case ends.

**Extensions**

* 1a. Tutor does not provide any tag.

    * 1a1. TutEasy shows an error message.

      Use case resumes at step 1.

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

* 5a. The student already has the maximum number of tags.

    * 5a1. TutEasy rejects the new tag.
    * 5a2. TutEasy shows a tag limit error message.

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

    * 4c1. TutEasy switches the display back to the original alphabetically sorted student list.

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

1. Tutor requests to find students by partial name keywords, tag keywords, or lesson timing keywords.

2. TutEasy validates the chosen search mode and search input.

3. TutEasy searches the student list.

4. TutEasy ranks matching students by relevance.

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
2.  Should be able to hold up to 1000 persons without noticeable sluggishness in performance for typical usage.
3.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
4.  With 1000 student records and 10,000 lesson records, each documented command should display either a success or an error message within 2 seconds on a computer with a 2 GHz dual-core processor and 8 GB of RAM.
5.  With 1000 student records and 10,000 lesson records, the application should be ready to accept a command within 5 seconds of launch on a computer with a 2 GHz dual-core processor, 8 GB of RAM, and a solid-state drive.
6.  All documented features should remain usable when the user's computer has no network connection.
7.  The application should not transmit any tutor, student, or lesson data over a network.
8.  After the application reports that a data-modifying command has succeeded, the resulting data should still be present after the application is terminated and restarted immediately.
9.  If a data file cannot be read, the application should leave that file unchanged unless the user explicitly confirms that it may be replaced.
10. The application should be distributed as a single JAR file that requires no software other than Java `25` or above on a supported OS.
11. In a usability test, at least four out of five first-time users who match the target user profile should be able to add a student, schedule a lesson, and find that lesson within 10 minutes after reading the Quick Start section of the User Guide, without assistance.
12. At a display resolution of 1280 x 720 and 100% OS display scaling, no text or control required for a documented workflow should be clipped or overlap another UI element.

### Glossary

* **Mainstream OS**: A commonly used operating system, such as Windows, macOS or Linux.
* **Student**: A person whose details are stored by the tutor and who can be enrolled in one or more classes.
* **Tutor**: The user of TutEasy who manages student records, classes and lessons.
* **Contact details**: Information used to contact a student or their parent or guardian, such as a phone number, email address or home address.
* **Private contact detail**: A contact detail that the tutor considers sensitive and does not want others to see.
* **Teaching mode**: Whether a lesson is conducted online or in person.
* **Location**: The physical address or online meeting link used to attend a lesson.
* **Subject**: The area of study taught in a class, such as Mathematics, Science or a specific university module.
* **Academic level**: The educational stage of a student or the intended standard of a class, such as Primary 5, Secondary 4 or JC1.
* **Class**: A teaching arrangement for one or more students, with an associated subject, academic level, schedule, teaching mode and location.
* **Lesson**: An individual session of a class, scheduled for a specific date and time.
* **One-off class**: A class consisting of a single scheduled lesson.
* **Recurring class**: A class whose lessons repeat according to a specified recurrence rule.
* **Recurrence rule**: The pattern determining when lessons repeat, such as every Monday or every two weeks.
* **Recurrence period**: The interval between repeating lessons, such as one week or two weeks.
* **Lesson series**: The set of lessons associated with a recurring class.
* **Timeslot**: A period identified by its start and end times on a specified day or date.
* **Scheduling conflict**: An overlap in the scheduled times of two or more lessons.
* **Scheduled**: A lesson status indicating that the lesson is planned and has not been marked as completed or cancelled.
* **Completed**: A lesson status indicating that the lesson has taken place.
* **Cancelled**: A lesson status indicating that a planned lesson has been called off.
* **Enrolment**: The association between a student and a class they attend.
* **Roster**: The list of students enrolled in a particular class.
* **Tag**: A label attached to a student record to help organise or identify students. A tag does not itself represent enrolment in a class.
* **Timetable**: A calendar view showing the tutor’s lessons and their scheduled dates and times.
* **Archived record**: An inactive student or class record hidden from the active view but retained for reference rather than permanently deleted.
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

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases … }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases … }_

### Searching by subject

Use a disposable data file with three students: Alice taking Mathematics and Physics, Bob taking Computer Science,
and Carol taking no subjects. These tests assume the student view; use `list` first if the timetable is open.

| Test input | Expected result |
| --- | --- |
| `find sub/mAtH` | Alice appears once; feedback reports one result. |
| `find sub/phys computer` | Alice and Bob appear in address-book order. |
| `find sub/math Math physics` | Alice appears once despite multiple matching keywords and classes. |
| `find Carol`, then `find sub/computer` | Bob appears; the subject search includes students outside the old results. |
| `find sub/Chemistry` | Empty results and a zero-result message. |
| `list` | All three students reappear. |
| `find sub/` or `find sub/Math!` | Error message; previous results and student records remain unchanged. |
| `find sub/Math sub/Physics` | Duplicate-prefix error; previous results remain unchanged. |
| `find sub/Math t/priority` or `find Alice sub/Math` | Mixed-mode error; previous results remain unchanged. |
| `timetable`, then `find sub/Math` | Timetable read-only error; use `list` before searching. |
| `find sub/computer`, then `delete 1` | Bob is deleted, even if he was not first in the complete list. |

After a subject search, exit and relaunch the app. All remaining students and their enrolments should be present;
the full student list should be displayed. Repeat `find sub/math` to check that saved class subjects remain searchable.

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

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
