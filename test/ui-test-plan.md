# UI Test Plan

## TC-01: Mask add and edit feedback in privacy mode

Aim: Verify that privacy mode masks student and guardian contact details in successful add and edit feedback.

Command:

```text
zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && ./gradlew test --tests "seedu.address.logic.MessagesTest.format_privacyEnabled_masksStudentAndGuardianContactDetails" --tests "seedu.address.logic.commands.AddCommandTest.execute_privacyEnabled_masksContactDetailsInFeedback" --tests "seedu.address.logic.commands.EditCommandTest.execute_privacyEnabled_masksContactDetailsInFeedback" --console=plain -q'
```

Input:

```text
```

Expected output:

```text
```
