---
  layout: default.md
  title: "User Guide"
  pageNav: 3
---

# AB-3 User Guide

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

<!-- * Table of Contents -->
<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

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

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... [sub/SUBJECT cost/COST c/DAYHHmm-HHmm]`

<box type="tip" seamless>

**Tip:** A student can have up to 10 tags. Tags must be unique, contain no more than 30 characters,
and use only lowercase letters, numbers, and single hyphens between characters.
</box>

* `sub/SUBJECT`, `cost/COST`, and `c/DAYHHmm-HHmm` must be supplied together when adding a class.
* Times use the 24-hour range `0000`–`2359`, and the end time must be later than the start time.

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`
* `add n/John Davis p/91234567 e/johndavis@gmail.com a/Clementi t/exam-prep sub/Math cost/30 c/Monday1800-1930`

### Adding a class to a student: `addclass`

Adds a recurring weekly class to an existing student.

Format: `addclass INDEX sub/SUBJECT cost/COST c/DAYHHmm-HHmm`

Example: `addclass 1 sub/Physics cost/35 c/Wednesday1700-1830`

### Viewing the weekly timetable: `timetable`

Displays classes grouped by day and ordered by start time. Each entry includes its subject and students.

Format: `timetable`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

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

### Tagging a student: `tag`

Adds one or more tags to an existing student.

Format: `tag INDEX TAG [MORE_TAGS]`

* `INDEX` refers to the index number shown in the displayed student list.
* Each tag must be unique for that student.
* A tag can contain up to 30 characters and use only lowercase letters, numbers, and single hyphens between
  characters.
* Spaces separate tags. For example, `exam prep` adds the two tags `exam` and `prep`, while `exam-prep` adds
  one tag. Use hyphens when a single tag contains multiple words.
* A student can have at most 10 tags.

Example: `tag 3 exam-prep needs-follow-up`

### Removing tags from a student: `untag`

Removes one or more existing tags from a student, or removes all their tags.

Format: `untag INDEX TAG [MORE_TAGS]` or `untag INDEX -all`

Examples:
* `untag 2 parent-follow-up`
* `untag 5 -all`

### Locating persons by name, tag, or lesson: `find`

Finds persons whose names, tags, or lessons contain any of the given keywords.

Formats:
* `find KEYWORD [MORE_KEYWORDS]`
* `find t/TAG_KEYWORD [MORE_TAG_KEYWORDS]`
* `find c/DAYHHmm`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* Without `t/`, the search considers only names.
* With `t/`, the search considers only tags.
* With `c/`, the search returns students whose classes start at the specified day and time.
* Name and tag searches require full-word matches; for example, `Han` does not match `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find t/friends` returns persons tagged `friends`
* `find c/Monday1800` returns students whose class starts on Monday at 18:00
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

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

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<box type="warning" seamless>

**Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</box>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action     | Format, Examples
-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------
**Add**    | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]... [sub/SUBJECT cost/COST c/DAYHHmm-HHmm]` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/Clementi t/friend sub/Math cost/30 c/Monday1800-1930`
**Add class** | `addclass INDEX sub/SUBJECT cost/COST c/DAYHHmm-HHmm`<br> e.g., `addclass 1 sub/Physics cost/35 c/Wednesday1700-1830`
**Clear**  | `clear`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Edit**   | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]... `<br> e.g.,`edit 2 n/James Lee e/jameslee@example.com`
**Tag**    | `tag INDEX TAG [MORE_TAGS]`<br> e.g., `tag 3 exam-prep needs-follow-up`
**Untag**  | `untag INDEX TAG [MORE_TAGS]` or `untag INDEX -all`<br> e.g., `untag 2 parent-follow-up`
**Find**   | `find KEYWORD [MORE_KEYWORDS]`, `find t/TAG_KEYWORD [MORE_TAG_KEYWORDS]`, or `find c/DAYHHmm`<br> e.g., `find James Jake`, `find t/friends`, `find c/Monday1800`
**List**   | `list`
**Help**   | `help`
**Timetable** | `timetable`
