# v1.4 manual tests: contact attributes with PR 88's class catalogue

This branch includes the latest upstream class catalogue, scheduling, pinning, privacy, and batch commands. Guardian/class attribute entry remains scoped to `add`.
Existing class add/list/edit/enrol/unenrol/delete commands use Aaron's class-ID syntax.

## Safe setup

Close any running TutEasy window. From the repository build with `./gradlew shadowJar`, then launch
in a fresh test-data folder so the steps below do not affect real students:

```sh
TEST_DIR=$(mktemp -d /tmp/tuteasy-v14.XXXXXX)
cd "$TEST_DIR"
java -jar /Users/Laxshan/Documents/tp/build/libs/tuteasy.jar
```

Keep the terminal open for restarting. Only in this isolated instance, enter `clear` to remove sample data.

## 1. Add a contact and an online class

```text
add n/Alice Tan p/91234567 e/alice@example.com a/Clementi gn/Mary Tan gp/98765432 ge/mary@example.com sub/Math cost/30 c/Saturday1800-1930 mode/online link/https://meet.example.com/math?room=1&code=123 notes/Bring calculator
```

Expected: Alice shows all guardian fields, Online Math, the full URL, and the note. Math is class `C001`.
Alice is Unpaid and her balance is 30. `class list` shows the same details for C001; enter `list` to return.

## 2. Add optional fields and offline classes

```text
add n/Bob Tan p/81234567 e/bob@example.com a/Jurong gp/89999999
add n/Cara Tan p/71234567 e/cara@example.com a/Tampines sub/Physics cost/40 c/Saturday1600-1730 mode/offline location/Room 2
add n/Dan Tan p/61234567 e/dan@example.com a/Bedok sub/English cost/20 c/Saturday1400-1530
add n/Eve Tan p/51111111 e/eve@example.com a/Woodlands sub/Art cost/10 c/Saturday1200-1330 mode/online
```

Expected: Bob has only a guardian phone. Physics is Offline with Room 2, English defaults to Offline,
and Art is Online with no link (a meeting link is optional). These classes have IDs C002, C003, and C004.

## 3. Existing class enrolment and shared details

```text
class payment 1 id/C001 status/paid
class enrol 2 id/C001
add n/Fay Tan p/41111111 e/fay@example.com a/Pasir Ris sub/Math cost/30 c/Saturday1800-1930
```

Expected: Bob and Fay inherit Math's Online mode, URL, and note. Alice's guardian details and Paid status
stay unchanged; Bob and Fay are Unpaid. The catalogue still contains one Math class, C001.
Enter `timetable`: Saturday has one Math entry with Alice, Bob, and Fay and the shared details.
Enter `list` before continuing.

## 4. Edit the canonical class without losing attributes

```text
class edit id/C001 sub/Advanced Math cost/35 c/Saturday1900-2030
edit 1 p/91111111
tag 1 exam-prep
```

Expected: all Math enrolments keep C001 and show Advanced Math at the new time and cost. Online mode,
URL, and note survive both the class edit and contact edit. Alice's guardian survives. Alice remains Paid
with balance 0; Bob and Fay remain Unpaid with balance 35. `class list` shows the updated class and details.
Return using `list`.

## 5. Fill an empty catalogue class's details through its first contact

```text
class add sub/History cost/25 c/Saturday1000-1100
add n/Grace Tan p/21111111 e/grace@example.com a/Punggol gn/Gina gp/22222222 sub/History cost/25 c/Saturday1000-1100 mode/online link/https://meet.example.com/history location/Online room
```

Expected: History remains C005, without a duplicate class being created. The first contact fills its
Online mode, URL, and location. The catalogue, Grace's card, and timetable all show the same details.

## 6. Reject invalid data without partial additions

Run each separately. Each should show an error and leave the seven contacts and five classes unchanged:

```text
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere gp/abc
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere ge/not-an-email
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere gn/@Mary
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere gp/123 gp/456
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere mode/online
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere sub/Music cost/10 c/Saturday0800-0900 mode/hybrid
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere sub/Music cost/10 c/Saturday0800-0900 mode/offline link/https://example.com
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere sub/Music cost/10 c/Saturday0800-0900 mode/online link/not-a-url
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere sub/Advanced Math cost/35 c/Saturday1900-2030 mode/offline
```

Also try a duplicate `link/`, an empty `mode/`, or a 501-character note. No class ID should be consumed
by a failed addition. New guardian/mode/link input is not added to `edit`, `class enrol`, or `class edit`.

## 7. Restart and retain empty classes

Enter `exit`, then relaunch from the same isolated folder:

```sh
java -jar /Users/Laxshan/Documents/tp/build/libs/tuteasy.jar
```

Expected: IDs, guardians, modes, links, locations, notes, class edits, payments, and balances survive.
Resize and scroll: attribute text should wrap on student cards, class cards, and timetable entries.

Then enter:

```text
class unenrol 1 id/C001
class unenrol 2 id/C001
class unenrol 6 id/C001
class list
```

Expected: C001 remains in the catalogue with Online mode, URL, and note, even with no students.
Return using `list`, then `class enrol 2 id/C001`: Bob inherits the retained details and is Unpaid.

## 8. Delete a class

Run `class delete id/C001`. The class and its enrolments disappear; guardian contacts remain.
Adding a new class must receive a new ID rather than reuse C001.

## 9. Privacy and pinned contacts

With a guardian contact visible, enter `privacy on`. Student phone/email/address and guardian details
should be masked. Enter `privacy off` and check that the original values return.
Run `pin 1`, edit that student's phone, tag the student, and enrol/unenrol them by class ID.
The pin and guardian details must survive every operation and a restart. `unpin 1` removes only the pin.

## 10. One-off online classes

Add a fresh contact with a complete class using `c/2026-10-24T0800-0900 mode/online link/https://example.com/one-off`.
Check its class ID, online details, and exact date in the class catalogue and student card.
Restart and confirm all values remain. The timetable should show it only if that exact date is in the displayed range.
Enrol other contacts using `class enrol INDEX INDEX sub/SUBJECT c/2026-10-24T0800-0900` and verify shared details.

## Automated verification

From the repository run `./gradlew check coverage`.
The tests cover Aaron's class commands plus canonical attributes, empty classes, ID/payment preservation,
first-contact enrichment, guardian preservation, pre-catalogue v1.4 migration, old catalogue files, and UI display.
