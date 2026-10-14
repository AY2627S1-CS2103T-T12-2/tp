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

For multi-word subjects, keywords are separate alternatives (an OR search). For example, `find sub/Computer Science`
also matches a student taking Environmental Science. Use a more distinctive keyword, such as `Computer`, to narrow
the results. Repeating a keyword does not create duplicate results.

Matching uses partial keywords and returns each student once, even if several classes match.
Subject results keep address-book order. Search covers all students, including those outside an existing search.
Use `list` to return to the full student list. A search with no matches displays an empty list and a zero-result message.

Provide one `sub/` prefix with at least one keyword. Keywords contain letters and numbers.
Do not combine subject search with name, tag (`t/`), or class (`c/`) search in the same command.
For example, `find sub/Math t/priority` and `find Alice sub/Math` are rejected.
In the read-only timetable view, first use `list` to return to the student view.

An invalid subject search leaves the previous results and student data unchanged. After a successful search,
commands such as `edit 1` and `delete 1` refer to the first student in the displayed results.

Searching preserves all students and their class enrolments in the saved data. Restarting the application restores
the full list; search again to apply a subject filter.

Search uses enrolled classes and also supports older subject-only student records. When older complete records have
been migrated to classes, their current class enrolments determine the subjects returned by a search.
