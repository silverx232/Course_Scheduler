package wgu.c196.rachel.coursescheduler.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wgu.c196.rachel.coursescheduler.dao.AssessmentDao;
import wgu.c196.rachel.coursescheduler.dao.CourseDao;
import wgu.c196.rachel.coursescheduler.dao.CourseInstructorDao;
import wgu.c196.rachel.coursescheduler.dao.TermDao;
import wgu.c196.rachel.coursescheduler.model.Assessment;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;
import wgu.c196.rachel.coursescheduler.model.Term;

/**
 * Class that builds the SQLite database in the Room style.
 */
// entities: the model tables that will be in the database
@Database(entities = {Term.class, Course.class, CourseInstructor.class, Assessment.class},
        version = 1, exportSchema = false)
@TypeConverters({DatabaseConverter.class})
public abstract class ScheduleRoomDatabase extends RoomDatabase {

    public abstract TermDao termDao();
    public abstract CourseDao courseDao();
    public abstract AssessmentDao assessmentDao();
    public abstract CourseInstructorDao courseInstructorDao();

    // The number of threads the database can operate on
    public static final int NUMBER_OF_THREADS = 4;

    // Holds the instance of the database. There should only be one instance of the database in the program.
    private static volatile ScheduleRoomDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // This callback creates test data in the database if the database has not already been created
    private static final RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                    super.onCreate(db);

                    databaseWriteExecutor.execute(() -> {
                        TermDao termDao = INSTANCE.termDao();

                        //create tables/database

                        termDao.insert(new Term("Term 1", LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)));
                        termDao.insert(new Term("Term 2", LocalDate.now(), LocalDate.now()));
                        termDao.insert(new Term("Term 3", LocalDate.now(), LocalDate.now()));
                        termDao.insert(new Term("Term 4", LocalDate.now(), LocalDate.now()));
                        termDao.insert(new Term("Term 5", LocalDate.now(), LocalDate.now()));

                        Term term = new Term();
                        term.setTitle("Test6");
                        termDao.insert(term);

                        CourseInstructorDao courseInstructorDao = INSTANCE.courseInstructorDao();
                        courseInstructorDao.insert(new CourseInstructor("Wooyoung", "111-1111", "wy@ateez"));
                        courseInstructorDao.insert(new CourseInstructor("San", "222-1111", "san@ateez"));
                        courseInstructorDao.insert(new CourseInstructor("Hongjoong", "333-1111", "leader@ateez"));
                        courseInstructorDao.insert(new CourseInstructor("Seonghwa", "444-1111", "sh@ateez"));
//                        courseInstructorDao.insert(new CourseInstructor());

                        CourseDao courseDao = INSTANCE.courseDao();

                        Course course = new Course();
                        course.setTitle("Dancing");
                        course.setCourseInstructorId(1);
                        course.setTermId(1);
                        course.setStatus(Course.Status.IN_PROGRESS);
                        course.setStartDate(LocalDate.now());
                        course.setEndDate(LocalDate.now().plusDays(3));
                        course.setNote("A note");
                        courseDao.insert(course);

                        course = new Course();
                        course.setTitle("Singing");
                        course.setCourseInstructorId(2);
                        course.setTermId(1);
                        course.setStatus(Course.Status.IN_PROGRESS);
                        course.setStartDate(LocalDate.now());
                        course.setEndDate(LocalDate.now().plusDays(5));
                        course.setNote("This is just a short note.");
                        courseDao.insert(course);

                        course = new Course();
                        course.setTitle("Rap");
                        course.setCourseInstructorId(3);
                        course.setTermId(1);
                        course.setStatus(Course.Status.PLAN_TO_TAKE);
                        courseDao.insert(course);

                        course = new Course();
                        course.setTitle("Cleaning");
                        course.setCourseInstructorId(4);
                        course.setTermId(1);
                        course.setStatus(Course.Status.COMPLETED);
                        courseDao.insert(course);

//                        course = new Course();
//                        course.setTitle("Test");
//                        course.setCourseInstructorId(5);
//                        course.setTermId(1);
//                        courseDao.insert(course);

                        AssessmentDao assessmentDao = INSTANCE.assessmentDao();
                        assessmentDao.insert(new Assessment("Test1", Assessment.Type.OBJECTIVE, LocalDate.now(), LocalDate.now(), 1));
                        assessmentDao.insert(new Assessment("Test2", Assessment.Type.PERFORMANCE, LocalDate.now(), LocalDate.now(), 1));
                        assessmentDao.insert(new Assessment("Test3", Assessment.Type.OBJECTIVE, LocalDate.now(), LocalDate.now(), 2));
                        assessmentDao.insert(new Assessment("Test4", Assessment.Type.PERFORMANCE, LocalDate.now(), LocalDate.now(), 2));
                        assessmentDao.insert(new Assessment("Test5", Assessment.Type.OBJECTIVE, LocalDate.now(), LocalDate.now(), 3));
                        assessmentDao.insert(new Assessment("Test6", Assessment.Type.PERFORMANCE, LocalDate.now(), LocalDate.now(), 3));
                        assessmentDao.insert(new Assessment("Test7", Assessment.Type.OBJECTIVE, LocalDate.now(), LocalDate.now(), 1));
                        assessmentDao.insert(new Assessment("Test8", Assessment.Type.PERFORMANCE, LocalDate.now(), LocalDate.now(), 1));
                        assessmentDao.insert(new Assessment("Test9", Assessment.Type.OBJECTIVE, LocalDate.now(), LocalDate.now(), 1));


                    });
                }
            };

    /**
     * Method that gets an instance of the database.
     *
     * <p> There should only be a single instance of the database in the program. This method returns
     * that instance, or creates one if it doesn't already exist. </p>
     * @param context The context calling the database
     * @return Returns an instance of the database
     */
    // Returns the instance of the TermRoomDatabase, so that only a single instance of the database
    // is being used throughout the program.
    public static ScheduleRoomDatabase getDatabase(final Context context) {

        // If the instance has not been created yet
        if (INSTANCE == null) {
            synchronized (ScheduleRoomDatabase.class) {
                if (INSTANCE == null) {

                    // Create an instance of the database
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ScheduleRoomDatabase.class, "term_database")
                            .addCallback(sRoomDatabaseCallback)  // Could be commented out if you don't need the callback method, Callbacks are executed after the database is built. Then it goes back and calls the callbacks
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
