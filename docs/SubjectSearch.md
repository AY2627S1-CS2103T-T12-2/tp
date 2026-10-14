---
  layout: default.md
  title: "Subject search"
  pageNav: 3
---

# Searching students by subject

Use `find sub/KEYWORD [MORE_KEYWORDS]...` to find students by their subjects.

* `find sub/Math` matches Math, Mathematics, and Additional Mathematics.
* `find sub/physics chemistry` returns students taking either subject.
* `find sub/COMPUTER SCIENCE` matches either keyword within subject names, ignoring case.

Matching uses partial keywords and returns each student once, even if several classes match.
Subject results keep address-book order. Search covers all students, including those outside an existing search.
Use `list` to return to the full student list. A search with no matches displays an empty list and a zero-result message.

Provide one `sub/` prefix with at least one keyword. Keywords contain letters and numbers.
Do not combine subject search with name, tag (`t/`), or class (`c/`) search in the same command.
For example, `find sub/Math t/priority` and `find Alice sub/Math` are rejected.
In the read-only timetable view, first use `list` to return to the student view.

Search uses enrolled classes and also supports older subject-only student records. When older complete records have
been migrated to classes, their current class enrolments determine the subjects returned by a search.
