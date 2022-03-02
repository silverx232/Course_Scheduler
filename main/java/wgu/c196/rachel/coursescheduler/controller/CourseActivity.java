package wgu.c196.rachel.coursescheduler.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.adapter.CourseRecyclerAdapter;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityCourseBinding;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;
import wgu.c196.rachel.coursescheduler.util.DeleteDialog;

/**
 * Controller for viewing all courses.
 *
 * <p> This class lets the user view information about the courses in the database. </p>
 */
public class CourseActivity extends AppCompatActivity implements DeleteDialog.DeleteDialogListener {
    /**
     * Tag for passing a term ID through an Intent. If an intent is passed, the activity will only
     * load courses for the indicated term.
     */
    public static final String TERM_ID = "term id";

    private ActivityCourseBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private CourseRecyclerAdapter courseRecyclerAdapter;
    private LiveData<List<Course>> courseList;
    private boolean isDelete = false;
    private List<Course> deleteList = new ArrayList<>();

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It gets a list of all courses
     * in the database and loads the Recycler Adapter with the list. Or, if an intent was passed,
     * it will load the courses that have the indicated term ID. Clicking on a course will bring
     * the user to a view of that course.</p>
     * @param savedInstanceState contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_course);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_course);

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(
                CourseActivity.this.getApplication())
                .create(ScheduleViewModel.class);

        binding.deleteCourseFab.setVisibility(View.GONE);
        binding.courseRecyclerView.setHasFixedSize(true);
        binding.courseRecyclerView.setLayoutManager(new LinearLayoutManager(CourseActivity.this));

        Bundle intentData = getIntent().getExtras();

        if (intentData != null) {
            int termId = intentData.getInt(TERM_ID);
            courseList = scheduleViewModel.getCoursesForTerm(termId);
        } else {
            courseList = scheduleViewModel.getAllCourses();
        }

        scheduleViewModel.getAllInstructors().observe(CourseActivity.this, new Observer<List<CourseInstructor>>() {
            @Override
            public void onChanged(List<CourseInstructor> instructorList) {
                Map<Integer, CourseInstructor> instructorMap = new HashMap<>();
                for (CourseInstructor instructor : instructorList) {
                    instructorMap.put(instructor.getId(), instructor);
                }

                courseListAdapterSetup(instructorMap);
            }
        });

        binding.deleteCourseFab.setOnClickListener(view -> {
            showDeleteDialog();
        });
    }

    /**
     * Sets up the Recycler Adapter for the list of courses.
     *
     * @param instructorMap A list of all courses instructors mapped to their ID. Used by the
     *                      recycler adapter.
     */
    private void courseListAdapterSetup(Map<Integer, CourseInstructor> instructorMap) {
        courseList.observe(this, new Observer<List<Course>>() {
            @Override
            public void onChanged(List<Course> courses) {
                courseRecyclerAdapter = new CourseRecyclerAdapter(courses, instructorMap,
                    new CourseRecyclerAdapter.OnContactClickListener() {
                        @Override
                        public void onContactClick(int position, View view) {
                            Course course = courses.get(position);

                            if (isDelete) {
                                selectDeleteCourses(course, view);
                            } else {
                                Intent intent = new Intent(CourseActivity.this, CourseViewActivity.class);
                                intent.putExtra(CourseViewActivity.COURSE_ID, course.getId());
                                startActivity(intent);
                            }
                        }
                    });

                binding.courseRecyclerView.setAdapter(courseRecyclerAdapter);
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
        getMenuInflater().inflate(R.menu.term_list_menu, menu);
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
        int itemId = item.getItemId();

        if (itemId == R.id.home_item) {
            startActivity(new Intent(CourseActivity.this, HomePageActivity.class));
            return true;
        } else if (itemId == R.id.add_item) {
            startActivity(new Intent(CourseActivity.this, AddCourseActivity.class));
            return true;
        } else if (itemId == R.id.delete_item) {
            isDelete = true;
            showDeleteButtons();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Tells the recycler adapter to show or hide the delete checkboxes in the ViewHolders, and
     * hides the delete FAB.
     */
    private void showDeleteButtons() {
        if (isDelete)
            binding.deleteCourseFab.setVisibility(View.VISIBLE);
        else
            binding.deleteCourseFab.setVisibility(View.GONE);

        courseRecyclerAdapter.setDelete(isDelete);
        courseRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Deletes the courses in the delete list from the database.
     */
    private void deleteCourses() {
        //Delete courses from database
        for (Course course : deleteList) {
            scheduleViewModel.delete(course);
        }
        deleteList.clear();

        // hide delete buttons and reset adapter
        isDelete = false;
        showDeleteButtons();
    }

    /**
     * Checks or unchecks a course as being selected for deletion.
     *
     * <p> This method sets the view's delete checkbox to the opposite of what it currently is.
     * It then uses that boolean value to either add or remove the view's course from a
     * list of courses to be deleted. </p>
     * @param course The course whose information is located in the view
     * @param view The ViewHolder view from the recycler adapter
     */
    private void selectDeleteCourses(Course course, View view) {
        CheckBox deleteCheckbox = view.findViewById(R.id.course_delete_checkbox);
        boolean isChecked = !deleteCheckbox.isChecked();

        deleteCheckbox.setChecked(isChecked);
        if (isChecked)
            deleteList.add(course);
        else
            deleteList.remove(course);
    }

    /**
     * Shows a message asking for confirmation to delete the selected courses.
     */
    private void showDeleteDialog() {
        int deleteSize = deleteList.size();

        if (deleteSize == 0) {
            isDelete = false;
            showDeleteButtons();
            return;
        }

        Bundle bundle = new Bundle();

        if (deleteSize == 1) {
            bundle.putString(DeleteDialog.MESSAGE, getString(R.string.delete_course));
        } else {
            bundle.putString(DeleteDialog.MESSAGE, getString(R.string.delete_multiple_courses));
        }

        DeleteDialog dialog = new DeleteDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "DeleteDialog");
    }

    /**
     * Deletes the selected courses from the database when the user confirms deletion.
     *
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDeleteDialogPositive(DialogFragment dialog) {
        deleteCourses();
    }

    /**
     * Tells the activity to clear the deletion list and buttons when the user cancels deletion.
     *
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDeleteDialogNegative(DialogFragment dialog) {
        deleteList.clear();
        isDelete = false;
        showDeleteButtons();
    }
}