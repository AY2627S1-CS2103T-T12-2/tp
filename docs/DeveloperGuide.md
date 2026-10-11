---
  layout: default.md
  title: "Developer Guide"
  pageNav: 3
---

# AB-3 Developer Guide

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
`TimetablePanel`, and `StatusBarFooter`. `MainWindow` switches the main content placeholder between the person list and
the read-only timetable according to the `ViewChange` carried by a successful `CommandResult`. These UI classes inherit
from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

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

`LogicManager` also tracks whether the person list or timetable is active. In timetable mode it rejects every command
except `list`, `help`, and `exit` before execution. `timetable` and `list` return view-change instructions that keep this
state synchronized with `MainWindow`.

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
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* derives immutable timetable day and class entries from every person's lessons. The timetable is not stored separately,
  and it uses the complete address book rather than the filtered person list.
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

### Timetable view

`TimetableCommand` does not modify model data. It returns a `CommandResult` requesting the `TIMETABLE` view.
`MainWindow` responds by requesting a current timetable from `Logic` and replacing `PersonListPanel` with a newly built
`TimetablePanel`.

`ModelManager#getTimetable(LocalDate)` passes the complete address-book person list to `TimetableBuilder`. The builder
iterates through each person's `Lesson` values, omits days before the supplied date's weekday, and groups equal lessons
with their students. It then returns one immutable `TimetableDay` from the current day through Saturday, including empty
days. Entries within a day are ordered by start time, subject, and end time.

The UI renders the projection in three levels: `TimetablePanel` provides the scrolling list, `TimetableDayCard` displays
a day heading and empty state, and `TimetableEntryCard` displays a class's time, subject, and students. Entering `list`
restores the existing person panel and normal command availability.

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
| `* * *`  | tutor                                       | record a student's subject and academic level                      | prepare suitable lesson materials                                         |
| `* * *`  | tutor                                       | create a class with its subject, level, times, mode, and location  | represent a complete teaching arrangement                                 |
| `* * *`  | tutor                                       | give each class a recognizable name or identifier                  | distinguish classes with similar subjects and times                       |
| `* * *`  | tutor                                       | edit or remove a class independently of its students               | change my timetable without incorrectly altering student records          |
| `* * *`  | tutor                                       | add students to or remove students from a class                    | maintain individual and group class rosters                               |
| `* * *`  | tutor teaching a student multiple subjects  | enroll the same student in multiple classes                        | represent their different teaching arrangements accurately               |
| `* * *`  | tutor                                       | record each class's day, start time, and end time                  | know when I am teaching and identify scheduling overlaps                  |
| `* * *`  | tutor                                       | create either a one-off or recurring class with a recurrence period | schedule both ad-hoc and regular lessons                                  |
| `* * *`  | tutor with recurring classes                | have recurring lessons appear automatically                        | avoid creating the same lesson every week                                 |
| `* * *`  | tutor                                       | view my tutoring classes in a weekly timetable                     | understand my schedule at a glance                                        |
| `* * *`  | tutor                                       | navigate between timetable weeks and return to the current week   | inspect past and future schedules easily                                  |
| `* * *`  | tutor                                       | see the subject, students, time, and teaching mode for each class  | view important lesson details immediately                                 |
| `* * *`  | tutor                                       | view all classes for a selected day                                | prepare for upcoming lessons                                               |
| `* * *`  | tutor teaching online and in-person lessons | record whether a lesson is online or in person                     | know how to attend it                                                      |
| `* * *`  | tutor                                       | store the meeting link or lesson address                           | access the class location quickly                                          |
| `* * *`  | tutor                                       | reschedule or cancel an individual lesson                          | keep timetable changes accurate                                            |
| `* * *`  | tutor changing a recurring class            | apply a change to one lesson, future lessons, or the entire series | avoid changing unrelated lessons accidentally                             |
| `* * *`  | tutor                                       | mark a lesson as scheduled, completed, or cancelled                | keep my timetable and lesson history accurate                             |
| `* * *`  | tutor with a busy schedule                  | be warned when two classes overlap                                 | avoid scheduling conflicts                                                 |
| `* * *`  | tutor                                       | view a class's roster and each student's enrolled classes         | understand class membership from either direction                         |
| `* * *`  | tutor                                       | clear an active search, filter, or grouping                        | return to the complete student list easily                                |
| `* * *`  | keyboard-driven user                        | receive clear success messages and actionable error messages      | know whether a command worked and how to correct invalid input            |
| `* * *`  | tutor                                       | have my student and class information saved and restored automatically | avoid losing my records when the application restarts                 |
| `* * *`  | tutor                                       | have invalid dates, times, contact details, and recurrence rules rejected | prevent incorrect data from entering my records or timetable       |
| `* *`    | tutor                                       | undo an accidental add, edit, deletion, cancellation, or rescheduling | recover from mistakes quickly                                          |
| `* *`    | tutor                                       | archive inactive students and discontinued classes                | hide old records without permanently deleting their history               |
| `* *`    | tutor                                       | be warned about possible duplicate students or classes            | avoid creating redundant records accidentally                             |
| `* *`    | tutor                                       | export and restore a backup of my records                          | recover from device or file failure                                       |
| `* *`    | tutor deleting a student or class           | see which enrollments and lessons will be affected                | avoid removing related information accidentally                           |
| `* *`    | tutor                                       | view my next few lessons when the application opens               | prepare without navigating the full timetable                             |
| `* *`    | tutor with many students                    | sort and filter my students                                        | manage a large contact list easily                                         |
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

1. Tutor requests to delete a specific student by name.

2. TutEasy searches for students matching the given name.

3. TutEasy finds exactly one matching student.

4. TutEasy deletes the student from the address book.

5. TutEasy refreshes the student list immediately.

6. TutEasy displays a success message.

   Use case ends.

**Extensions**

* 2a. No student with the given name is found.

    * 2a1. TutEasy does not delete any student.
    * 2a2. TutEasy shows an error message.

      Use case resumes at step 1.

* 3a. More than one student with the same name is found.

    * 3a1. TutEasy does not delete any student.
    * 3a2. TutEasy shows a message asking the tutor to select the correct student from the displayed matches.

      Use case resumes at step 4.

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

3. TutEasy switches the display to timetable mode.

4. TutEasy shows students and classes arranged by day and time.

   Use case ends.

**Extensions**

* 1a. Tutor requests to view the default student list.

    * 1a1. TutEasy switches the display back to the original alphabetically sorted student list.

      Use case ends.

* 2a. Tutor is already in the requested view mode.

    * 2a1. TutEasy does not switch the view.
    * 2a2. TutEasy shows a message informing the tutor that the current view mode is already active.

      Use case ends.

* 1b. Tutor enters non‑alphabetic characters that cause an invalid command.

    * 1b1. TutEasy shows an error message.

      Use case resumes at step 1.

**Use case: Find students**

**MSS**

1. Tutor requests to find students by one or more keywords or by a tag.

2. TutEasy searches the student list.

3. TutEasy displays the matching students.

   Use case ends.

**Extensions**

* 2a. No student matches the keyword or tag.

    * 2a1. TutEasy displays a message to show that there are no people matched.

      Use case resumes at step 1.

* 2b. Multiple search results refer to the same student.

    * 2b1. TutEasy removes the duplicate results.
    * 2b2. TutEasy displays only the unique matching students.

      Use case resumes at step 3.

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

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases … }_
