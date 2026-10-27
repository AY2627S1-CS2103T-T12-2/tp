---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# TutEasy User Guide

TutEasy is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, TutEasy can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for TutEasy.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar tuteasy.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to TutEasy.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

Subject searches are described in the [subject search guide](SubjectSearch.md).

<box type="info" seamless>

**Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `...` can appear zero or more times.<br>
  For example, `[t/TAG]... ` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</box>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... [gn/GUARDIAN_NAME] [gp/GUARDIAN_PHONE] [ge/GUARDIAN_EMAIL] [sub/SUBJECT cost/COST c/SCHEDULE [mode/online|offline] [link/URL] [location/LOCATION] [notes/NOTES]]`

<box type="tip" seamless>

**Tip:** A student can have up to 10 tags. Tags must be unique, contain no more than 30 characters,
and use only lowercase letters, numbers, and single hyphens between characters.
</box>

* `sub/SUBJECT`, `cost/COST`, and `c/SCHEDULE` must be supplied together when adding a class.
* Times use the 24-hour range `0000`–`2359`, and the end time must be later than the start time.
* A weekly schedule uses `DAYHHmm-HHmm`, for example `Monday1800-1930`.
* A one-off schedule uses `yyyy-MM-ddTHHmm-HHmm`, for example `2026-10-20T1800-1930`.
* If class details are supplied, TutEasy creates the class if necessary and enrols the new student in it.

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`
* `add n/John Davis p/91234567 e/johndavis@gmail.com a/Clementi t/exam-prep sub/Math cost/30 c/Monday1800-1930`
* `add n/Jane Tan p/92345678 e/jane@example.com a/Bishan sub/Math cost/30 c/2026-10-20T1800-1930`

### Adding guardian and class details to a contact

The `add` command accepts optional guardian name (`gn/`), phone (`gp/`), and email (`ge/`) fields.
These are independent: for example, supply just `gp/` when only a parent's phone is known.
Non-empty guardian values follow the same validation rules as student contact fields.
The details appear on the student card and are saved across restarts. Privacy mode masks guardian details
alongside the student's phone, email, and address.

When adding a contact with a class (`sub/`, `cost/`, and complete `c/SCHEDULE`), you may also supply
`mode/online` or `mode/offline`. An online class may optionally have an HTTP(S) meeting URL in `link/`.
Offline classes cannot store a non-empty link. New classes default to offline when the mode is omitted.
Mode values are case-insensitive. Optional `location/` and `notes/` each accept up to 500 characters
of single-line text. Class fields require a complete class in the same `add` command.

Examples:

* `add n/Alice Tan p/91234567 e/alice@example.com a/Clementi gn/Mary Tan gp/98765432 ge/mary@example.com sub/Math cost/30 c/Saturday1800-1930 mode/online link/https://meet.example.com/math`
* `add n/Bob Tan p/81234567 e/bob@example.com a/Jurong gp/89999999`
* `add n/Cara Tan p/71234567 e/cara@example.com a/Tampines sub/Physics cost/40 c/Saturday1600-1730 mode/offline location/Room 2 notes/Bring calculator`

Each prefix may be supplied once. Links must contain a valid host and cannot contain spaces or
embedded credentials. A link is allowed only when the class is online.

Class IDs identify independently stored shared classes. When adding a contact to an existing class,
omitted fields inherit the class catalogue's values even when the class has no enrolled students.
If the catalogue class has no students and no extra details, the first contact may fill its mode,
link, location, and notes. For classes already in use, explicit values must agree with the shared values.
Existing classmates, guardian contacts, and payment statuses are not changed by adding a contact.
Mode and details appear on student cards and timetable entries and are saved once in the shared class catalogue.
Each student's enrolment stores only its class reference, agreed rate, and payment status.
Older saved classes without a mode default to offline, or online if they already contain a link.

The new field-entry scope is `add` only. `edit` and `class enrol` retain their existing syntax;
ordinary contact edits and enrolments preserve stored attributes. Aaron's `class edit` changes
subject, cost, or timing while retaining mode, link, location, notes, and individual payment status. Guardian/class-field editing is
not provided in this change.

Class details remain in the class catalogue after its last student unenrols. `class delete id/CLASS_ID`
removes the class and all of its enrolments. Class listing and timetable entries display the shared details,
including classes with no students. Use `list` to return from either read-only view before changing data.

### Adding a class: `class add`

Creates a class independently of any student. TutEasy assigns a stable class ID such as `C001`; IDs remain unchanged
when a class is edited and are not reused after deletion.

Format: `class add sub/SUBJECT cost/COST c/SCHEDULE`

