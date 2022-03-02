package wgu.c196.rachel.coursescheduler.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import wgu.c196.rachel.coursescheduler.model.Term;

/**
 * Sets up CRUD operations for the Term table in the database.
 */
@Dao
public interface TermDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Term term);

    @Update
    void update(Term term);

    @Delete
    void delete(Term term);

    @Query("DELETE FROM term_table")
    void deleteAll();

    @Query("SELECT * FROM term_table")
    LiveData<List<Term>> getAllTerms();

    @Query("SELECT COUNT(*) FROM term_table")
    int getSize();

    @Query("SELECT * FROM term_table WHERE id = :termId")
    Term getTermFromId(int termId);

    @Query("SELECT * FROM term_table WHERE id = :termId")
    LiveData<Term> getLiveTerm(int termId);

    @Query("SELECT * FROM term_table")
    List<Term> getAllTermsNonLive();
}
