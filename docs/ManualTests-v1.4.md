# v1.4 manual tests: class details and guardian contacts

Run this checklist on branch `v1.4`. Use a separate test-data folder so that `clear` below does not remove
real students. The app reads its data and preferences relative to its launch folder.

## Start an isolated test instance

Close any existing TutEasy window. From the repository, build the application:

```sh
./gradlew shadowJar
```

Then start it in a fresh folder (keep this terminal open for the restart test):

```sh
TEST_DIR=$(mktemp -d /tmp/tuteasy-v14.XXXXXX)
cd "$TEST_DIR"
java -jar /Users/Laxshan/Documents/tp/build/libs/tuteasy.jar
```

Only in this isolated instance, enter `clear` to remove sample data. Enter these commands one at a time:

```text
add n/Alice Tan p/91234567 e/alice@example.com a/Clementi gn/Mary Tan gp/98765432 ge/mary@example.com
add n/Bob Tan p/81234567 e/bob@example.com a/Jurong
class enrol 1 sub/Math cost/30 c/Saturday1800-1930
class enrol 2 sub/Math cost/30 c/Saturday1800-1930
class enrol 1 sub/Physics cost/40 c/Saturday1600-1730
```

Expected: Alice has guardian details, Bob has none. Alice's balance is 70 and Bob's is 30.

## 1. Edit guardian details

| Command | Expected result |
| --- | --- |
| `edit 1 gp/88888888` | Only Alice's guardian phone changes; name and email remain. |
| `edit 1 ge/` | Guardian email disappears; name and phone remain. |
| `edit 1 ge/newmary@example.com` | New email appears. |
| `edit 1 p/91111111` | Student phone changes; all guardian and class details remain. |
| `tag 1 exam-prep` | Tag appears without removing guardian or class details. |
| `find Bob` then `edit 1 gn/Ben Tan gp/89999999` then `list` | Bob gets the guardian details; Alice's guardian remains unchanged. |

## 2. Store shared class details

Enter:

```text
class details sub/Math c/Saturday1800-1930 link/https://meet.example.com/math?room=1&code=123 location/Room 2 notes/Bring calculator
```

Expected: the link, location, and notes appear for Math on both student cards. Physics stays unchanged.
Resize the window and scroll: details should wrap, remain associated with Math, and not obscure guardian details.
The meeting URL is stored/displayed as text; entering it does not open a browser.

Enter `timetable`: Saturday has one Math entry with both students and the same class details.
Enter `list` to return (the timetable is read-only).

## 3. Preserve omitted fields and clear explicitly

| Command | Expected result |
| --- | --- |
| `class details sub/math c/Saturday1800-1930 notes/Bring textbook` | Updates notes for both students, preserves link and location; subject matching ignores case. |
| `class details sub/Math c/Saturday1800-1930 link/` | Removes link for both students, retains location and notes. |
| `find Alice` then `class details sub/Math c/Saturday1800-1930 location/Room 3` then `list` | Both students show Room 3, including Bob who was hidden by the search. |

## 4. Preserve payment status and inherit shared details

```text
class payment 1 sub/Math c/Saturday1800-1930 status/paid
class details sub/Math c/Saturday1800-1930 notes/Bring workbook
add n/Cara Tan p/71234567 e/cara@example.com a/Tampines gp/77777777
class enrol 3 sub/Math cost/30 c/Saturday1800-1930
add n/Dan Tan p/61234567 e/dan@example.com a/Bedok sub/Math cost/30 c/Saturday1800-1930
```

Expected:

- Alice's Math is Paid and her balance is 40; other students' Math remains Unpaid.
- All four students show Room 3 and Bring workbook for Math; the link stays cleared.
- Cara can have only a guardian phone, without a name or email.
- The timetable still groups all four students into one Math entry with the shared details.

## 5. Restart and verify persistence

Enter `exit`. From the same isolated folder, relaunch:

```sh
java -jar /Users/Laxshan/Documents/tp/build/libs/tuteasy.jar
```

Expected: all four students, guardian edits, shared details, tags, payment statuses, and balances survive.
Verify that Alice's Math is still Paid and the others' Math remains Unpaid.

## 6. Reject invalid input without changing data

Run each command separately. Each should show an error, keep the app responsive, and leave data unchanged:

```text
edit 1 gp/abc
edit 1 gp/12
edit 1 ge/not-an-email
edit 1 gn/@Mary
edit 99 gn/Mary
edit 1 gp/123 gp/456
add n/Eve Tan p/51111111 e/eve@example.com a/Woodlands ge/invalid
class details sub/Math c/Saturday1800-1930
class details sub/Math c/Saturday1800-1930 link/not-a-url
class details sub/Math c/Saturday1800-1930 link/javascript:alert(1)
class details sub/Math c/Saturday1800-1930 link/https://
class details sub/Math c/Saturday1800-1930 link/https://example.com link/https://other.example.com
class details sub/Unknown c/Saturday1800-1930 notes/Test
class details sub/Math c/Saturday2400-2500 notes/Test
```

Also try `notes/` followed by 501 letters: it should be rejected. A value of exactly 500 letters should be accepted;
restore `notes/Bring workbook` afterward.

## 7. Clear details and test the class lifecycle

| Command | Expected result |
| --- | --- |
| `edit 1 gn/ gp/ ge/` | Alice's guardian row disappears; other student/lesson data remains. |
| `class details sub/Math c/Saturday1800-1930 location/ notes/` | All optional Math details disappear from student cards and timetable; payment status stays unchanged. |
| `class details sub/Math c/Saturday1800-1930 notes/Temporary note` | Note appears for everyone. |
| `class unenrol 1 sub/Math c/Saturday1800-1930` | Alice loses Math; her Physics and student details remain, other Math students retain the note. |
| `class delete sub/Math c/Saturday1800-1930` | Math disappears for every student; guardian contacts remain. |
| `class enrol 2 sub/Math cost/30 c/Saturday1800-1930` | Recreated Math has no extra details and is Unpaid. |

## Automated verification

From the repository, run:

```sh
./gradlew check
```

The tests cover both features, backward-compatible JSON loading, real save/load round trips, malformed saved
values, filtered lists, clearing fields, class inheritance, payment preservation, and JavaFX labels.
