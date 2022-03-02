package wgu.c196.rachel.coursescheduler.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.adapter.InstructorRecyclerAdapter;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityInstructorsBinding;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;
import wgu.c196.rachel.coursescheduler.util.DeleteDialog;

/**
 * Controller for viewing all course instructors.
 *
 * <p> This class lets the user view information about the course instructors in the database. </p>
 */
public class InstructorsActivity extends AppCompatActivity implements DeleteDialog.DeleteDialogListener {
    private ActivityInstructorsBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private InstructorRecyclerAdapter recyclerAdapter;
    private boolean isDelete = false;
    private List<CourseInstructor> deleteList = new ArrayList<>();

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It gets a list of all course instructors
     * in the database and loads the Recycler Adapter with the list. Clicking on a course instructor
     * will let the user edit that instructor's information. </p>
     * @param savedInstanceState contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instructors);

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication())
                .create(ScheduleViewModel.class);

        binding.deleteInstructorsFab.setVisibility(View.GONE);
        binding.deleteInstructorsFab.setOnClickListener(view -> {
            showDeleteDialog();
        });

        // Load recycler view
        binding.instructorsRecyclerview.setHasFixedSize(true);
        binding.instructorsRecyclerview.setLayoutManager(new LinearLayoutManager(InstructorsActivity.this));

        scheduleViewModel.getAllInstructors().observe(this, new Observer<List<CourseInstructor>>() {
            @Override
            public void onChanged(List<CourseInstructor> instructorList) {
                if (instructorList == null)
                    return;

                recyclerAdapter = new InstructorRecyclerAdapter(instructorList, (position, view) -> {
                    //OnContactClickListener
                    CourseInstructor instructor = instructorList.get(position);

                    if (isDelete) {
                        selectDeleteInstructors(instructor, view);
                    } else {
                        Intent intent = new Intent(InstructorsActivity.this, AddCourseInstructorActivity.class);
                        intent.putExtra(AddCourseInstructorActivity.INSTRUCTOR_ID, instructor.getId());
                        startActivity(intent);
                    }
                });
                binding.instructorsRecyclerview.setAdapter(recyclerAdapter);
            }
        });
    }

    /**
     * Checks or unchecks a course instructor as being selected for deletion.
     *
     * <p> This method sets the view's delete checkbox to the opposite of what it currently is.
     * It then uses that boolean value to either add or remove the view's course instructor from a
     * list of instructors to be deleted. </p>
     * @param instructor The course instructor whose information is located in the view
     * @param view The ViewHolder view from the recycler adapter
     */
    private void selectDeleteInstructors(CourseInstructor instructor, View view) {
        CheckBox deleteCheckBox = view.findViewById(R.id.delete_checkbox);
        boolean isChecked = !deleteCheckBox.isChecked();

        deleteCheckBox.setChecked(isChecked);

        if (isChecked)
            deleteList.add(instructor);
        else
            deleteList.remove(instructor);
    }

    /**
     * Deletes the instructors in the delete list from the database.
     */
    private void deleteInstructors() {
        for (CourseInstructor instructor : deleteList) {
            scheduleViewModel.delete(instructor);
        }
        deleteList.clear();

        isDelete = false;
        showDeleteButtons();
    }

    /**
     * Tells the recycler adapter to show or hide the delete checkboxes in the ViewHolders, and
     * hides the delete FAB.
     */
    private void showDeleteButtons() {
        if (isDelete) {
            binding.deleteInstructorsFab.setVisibility(View.VISIBLE);
        } else {
            binding.deleteInstructorsFab.setVisibility(View.GONE);
        }

        recyclerAdapter.setDelete(isDelete);
        recyclerAdapter.notifyDataSetChanged();
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
        inflater.inflate(R.menu.term_list_menu, menu);
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
            startActivity(new Intent(InstructorsActivity.this, HomePageActivity.class));
            return true;
        } else if (itemId == R.id.add_item) {
            startActivity(new Intent(InstructorsActivity.this, AddCourseInstructorActivity.class));
            return true;
        } else if (itemId == R.id.delete_item) {
            isDelete = true;
            showDeleteButtons();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a message asking for confirmation to delete the selected course instructors.
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
            bundle.putString(DeleteDialog.MESSAGE, getString(R.string.delete_instructor));
        } else {
            bundle.putString(DeleteDialog.MESSAGE, getString(R.string.delete_multiple_instructors));
        }

        DeleteDialog dialog = new DeleteDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "DeleteDialog");
    }

    /**
     * Deletes the selected course instructors from the database when the user confirms deletion.
     *
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDeleteDialogPositive(DialogFragment dialog) {
        deleteInstructors();
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