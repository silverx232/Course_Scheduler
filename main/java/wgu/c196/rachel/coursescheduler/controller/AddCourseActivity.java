package wgu.c196.rachel.coursescheduler.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityAddCourseBinding;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;
import wgu.c196.rachel.coursescheduler.model.Term;
import wgu.c196.rachel.coursescheduler.util.InformationDialog;

/**
 * Controller for adding a course.
 *
 * <p> This activity lets the user add a course or edit an existing course. </p>
 */
public class AddCourseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    /**
     * Tag for passing a course ID through an intent. Used for editing a course.
     */
    public static final String COURSE_ID = "course_id";

    private ActivityAddCourseBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private boolean isEditCourse = false;
    private Course editCourse;
    private LocalDate startDate;
    private LocalDate endDate;
    private ArrayAdapter<CourseInstructor> instructorAdapter;
    private ArrayAdapter<Course.Status> statusAdapter;
    private ArrayAdapter<Term> termAdapter;
    private static final String NEW_COURSE_INSTRUCTOR = "New Course Instructor";
    private static final String NEW_TERM = "New Term";

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It checks whether information was
     * supplied through an Intent. If so, a course is being edited instead of added. So it
     * loads that course's information into the screen for editing. </p>
     * @param savedInstanceState Contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_course);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_course);

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication())
                .create(ScheduleViewModel.class);

        binding.endDateCalendarview2.setVisibility(View.GONE);
        binding.startDateCalendarview2.setVisibility(View.GONE);

        // Set up calendar views to match their select buttons
        binding.startDateButton.setOnClickListener(view -> {
            binding.startDateCalendarview2.setVisibility(View.VISIBLE);
            binding.endDateCalendarview2.setVisibility(View.GONE);
        });

        binding.endDateButton.setOnClickListener(view -> {
            binding.startDateCalendarview2.setVisibility(View.GONE);
            binding.endDateCalendarview2.setVisibility(View.VISIBLE);
        });

        //Set up Calendars
        binding.startDateCalendarview2.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // The Calendar class stores months as 0 to 11, but LocalDate accepts them as 1 to 12
                month++;
                startDate = LocalDate.of(year, month, dayOfMonth);
                binding.startDateButton.setText(startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
            }
        });

        binding.endDateCalendarview2.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // The Calendar class stores months as 0 to 11, but LocalDate accepts them as 1 to 12
                month++;
                endDate = LocalDate.of(year, month, dayOfMonth);
                binding.endDateButton.setText(endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
            }
        });

        // Set up spinners
        scheduleViewModel.getAllInstructors().observe(this, new Observer<List<CourseInstructor>>() {
            @Override
            public void onChanged(List<CourseInstructor> instructorList) {
                CourseInstructor instructor = new CourseInstructor();
                instructor.setName(NEW_COURSE_INSTRUCTOR);
                instructorList.add(instructor);

                instructorAdapter = new ArrayAdapter<>(AddCourseActivity.this,
                        android.R.layout.simple_spinner_item, instructorList);
                instructorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.instructorSpinner.setAdapter(instructorAdapter);
            }
        });

        List<Course.Status> statusList = Course.Status.statusList;
        statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.courseStatusSpinner.setAdapter(statusAdapter);

        scheduleViewModel.getAllTerms().observe(this, new Observer<List<Term>>() {
            @Override
            public void onChanged(List<Term> termList) {
                Term term = new Term();
                term.setTitle(NEW_TERM);
                termList.add(term);

                termAdapter = new ArrayAdapter<>(AddCourseActivity.this,
                        android.R.layout.simple_spinner_item, termList);
                termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.termSpinner.setAdapter(termAdapter);
            }
        });

        // Set onItemSelectedListener for instructor and term spinners
        binding.termSpinner.setOnItemSelectedListener(this);
        binding.instructorSpinner.setOnItemSelectedListener(this);

        binding.saveButton2.setOnClickListener(view -> {
            saveCourse();
        });

        Bundle intentData = getIntent().getExtras();
        if (intentData != null) {
            isEditCourse = true;
            // run in background thread and update course info
            startEditCourseTask(intentData.getInt(COURSE_ID));
        } else {
            editCourse = new Course();
        }
    }

    /**
     * Loads the information from a user selected course.
     *
     * <p> if the user is editing a course (indicated through passing an Intent), the
     * information from the indicated course is loaded into the form. </p>
     * @param courseId The ID of the course that is being edited
     */
    private void startEditCourseTask(int courseId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // do in background

            // Set up Spinners in background
            List<CourseInstructor> instructorList = scheduleViewModel.getAllInstructorsNonLive();
            CourseInstructor newInstructor = new CourseInstructor();
            newInstructor.setName(NEW_COURSE_INSTRUCTOR);
            instructorList.add(newInstructor);

            instructorAdapter = new ArrayAdapter<>(AddCourseActivity.this,
                    android.R.layout.simple_spinner_item, instructorList);
            instructorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.instructorSpinner.setAdapter(instructorAdapter);

            // Term spinner
            List<Term> termList = scheduleViewModel.getAllTermsNonLive();
            Term newTerm = new Term();
            newTerm.setTitle(NEW_TERM);
            termList.add(newTerm);

            termAdapter = new ArrayAdapter<>(AddCourseActivity.this,
                    android.R.layout.simple_spinner_item, termList);
            termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.termSpinner.setAdapter(termAdapter);

            // Obtain the edit course
            editCourse = scheduleViewModel.getCourseFromId(courseId);
            CourseInstructor instructor = scheduleViewModel.getInstructorFromId(editCourse.getCourseInstructorId());
            Term term = scheduleViewModel.getTermFromId(editCourse.getTermId());

            // On post execute
            runOnUiThread(() -> {
                binding.addCourseTextview.setText(getString(R.string.edit_course));

                // Populate course information
                startDate = editCourse.getStartDate();
                if (startDate != null)
                    binding.startDateButton.setText(startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));

                endDate = editCourse.getEndDate();
                if (endDate != null)
                    binding.endDateButton.setText(endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));

                binding.courseNameEdittext.setText(editCourse.getTitle());
                binding.editNoteText.setText(editCourse.getNote());

                if (statusAdapter == null)
                    Log.d("TAG", "startEditCourseTask: status adapter is null");

                if (instructorAdapter == null)
                    Log.d("TAG", "startEditCourseTask: instructor adapter is null");

                if (termAdapter == null)
                    Log.d("TAG", "startEditCourseTask: Term adapter is null");

                // Set spinners
                int position = statusAdapter.getPosition(editCourse.getStatus());
                binding.courseStatusSpinner.setSelection(position);

                position = instructorAdapter.getPosition(instructor);
                binding.instructorSpinner.setSelection(position);

                position = termAdapter.getPosition(term);
                binding.termSpinner.setSelection(position);
            });
        });
    }

    /**
     * Saves a course in the database.
     *
     * <p> Creates a course with the user supplied information and saves it in the database.
     * A course title, instructor, and term is required. After saving the course, the
     * user is returned to viewing all courses. </p>
     */
    private void saveCourse() {
        String title = binding.courseNameEdittext.getText().toString().trim();
        if (title.isEmpty()) {
            showRequiredDialog(getString(R.string.course_name_required));
            return;
        }

        CourseInstructor instructor = (CourseInstructor) binding.instructorSpinner.getSelectedItem();
        if (instructor == null) {
            showRequiredDialog(getString(R.string.instructor_required));
            return;
        }

        Term term = (Term) binding.termSpinner.getSelectedItem();
        if (term == null) {
            showRequiredDialog(getString(R.string.term_required));
            return;
        }

        Course.Status status = (Course.Status) binding.courseStatusSpinner.getSelectedItem();
        String note = binding.editNoteText.getText().toString().trim();

        Course userCourse = new Course();
        userCourse.setTitle(title);
        userCourse.setStartDate(startDate);
        userCourse.setEndDate(endDate);
        userCourse.setStatus(status);
        userCourse.setCourseInstructorId(instructor.getId());
        userCourse.setNote(note);
        userCourse.setTermId(term.getId());

        if (isEditCourse) {
            userCourse.setId(editCourse.getId());
            scheduleViewModel.update(userCourse);
        } else {
            scheduleViewModel.insert(userCourse);
        }

        Intent intent = new Intent(AddCourseActivity.this, CourseActivity.class);
        startActivity(intent);
    }

    /**
     * Tells the activity what to do when an item is selected in the spinner.
     *
     * <p> When an item is selected in the spinner, this method checks the item selected. If that item
     * is "New Term" or "New Course Instructor" then the user is directed to the activity that adds
     * a new term or instructor. </p>
     * @param parent The AdapterView (in this case spinner) where the item was selected
     * @param view The view calling the method
     * @param position The position of the AdapterView (spinner)
     * @param id The ID of the item selected
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int spinnerId = parent.getId();

        if (spinnerId == R.id.term_spinner) {
            Term term = (Term) parent.getSelectedItem();
            if (term.getTitle().equals(NEW_TERM)) {
                startActivity(new Intent(AddCourseActivity.this, AddTermActivity.class));
                parent.setSelection(0);
            }
        }
        else if (spinnerId == R.id.instructor_spinner) {
            CourseInstructor instructor = (CourseInstructor) parent.getSelectedItem();
            if (instructor.getName().equals(NEW_COURSE_INSTRUCTOR)) {
                startActivity(new Intent(AddCourseActivity.this, AddCourseInstructorActivity.class));
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
            startActivity(new Intent(AddCourseActivity.this, HomePageActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a message informing the user that the information in message is required in order to
     * save the course.
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