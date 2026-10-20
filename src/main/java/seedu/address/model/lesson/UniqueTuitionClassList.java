package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** A list of tuition classes with unique IDs and unique subject/timing combinations. */
public final class UniqueTuitionClassList {

    private final ObservableList<TuitionClass> internalList = FXCollections.observableArrayList();
    private final ObservableList<TuitionClass> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /** Returns whether a class with this ID exists. */
    public boolean containsId(ClassId classId) {
        requireNonNull(classId);
        return internalList.stream().anyMatch(item -> item.getClassId().equals(classId));
    }

    /** Returns whether a class with the same subject and timing exists. */
    public boolean containsDetails(TuitionClass tuitionClass) {
        requireNonNull(tuitionClass);
        return internalList.stream().anyMatch(item -> item.hasSameDetails(tuitionClass));
    }

    public TuitionClass get(ClassId classId) {
        return internalList.stream().filter(item -> item.getClassId().equals(classId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Class does not exist: " + classId));
    }

    /** Adds a non-duplicate class. */
    public void add(TuitionClass tuitionClass) {
        requireNonNull(tuitionClass);
        if (containsId(tuitionClass.getClassId()) || containsDetails(tuitionClass)) {
            throw new IllegalArgumentException("Duplicate tuition class");
        }
        internalList.add(tuitionClass);
        internalList.sort(Comparator.comparing(TuitionClass::getClassId));
    }

    public void set(TuitionClass target, TuitionClass edited) {
        requireNonNull(target);
        requireNonNull(edited);
        int index = internalList.indexOf(target);
        if (index < 0) {
            throw new NoSuchElementException("Class does not exist: " + target.getClassId());
        }
        if (!target.getClassId().equals(edited.getClassId())) {
            throw new IllegalArgumentException("A class ID cannot be changed.");
        }
        boolean duplicateDetails = internalList.stream()
                .anyMatch(item -> item != target && item.hasSameDetails(edited));
        if (duplicateDetails) {
            throw new IllegalArgumentException("Duplicate tuition class");
        }
        internalList.set(index, edited);
        internalList.sort(Comparator.comparing(TuitionClass::getClassId));
    }

    /** Removes an existing class. */
    public void remove(TuitionClass tuitionClass) {
        if (!internalList.remove(tuitionClass)) {
            throw new NoSuchElementException("Class does not exist: " + tuitionClass.getClassId());
        }
    }

    public void setClasses(Collection<TuitionClass> classes) {
        requireNonNull(classes);
        internalList.clear();
        classes.forEach(this::add);
    }

    public ObservableList<TuitionClass> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof UniqueTuitionClassList otherList
                && internalList.equals(otherList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }
}
