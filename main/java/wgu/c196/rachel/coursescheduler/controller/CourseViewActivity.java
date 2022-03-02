package wgu.c196.rachel.coursescheduler.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.adapter.AssessmentRecyclerAdapter;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityCourseViewBinding;
import wgu.c196.rachel.coursescheduler.model.Assessment;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;
import wgu.c196.rachel.coursescheduler.util.AlertChannels;
import wgu.c196.rachel.coursescheduler.util.AlertReceiver;
import wgu.c196.rachel.coursescheduler.util.DeleteDialog;
import wgu.c196.rachel.coursescheduler.util.InformationDialog;

/**
 * Controller for viewing a course.
 *
 * <p> This class lets the user view information about a selected course. </p>
 */
public class CourseViewActivity extends AppCompatActivity implements DeleteDialog.DeleteDialogListener {
    /**
     * Tag for passing a course ID of the user selected course through an intent.
     */
    public static final String COURSE_ID = "course_id";

    private ActivityCourseViewBinding binding;
    private Course course;
    private CourseInstructor instructor;
    private ScheduleViewModel scheduleViewModel;
    private AssessmentRecyclerAdapter assessmentRecyclerAdapter;

    // To make the request codes for pending intents unique
    private static final int START_DATE_RC = 1000;
    private static final int END_DATE_RC = 2000;

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It checks for the course ID provided in
     * the Intent, finds that course, and loads the information into the views. An Intent should
     * always be provided. It also loads a recycler view to view all assessments that are associated
     * with the given course. Clicking on an assessment will allow the user to view information on
     * that assessment. </p>
     * @param savedInstanceState contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_course_view);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_course_view);

        int courseId = 1;
        Bundle intentData = getIntent().getExtras();
        if (intentData != null) {
            courseId = intentData.getInt(COURSE_ID);
        }

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication())
                .create(ScheduleViewModel.class);

        binding.alertStartDateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAlerts(buttonView, isChecked);
            }
        });

        binding.alertEndDateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAlerts(buttonView, isChecked);
            }
        });

