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
import seedu.address.model.lesson.Lesson;
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

        return new Person[] {
            createPerson("Alex Yeoh", "87438807", "alexyeoh@example.com",
                    "Blk 30 Geylang Street 29, #06-40",
                    new GuardianContact("Michelle Yeoh", "81234567", "michelle@example.com"), true,
                    getLessonSet(new Enrollment(mathematics, PaymentStatus.PAID),
                            new Enrollment(physics, PaymentStatus.UNPAID)),
                    "priority", "secondary-4"),
            createPerson("Bernice Yu", "99272758", "berniceyu@example.com",
                    "Blk 30 Lorong 3 Serangoon Gardens, #07-18",
                    new GuardianContact("Daniel Yu", "82345678", ""), false,
                    getLessonSet(new Enrollment(mathematics, PaymentStatus.UNPAID),
                            new Enrollment(english, PaymentStatus.PAID)),
                    "secondary-3", "needs-practice"),
            createPerson("Charlotte Oliveiro", "93210283", "charlotte@example.com",
                    "Blk 11 Ang Mo Kio Street 74, #11-04",
                    new GuardianContact("Rachel Oliveiro", "83456789", "rachel@example.com"), false,
                    getLessonSet(new Enrollment(english, PaymentStatus.UNPAID),
                            new Enrollment(chemistryWorkshop, PaymentStatus.UNPAID)),
                    "secondary-4", "exam-prep"),
            createPerson("David Li", "91031282", "lidavid@example.com",
                    "Blk 436 Serangoon Gardens Street 26, #16-43",
                    GuardianContact.EMPTY, false,
                    getLessonSet(new Enrollment(physics, PaymentStatus.PAID),
                            new Enrollment(chemistryWorkshop, PaymentStatus.PAID)),
                    "junior-college", "science"),
            createPerson("Irfan Ibrahim", "92492021", "irfan@example.com",
                    "Blk 47 Tampines Street 20, #17-35",
                    new GuardianContact("Nur Ibrahim", "84567890", "nur@example.com"), false,
                    getLessonSet(new Enrollment(mathematics, PaymentStatus.UNPAID)),
                    "secondary-2", "new-student"),
            createPerson("Roy Balakrishnan", "92624417", "royb@example.com",
                    "Blk 45 Aljunied Street 85, #11-31",
                    new GuardianContact("Anita Balakrishnan", "85678901", "anita@example.com"), true,
                    getLessonSet(), "trial", "not-enrolled")
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

    private static Person createPerson(String name, String phone, String email, String address,
            GuardianContact guardianContact, boolean pinned, Set<Lesson> lessons, String... tags) {
        return new Person(new Name(name), new Phone(phone), new Email(email), new Address(address),
                getTagSet(tags), lessons, Set.of(), Optional.empty(), Optional.empty(), guardianContact, pinned);
    }

    /** Returns a lesson set that retains the display order of the supplied lessons. */
    private static Set<Lesson> getLessonSet(Lesson... lessons) {
        return new LinkedHashSet<>(Arrays.asList(lessons));
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
