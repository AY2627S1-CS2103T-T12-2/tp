# Title

`add` silently creates a student when class fields are incomplete

> Already filed as [#148](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/148). This local copy is retained as the
> test record; do not file a duplicate.

## Assignee and labels

- Assignee: `Slytherax`
- Labels: `type.Bug`, `severity.Low`, `alpha-bug`
- Feature: Guardian and class attributes

## Summary

The `add` command accepts `sub/` and/or `cost/` without `c/`, reports that the student was added, and stores partial
legacy subject/cost data without creating the intended class. PR #124 groups `sub/`, `cost/`, and `c/` together and
states that all three must be supplied together when adding a class.

This is inconsistent with the same parser's handling of an incomplete combination that includes `c/`, which is
correctly rejected.

## Test environment

- TutEasy version: V1.4, release commit `88e422e4c835b0ddef4ff97754e27bdac1cba5b0`
- Documentation baseline: PR #124 head `f8ae2093b05461b03bb25e4d5652ef4282878d7c`
- JAR SHA-256: `ccb1b423fd179c4206e280ef047e4b086b4beb68562c9b81a1f124223063382f`
- OS: Windows 11 25H2, build `26200.9711`
- Java: Oracle Java `25.0.4+7-LTS-189`, 64-bit

## Steps to reproduce

1. Launch TutEasy V1.4 in a disposable test folder and remain in the student-list view.
2. Run:

   ```text
   add n/Incomplete Cost p/95550021 e/incomplete.cost@example.com a/Test cost/20
   ```

3. Run:

   ```text
   add n/Incomplete SubCost p/95550023 e/incomplete.subcost@example.com a/Test sub/English cost/20
   ```

4. Run `find Incomplete`.
5. Close and relaunch TutEasy, then run `find Incomplete` again.

The same problem also occurs with `sub/English` alone.

## Expected result

Each incomplete command is rejected before any data is changed. Feedback should explain that `sub/`, `cost/`, and a
complete `c/SCHEDULE` must be supplied together. No student or class should be added.

## Actual result

Both commands report `Student Added`.

After `find Incomplete`, TutEasy shows:

- `Incomplete Cost` with `Cost/hour: 20` but no class.
- `Incomplete SubCost` with `Subjects: English` and `Cost/hour: 20` but no class.

Both partial records remain after restart. By contrast, combinations containing `c/` but missing `sub/` or `cost/` are
correctly rejected as invalid command format.

## Impact

A tutor who forgets the schedule can receive a success message even though no class was created. The application saves
a partially configured student instead of rejecting the invalid request atomically, so the tutor must discover and
repair or delete the record manually.

Proposed severity: **Medium**. The issue silently changes persistent data during a common add workflow and contradicts
the documented all-or-nothing class-field group, although the incorrect record can be repaired manually.

## Evidence to upload

- `attributes-13-cost-only.png` — command reports success.
- `attributes-15-sub-cost-no-schedule.png` — command reports success.
- `attributes-bug-01-confirm-partial-add.png` — both partial records are visible.
- `persistence-08-incomplete-records-remain.png` — both remain after restart.

Local evidence folder:

`F:\StudySG\CS2103T\tp\manual-test-evidence-v1.4-yanzhu-corrected`

## Tester status

Guardian and class attributes — **FAIL** until fixed and retested.
