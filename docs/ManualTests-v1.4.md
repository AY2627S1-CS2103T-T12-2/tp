# v1.4 manual tests: inline class fields and guardian contacts

Use a separate test-data folder so the commands below do not change real students.
From the repository, build with `./gradlew shadowJar`. Close any existing TutEasy window, then run:

```sh
TEST_DIR=$(mktemp -d /tmp/tuteasy-v14.XXXXXX)
cd "$TEST_DIR"
java -jar /Users/Laxshan/Documents/tp/build/libs/tuteasy.jar
```

Keep the terminal open for the restart test. Only in this isolated instance, enter `clear` to remove sample data.

## 1. Add online and offline classes inline

Enter the commands one at a time:

```text
add n/Alice Tan p/91234567 e/alice@example.com a/Clementi gn/Mary Tan gp/98765432 ge/mary@example.com sub/Math cost/30 c/Saturday1800-1930 mode/online link/https://meet.example.com/math?room=1&code=123
add n/Bob Tan p/81234567 e/bob@example.com a/Jurong
class enrol 2 sub/Math cost/30 c/Saturday1800-1930
class enrol 1 sub/Physics cost/40 c/Saturday1600-1730 mode/offline location/Room 2
```

Expected:

- Alice shows her guardian's name, phone, and email; Bob has none.
- Both students' Math shows Online and the same meeting URL. Bob inherits the existing class fields.
- Alice's Physics shows Offline, Room 2, and no link. Alice's balance is 70 and Bob's is 30.
- The existing `add` and `class enrol` commands accept these fields; no separate details command is needed.

Enter `timetable`: Saturday shows one Math class with both students, its online mode and URL,
and a separate offline Physics class. Enter `list` before continuing.

## 2. Edit guardian fields independently

| Command | Expected result |
| --- | --- |
| `edit 1 gp/88888888` | Changes only Alice's guardian phone. |
| `edit 1 ge/` | Clears only guardian email. |
| `edit 1 ge/newmary@example.com p/91111111` | Changes guardian email and student phone, preserves class fields. |
| `tag 1 exam-prep` | Adds a tag without removing guardian or class fields. |
| `find Bob` then `edit 1 gn/Ben Tan gp/89999999` then `list` | Edits Bob's guardian using the displayed index; Alice stays unchanged. |

## 3. Edit online fields and switch modes

| Command | Expected result |
| --- | --- |
| `edit 1 sub/Math c/Saturday1800-1930 link/https://meet.example.com/newmath` | Replaces the link for both Math students; mode remains Online. |
| `edit 1 sub/Math c/Saturday1800-1930 link/` | Clears only the link; class stays Online. |
| `edit 1 sub/Math c/Saturday1800-1930 link/https://meet.example.com/math notes/Bring calculator` | Saves the new URL and notes. |
| `edit 1 sub/Math c/Saturday1800-1930 mode/offline` | Both students' Math switches to Offline and its link is cleared. Physics and guardian details stay unchanged. |
| `edit 1 sub/Math c/Saturday1800-1930 mode/online` | Both students' Math switches to Online with no link; a link is optional. |
| `find Alice` then `edit 1 sub/Math c/Saturday1800-1930 link/https://meet.example.com/finalmath` then `list` | Both Alice and hidden classmate Bob get the link. |

## 4. Preserve payments and inherit shared details

```text
class payment 1 sub/Math c/Saturday1800-1930 status/paid
edit 1 sub/Math c/Saturday1800-1930 mode/offline
edit 1 sub/Math c/Saturday1800-1930 mode/online link/https://meet.example.com/finalmath
add n/Cara Tan p/71234567 e/cara@example.com a/Tampines gp/77777777
class enrol 3 sub/Math cost/30 c/Saturday1800-1930
add n/Dan Tan p/61234567 e/dan@example.com a/Bedok sub/Math cost/30 c/Saturday1800-1930
```

Expected: Alice's Math remains Paid and her balance is 40. Everyone else's Math is Unpaid, with balance 30.
All four students share the Online mode and meeting link. Cara can store only a guardian phone.
The timetable still has one Math entry with all four students.

## 5. Reject invalid input without changing any data

Run these separately. Each should show an error and leave all student and class fields unchanged:

```text
edit 1 gp/abc
edit 1 ge/not-an-email
edit 1 gn/@Mary
edit 99 gn/Mary
edit 1 gp/123 gp/456
edit 1 mode/online
edit 1 sub/Math c/Saturday1800-1930
edit 1 sub/Unknown c/Saturday1800-1930 mode/online
edit 1 sub/Math c/Saturday1800-1930 mode/hybrid
edit 1 sub/Math c/Saturday1800-1930 mode/
edit 1 sub/Math c/Saturday1800-1930 mode/online mode/offline
edit 1 sub/Math c/Saturday1800-1930 mode/offline link/https://example.com
edit 1 n/Should Not Change sub/Physics c/Saturday1600-1730 link/https://example.com
edit 1 sub/Math c/Saturday1800-1930 link/not-a-url
edit 1 sub/Math c/Saturday1800-1930 link/javascript:alert(1)
add n/Eve Tan p/51111111 e/eve@example.com a/Woodlands mode/online
add n/Eve Tan p/51111111 e/eve@example.com a/Woodlands sub/Math cost/30 c/Saturday1800-1930 mode/offline
class details sub/Math c/Saturday1800-1930 link/https://example.com
```

The Physics link error must also leave Alice's name unchanged: an invalid class edit cannot partially apply contact edits.
The final command is intentionally invalid because the standalone command has been removed.

Also verify an offline class rejects a non-empty link unless `mode/online` is supplied in the same command.
Try duplicate `link/` fields and a 501-character note: both should be rejected.

## 6. Restart and check saved data

Enter `exit` and relaunch from the same isolated terminal folder:

```sh
java -jar /Users/Laxshan/Documents/tp/build/libs/tuteasy.jar
```

Expected: mode, meeting link, location, notes, guardian edits, payment statuses, tags, and balances survive.
Alice's Math is Paid and the other students' Math is Unpaid. Physics remains Offline without a link.
Resize and scroll the app: mode/link text should wrap and remain associated with its class.

## 7. Clear contacts and verify class lifecycle

| Command | Expected result |
| --- | --- |
| `edit 1 gn/ gp/ ge/` | Clears Alice's guardian only. |
| `edit 1 sub/Math c/Saturday1800-1930 mode/offline location/ notes/` | Math becomes Offline with no optional details, preserving payment status. |
| `class unenrol 1 sub/Math c/Saturday1800-1930` | Removes Alice from Math; Physics and contacts remain. |
| `class delete sub/Math c/Saturday1800-1930` | Removes Math for everyone while preserving guardians. |
| `class enrol 2 sub/Math cost/30 c/Saturday1800-1930` | Recreates Math as Offline and Unpaid, without a link. |

## Automated verification

From the repository, run `./gradlew check`.
The tests cover inline fields, mode transitions, offline link rejection, guardian contacts, filtered edits,
shared inheritance/conflicts, no partial updates, saved-data compatibility, and UI labels.
