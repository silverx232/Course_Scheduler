package wgu.c196.rachel.coursescheduler.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityAddAssessmentBinding;
import wgu.c196.rachel.coursescheduler.model.Assessment;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.util.InformationDialog;

/**
 * Controller for adding an assessment.
 *
 * <p> This activity lets the user add an assessment or edit an existing assessment. </p>
 */
public class AddAssessmentActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    /**
     * Tag for passing an assessment ID through an intent. Used for editing an assessment.
     */
    public static final String ASSESSMENT_ID = "assessment id";

    private ActivityAddAssessmentBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private ArrayAdapter<Course> courseAdapter;
    private ArrayAdapter<Assessment.Type> typeAdapter;
    private boolean isEdit = false;
    private Assessment userAssessment;
    private LocalDate startDate;
    private LocalDate endDate;

    private static final String NEW_COURSE = "New Course";

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It checks whether information was
     * supplied through an Intent. If so, an assessment is being edited instead of added. So it
     * loads that assessment's information into the screen for editing. </p>
     * @param savedInstanceState contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_assessment);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_assessment);

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication())
                .create(ScheduleViewModel.class);

        // Set up Calendars
        binding.startDateCalendar.setVisibility(View.GONE);
        binding.endDateCalendar.setVisibility(View.GONE);

        binding.selectStartDateButton.setOnClickListener(view -> {
            binding.startDateCalendar.setVisibility(View.VISIBLE);
            binding.endDateCalendar.setVisibility(View.GONE);
        });

        binding.selectEndDateButton.setOnClickListener(view -> {
            binding.startDateCalendar.setVisibility(View.GONE);
            binding.endDateCalendar.setVisibility(View.VISIBLE);
        });

        binding.startDateCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // The Calendar class stores months as 0 to 11, but LocalDate accepts them as 1 to 12
                month++;
                startDate = LocalDate.of(year, month, dayOfMonth);
                binding.selectStartDateButton.setText(startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
            }
        });

        binding.endDateCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // The Calendar class stores months as 0 to 11, but LocalDate accepts them as 1 to 12
                month++;
                endDate = LocalDate.of(year, month, dayOfMonth);
                binding.selectEndDateButton.setText(endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
            }
        });

        // Set up spinners
        scheduleViewModel.getAllCourses().observe(this, new Observer<List<Course>>() {
            @Override
            public void onChanged(List<Course> courseList) {
                // Add an option to make a new course to end of list
                Course course = new Course();
                course.setTitle(NEW_COURSE);
                courseList.add(course);

                courseAdapter = new ArrayAdapter<>(AddAssessmentActivity.this,
                        android.R.layout.simple_spinner_item, courseList);
                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.courseSpinner.setAdapter(courseAdapter);
            }
        });

        List<Assessment.Type> typeList = Assessment.Type.typeList;
        typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, typeList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.typeSpinner.setAdapter(typeAdapter);

        // Populate information if editing an assessment
        Bundle intentData = getIntent().getExtras();
        if (intentData != null) {
            isEdit = true;
            scheduleViewModel.getAssessmentFromId(intentData.getInt(ASSESSMENT_ID))
                    .observe(this, new Observer<Assessment>() {
                @Override
                public void onChanged(Assessment assessment) {
                    userAssessment = assessment;
                    populateInformation();
                }
            });
        } else {
            userAssessment = new Assessment();
        }

        binding.saveButton.setOnClickListener(view -> {
            saveAssessment();
        });

        // Set onItemSelectedListener for selecting new course
        binding.courseSpinner.setOnItemSelectedListener(this);
    }

    /**
     * Loads the information from a user selected assessment.
     *
     * <p> If the user is editing an assessment (indicated through passing an Intent), the information
     * from the indicated assessment is loaded into the form. </p>
     */
    private void populateInformation() {
        binding.addAssessmentTitle.setText(R.string.edit_assessment);
        binding.assessmentTitleEdittext.setText(userAssessment.getTitle());

        startDate = userAssessment.getStartDate();
        if (startDate != null) {
            binding.selectStartDateButton.setText(startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        }

        endDate = userAssessment.getEndDate();
        if (endDate != null) {
            binding.selectEndDateButton.setText(endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        }

        // Set spinners
        binding.typeSpinner.setSelection(typeAdapter.getPosition(userAssessment.getType()));

        scheduleViewModel.getLiveCourse(userAssessment.getCourseId())
                .observe(this, new Observer<Course>() {
                    @Override
                    public void onChanged(Course course) {
                        int position = courseAdapter.getPosition(course);
                        binding.courseSpinner.setSelection(position);
                    }
                });
    }

    /**
     * Saves an assessment in the database.
     *
     * <p> Creates an assessment with the user supplied information and saves it in the database.
     * An assessment title and the course it is for is required. After saving the assessment, the
     * user is returned to viewing all assessments. </p>
     */
    private void saveAssessment() {
        String title = binding.assessmentTitleEdittext.getText().toString().trim();
        if (title.isEmpty()) {
            showRequiredDialog(getString(R.string.assessment_title_required));
            return;
        }

        Course course = (Course) binding.courseSpinner.getSelectedItem();
        if (course == null) {
            showRequiredDialog(getString(R.string.course_required));
            return;
        }

        Assessment.Type type = (Assessment.Type) binding.typeSpinner.getSelectedItem();

        userAssessment.setTitle(title);
        userAssessment.setStartDate(startDate);
        userAssessment.setEndDate(endDate);
        userAssessment.setType(type);
        userAssessment.setCourseId(course.getId());

        if (isEdit) {
            scheduleViewModel.update(userAssessment);
        } else {
            scheduleViewModel.insert(userAssessment);
        }

        // Go to AssessmentsActivity
        startActivity(new Intent(AddAssessmentActivity.this, AssessmentsActivity.class));
    }

    /**
     * Tells the activity what to do when an item is selected in the spinner.
     *
     * <p> When an item is selected in the spinner, this method checks the item selected. If that item
     * is "New Course" then the user is directed to the activity that adds a new course. </p>
     * @param parent The AdapterView (in this case spinner) where the item was selected
     * @param view The view calling the method
     * @param position The position of the AdapterView (spinner)
     * @param id The ID of the item selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinnerId = parent.getId();

        if (spinnerId == R.id.course_spinner) {
            Course course = (Course) parent.getSelectedItem();
            if (course.getTitle().equals(NEW_COURSE)) {
                startActivity(new Intent(AddAssessmentActivity.this, AddCourseActivity.class));
                parent.setSelection(0);
            }
        }
    }

    /**
     * Defines what happens when nothing is selected in the AdapterView. In this case, do nothing.
     *
     * @param parent The AdapterView where nothing was selected
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

    /**
     * Inflates the menu using the specified layout.
     *
     * @param menu The menu to be created
     * @return Returns true to indicate that a menu was created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_item_menu, menu);
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

        if (id == R.id.edit_home_item) {
            startActivity(new Intent(AddAssessmentActivity.this, HomePageActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a message informing the user that the information in message is required in order to
     * save the assessment.
     *
     * @param message The message about the required information
     */
    private void showRequiredDialog(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(InformationDialog.MESSAGE, message);

        InformationDialog informationDialog = new InformationDialog();
        informationDialog.setArguments(bundle);
        informationDialog.show(getSupportFragmentManager(), "InformationDialog");
    }
}