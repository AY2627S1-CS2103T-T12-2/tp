# Yanzhu alpha-test report — TutEasy v1.4

## Scope and method

This report follows the assignment table supplied by Yanzhu. It tests the features for which **Yanzhu is a tester**, not
the features authored by Yanzhu:

| Feature | Yanzhu's assigned role |
| --- | --- |
| Lesson scheduling model | Tester 1 — normal usage and documentation |
| Privacy mode | Tester 1 — normal usage and documentation |
| Timetable view | Tester 1 — normal usage and documentation |
| Guardian and class attributes | Tester 2 — edge cases and integration |

Testing was black-box only: commands were entered into the released GUI and results were checked against the User Guide
and Developer Guide. Product source code was not used to decide expected behavior.

The documentation baseline combines the v1.4 release with the open documentation update in PR #124. Findings already
corrected by PR #124 are excluded. The released JAR was not rebuilt or modified.

## Environment

| Item | Value |
| --- | --- |
| Application | TutEasy V1.4 |
| Release commit | `88e422e4c835b0ddef4ff97754e27bdac1cba5b0` |
| JAR | `F:\StudySG\CS2103T\v1.4_Test\tuteasy.jar` |
| JAR SHA-256 | `ccb1b423fd179c4206e280ef047e4b086b4beb68562c9b81a1f124223063382f` |
| Documentation overlay | PR #124, head `f8ae2093b05461b03bb25e4d5652ef4282878d7c` |
| OS | Windows 11 25H2, build `26200.9711` (registry product string: Windows 10 Home China) |
| Java | Oracle Java `25.0.4+7-LTS-189`, 64-bit |
| Main test folder | `F:\StudySG\CS2103T\v1.4_Test` |
| Clean documentation-example folder | `F:\StudySG\CS2103T\tp\manual-test-runs\ug-default-example` |

All screenshots and command/click logs are in
[`manual-test-evidence-v1.4-yanzhu-corrected`](../manual-test-evidence-v1.4-yanzhu-corrected/).

## Executive result

