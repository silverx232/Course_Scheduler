package wgu.c196.rachel.coursescheduler.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityAssessmentViewBinding;
import wgu.c196.rachel.coursescheduler.model.Assessment;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.util.AlertChannels;
import wgu.c196.rachel.coursescheduler.util.AlertReceiver;
import wgu.c196.rachel.coursescheduler.util.DeleteDialog;
import wgu.c196.rachel.coursescheduler.util.InformationDialog;

/**
 * Controller for viewing an assessment.
 *
 * <p> This class lets the user view information about a selected assessment. </p>
 */
public class AssessmentViewActivity extends AppCompatActivity implements DeleteDialog.DeleteDialogListener {
    /**
     * Tag for passing an assessment ID of the user select assessment through an intent.
     */
    public static final String ASSESSMENT_ID = "assessment id";

    private ActivityAssessmentViewBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private Assessment userAssessment;

    // To differentiate from pending alerts that are Courses
    private static final int START_DATE_RC = 3000;
    private static final int END_DATE_RC = 4000;

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It checks for the assessment ID provided
     * in the Intent, finds that assessment, and loads the information into the views. An Intent
     * should always be provided. </p>
     * @param savedInstanceState contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_assessment_view);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_assessment_view);

        int assessmentId = 1;
        Bundle intentData = getIntent().getExtras();
        if (intentData != null) {
            assessmentId = intentData.getInt(ASSESSMENT_ID);
        }

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication())
                .create(ScheduleViewModel.class);

        binding.assessmentStartDateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAlerts(buttonView, isChecked);
            }
        });

        binding.assessmentEndDateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAlerts(buttonView, isChecked);
            }
        });

        // Load assessment information
        scheduleViewModel.getAssessmentFromId(assessmentId).observe(this, new Observer<Assessment>() {
            @Override
            public void onChanged(Assessment assessment) {
                if (assessment == null)
                    return;

                userAssessment = assessment;

                binding.assessmentTitleTextview.setText(assessment.getTitle());
                binding.assessmentTypeInfo.setText(assessment.getType().toString());
                binding.startDateInfo.setText(assessment.getStartDate()
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
                binding.endDateInfo.setText(assessment.getEndDate()
                        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));

                setCourseNameTask();
            }
        });

    }

    /**
     * Loads information on the assessment's course and alerts.
     *
     * <p> This method finds the assessment's course in the database and loads the information.
     * It also checks if there are any pending intents for the start and end dates. </p>
     */
    private void setCourseNameTask() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Course course = scheduleViewModel.getCourseFromId(userAssessment.getCourseId());

            // Set alert switches
            Intent intent = new Intent(AssessmentViewActivity.this, AlertReceiver.class);
            PendingIntent startIntent = PendingIntent.getBroadcast(AssessmentViewActivity.this,
                    START_DATE_RC + userAssessment.getId(), intent, PendingIntent.FLAG_NO_CREATE);

            PendingIntent endIntent = PendingIntent.getBroadcast(AssessmentViewActivity.this,
                    END_DATE_RC + userAssessment.getId(), intent, PendingIntent.FLAG_NO_CREATE);

            runOnUiThread(() -> {
                binding.courseNameInfo.setText(course.getTitle());

                if (startIntent != null)
                    binding.assessmentStartDateSwitch.setChecked(true);

                if (endIntent != null)
                    binding.assessmentEndDateSwitch.setChecked(true);
            });
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
        getMenuInflater().inflate(R.menu.view_item_menu, menu);
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

        if (id == R.id.view_home_item) {
            startActivity(new Intent(AssessmentViewActivity.this, HomePageActivity.class));
            return true;
        } else if (id == R.id.view_edit_item) {
            Intent intent = new Intent(AssessmentViewActivity.this, AddAssessmentActivity.class);
            intent.putExtra(AddAssessmentActivity.ASSESSMENT_ID, userAssessment.getId());
            startActivity(intent);
            return true;
        } else if (id == R.id.view_delete_item) {
            showDeleteDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Deletes the assessment from the database.
     *
     * <p> This method deletes the user selected assessment from the database. It then goes to the
     * activity to view all assessments in the deleted assessment's course. </p>
     */
    private void deleteAssessment() {
        scheduleViewModel.delete(userAssessment);

        Intent intent = new Intent(AssessmentViewActivity.this, CourseViewActivity.class);
        intent.putExtra(CourseViewActivity.COURSE_ID, userAssessment.getCourseId());
        startActivity(intent);
    }

    /**
     * Shows a message asking for confirmation to delete the assessment.
     */
    private void showDeleteDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(DeleteDialog.MESSAGE, getString(R.string.delete_assessment));

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
        deleteAssessment();
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
     * <p> A user can choose to set a reminder about the start or end date of the assessment. A start
     * or end date is required in order to set their alerts. This method will determine which
     * switch was clicked and whether to set up or cancel an alert. </p>
     * @param buttonView The switch that was clicked
     * @param isChecked True if the switch was clicked on, false if the switch was clicked off
     */
    private void setAlerts(CompoundButton buttonView, boolean isChecked) {
        int viewId = buttonView.getId();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AssessmentViewActivity.this, AlertReceiver.class);
        intent.putExtra(AlertReceiver.CHANNEL, AlertChannels.ASSESSMENT);

        if (viewId == R.id.assessment_start_date_switch) {
            intent.putExtra(AlertReceiver.NOTIFICATION_ID, START_DATE_RC + userAssessment.getId());
            intent.putExtra(AlertReceiver.MESSAGE, "Assessment " + userAssessment.getTitle() +
                    " is starting today");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(AssessmentViewActivity.this, START_DATE_RC + userAssessment.getId(), intent, 0);

            if (isChecked) {
                LocalDate date = userAssessment.getStartDate();
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
        } else {  // end date switch
            intent.putExtra(AlertReceiver.NOTIFICATION_ID, END_DATE_RC + userAssessment.getId());
            intent.putExtra(AlertReceiver.MESSAGE, "Assessment " + userAssessment.getTitle() + " is ending today.");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(AssessmentViewActivity.this,
                    END_DATE_RC + userAssessment.getId(), intent, 0);

            if (isChecked) {
                LocalDate date = userAssessment.getEndDate();
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
}