Example: `class add sub/Physics cost/35 c/Wednesday1700-1830`

Classes cannot share the same subject and timing, and overlapping class times are rejected.

### Viewing classes: `class list`

Opens a read-only graphical list showing every class's stable ID, subject, schedule, cost, and stored mode/details.

Format: `class list`

This command is available from both the student list and timetable. While the class list is open, `list`, `timetable`,
`class list`, `privacy`, `help`, and `exit` are available. Use `list` to return to the student list or `timetable` to
open the timetable.

### Editing a class: `class edit`

Updates a class while retaining its stable ID and enrolments. Supply at least one field to change. Payment statuses and
existing students' agreed rates are preserved, and the new shared details appear for every enrolled student. Changing
`cost/` changes the default rate offered to future enrolments; it does not rewrite existing agreed rates.

Format: `class edit id/CLASS_ID [sub/SUBJECT] [cost/COST] [c/SCHEDULE]`

Examples:

* `class edit id/C001 cost/40`
* `class edit id/C001 sub/Advanced Math c/Tuesday1800-1930`

### Enrolling students in a class: `class enrol`

Enrols one or more existing students in a weekly or one-off class. Students are identified by their indexes in the
currently displayed student list. Weekly classes recur on their specified weekday; one-off classes occur only on their
specified date.

To enrol one student using a stable class ID, use:

`class enrol STUDENT_INDEX id/CLASS_ID`

Example: `class enrol 1 id/C001`

To enrol one or more students in an existing class using its subject and complete lesson schedule, omit the cost and
provide all student indexes before the prefixes:

`class enrol STUDENT_INDEX [STUDENT_INDEX]... sub/SUBJECT c/SCHEDULE`

Example: `class enrol 1 3 5 sub/Physics c/Wednesday1700-1830`

The existing single-student format that supplies the class cost is also supported:

`class enrol STUDENT_INDEX sub/SUBJECT cost/COST c/SCHEDULE`

The same class can be assigned to multiple students, and each enrolment keeps its own agreed rate. A student cannot be
enrolled in the same class more than once. The single-student format records the supplied `cost/` as that student's
agreed rate. For ID-based and subject-and-timing enrolment, TutEasy uses the class catalogue's current default rate.
This also supports classes that currently have no enrolled students.

Batch enrolment validates every index and the target class before changing any student. Repeated or invalid indexes
and a missing class cause the command to fail without changing any student. Selected students who are already enrolled
are skipped while the remaining students are enrolled. The feedback identifies both groups. If all selected students
are already enrolled, no changes are made and an informative message is shown. `cost/` cannot be supplied when multiple
indexes are used.

Enrolled classes appear as coloured badges below each student's email. Each badge includes the subject and lesson
timing, and students enrolled in the same class receive a badge with the same colour. When a student has multiple
classes, each badge group appears on its own row.

A subdued balance badge beside each student's name shows the total agreed rates of that student's unpaid enrolments.
Paid enrolments are excluded. The badge remains visible as `Balance: 0` when the student has no unpaid enrolments.

### Unenrolling a student from a class: `class unenrol`

Removes one class from one student without affecting other students enrolled in that class.

Format: `class unenrol STUDENT_INDEX id/CLASS_ID`

Example: `class unenrol 1 id/C001`

### Updating an enrolment's payment status: `class payment`

Marks one student's enrolment in a weekly or one-off class as paid or unpaid. The student is identified by the index
shown in the current student list, while the stable class ID identifies the enrolment.

Format: `class payment STUDENT_INDEX id/CLASS_ID status/paid|unpaid`

Examples:

* `class payment 1 id/C001 status/paid`
* `class payment 1 id/C001 status/unpaid`

New and previously saved enrolments default to unpaid. A subdued status label appears immediately after the
corresponding coloured class badge, without sharing the badge's background colour. Each class badge and status label
stay together on one row, keeping the status associated with the correct class. Updating one student's payment status
does not affect other students enrolled in the same class. If the specified student is not enrolled in that class, no
data is changed.

Payment status describes the current enrolment as a whole; it is not a payment history for individual weekly sessions
or billing months. Correspondingly, the displayed balance is the sum of the current unpaid enrolments rather than a
history of accumulated charges.

A successful status change is saved automatically and restored after restarting TutEasy. The balance updates to reflect
the new unpaid total. Repeating the current status reports that it is already set. An invalid student index, invalid
status, or student who is not enrolled in the specified class receives an error without changing the enrolment.

### Deleting a class: `class delete`

