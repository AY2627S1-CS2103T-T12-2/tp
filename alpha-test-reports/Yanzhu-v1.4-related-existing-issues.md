# Yanzhu v1.4 test: issue inventory

## New findings from Yanzhu's assigned test areas

There are four distinct findings suitable for Yanzhu's issue count: the incomplete-class-fields issue already filed as
[#148](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/148), plus three additional reproducible findings from the
follow-up run. Drafts for the latter are ready under `issue-drafts/`.

| Finding | Assignment area | Tracking | Recommended assignee |
| --- | --- | --- | --- |
| `add` accepts incomplete class fields and persists a partial student | Guardian and class attributes | [#148](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/148), open | `Slytherax` (already assigned) |
| A valid `notes/` value containing `sub/` is parsed as a duplicate subject field | Guardian and class attributes | [Draft 02](issue-drafts/02-notes-prefix-parsed-as-command-field.md) | `Slytherax` |
| Long class details are truncated in `class list` | Guardian and class attributes / class-list integration | [Draft 03](issue-drafts/03-class-list-clips-long-class-details.md) | `AaronL248` (Aaron, class-list component) |
| Long class details are truncated in `timetable` | Timetable | [Draft 04](issue-drafts/04-timetable-clips-long-class-details.md) | Aarav (timetable primary author; likely `purpleshark74`) |

The class-list and timetable truncation reports are reproducible in separate view components and have different primary
authors. If the team treats them as one shared UI issue, combine drafts 03 and 04 rather than filing duplicates; in that
case the set becomes three findings including #148.

## Existing issues to link, not file again

These repository issues overlap Yanzhu's assigned areas, but were already open before the follow-up findings. They should
be linked in the test report rather than filed again.

| Finding | Existing issue | Current assignee | Local test status |
| --- | --- | --- | --- |
| UG `class add sub/Physics cost/35 c/Wednesday1700-1830` example overlaps the default English class | [#127](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/127) | `purpleshark74` | Reproduced |
| Privacy-mode tag-edit feedback exposes contact data | [#125](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/125) | `wenyiliangg` | Existing issue; not retested in this follow-up |
| Guardian names reject common hyphens and apostrophes | [#142](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/142) | `Slytherax` | Hyphen variant reproduced |
| Impossible dates and clock values receive generic schedule errors | [#144](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/144) | `AaronL248` | Existing issue; local equal-time case had generic feedback |

Issue [#143](https://github.com/AY2627S1-CS2103T-T12-2/tp/issues/143) on common punctuation in class subjects is adjacent
to class attributes but outside the photo's specific test areas. Do not count it toward Yanzhu's assigned-feature total.
