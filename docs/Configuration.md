---
  layout: default.md
  title: "Configuration guide"
---

# Configuration guide

TutEasy does not use an external configuration file. In particular, it does not read a `config.json` file.

Student data and preferences are stored beside the running JAR (or in the working directory during development):

* `preferences.json` stores the GUI window settings and privacy-mode preference.
* `data/addressbook.json` stores student and class data.

Application logs (`tuteasy.log`) remain relative to the working directory.

On the first launch after upgrading, missing data and preference files are copied from the launch directory
if present. Existing files beside the JAR are never overwritten, and the original files are retained.
If your old data is elsewhere, copy it beside the JAR before launching. The status bar shows the absolute data path.

The logging level is fixed at `INFO`. Changing these paths or the logging level requires updating the source code and
rebuilding the application.
