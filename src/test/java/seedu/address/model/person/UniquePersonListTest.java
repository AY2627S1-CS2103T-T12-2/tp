package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.EnrollmentId;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.testutil.PersonBuilder;

public class UniquePersonListTest {

    private final UniquePersonList uniquePersonList = new UniquePersonList();

    @Test
    public void contains_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.contains(null));
    }

    @Test
    public void contains_personNotInList_returnsFalse() {
        assertFalse(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        assertTrue(uniquePersonList.contains(ALICE));
    }

    @Test
    public void contains_personWithSameIdentityFieldsInList_returnsTrue() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(uniquePersonList.contains(editedAlice));
    }

    @Test
    public void add_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.add(null));
    }

    @Test
    public void add_duplicatePerson_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(ALICE));
    }

    @Test
    public void add_duplicateStudentId_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        Person differentPersonWithSameId = new PersonBuilder(BOB)
                .withStudentId(ALICE.getStudentId()).build();

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(differentPersonWithSameId));
    }

    @Test
    public void add_duplicateEnrollmentId_throwsDuplicatePersonException() {
        EnrollmentId duplicateId = new EnrollmentId("E-00000000-0000-0000-0000-000000000001");
        Person alice = withEnrollment(ALICE, duplicateId);
        Person bob = withEnrollment(BOB, duplicateId);
        uniquePersonList.add(alice);

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.add(bob));
    }

    @Test
    public void setPerson_nullTargetPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(null, ALICE));
    }

    @Test
    public void setPerson_nullEditedPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPerson(ALICE, null));
    }

    @Test
    public void setPerson_targetPersonNotInList_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.setPerson(ALICE, ALICE));
    }

    @Test
    public void setPerson_editedPersonIsSamePerson_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(ALICE);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasSameIdentity_success() {
        uniquePersonList.add(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        uniquePersonList.setPerson(ALICE, editedAlice);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(editedAlice);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasDifferentIdentity_success() {
        uniquePersonList.add(ALICE);
        uniquePersonList.setPerson(ALICE, BOB);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPerson_editedPersonHasNonUniqueIdentity_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, BOB));
    }

    @Test
    public void setPerson_editedPersonHasDuplicateStudentId_throwsDuplicatePersonException() {
        uniquePersonList.add(ALICE);
        uniquePersonList.add(BOB);
        Person editedAlice = new PersonBuilder(ALICE).withStudentId(BOB.getStudentId()).build();

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, editedAlice));
    }

    @Test
    public void setPerson_editedPersonHasDuplicateEnrollmentId_throwsDuplicatePersonException() {
        EnrollmentId duplicateId = new EnrollmentId("E-00000000-0000-0000-0000-000000000001");
        Person bob = withEnrollment(BOB, duplicateId);
        Person editedAlice = withEnrollment(ALICE, duplicateId);
        uniquePersonList.add(ALICE);
        uniquePersonList.add(bob);

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPerson(ALICE, editedAlice));
        assertEquals(List.of(ALICE, bob), uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void remove_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.remove(null));
    }

    @Test
    public void remove_personDoesNotExist_throwsPersonNotFoundException() {
        assertThrows(PersonNotFoundException.class, () -> uniquePersonList.remove(ALICE));
    }

    @Test
    public void remove_existingPerson_removesPerson() {
        uniquePersonList.add(ALICE);
        uniquePersonList.remove(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullUniquePersonList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((UniquePersonList) null));
    }

    @Test
    public void setPersons_uniquePersonList_replacesOwnListWithProvidedUniquePersonList() {
        uniquePersonList.add(ALICE);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        uniquePersonList.setPersons(expectedUniquePersonList);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_nullList_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> uniquePersonList.setPersons((List<Person>) null));
    }

    @Test
    public void setPersons_list_replacesOwnListWithProvidedList() {
        uniquePersonList.add(ALICE);
        List<Person> personList = List.of(BOB);
        uniquePersonList.setPersons(personList);
        UniquePersonList expectedUniquePersonList = new UniquePersonList();
        expectedUniquePersonList.add(BOB);
        assertEquals(expectedUniquePersonList, uniquePersonList);
    }

    @Test
    public void setPersons_listWithDistinctPersons_replacesOwnList() {
        List<Person> persons = List.of(ALICE, BOB);

        uniquePersonList.setPersons(persons);

        assertEquals(persons, uniquePersonList.asUnmodifiableObservableList());
    }

    @Test
    public void setPersons_listWithDuplicatePersons_throwsDuplicatePersonException() {
        List<Person> listWithDuplicatePersons = List.of(ALICE, ALICE);
        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(listWithDuplicatePersons));
    }

    @Test
    public void setPersons_listWithSameIdentityAndDifferentStudentIds_throwsDuplicatePersonException() {
        Person sameIdentityWithDifferentStudentId = new PersonBuilder(ALICE)
                .withStudentId(new StudentId("S-00000000-0000-0000-0000-000000000003")).build();

        assertThrows(DuplicatePersonException.class, () ->
                uniquePersonList.setPersons(List.of(ALICE, sameIdentityWithDifferentStudentId)));
    }

    @Test
    public void setPersons_listWithDuplicateStudentIds_throwsDuplicatePersonException() {
        Person differentPersonWithSameId = new PersonBuilder(BOB)
                .withStudentId(ALICE.getStudentId()).build();

        assertThrows(DuplicatePersonException.class, () ->
                uniquePersonList.setPersons(List.of(ALICE, differentPersonWithSameId)));
    }

    @Test
    public void setPersons_listWithDuplicateEnrollmentIds_throwsDuplicatePersonException() {
        EnrollmentId duplicateId = new EnrollmentId("E-00000000-0000-0000-0000-000000000001");
        Person alice = withEnrollment(ALICE, duplicateId);
        Person bob = withEnrollment(BOB, duplicateId);

        assertThrows(DuplicatePersonException.class, () -> uniquePersonList.setPersons(List.of(alice, bob)));
    }

    @Test
    public void asUnmodifiableObservableList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, ()
            -> uniquePersonList.asUnmodifiableObservableList().remove(0));
    }

    @Test
    public void toStringMethod() {
        assertEquals(uniquePersonList.asUnmodifiableObservableList().toString(), uniquePersonList.toString());
    }

    private Person withEnrollment(Person person, EnrollmentId enrollmentId) {
        Enrollment enrollment = new Enrollment(enrollmentId, person.getStudentId(),
                new ClassId("C001"), new Cost("30"), PaymentStatus.UNPAID);
        return new PersonBuilder(person).withEnrollments(enrollment).build();
    }
}