| Feature | Result | Reason |
| --- | --- | --- |
| Lesson scheduling model | **PASS WITH MINOR ISSUES** | Runtime scheduling behavior passed, but the exact UG `class add` example fails against fresh sample data; this is already tracked by issue #127. |
| Privacy mode | **PASS** | Masking, unmasking, guardian masking, use from another view, and restart persistence passed. |
| Timetable view | **PASS WITH MINOR ISSUES** | Navigation, recurrence, read-only behavior, and persistence passed. Long class notes are truncated in timetable entries; see draft 04. |
| Guardian and class attributes | **FAIL** | Incomplete class fields silently add partial student records (#148). Follow-up tests also found a valid notes value rejected when it contains `sub/`, and long details truncated in class cards; see drafts 02 and 03. |

**Overall status: FAIL.** No critical or high-severity defect was found, but the Tester 2 feature cannot be signed off
until the new incomplete-class-fields defect is fixed and retested. The other assigned tester must also pass the feature.

---

## 1. Lesson scheduling model — Tester 1

Documentation checked:

- Weekly schedule: `DAYHHmm-HHmm`.
- One-off schedule: `yyyy-MM-ddTHHmm-HHmm`.
- Times are within `0000`–`2359` and end is later than start.
- Same-day/date overlaps are rejected; endpoint-adjacent ranges are allowed.
- Weekly/one-off schedules persist without being expanded into stored occurrences.

| ID | Test attempted | Expected | Actual | Result / evidence |
| --- | --- | --- | --- | --- |
| LS-01 | Add Alice with `c/Wednesday1800-1930` | Student and weekly Mathematics class are added. | Added; card shows `Wed 18:00–19:30`. | PASS — `schedule-01-add-weekly.png` |
| LS-02 | Add Bob with `c/2026-10-30T1600-1730` | Student and one-off Physics class are added. | Added; card shows `30 Oct 2026 16:00–17:30`. | PASS — `schedule-02-add-oneoff.png` |
| LS-03 | Add `Wednesday1930-2000` beside the class ending at 19:30 | Endpoint adjacency is allowed. | Computing was added successfully. | PASS — `schedule-03-adjacent-valid.png` |
| LS-04 | Add `Wednesday1900-2000` | Reject overlap with both adjacent Wednesday classes; no student added. | Rejected and both conflicts were named. | PASS — `schedule-04-weekly-overlap-rejected.png` |
| LS-05 | Add one-off `2026-10-28T1830-1900` | Reject overlap with weekly Mathematics on that Wednesday. | Rejected with the correct weekly conflict. | PASS — `schedule-05-oneoff-weekly-overlap-rejected.png` |
| LS-06 | Add weekly `Friday1600-1700` | Reject overlap with one-off Physics on Friday 30 Oct. | Rejected with the correct one-off conflict. | PASS — `schedule-06-weekly-oneoff-overlap-rejected.png` |
| LS-07 | Add empty classes at `Sunday0000-0001` and `Sunday2358-2359` | Both documented clock boundaries are accepted. | Both classes were added. | PASS — `schedule-07-midnight-boundary-valid.png`, `schedule-08-endday-boundary-valid.png` |
| LS-08 | Use one-off input without `T`; use equal start/end time | Both are rejected without changing data. | Both were rejected with format guidance. | PASS — `schedule-09-invalid-oneoff-format.png`, `schedule-10-equal-times-rejected.png` |
| LS-09 | Restart TutEasy | Weekly and one-off schedules remain and the timetable can rebuild them. | Both remained visible after restart. | PASS — `persistence-06-timetable-after-restart.png` |
| LS-DOC-01 | In a clean v1.4 folder, run the exact UG example `class add sub/Physics cost/35 c/Wednesday1700-1830` | A worked example should succeed with the default sample data or state its prerequisite. | It fails because it overlaps default English, Wednesday 16:00–17:30. | MINOR DOC ISSUE — `schedule-doc-01c-exact-class-add-example-default-data.png` |

### Lesson-scheduling issue handling

LS-DOC-01 is already reported as
[#127 — UG class-add example conflicts with the default sample timetable](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/127).
It is open and already assigned to `purpleshark74`, which is appropriate because this is a UG example in Aarav's PR
#124. Do **not** file a duplicate issue.

Final status: **PASS WITH MINOR ISSUES**.

---

## 2. Privacy mode — Tester 1

| ID | Test attempted | Expected | Actual | Result / evidence |
| --- | --- | --- | --- | --- |
| PR-01 | `find Cara`, then `privacy on` | Student phone, address, email, and all guardian fields become seven asterisks; class details stay visible. | All six private values were masked; class mode/link/location/notes remained visible. | PASS — `privacy-02-on-masks.png` |
| PR-02 | Check status bar while privacy is on | Status bar says `Privacy mode enabled`. | Correct status shown. | PASS — `privacy-02-on-masks.png` |
| PR-03 | Open `class list` while privacy is enabled | Command is allowed and privacy status remains enabled. | Class list opened and status remained. | PASS — `privacy-03-class-list-allowed.png` |
| PR-04 | Run `privacy off` from class-list view | Command is allowed and original details are restored when returning to student view. | Command succeeded; Cara's original details returned. | PASS — `privacy-04-off-from-class-list.png`, `privacy-06-find-cara-restored.png` |
| PR-05 | Enable privacy, close, and relaunch | Privacy setting and masks persist across restart. | App reopened with masked fields and `Privacy mode enabled`. | PASS — `persistence-01-privacy-on-before-restart.png`, `persistence-02-after-restart-initial.png` |

No feature-specific screenshot in the PR #124 UG contradicted this behavior.

Final status: **PASS**.

---

## 3. Timetable view — Tester 1

| ID | Test attempted | Expected | Actual | Result / evidence |
| --- | --- | --- | --- | --- |
| TT-01 | Run `timetable` on 28 Oct 2026 | Range is Sun–Sat `25–31 Oct 2026`; today is highlighted. | Correct range and Wednesday highlight shown. | PASS — `timetable-01-current-week.png` |
| TT-02 | Inspect entries | Entries show time, subject, mode/details, and students; same-day classes are time ordered. | Mathematics 18:00 then Computing 19:30; attending students shown. | PASS — `timetable-01-current-week.png` |
| TT-03 | Click Previous week, Next week, Next week, Today | Each click moves exactly seven days; Today returns to current week. | Ranges changed to 18–24 Oct, 25–31 Oct, 1–7 Nov, then 25–31 Oct. | PASS — `timetable-02-previous-week.png`, `timetable-04-next-week.png`, `timetable-05-today.png` |
| TT-04 | Inspect empty days and recurring boundary classes | Every day has a heading; empty days say `No classes`; weekly classes repeat. | Empty Monday shown; Sunday boundary classes recur in adjacent weeks. | PASS — `timetable-02-previous-week.png`, `timetable-04-next-week.png` |
| TT-05 | Check one-off Physics in adjacent weeks | One-off class appears only in the week containing 30 Oct 2026. | Present in current week and absent from adjacent weeks. | PASS |
| TT-06 | Attempt `add ...` while timetable is open | Reject before modification and explain how to return. | Rejected with timetable read-only guidance; no student added. | PASS — `timetable-06-readonly-reject.png` |
| TT-07 | Run `class list`, `timetable`, then `list` | All three documented view changes work. | All transitions succeeded; `list` restored modifying commands. | PASS — `timetable-07-class-list-allowed.png`, `timetable-08-return-timetable.png`, `timetable-09-list-return.png` |
| TT-08 | Restart and open timetable again | Saved schedules rebuild; timetable starts at current week. | Correct current-week timetable rebuilt after restart. | PASS — `persistence-06-timetable-after-restart.png` |
| TT-09 | Display a class with a valid 500-character note in its week | The timetable entry wraps the permitted note so it remains readable. | The rendered details label shows `Mode: Offline | Notes: ...`; UI Automation reports a 276x34 px rendered label beside the full 523-character text node. | MINOR — `manual-test-extra/evidence/extra-long-notes-timetable-bounds.txt`; issue draft 04 |

Final status: **PASS WITH MINOR ISSUES**.

---

## 4. Guardian and class attributes — Tester 2

### Successful, boundary, and integration cases

| ID | Test attempted | Expected | Actual | Result / evidence |
| --- | --- | --- | --- | --- |
| GA-01 | Add Cara with guardian name/phone/email and online Chemistry with link/location/notes | All fields display on the student card and shared class. | All fields displayed correctly. | PASS — `attributes-01-add-full.png`, `privacy-01-find-cara.png` |
| GA-02 | Add only `gp/81234567`; add empty `gn/ gp/ ge/` | Independent guardian field works; empty values mean missing data. | Both students added successfully. | PASS — `attributes-23-guardian-phone-only.png`, `attributes-24-empty-guardian-fields.png` |
| GA-03 | Repeat `gn/`, `gp/`, or `mode/` | Reject duplicate single-valued prefix; no student added. | Rejected with the exact repeated prefix. | PASS — `attributes-04-duplicate-guardian-prefix.png`, `attributes-25-repeated-guardian-phone.png`, `attributes-09-repeated-mode-prefix.png` |
| GA-04 | Use invalid guardian phone or `mode/hybrid` | Reject with field-specific feedback; no student added. | Both rejected with useful feedback. | PASS — `attributes-26-invalid-guardian-phone.png`, `attributes-05-invalid-mode.png` |
| GA-05 | Use offline/default-offline class with a link | Reject because only online classes can have links. | Both variants rejected. | PASS — `attributes-06-offline-link-conflict.png`, `attributes-07-link-default-offline.png` |
| GA-06 | Use URL containing credentials | Reject because links cannot embed credentials. | Rejected with link validation feedback. | PASS — `attributes-08-credential-link.png` |
| GA-07 | Supply mode/location without a complete class | Reject and explain the required class fields. | Rejected; no student added. | PASS — `attributes-10-details-without-class.png` |
| GA-08 | Use 500-character location; use 501-character notes | 500 is accepted; 501 is rejected; failed input does not add a student/class. | Boundary behaved correctly. | PASS — `attributes-11-location-500-valid.png`, `attributes-12-notes-501-rejected.png` |
| GA-09 | Add Dana to existing Chemistry while omitting attributes | Inherit shared online/link/location/notes values. | Dana inherited all values. | PASS — `attributes-19-find-inherited-details.png` |
| GA-10 | Add another Chemistry student with conflicting `mode/offline` | Reject entire add; existing students/class unchanged. | Rejected; `find Conflict` returned zero. | PASS — `attributes-20-conflicting-mode-atomic.png`, `attributes-21-confirm-conflict-not-added.png` |
| GA-11 | Edit Dana's name/phone; edit class C003 cost | Guardian/class attributes are preserved; existing agreed rate remains 35. | Dana still showed online/link/location/notes and balance 35 after both edits. | PASS — `attributes-29-edit-student-preserves-class.png`, `attributes-32-find-dana-after-class-edit.png` |
| GA-12 | Restart | Guardian and shared class fields persist; privacy integration still works. | Cara and Dana retained all attributes; privacy setting also persisted. | PASS — `persistence-03-find-dana-class-details.png`, `persistence-05-find-cara-guardian.png` |

### Reproduced bug: incomplete class fields are silently accepted

PR #124 documents `sub/`, `cost/`, and `c/` as one optional group and says they must be supplied together. The following
invalid combinations were tested:

| Input combination | Expected | Actual |
| --- | --- | --- |
| `sub/English` only | Reject; do not add student | Student was added without a class. |
| `cost/20` only | Reject; do not add student | Student was added with legacy `Cost/hour: 20`. |
| `sub/English cost/20` without `c/` | Reject; do not add student | Student was added with `Subjects: English` and `Cost/hour: 20`, but no class. |
| `c/...` without one or both companion fields | Reject | Correctly rejected. |

The first three forms report `Student Added`, so a user can believe the requested class data was accepted. The partial
student records remain after restart. Evidence:

- `attributes-03-missing-class-fields.png`
- `attributes-13-cost-only.png`
- `attributes-15-sub-cost-no-schedule.png`
- `attributes-bug-01-confirm-partial-add.png`
- `persistence-08-incomplete-records-remain.png`

This was not found in existing repository issues using searches for incomplete class fields and cost without schedule.
This defect has been filed as [#148](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/148). The issue body is also
preserved in [`issue-drafts/01-incomplete-class-fields-silently-add-student.md`](issue-drafts/01-incomplete-class-fields-silently-add-student.md)
for the local test record; do not file it again.

Classification used: **type.Bug**, **severity.Low**, **alpha-bug**.

Recommended assignee: **`Slytherax` (Laxshan)**, the assignment table's primary author for Guardian and class
attributes. Yanzhu is the tester and should not be assigned this implementation defect.

### Existing guardian limitation

`gn/Mary-Anne OConnor` was rejected because guardian names accept only alphanumeric characters and spaces. This is
already tracked as [#142 — Guardian names reject common hyphens and apostrophes](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/142),
open and assigned to `Slytherax`. Do **not** file a duplicate.

### Additional guardian/class-attribute findings

The follow-up run also reproduced two separate issues in this area:

- A note containing `sub/` is rejected as a repeated subject prefix, despite `notes/` allowing ordinary single-line text.
  See [`issue-drafts/02-notes-prefix-parsed-as-command-field.md`](issue-drafts/02-notes-prefix-parsed-as-command-field.md).
- A valid 500-character class note is accepted and saved, but the class-list rendering shows an ellipsis instead of the
  note. See [`issue-drafts/03-class-list-clips-long-class-details.md`](issue-drafts/03-class-list-clips-long-class-details.md).

Final status: **FAIL** until the incomplete-class-fields defect is fixed and retested. The two low-severity findings are
also reproducible and should be tracked separately.

---

## Failure evidence and logs

- Main black-box run log: `manual-test-evidence-v1.4-yanzhu-corrected/logs/main-run-tuteasy.log.0`
- Clean default-example log: `manual-test-evidence-v1.4-yanzhu-corrected/logs/clean-default-example-tuteasy.log.0`
- Command record: `manual-test-evidence-v1.4-yanzhu-corrected/commands.tsv`
- Click record: `manual-test-evidence-v1.4-yanzhu-corrected/clicks.tsv`

The evidence folder includes successful cases too, but only the screenshots named in the issue draft need to be uploaded
to the new GitHub issue.

## Additional reproducible findings from follow-up testing

These checks used the same released JAR and documentation baseline above, on a disposable copy of the app data. They are
within Yanzhu's assigned Guardian/class-attributes or Timetable areas. Searches of open and closed repository issues
found no matching issues for these three findings.

| Finding | Expected | Actual | Draft |
| --- | --- | --- | --- |
| A valid `notes/` value containing `sub/` is parsed as another subject field. | Store the whole note as free text. | Command is rejected as a duplicate `sub/`; no student is added. | [`02-notes-prefix-parsed-as-command-field.md`](issue-drafts/02-notes-prefix-parsed-as-command-field.md) |
| Long class details in `class list`. | Wrap the permitted 500-character note. | The rendered label shows `Mode: Offline | Notes: ...`. | [`03-class-list-clips-long-class-details.md`](issue-drafts/03-class-list-clips-long-class-details.md) |
| Long class details in `timetable`. | Wrap the permitted 500-character note. | The rendered label shows `Mode: Offline | Notes: ...`. | [`04-timetable-clips-long-class-details.md`](issue-drafts/04-timetable-clips-long-class-details.md) |

The two clipping findings affect separate views and have separate primary authors. If the team prefers one issue per
shared symptom rather than one per affected component, combine drafts 03 and 04; do not file both in that case.

Additional checks passed: 501-character `location/` was rejected without adding a student; an invalid guardian email did
not consume a class ID; tagging preserved guardian details; deleting a class preserved its student's guardian record; and
deleting the highest class ID did not cause that ID to be reused.