//        // Done with AsyncTask
//        startGetCourseTask(courseId);

        // Load course information
        // Done with Executors
        startCourseTask(courseId);

        // Load assessments recyclerview
        binding.courseAssessmentsRecyclerview.setHasFixedSize(true);
        binding.courseAssessmentsRecyclerview.setLayoutManager(new LinearLayoutManager(CourseViewActivity.this));

        scheduleViewModel.getAssessmentsForCourse(courseId).observe(this, new Observer<List<Assessment>>() {
            @Override
            public void onChanged(List<Assessment> assessmentList) {
                assessmentRecyclerAdapter = new AssessmentRecyclerAdapter(assessmentList,
                    (position, view) -> {  //OnContactClickListener
                        Assessment assessment = assessmentList.get(position);
                        Intent intent = new Intent(CourseViewActivity.this, AssessmentViewActivity.class);
                        intent.putExtra(AssessmentViewActivity.ASSESSMENT_ID, assessment.getId());
                        startActivity(intent);
                    });
                binding.courseAssessmentsRecyclerview.setAdapter(assessmentRecyclerAdapter);
            }
        });
    }


    /**
     * Loads the information from a user selected course.
     *
     * <p> This method uses the supplied course ID to find the course in the database. It then
     * loads the course information into the views. It also checks if there are any pending intents
     * for the start and end dates. </p>
     */
    private void startCourseTask(int courseId) {
        // The newer way to do AsyncTask, since AsyncTask was deprecated
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //do in background
                course = scheduleViewModel.getCourseFromId(courseId);
                instructor = scheduleViewModel.getInstructorFromId(course.getCourseInstructorId());

                Intent intent = new Intent(CourseViewActivity.this, AlertReceiver.class);
                PendingIntent startIntent = PendingIntent.getBroadcast(CourseViewActivity.this,
                        START_DATE_RC + course.getId(), intent, PendingIntent.FLAG_NO_CREATE);

                PendingIntent endIntent = PendingIntent.getBroadcast(CourseViewActivity.this,
                        END_DATE_RC + course.getId(), intent, PendingIntent.FLAG_NO_CREATE);

                //post execute / access main thread for UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Populate the course information
                        binding.courseViewTitle.setText(course.getTitle());

                        if (course.getStatus() != null)
                            binding.statusTextview.setText(course.getStatus().toString());

                        if (course.getStartDate() != null)
                            binding.startDateTextview2.setText(course.getStartDate()
                                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));

                        if (course.getEndDate() != null)
                            binding.endDateTextview2.setText(course.getEndDate()
                                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));

                        binding.noteTextview.setText(course.getNote());

                        if (instructor != null) {
                            binding.instructorNameTextview.setText(instructor.getName());
                            binding.instructorNumberTextview.setText(instructor.getPhoneNumber());
                            binding.instructorEmailTextview.setText(instructor.getEmail());
                        }

                        if (startIntent != null) {
                            binding.alertStartDateSwitch.setChecked(true);
                        }

                        if (endIntent != null) {
                            binding.alertEndDateSwitch.setChecked(true);
                        }
                    }
                });
            }
        });
    }

    /**
     * Inflates the menu using the specified layout.
     *
     * @param menu The menu to be created
     * @return Returns true to indicate that a menu was created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_view_menu, menu);
        return true;
    }

    /**
     * Tells the activity what to do when the user clicks a menu item.
     *
     * @param item The menu item that the user clicked on
     * @return Returns true if the menu item matches a listed item and the indicated action was performed
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.course_home_menuItem) {
            startActivity(new Intent(CourseViewActivity.this, HomePageActivity.class));
            return true;
        } else if (id == R.id.course_edit_menuItem) {
            Intent intent = new Intent(CourseViewActivity.this, AddCourseActivity.class);
            intent.putExtra(AddCourseActivity.COURSE_ID, course.getId());
            startActivity(intent);
            return true;
        } else if (id == R.id.course_delete_menuItem) {
            showDeleteDialog();
            return true;
        } else if (id == R.id.course_share_menuItem) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, course.getNote());
            intent.putExtra(Intent.EXTRA_SUBJECT, "Note from course " + course.getTitle());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Deletes the course from the database.
     *
     * <p> This method deletes the user selected course from the database. It then goes to the
     * activity to view all courses in the deleted course's term. </p>
     */
    private void deleteCourse() {
        scheduleViewModel.delete(course);

        Intent intent = new Intent(CourseViewActivity.this, CourseActivity.class);
        intent.putExtra(CourseActivity.TERM_ID, course.getTermId());
        startActivity(intent);
    }

    /**
     * Shows a message asking for confirmation to delete the course.
     */
    private void showDeleteDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(DeleteDialog.MESSAGE, getString(R.string.delete_course));

        DeleteDialog dialog = new DeleteDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "DeleteDialog");
    }

    /**
     * Tells the activity what to do when the user confirms deletion.
     *
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDeleteDialogPositive(DialogFragment dialog) {
        deleteCourse();
    }

    /**
     * Tells the activity what to do when the user cancels deletion. In this case, do nothing.
     *
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDeleteDialogNegative(DialogFragment dialog) {
        // do nothing
    }

    /**
     * Shows a message informing the user that a date is required in order to create an alert.
     */
    private void showNullDateDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(InformationDialog.MESSAGE, getString(R.string.date_cannot_be_null));

        InformationDialog dialog = new InformationDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "NullDateDialog");
    }

    /**
     * Sets or cancels an alert depending on a user toggled switch.
     *
     * <p> A user can choose to set a reminder about the start or end date of the course. A start
     * or end date is required in order to set their alerts. This method will determine which
     * switch was clicked and whether to set up or cancel an alert. </p>
     * @param buttonView The switch that was clicked
     * @param isChecked True if the switch was clicked on, false if the switch was clicked off
     */
    private void setAlerts(CompoundButton buttonView, boolean isChecked) {
        int viewId = buttonView.getId();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(CourseViewActivity.this, AlertReceiver.class);
        intent.putExtra(AlertReceiver.CHANNEL, AlertChannels.COURSE);

        if (viewId == R.id.alert_start_date_switch) {
            intent.putExtra(AlertReceiver.NOTIFICATION_ID, START_DATE_RC + course.getId());
            intent.putExtra(AlertReceiver.MESSAGE, "Course " + course.getTitle() + " is starting today");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(CourseViewActivity.this,
                    START_DATE_RC + course.getId(), intent, 0);

            if (isChecked) {
                LocalDate date = course.getStartDate();

                if (date == null) {
                    showNullDateDialog();
                    buttonView.setChecked(false);
                    return;
                }

                // AlarmManager uses time in Milliseconds
                Instant dateInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

                alarmManager.set(AlarmManager.RTC_WAKEUP, dateInstant.toEpochMilli(), pendingIntent);
            } else {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        } else { // end date switch
            intent.putExtra(AlertReceiver.NOTIFICATION_ID, END_DATE_RC + course.getId());
            intent.putExtra(AlertReceiver.MESSAGE, "Course " + course.getTitle() + " is ending today");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(CourseViewActivity.this,
                    END_DATE_RC + course.getId(), intent, 0
            );

            if (isChecked) {
                LocalDate date = course.getEndDate();

                if (date == null) {
                    showNullDateDialog();
                    buttonView.setChecked(false);
                    return;
                }

                // AlarmManager uses time in Milliseconds
                Instant dateInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

                alarmManager.set(AlarmManager.RTC_WAKEUP, dateInstant.toEpochMilli(), pendingIntent);
            } else {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    //    // AsyncTask, which is deprecated
//    private void startGetCourseTask(int courseId) {
//        GetCourseTask getCourseTask = new GetCourseTask(this);
//        getCourseTask.execute(courseId);
//    }
//
//    private static class GetCourseTask extends AsyncTask<Integer, Void, Void> {
//        private WeakReference<CourseViewActivity> activityWeakReference;
//
//        public GetCourseTask(CourseViewActivity activity) {
//            activityWeakReference = new WeakReference<>(activity);
//        }
//
//        @Override
//        protected Void doInBackground(Integer... integers) {
//            CourseViewActivity activity = activityWeakReference.get();
//            if (activity == null || activity.isFinishing()) {
//                return null;
//            }
//
//            activity.course = activity.scheduleViewModel.getCourseFromId(integers[0]);
//            activity.instructor = activity.scheduleViewModel.getInstructorFromId(activity.course.getCourseInstructorId());
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void unused) {
//            CourseViewActivity activity = activityWeakReference.get();
//            if (activity == null || activity.isFinishing()) {
//                return;
//            }
//
//            // Populate the course information
//            activity.binding.courseViewTitle.setText(activity.course.getTitle());
//
//            if (activity.course.getStatus() != null)
//                activity.binding.statusTextview.setText(activity.course.getStatus().toString());
//
//            if (activity.course.getStartDate() != null)
//                activity.binding.startDateTextview2.setText(activity.course.getStartDate()
//                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
//
//            if (activity.course.getEndDate() != null)
//                activity.binding.endDateTextview2.setText(activity.course.getEndDate()
//                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
//
//            activity.binding.noteTextview.setText(activity.course.getNote());
//
//            if (activity.instructor != null) {
//                activity.binding.instructorNameTextview.setText(activity.instructor.getName());
//                activity.binding.instructorNumberTextview.setText(activity.instructor.getPhoneNumber());
//                activity.binding.instructorEmailTextview.setText(activity.instructor.getEmail());
//            }
//        }
//    }


}