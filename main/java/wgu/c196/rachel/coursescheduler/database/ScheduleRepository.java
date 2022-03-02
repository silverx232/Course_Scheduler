package wgu.c196.rachel.coursescheduler.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import wgu.c196.rachel.coursescheduler.dao.AssessmentDao;
import wgu.c196.rachel.coursescheduler.dao.CourseDao;
import wgu.c196.rachel.coursescheduler.dao.CourseInstructorDao;
import wgu.c196.rachel.coursescheduler.dao.TermDao;
import wgu.c196.rachel.coursescheduler.model.Assessment;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;
import wgu.c196.rachel.coursescheduler.model.Term;

/**
 * Class that acts as a repository between the database and the View Model.
 */
public class ScheduleRepository {

    private TermDao termDao;
    private CourseDao courseDao;
    private AssessmentDao assessmentDao;
    private CourseInstructorDao courseInstructorDao;

    // Database write executor is used to write to the database on background threads.
    // Classes reading from the database should call that method in their own background thread.

    public ScheduleRepository(Application application) {
        // Get an instance of the database
        ScheduleRoomDatabase database = ScheduleRoomDatabase.getDatabase(application);

        termDao = database.termDao();
        courseDao = database.courseDao();
        assessmentDao = database.assessmentDao();
        courseInstructorDao = database.courseInstructorDao();
    }

    public LiveData<List<Term>> getAllTerms() {
        return termDao.getAllTerms();
    }

    public LiveData<List<Course>> getAllCourses() {
        return courseDao.getAllCourses();
    }

    public LiveData<List<CourseInstructor>> getAllInstructors() {
        return courseInstructorDao.getAllInstructors();
    }

    public LiveData<List<Assessment>> getAllAssessments() {
        return assessmentDao.getAllAssessments();
    }

    public LiveData<List<Course>> getCoursesForTerm(int termId) {
        return courseDao.getCoursesForTerm(termId);
    }

    public void insert(Term term) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            termDao.insert(term);
        });
    }

    public void insert(Course course) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            courseDao.insert(course);
        });
    }

    public void insert(CourseInstructor courseInstructor) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            courseInstructorDao.insert(courseInstructor);
        });
    }

    public void insert(Assessment assessment) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            assessmentDao.insert(assessment);
        });
    }

    public CourseInstructor getInstructorFromId(int id) {
        return courseInstructorDao.getInstructorFromId(id);
    }

    public Course getCourseFromId(int courseId) {
        return courseDao.getCourseFromId(courseId);
    }

    public Term getTermFromId(int termId) {
        return termDao.getTermFromId(termId);
    }

    public int getCourseSize() {
        return courseDao.getSize();
    }

    public int getTermSize() {
        return termDao.getSize();
    }

    public int getAssessmentSize() {
        return assessmentDao.getSize();
    }

    public int getCourseInstructorSize() {
        return courseInstructorDao.getSize();
    }

    public void update(Term term) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            termDao.update(term);
        });
    }

    public void update(Course course) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            courseDao.update(course);
        });
    }

    public void update(Assessment assessment) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            assessmentDao.update(assessment);
        });
    }

    public void update(CourseInstructor courseInstructor) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            courseInstructorDao.update(courseInstructor);
        });
    }

    public void delete(Term term) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            termDao.delete(term);
        });
    }

    public void delete(Course course) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            courseDao.delete(course);
        });
    }

    public void delete(CourseInstructor courseInstructor) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            courseInstructorDao.delete(courseInstructor);
        });
    }

    public void delete(Assessment assessment) {
        ScheduleRoomDatabase.databaseWriteExecutor.execute(() -> {
            assessmentDao.delete(assessment);
        });
    }

    public LiveData<List<Assessment>> getAssessmentsForCourse(int courseId) {
        return assessmentDao.getAssessmentsForCourse(courseId);
    }

    public LiveData<Assessment> getAssessmentFromId(int assessmentId) {
        return assessmentDao.getAssessmentFromId(assessmentId);
    }

    public LiveData<CourseInstructor> getLiveInstructor(int instructorId) {
        return courseInstructorDao.getLiveInstructor(instructorId);
    }

    public LiveData<Term> getLiveTerm(int termId) {
        return termDao.getLiveTerm(termId);
    }

    public LiveData<Course> getLiveCourse(int courseId) {
        return courseDao.getLiveCourse(courseId);
    }

    public List<Course> getCoursesForTermNotLive(int termId) {
        return courseDao.getCoursesForTermNotLive(termId);
    }

    public List<Term> getAllTermsNonLive() {
        return termDao.getAllTermsNonLive();
    }

    public List<Course> getAllCoursesNonLive() {
        return courseDao.getAllCoursesNonLive();
    }

    public List<CourseInstructor> getAllInstructorsNonLive() {
        return courseInstructorDao.getAllInstructorsNonLive();
    }
}