Deletes a class globally by removing it from every enrolled student. Students and their other information are not
deleted. The command searches the complete address book even when the displayed student list is filtered.

Format: `class delete id/CLASS_ID`

Example: `class delete id/C001`

### Viewing the weekly timetable: `timetable`

Opens a graphical, read-only timetable containing the complete Sunday-to-Saturday week that includes the current date.
Today is highlighted and placed at the top of the scrolling area where possible. Scroll upward to see earlier days in
the week and downward to see later days. Every day has a dated heading, even when it has no classes.

Use **Previous week** and **Next week** to move exactly seven days at a time. Use **Today** to recalculate the current
date, return to its week, and reload the timetable. Today remains available even while the current week is displayed.
The fixed header shows the selected range, for example `18–24 Oct 2026` or `27 Dec 2026 – 2 Jan 2027`.

Each class entry shows its start and end time, subject, and all attending students. Classes are ordered by start time,
then subject, then end time. Student names within a class are ordered alphabetically.

Weekly classes appear on their matching weekday. One-off classes appear only when their exact date falls within the
selected week. Navigating between weeks does not change or save address-book data.

Format: `timetable`

While the timetable is open, `class list`, `list`, `privacy`, `help`, and `exit` are available. `class list` replaces
the timetable with the class list. Use `list` to return to the student list and re-enable modifying commands. Re-entering
`timetable` starts again from the current week rather than the previously viewed week.

### Listing all persons: `list`

Shows all students in the address book. Pinned students appear before unpinned students.

Format: `list`

### Pinning a student: `pin`

Pins a student so that they appear above unpinned students in the student list. The pin is saved and remains after
TutEasy is restarted.

Format: `pin INDEX`

* `INDEX` refers to the index number shown in the displayed student list.
* The student must not already be pinned.
* During a search, only matching students are shown; matching pinned students appear before matching unpinned students.
* Search relevance and the existing list order are preserved within the pinned and unpinned groups.

Example: `pin 3`

Pinned students display a `PINNED` badge beside their name and balance.

### Unpinning a student: `unpin`

Unpins a student and returns them to the unpinned section of the student list.

Format: `unpin INDEX`

* `INDEX` refers to the index number shown in the displayed student list.
* The student must already be pinned.

Example: `unpin 1`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]... `

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, ...
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Tagging students: `tag`

Adds tags to one or more existing students.

Formats:

* `tag INDEX TAG [MORE_TAGS]`
* `tag STUDENT_INDEX [MORE_INDEXES] t/TAG`

* Indexes refer to the index numbers shown in the displayed student list.
* Each tag must be unique for that student.
* A tag can contain up to 30 characters and use only lowercase letters, numbers, and single hyphens between
  characters.
* Spaces separate tags. For example, `exam prep` adds the two tags `exam` and `prep`, while `exam-prep` adds
  one tag. Use hyphens when a single tag contains multiple words.
* A student can have at most 10 tags.
* The first format preserves the existing behavior of adding one or more tags to one student.
* The batch format uses `t/` to separate one tag from one or more unique student indexes. This also allows numeric
  tags such as `t/2026` without confusing them with student indexes.
* The batch command validates every index and the tag before updating any student. An invalid or repeated index, or
  an invalid tag, causes the command to fail without changes.
* In batch mode, students who already have the tag are skipped. Students who have reached the 10-tag limit are also
  skipped, while eligible selected students still receive the tag.
* Batch feedback identifies students who received the tag, already had it, or had reached the tag limit. If no
  selected student can be updated, no changes are made and the feedback explains why.

Examples:

* `tag 3 exam-prep needs-follow-up`
* `tag 1 3 5 t/math-2026`

### Removing tags from a student: `untag`

Removes one or more existing tags from a student, or removes all their tags.

Format: `untag INDEX TAG [MORE_TAGS]` or `untag INDEX -all`

Examples:
* `untag 2 parent-follow-up`
* `untag 5 -all`

### Locating persons by name, tag, or lesson: `find`

Finds students whose names, tags, or lesson timings partially match the given search input.

Formats:
* `find KEYWORD [MORE_KEYWORDS]`
* `find t/TAG_KEYWORD [MORE_TAG_KEYWORDS]`
* `find c/DAY [TIME_HHmm]`, `find c/TIME_HHmm`, or `find c/DATE [TIME_HHmm]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Without `t/`, the search considers only names.
* With `t/`, the search considers only tags. Stored tags use lowercase, but tag search keywords are case-insensitive
  for input convenience, e.g. `find t/PRI` matches `priority`.
