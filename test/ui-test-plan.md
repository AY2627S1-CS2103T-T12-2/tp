# UI Test Plan

## TC-01: Preserve pinned search tie order

Aim: Verify that a second search preserves the displayed order of pinned students when their relevance scores tie.

Command:

```text
zsh -lc 'source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk use java 25.0.3.fx-zulu >/dev/null && ./gradlew test --tests "seedu.address.logic.commands.FindCommandTest.execute_equalScoreAfterPinning_preservesCurrentOrder" --console=plain -q'
```

Input:

```text
```

Expected output:

```text
```
