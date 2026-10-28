# Class list truncates long class details instead of wrapping them

**Recommended labels:** `type.Bug`, `severity.Low`, `alpha-bug`  
**Recommended assignee:** `AaronL248` (Aaron, class-management author; the affected component is the class list)

## Test environment

- Application: TutEasy V1.4, release commit `88e422e4c835b0ddef4ff97754e27bdac1cba5b0`
- JAR SHA-256: `ccb1b423fd179c4206e280ef047e4b086b4beb68562c9b81a1f124223063382f`
- Documentation baseline: PR #124, head `f8ae2093b05461b03bb25e4d5652ef4282878d7c`
- OS: Windows 11 25H2, build `26200.9711`
- Java: Oracle Java `25.0.4+7-LTS-189`, 64-bit
- Window: 1280x720 logical size at 200% Windows display scaling

## Steps to reproduce

1. Launch TutEasy V1.4 in a disposable folder.
2. Add a class with a valid 500-character `notes/` value. For example, add a new student and class with `notes/` followed
   by 500 `x` characters.
3. Run `class list` and scroll to that class.

## Expected result

The stored note remains readable in the class card, wrapping onto additional lines as needed. The User Guide allows
class notes up to 500 characters, and `docs/ManualTests-v1.4.md` says class-card attribute text should wrap.

## Actual result

The class-list entry displays `Mode: Offline | Notes: ...` instead of the note. UI Automation exposes the full 523-character
text node on one line (2219x34 px) and a paired rendered label containing only `Mode: Offline | Notes: ...` (276x34 px).
The value is accepted and stored; only its class-list presentation is truncated.

## Impact

Tutors cannot read longer class notes from the class list, despite the documented 500-character limit. They must open a
student card or inspect another view to recover the information.

## Evidence

- `manual-test-extra/evidence/extra-long-notes-classlist-bounds.txt`
- `manual-test-extra/evidence/extra-long-notes-class-list-evidence.txt`

## Duplicate check

Searches of open and closed repository issues for `notes truncated`, `details clipped`, and related class-list terms found
no matching issue as of 2026-10-28. The timetable has a separately reproducible truncation in its own view; see the
companion draft `04-timetable-clips-long-class-details.md`.
