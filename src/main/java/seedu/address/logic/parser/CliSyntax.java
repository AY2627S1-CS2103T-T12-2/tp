package seedu.address.logic.parser;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_TAG = new Prefix("t/");
    public static final Prefix PREFIX_SUBJECT = new Prefix("sub/");
    public static final Prefix PREFIX_COST = new Prefix("cost/");
    public static final Prefix PREFIX_CLASS = new Prefix("c/");
    public static final Prefix PREFIX_STATUS = new Prefix("status/");
    public static final Prefix PREFIX_LESSON = PREFIX_CLASS;
    public static final Prefix PREFIX_GUARDIAN_NAME = new Prefix("gn/");
    public static final Prefix PREFIX_GUARDIAN_PHONE = new Prefix("gp/");
    public static final Prefix PREFIX_GUARDIAN_EMAIL = new Prefix("ge/");
    public static final Prefix PREFIX_LINK = new Prefix("link/");
    public static final Prefix PREFIX_LOCATION = new Prefix("location/");
    public static final Prefix PREFIX_NOTES = new Prefix("notes/");

}
