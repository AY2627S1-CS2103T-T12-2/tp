# v1.4 manual tests: add contact details

This change adds guardian and class attributes through `add` only. Use a separate test-data folder:

1. From the repository, build with `./gradlew shadowJar` and close any existing TutEasy window.
2. Launch the built app in a fresh folder using the commands below.
3. Only in that isolated instance, enter `clear` to remove sample data.

```sh
TEST_DIR=$(mktemp -d /tmp/tuteasy-v14.XXXXXX)
cd "$TEST_DIR"
java -jar /Users/Laxshan/Documents/tp/build/libs/tuteasy.jar
```

## 1. Add a contact with all details

```text
add n/Alice Tan p/91234567 e/alice@example.com a/Clementi gn/Mary Tan gp/98765432 ge/mary@example.com sub/Math cost/30 c/Saturday1800-1930 mode/online link/https://meet.example.com/math?room=1&code=123 notes/Bring calculator
```

Expected: Alice has the guardian name, phone, and email. Math shows Online, the full meeting URL,
and the note. Her balance is 30 and the enrolment is Unpaid.

## 2. Add optional contacts and offline classes

```text
add n/Bob Tan p/81234567 e/bob@example.com a/Jurong gp/89999999
add n/Cara Tan p/71234567 e/cara@example.com a/Tampines sub/Physics cost/40 c/Saturday1600-1730 mode/offline location/Room 2
add n/Dan Tan p/61234567 e/dan@example.com a/Bedok sub/English cost/20 c/Saturday1400-1530
add n/Eve Tan p/51111111 e/eve@example.com a/Woodlands sub/Art cost/10 c/Saturday1200-1330 mode/online
```

Expected:

- Bob stores only a guardian phone, with no class.
- Cara has no guardian and an Offline Physics class with Room 2 and no link.
- Dan's class defaults to Offline without a link.
- Eve's Art class is Online with no link, because links are optional for online classes.

## 3. Add another contact to the existing class

```text
class payment 1 sub/Math c/Saturday1800-1930 status/paid
add n/Fay Tan p/41111111 e/fay@example.com a/Pasir Ris sub/Math cost/30 c/Saturday1800-1930
```

Expected: Fay inherits Math's Online mode, URL, and note. Alice's guardian stays unchanged.
Alice remains Paid with balance 0; Fay is Unpaid with balance 30.
Enter `timetable`: Saturday has one Math entry with both students and its online details.
Enter `list` before continuing.

## 4. Reject invalid attributes without adding a contact

Run each command separately. Each should show an error and leave the six contacts above unchanged:

```text
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere gp/abc
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere ge/not-an-email
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere gn/@Mary
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere gp/123 gp/456
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere mode/online
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere sub/Music cost/10 c/Saturday1000-1100 mode/hybrid
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere sub/Music cost/10 c/Saturday1000-1100 mode/offline link/https://example.com
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere sub/Music cost/10 c/Saturday1000-1100 mode/online link/not-a-url
add n/Invalid p/31111111 e/invalid@example.com a/Nowhere sub/Math cost/30 c/Saturday1800-1930 mode/offline
```

Also try a duplicate `link/`, an empty `mode/`, a 501-character note, and a link without `mode/online`
on a new offline class. All should be rejected. Exactly 500 note characters should be accepted on a new valid class.

## 5. Existing commands preserve data

Enter `edit 1 p/91111111` and `tag 1 exam-prep`.
Expected: Alice's student phone and tag change while her guardian, online details, and payment status remain.
The new guardian/class prefixes are not supported by `edit` or `class enrol` in this change.

## 6. Restart and inspect the UI

Enter `exit`, then relaunch from the same isolated terminal folder:

```sh
java -jar /Users/Laxshan/Documents/tp/build/libs/tuteasy.jar
```

Expected: all successfully added contacts, guardian values, modes, links, notes, location, payment statuses,
and balances survive.
Resize and scroll: the additional text should wrap and remain associated with the correct contact/class.

## Automated verification

From the repository, run `./gradlew check`.
Tests cover adding the new attributes, optional fields, invalid input, shared class inheritance/conflicts,
save/load compatibility, UI display, and preservation during existing commands.
