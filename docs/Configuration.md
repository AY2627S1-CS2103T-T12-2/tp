---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

TutEasy does not use an external configuration file. In particular, it does not read a `config.json` file.

The application uses the following fixed paths, relative to the directory from which it is run:

* `preferences.json` stores the GUI window settings and privacy-mode preference.
* `data/addressbook.json` stores student and class data.
* `tuteasy.log` stores application logs.

The logging level is fixed at `INFO`. Changing these paths or the logging level requires updating the source code and
rebuilding the application.
