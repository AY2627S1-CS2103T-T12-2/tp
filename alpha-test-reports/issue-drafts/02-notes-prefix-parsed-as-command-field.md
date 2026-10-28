# Class notes containing `sub/` are parsed as another command field

**Recommended labels:** `type.Bug`, `severity.Low`, `alpha-bug`  
**Recommended assignee:** `Slytherax` (Laxshan, Guardian and class attributes primary author)

## Test environment

- Application: TutEasy V1.4, release commit `88e422e4c835b0ddef4ff97754e27bdac1cba5b0`
- JAR SHA-256: `ccb1b423fd179c4206e280ef047e4b086b4beb68562c9b81a1f124223063382f`
- Documentation baseline: PR #124, head `f8ae2093b05461b03bb25e4d5652ef4282878d7c`
- OS: Windows 11 25H2, build `26200.9711`
- Java: Oracle Java `25.0.4+7-LTS-189`, 64-bit

## Steps to reproduce

1. Launch TutEasy V1.4 in a disposable folder and use the student-list view.
2. Run:

   ```text
   add n/Prefix Notes p/95550107 e/prefix.notes@example.com a/Test sub/Reading cost/20 c/Sunday2100-2200 notes/Review sub/section 2 before class
   ```

3. Run `find Prefix Notes`.

## Expected result

The student and class are added. The class note is stored as `Review sub/section 2 before class`. The User Guide allows
single-line `notes/` text up to 500 characters and does not reserve command prefixes inside note values.

## Actual result

TutEasy rejects the command with:

```text
Multiple values specified for the following single-valued field(s): sub/
```

`find Prefix Notes` returns zero records. A control note containing ordinary slashes (`Pages 2/3 and 4/5`) is accepted,
so the failure is triggered by the recognized `sub/` token inside the free-text value.

## Impact

A valid class note cannot contain text such as `sub/section` because the parser treats it as a second subject field.
The command fails atomically, so the impact is limited to rejecting valid input.

## Evidence

- `manual-test-extra/evidence/extra-notes-prefix-like-sub.txt`
- `manual-test-extra/evidence/extra-notes-prefix-like-no-partial-add.txt`
- Control: `manual-test-extra/evidence/extra-notes-slash-control.txt`

## Duplicate check

Searches of open and closed repository issues for `notes sub/`, `prefix notes`, and location/notes parsing found no
matching issue as of 2026-10-28. This is separate from issue #148, which concerns incomplete class fields silently
creating a student.
