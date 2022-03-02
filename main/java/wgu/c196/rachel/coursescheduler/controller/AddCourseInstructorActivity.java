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

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityAddCourseInstructorBinding;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;
import wgu.c196.rachel.coursescheduler.util.InformationDialog;

/**
 * Controller for adding a course instructor.
 *
 * <p> This activity lets the user add a course instructor or edit an existing course instructor. </p>
 */
public class AddCourseInstructorActivity extends AppCompatActivity {
    /**
     * Tag for passing a course instructor ID through an intent. Used for editing a course instructor.
     */
    public final static String INSTRUCTOR_ID = "instructor id";

    private ActivityAddCourseInstructorBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private boolean isEdit = false;
    private CourseInstructor userInstructor;

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It checks whether information was
     * supplied through an Intent. If so, a course instructor is being edited instead of added. So it
     * loads that course instructor's information into the screen for editing. </p>
     * @param savedInstanceState Contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_course_instructor);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_course_instructor);

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication())
                .create(ScheduleViewModel.class);

        // Populate information if editing course instructor
        Bundle intentData = getIntent().getExtras();
        if (intentData != null) {
            isEdit = true;
            scheduleViewModel.getLiveInstructor(intentData.getInt(INSTRUCTOR_ID))
                    .observe(this, new Observer<CourseInstructor>() {
                @Override
                public void onChanged(CourseInstructor courseInstructor) {
                    userInstructor = courseInstructor;
                    populateInformation();
                }
            });
        } else {
            userInstructor = new CourseInstructor();
        }

        binding.saveButton3.setOnClickListener(view -> {
            saveInstructor();
        });
    }

    /**
     * Goes to the activity that shows all course instructors.
     */
    private void goInstructorsActivity() {
        startActivity(new Intent(AddCourseInstructorActivity.this, InstructorsActivity.class));
    }

    /**
     * Loads the information from a user selected course instructor.
     *
     * <p> if the user is editing a course instructor (indicated through passing an Intent), the
     * information from the indicated course instructor is loaded into the form. </p>
     */
    private void populateInformation() {
        binding.addInstructorTextview.setText(R.string.edit_course_instructor);
        binding.nameEdittext.setText(userInstructor.getName());
        binding.editTextPhone.setText(userInstructor.getPhoneNumber());
        binding.editTextEmail.setText(userInstructor.getEmail());
    }

    /**
     * Saves a course instructor in the database.
     *
     * <p> Creates a course instructor with the user supplied information and saves it in the database.
     * A name is required. After saving the course instructor, the user is returned to viewing
     * all courses instructors. </p>
     */
    private void saveInstructor() {
        String name = binding.nameEdittext.getText().toString().trim();

        if (name.isEmpty()) {
            showRequiredDialog(getString(R.string.instructor_name_required));
            return;
        }

        userInstructor.setName(name);
        userInstructor.setPhoneNumber(binding.editTextPhone.getText().toString().trim());
        userInstructor.setEmail(binding.editTextEmail.getText().toString().trim());

        if (isEdit) {
            scheduleViewModel.update(userInstructor);
        } else {
            scheduleViewModel.insert(userInstructor);
        }

        goInstructorsActivity();
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
            startActivity(new Intent(AddCourseInstructorActivity.this, HomePageActivity.class));
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