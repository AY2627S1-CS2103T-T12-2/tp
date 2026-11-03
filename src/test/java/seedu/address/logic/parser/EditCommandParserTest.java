package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GUARDIAN_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;
    private static final String MESSAGE_UNKNOWN_PREFIX = "Unrecognised parameter-like token: modee/";

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", "Unrecognised parameter-like token: i/");
    }

    @Test
    public void parse_individualGuardianFields_success() {
        assertParseSuccess(parser, "1 gn/Mary-Anne O'Connor", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianName("Mary-Anne O'Connor").build()));
        assertParseSuccess(parser, "1 gp/88888888", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianPhone("88888888").build()));
        assertParseSuccess(parser, "1 gp/+65 9123-4567", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianPhone("+65 9123-4567").build()));
        assertParseSuccess(parser, "1 ge/mary@example.com", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianEmail("mary@example.com").build()));
    }

    @Test
    public void parse_guardianFieldsWithStudentFields_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withAddress("Two")
                .withGuardianName("Mary").withGuardianPhone("88888888")
                .withGuardianEmail("mary@example.com").build();
        assertParseSuccess(parser, "1 a/Two gn/Mary gp/88888888 ge/mary@example.com",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
        assertParseSuccess(parser, "1 ge/mary@example.com gn/Mary a/Two\tgp/88888888",
                new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_clearGuardianFields_success() {
        assertParseSuccess(parser, "1 gn/", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianName("").build()));
        assertParseSuccess(parser, "1 gp/", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianPhone("").build()));
        assertParseSuccess(parser, "1 ge/", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianEmail("").build()));
        assertParseSuccess(parser, "1 gn/ gp/ ge/", new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withGuardianName("").withGuardianPhone("")
                        .withGuardianEmail("").build()));
    }

    @Test
    public void parse_invalidGuardianFields_failure() {
        for (String name : List.of("@Mary", "M".repeat(101))) {
            assertParseFailure(parser, "1 gn/" + name, GuardianContact.MESSAGE_COMMAND_NAME_CONSTRAINTS);
        }
        for (String phone : List.of("12", "65+91234567", "abcd", "1".repeat(16))) {
            assertParseFailure(parser, "1 gp/" + phone, "Guardian phone: " + Phone.MESSAGE_CONSTRAINTS);
        }
        for (String email : List.of("invalid", "mary@@example.com")) {
            assertParseFailure(parser, "1 ge/" + email, "Guardian email: " + Email.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "1 gn/Mary gp/invalid ge/mary@example.com",
                "Guardian phone: " + Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateGuardianPrefixes_failure() {
        for (Prefix prefix : List.of(PREFIX_GUARDIAN_NAME, PREFIX_GUARDIAN_PHONE, PREFIX_GUARDIAN_EMAIL)) {
            for (String values : List.of(" bad %svalid", " valid %sbad", " %s")) {
                String input = "1 " + prefix + String.format(values, prefix);
                assertParseFailure(parser, input, Messages.getErrorMessageForDuplicatePrefixes(prefix));
            }
        }
        assertParseFailure(parser, "1 gn/Mary gp/88888888 ge/mary@example.com gn/ gp/ ge/",
                Messages.getErrorMessageForDuplicatePrefixes(
                        PREFIX_GUARDIAN_NAME, PREFIX_GUARDIAN_PHONE, PREFIX_GUARDIAN_EMAIL));
    }

    @Test
    public void parse_unsupportedClassFields_failure() {
        String expected = "Class fields cannot be changed with edit. "
                + "Remove them and use a supported edit field.";
        for (String field : List.of("sub/Math", "cost/30", "c/Monday1800-1900", "mode/online",
                "link/https://example.com", "location/Room 2", "notes/Review", "id/C001", "status/paid")) {
            assertParseFailure(parser, "1 a/Two " + field, expected);
            assertParseFailure(parser, "1 " + field + " a/Two", expected);
            assertParseFailure(parser, "1 " + field, expected);
        }
        assertParseFailure(parser, "1 a/Two sub/Math cost/30 c/Monday1800-1900 mode/online", expected);
        assertParseFailure(parser, "1 a/Two notes/", expected);
    }

    @Test
    public void parse_tabSeparatedUnsupportedFields_failure() {
        assertParseFailure(parser, "1 a/Two\tsub/Math", EditCommandParser.MESSAGE_UNSUPPORTED_CLASS_EDIT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1" + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS); // invalid address
        assertParseFailure(parser, "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS); // invalid tag

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_EMPTY, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_EMPTY + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_EMPTY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_ADDRESS_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + TAG_DESC_HUSBAND
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + NAME_DESC_AMY + TAG_DESC_FRIEND;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // address
        userInput = targetIndex.getOneBased() + ADDRESS_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // tags
        userInput = targetIndex.getOneBased() + TAG_DESC_FRIEND;
        descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_unknownPrefixLikeTokenInAddress_failure() {
        assertParseFailure(parser, "1 a/Home modee/online", MESSAGE_UNKNOWN_PREFIX);
    }

    @Test
    public void parse_quotedAddressWithRecognisedPrefix_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withAddress("Unit p/333").build();

        assertParseSuccess(parser, "1 a/\"Unit p/333\"", new EditCommand(INDEX_FIRST_PERSON, descriptor));
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonTagValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS));
    }

    @Test
    public void parse_resetTags_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + TAG_EMPTY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }
}
