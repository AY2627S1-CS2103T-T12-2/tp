package seedu.address.model.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StudentId;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {

    /** Returns sample classes that exercise weekly, one-off, online and offline class behaviour. */
    public static TuitionClass[] getSampleTuitionClasses() {
        String oneOffSchedule = LocalDate.now() + "T1400-1530";
        return new TuitionClass[] {
            new TuitionClass(new ClassId("C001"), new Subject("Mathematics"), new Cost("45"),
                    new LessonTiming("Monday1800-1930"),
                    new ClassDetails(ClassMode.ONLINE, "https://zoom.us/j/123456789", "",
                            "Bring the algebra worksheet")),
            new TuitionClass(new ClassId("C002"), new Subject("English"), new Cost("40"),
                    new LessonTiming("Wednesday1600-1730"),
                    new ClassDetails(ClassMode.OFFLINE, "", "Room 3-12",
                            "Reading comprehension and composition")),
            new TuitionClass(new ClassId("C003"), new Subject("Physics"), new Cost("50"),
                    new LessonTiming("Saturday1000-1130"),
                    new ClassDetails(ClassMode.OFFLINE, "", "Science Lab 2",
                            "Safety goggles are provided")),
            new TuitionClass(new ClassId("C004"), new Subject("Chemistry Workshop"), new Cost("55"),
                    new OneOffLessonSchedule(oneOffSchedule),
                    new ClassDetails(ClassMode.ONLINE, "https://meet.google.com/abc-defg-hij", "",
                            "One-off exam revision workshop")),
            new TuitionClass(new ClassId("C005"), new Subject("Art"), new Cost("35"),
                    new LessonTiming("Sunday0900-1030"),
                    new ClassDetails(ClassMode.OFFLINE, "", "Studio 2",
                            "Open class with no students enrolled yet"))
        };
    }

    /** Returns sample students enrolled in the supplied centrally stored classes. */
    private static Person[] getSamplePersons(TuitionClass[] classes) {
        TuitionClass mathematics = classes[0];
        TuitionClass english = classes[1];
        TuitionClass physics = classes[2];
        TuitionClass chemistryWorkshop = classes[3];
        StudentId alexId = sampleStudentId(1);
        StudentId berniceId = sampleStudentId(2);
        StudentId charlotteId = sampleStudentId(3);
        StudentId davidId = sampleStudentId(4);
        StudentId irfanId = sampleStudentId(5);
        StudentId royId = sampleStudentId(6);

        return new Person[] {
            createPerson(alexId, "Alex Yeoh", "87438807", "alexyeoh@example.com",
                    "Blk 30 Geylang Street 29, #06-40",
                    new GuardianContact("Michelle Yeoh", "81234567", "michelle@example.com"), true,
                    getEnrollmentSet(
                            new Enrollment(alexId, mathematics, mathematics.getDefaultRate(), PaymentStatus.PAID),
                            new Enrollment(alexId, physics, physics.getDefaultRate(), PaymentStatus.UNPAID)),
                    "priority", "secondary-4"),
            createPerson(berniceId, "Bernice Yu", "99272758", "berniceyu@example.com",
                    "Blk 30 Lorong 3 Serangoon Gardens, #07-18",
                    new GuardianContact("Daniel Yu", "82345678", ""), false,
                    getEnrollmentSet(new Enrollment(berniceId, mathematics, new Cost("42"), PaymentStatus.UNPAID),
                            new Enrollment(berniceId, english, english.getDefaultRate(), PaymentStatus.PAID)),
                    "secondary-3", "needs-practice"),
            createPerson(charlotteId, "Charlotte Oliveiro", "93210283", "charlotte@example.com",
                    "Blk 11 Ang Mo Kio Street 74, #11-04",
                    new GuardianContact("Rachel Oliveiro", "83456789", "rachel@example.com"), false,
                    getEnrollmentSet(
                            new Enrollment(charlotteId, english, english.getDefaultRate(), PaymentStatus.UNPAID),
                            new Enrollment(charlotteId, chemistryWorkshop,
                                    chemistryWorkshop.getDefaultRate(), PaymentStatus.UNPAID)),
                    "secondary-4", "exam-prep"),
            createPerson(davidId, "David Li", "91031282", "lidavid@example.com",
                    "Blk 436 Serangoon Gardens Street 26, #16-43",
                    GuardianContact.EMPTY, false,
                    getEnrollmentSet(new Enrollment(davidId, physics, physics.getDefaultRate(), PaymentStatus.PAID),
                            new Enrollment(davidId, chemistryWorkshop,
                                    chemistryWorkshop.getDefaultRate(), PaymentStatus.PAID)),
                    "junior-college", "science"),
            createPerson(irfanId, "Irfan Ibrahim", "92492021", "irfan@example.com",
                    "Blk 47 Tampines Street 20, #17-35",
                    new GuardianContact("Nur Ibrahim", "84567890", "nur@example.com"), false,
                    getEnrollmentSet(
                            new Enrollment(irfanId, mathematics, mathematics.getDefaultRate(), PaymentStatus.UNPAID)),
                    "secondary-2", "new-student"),
            createPerson(royId, "Roy Balakrishnan", "92624417", "royb@example.com",
                    "Blk 45 Aljunied Street 85, #11-31",
                    new GuardianContact("Anita Balakrishnan", "85678901", "anita@example.com"), true,
                    getEnrollmentSet(), "trial", "not-enrolled")
        };
    }

    /** Returns sample students together with enrolments in representative sample classes. */
    public static Person[] getSamplePersons() {
        return getSamplePersons(getSampleTuitionClasses());
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        TuitionClass[] classes = getSampleTuitionClasses();
        for (TuitionClass tuitionClass : classes) {
            sampleAb.addTuitionClass(tuitionClass);
        }
        for (Person samplePerson : getSamplePersons(classes)) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    private static Person createPerson(StudentId studentId, String name, String phone, String email, String address,
            GuardianContact guardianContact, boolean pinned, Set<Enrollment> enrollments, String... tags) {
        return new Person(studentId, new Name(name), new Phone(phone), new Email(email), new Address(address),
                getTagSet(tags), Set.of(), enrollments, Set.of(), Optional.empty(), Optional.empty(),
                guardianContact, pinned);
    }

    /** Returns an enrollment set that retains the display order of the supplied enrollments. */
    private static Set<Enrollment> getEnrollmentSet(Enrollment... enrollments) {
        return new LinkedHashSet<>(Arrays.asList(enrollments));
    }

    /** Returns a deterministic ID so regenerated sample data remains stable. */
    private static StudentId sampleStudentId(int sequence) {
        return new StudentId(String.format("S-00000000-0000-0000-0000-%012d", sequence));
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
