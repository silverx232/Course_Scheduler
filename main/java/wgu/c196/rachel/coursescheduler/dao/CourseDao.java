package wgu.c196.rachel.coursescheduler.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import wgu.c196.rachel.coursescheduler.model.Course;

/**
 * Sets up CRUD operations for the Course table in the database.
 */
@Dao
public interface CourseDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Course course);

    @Update
    void update(Course course);

    @Delete
    void delete(Course course);

    @Query("DELETE FROM course_table")
    void deleteAll();

    @Query("SELECT * FROM course_table")
    LiveData<List<Course>> getAllCourses();

    @Query("SELECT * FROM course_table WHERE term_id = :termId")
    LiveData<List<Course>> getCoursesForTerm(int termId);

    @Query("SELECT * FROM course_table WHERE term_id = :termId")
    List<Course> getCoursesForTermNotLive(int termId);

    @Query("SELECT * FROM course_table WHERE course_instructor = :instructorId")
    LiveData<List<Course>> getCoursesForInstructor(int instructorId);

    @Query("SELECT * FROM course_table WHERE id = :courseId")
    Course getCourseFromId(int courseId);

    @Query("SELECT COUNT(*) FROM course_table")
    int getSize();

    @Query("SELECT * FROM course_table WHERE id = :courseId")
    LiveData<Course> getLiveCourse(int courseId);

    @Query("SELECT * FROM course_table")
    List<Course> getAllCoursesNonLive();

}