* Name and tag searches use partial matching, e.g. `ali` matches `Alice`, and `pri` matches `priority`.
* With `c/`, search by lesson day, exact date, time, or a supported combination. Days accept full names or prefixes,
  e.g. `Monday` or `Mon`. Exact dates use `yyyy-MM-dd`. Times use HHmm style from `0000` to `2359`, e.g. `1800`;
  shorter fragments such as `18` are accepted for partial matching.
* An empty search, empty `t/` or `c/` value, or invalid tag or lesson keyword is rejected with an error. The current
  displayed results remain unchanged.
* Name and tag searches return students matching at least one keyword. Matching pinned students are shown before
  matching unpinned students. Within each group, students with more matching keywords are shown first; ties keep the
  current list order.
* After a successful `find`, the command box shows this light-grey hint: `Type 'list' to return to the home page.`

Examples:
* `find Joh` returns `John` and `John Doe`
* `find t/fri` returns students tagged `friends`
* `find c/Monday` returns students with Monday lessons
* `find c/1800` returns students with lessons containing `1800`
* `find c/Mon 1800` returns students with Monday lessons containing `1800`
* `find c/2026-10-20` returns students with a one-off lesson on 20 October 2026
* `find c/2026-10-20 1800` additionally requires the one-off lesson to contain `1800`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

The command box shows light-grey hints for common `find` inputs, such as `find`, the available search formats, the
lesson example `find c/Monday 1800`, and the post-search `Type 'list'...` hint.

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, ...

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Hiding contact details: `privacy`

Privacy mode replaces the phone, email, and address values on every student card with a fixed seven-asterisk mask,
such as `Address: *******`. The underlying contact details are not changed or deleted.

Formats:

* `privacy on` masks all displayed contact details immediately.
* `privacy off` restores the original contact details immediately.

While privacy mode is active, the status bar displays `Privacy mode enabled`. The selected mode is saved and restored
when TutEasy restarts. Privacy mode is disabled by default for existing preference files that do not contain this
setting.

The command box suggests both valid modes after you type `privacy`.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

TutEasy automatically saves data after every command. You do not need to save manually.

### Editing the data file

TutEasy data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, TutEasy starts with an empty address book at the next run. The invalid file remains on disk until you run a command (TutEasy saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause TutEasy to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous TutEasy home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... [gn/GUARDIAN_NAME] [gp/GUARDIAN_PHONE] [ge/GUARDIAN_EMAIL] [sub/SUBJECT cost/COST c/SCHEDULE [mode/online\|offline] [link/URL] [location/LOCATION] [notes/NOTES]]` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/Clementi t/friend sub/Math cost/30 c/Monday1800-1930`
**Add class** | `class add sub/SUBJECT cost/COST c/SCHEDULE`<br> e.g., `class add sub/Physics cost/35 c/Wednesday1700-1830`
**List classes** | `class list`
**Edit class** | `class edit id/CLASS_ID [sub/SUBJECT] [cost/COST] [c/SCHEDULE]`<br> e.g., `class edit id/C001 cost/40`
**Enrol in class** | `class enrol STUDENT_INDEX id/CLASS_ID`<br>or `class enrol STUDENT_INDEX [STUDENT_INDEX]... sub/SUBJECT c/SCHEDULE`<br> e.g., `class enrol 1 3 5 sub/Physics c/Wednesday1700-1830`
**Unenrol from class** | `class unenrol STUDENT_INDEX id/CLASS_ID`<br> e.g., `class unenrol 1 id/C001`
**Update payment status** | `class payment STUDENT_INDEX id/CLASS_ID status/paid|unpaid`<br> e.g., `class payment 1 id/C001 status/paid`
**Delete class** | `class delete id/CLASS_ID`<br> e.g., `class delete id/C001`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Tag**    | `tag INDEX TAG [MORE_TAGS]` or `tag STUDENT_INDEX [MORE_INDEXES] t/TAG`<br> e.g., `tag 3 exam-prep needs-follow-up`, `tag 1 3 5 t/math-2026`
**Untag**  | `untag INDEX TAG [MORE_TAGS]` or `untag INDEX -all`<br> e.g., `untag 2 parent-follow-up`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`, `find t/TAG_KEYWORD [MORE_TAG_KEYWORDS]`, `find c/DAY [TIME_HHmm]`, `find c/TIME_HHmm`, or `find c/DATE [TIME_HHmm]`<br> e.g., `find Jam`, `find t/fri`, `find c/Mon 1800`, `find c/2026-10-20`
**List**   | `list`
**Help**   | `help`
**Timetable** | `timetable`
**Privacy mode** | `privacy on` or `privacy off`
