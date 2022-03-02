package wgu.c196.rachel.coursescheduler.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import wgu.c196.rachel.coursescheduler.database.ScheduleRepository;
import wgu.c196.rachel.coursescheduler.model.Assessment;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;
import wgu.c196.rachel.coursescheduler.model.Term;

/**
 * View Model for the database.
 *
 * <p> This class allows access between the GUI and the database. It manages data from the repository.
 * the AndroidViewModel survives screen rotation, and so is more efficient than just calling the
 * repository. </p>
 */
// "The ViewModel is designed to store and manage UI-related data in a lifecycle conscious way."
public class ScheduleViewModel extends AndroidViewModel {

    private static ScheduleRepository repository;

    public ScheduleViewModel(@NonNull Application application) {
        super(application);
        repository = new ScheduleRepository(application);
    }

    public LiveData<List<Term>> getAllTerms() {
        return repository.getAllTerms();
    }

    public LiveData<List<Course>> getAllCourses() {
        return repository.getAllCourses();
    }

    public LiveData<List<CourseInstructor>> getAllInstructors() {
        return repository.getAllInstructors();
    }

    public LiveData<List<Assessment>> getAllAssessments() {
        return repository.getAllAssessments();
    }

    public LiveData<List<Course>> getCoursesForTerm(int termId) {
        return repository.getCoursesForTerm(termId);
    }

    public void insert(Term term) {
        repository.insert(term);
    }

    public void insert(Course course) {
        repository.insert(course);
    }

    public void insert(CourseInstructor courseInstructor) {
        repository.insert(courseInstructor);
    }

    public void insert(Assessment assessment) {
        repository.insert(assessment);
    }

    public CourseInstructor getInstructorFromId(int id) {
        return repository.getInstructorFromId(id);
    }

    public Course getCourseFromId(int courseId) {
        return repository.getCourseFromId(courseId);
    }

    public Term getTermFromId(int termId) {
        return repository.getTermFromId(termId);
    }

    public int getCourseSize() {
        return repository.getCourseSize();
    }

    public int getTermSize() {
        return repository.getTermSize();
    }

    public int getAssessmentSize() {
        return repository.getAssessmentSize();
    }

    public int getCourseInstructorSize() {
        return repository.getCourseInstructorSize();
    }

    public void update(Term term) {
        repository.update(term);
    }

    public void update(Course course) {
        repository.update(course);
    }

    public void update(Assessment assessment) {
        repository.update(assessment);
    }

    public void update(CourseInstructor courseInstructor) {
        repository.update(courseInstructor);
    }

    public void delete(Term term) {
        repository.delete(term);
    }

    public void delete(Course course) {
        repository.delete(course);
    }

    public void delete(CourseInstructor courseInstructor) {
        repository.delete(courseInstructor);
    }

    public void delete(Assessment assessment) {
        repository.delete(assessment);
    }

    public LiveData<List<Assessment>> getAssessmentsForCourse(int courseId) {
        return repository.getAssessmentsForCourse(courseId);
    }

    public LiveData<Assessment> getAssessmentFromId(int assessmentId) {
        return repository.getAssessmentFromId(assessmentId);
    }

    public LiveData<CourseInstructor> getLiveInstructor(int instructorId) {
        return repository.getLiveInstructor(instructorId);
    }

    public LiveData<Term> getLiveTerm(int termId) {
        return repository.getLiveTerm(termId);
    }

    public LiveData<Course> getLiveCourse(int courseId) {
        return repository.getLiveCourse(courseId);
    }

    public List<Course> getCoursesForTermNotLive(int termId) {
        return repository.getCoursesForTermNotLive(termId);
    }

    public List<Term> getAllTermsNonLive() {
        return repository.getAllTermsNonLive();
    }

    public List<Course> getAllCoursesNonLive() {
        return repository.getAllCoursesNonLive();
    }

    public List<CourseInstructor> getAllInstructorsNonLive() {
        return repository.getAllInstructorsNonLive();
    }
}
