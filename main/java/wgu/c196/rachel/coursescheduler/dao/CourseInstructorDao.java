package wgu.c196.rachel.coursescheduler.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import wgu.c196.rachel.coursescheduler.model.CourseInstructor;

/**
 * Sets up CRUD operations for the Course Instructor table in the database.
 */
@Dao
public interface CourseInstructorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(CourseInstructor courseInstructor);

    @Update
    void update(CourseInstructor courseInstructor);

    @Delete
    void delete(CourseInstructor courseInstructor);

    @Query("DELETE FROM course_instructor_table")
    void deleteAll();

    @Query("SELECT * FROM course_instructor_table")
    LiveData<List<CourseInstructor>> getAllInstructors();

    @Query("SELECT * FROM course_instructor_table WHERE id = :instructorId")
    CourseInstructor getInstructorFromId(int instructorId);

    @Query("SELECT * FROM course_instructor_table WHERE id = :instructorId")
    LiveData<CourseInstructor> getLiveInstructor(int instructorId);

    @Query("SELECT COUNT(*) FROM course_instructor_table")
    int getSize();

    @Query("SELECT * FROM course_instructor_table")
    List<CourseInstructor> getAllInstructorsNonLive();
}
