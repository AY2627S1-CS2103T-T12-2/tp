# Timetable truncates long class details instead of wrapping them

**Recommended labels:** `type.Bug`, `severity.Low`, `alpha-bug`  
**Recommended assignee:** Aarav (timetable primary author; repository login appears to be `purpleshark74`)

## Test environment

- Application: TutEasy V1.4, release commit `88e422e4c835b0ddef4ff97754e27bdac1cba5b0`
- JAR SHA-256: `ccb1b423fd179c4206e280ef047e4b086b4beb68562c9b81a1f124223063382f`
- Documentation baseline: PR #124, head `f8ae2093b05461b03bb25e4d5652ef4282878d7c`
- OS: Windows 11 25H2, build `26200.9711`
- Java: Oracle Java `25.0.4+7-LTS-189`, 64-bit
- Window: 1280x720 logical size at 200% Windows display scaling

## Steps to reproduce

1. Launch TutEasy V1.4 in a disposable folder.
2. Add a weekly class with a valid 500-character `notes/` value. For example, add a student and class scheduled on Sunday
   with `notes/` followed by 500 `x` characters.
3. Run `timetable` and navigate to the Sunday containing that class.

## Expected result

The class note is readable in the timetable entry, wrapping onto additional lines as needed. The User Guide allows class
notes up to 500 characters, and `docs/ManualTests-v1.4.md` says timetable-entry attribute text should wrap.

## Actual result

The timetable entry displays `Mode: Offline | Notes: ...` instead of the note. UI Automation exposes the full 523-character
text node on one line (2130x34 px) and a paired rendered label containing only `Mode: Offline | Notes: ...` (276x34 px).
The value is accepted and stored; only its timetable presentation is truncated.

## Impact

Tutors cannot read longer class notes in the timetable, despite the documented 500-character limit. The timetable is the
view intended to support lesson preparation, so the missing note can hide useful preparation details.

## Evidence

- `manual-test-extra/evidence/extra-long-notes-timetable-bounds.txt`
- `manual-test-extra/evidence/extra-long-notes-timetable-evidence.txt`

## Duplicate check

Searches of open and closed repository issues for `notes truncated`, `details clipped`, and timetable-specific terms
found no matching issue as of 2026-10-28. The class list has the same visible symptom in its separate view component; see
the companion draft `03-class-list-clips-long-class-details.md`.
