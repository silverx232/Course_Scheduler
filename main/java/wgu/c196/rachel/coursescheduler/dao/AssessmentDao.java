package wgu.c196.rachel.coursescheduler.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import wgu.c196.rachel.coursescheduler.model.Assessment;

/**
 * Sets up CRUD operations for the Assessment table in the database.
 */
@Dao
public interface AssessmentDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Assessment assessment);

    @Update
    void update(Assessment assessment);

    @Delete
    void delete(Assessment assessment);

    @Query("DELETE FROM assessment_table")
    void deleteAll();

    @Query("SELECT * FROM assessment_table")
    LiveData<List<Assessment>> getAllAssessments();

    @Query("SELECT * FROM assessment_table WHERE course_id = :courseId")
    LiveData<List<Assessment>> getAssessmentsForCourse(int courseId);

    @Query("SELECT * FROM assessment_table WHERE id = :assessmentId")
    LiveData<Assessment> getAssessmentFromId(int assessmentId);

    @Query("SELECT COUNT(*) FROM assessment_table")
    int getSize();
}
