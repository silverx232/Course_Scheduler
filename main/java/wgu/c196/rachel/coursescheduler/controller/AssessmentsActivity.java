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
import wgu.c196.rachel.coursescheduler.adapter.AssessmentRecyclerAdapter;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityAssessmentsBinding;
import wgu.c196.rachel.coursescheduler.model.Assessment;
import wgu.c196.rachel.coursescheduler.util.DeleteDialog;

/**
 * Controller for viewing all assessments.
 *
 * <p> This class lets the user view information about the assessments in the database. </p>
 */
public class AssessmentsActivity extends AppCompatActivity implements DeleteDialog.DeleteDialogListener {
    private ActivityAssessmentsBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private AssessmentRecyclerAdapter recyclerAdapter;
    private boolean isDelete = false;
    private List<Assessment> deleteList = new ArrayList<>();

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It gets a list of all assessments
     * in the database and loads the Recycler Adapter with the list. Clicking on an assessment will
     * bring the user to a view of that assessment.</p>
     * @param savedInstanceState contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_assessments);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_assessments);

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication())
                .create(ScheduleViewModel.class);

        binding.deleteAssessmentFab.setVisibility(View.GONE);

        binding.deleteAssessmentFab.setOnClickListener(view -> {
            showDeleteDialog();
        });

        // Set up recycler adapter
        binding.assessmentsRecyclerview.setHasFixedSize(true);
        binding.assessmentsRecyclerview.setLayoutManager(new LinearLayoutManager(AssessmentsActivity.this));

        scheduleViewModel.getAllAssessments().observe(this, new Observer<List<Assessment>>() {
            @Override
            public void onChanged(List<Assessment> assessmentList) {
                recyclerAdapter = new AssessmentRecyclerAdapter(assessmentList, (position, view) -> {
                    // OnContactClickListener
                    Assessment assessment = assessmentList.get(position);

                    if (isDelete) {
                        selectDeleteAssessments(assessment, view);
                    } else {
                        Intent intent = new Intent(AssessmentsActivity.this, AssessmentViewActivity.class);
                        intent.putExtra(AssessmentViewActivity.ASSESSMENT_ID, assessment.getId());
                        startActivity(intent);
                    }
                });
                binding.assessmentsRecyclerview.setAdapter(recyclerAdapter);
            }
        });
    }

    /**
     * Checks or unchecks an assessment as being selected for deletion.
     *
     * <p> This method sets the view's delete checkbox to the opposite of what it currently is.
     * It then uses that boolean value to either add or remove the view's assessment from a
     * list of assessments to be deleted. </p>
     * @param assessment The assessment whose information is located in the view
     * @param view The ViewHolder view from the recycler adapter
     */
    private void selectDeleteAssessments(Assessment assessment, View view) {
        CheckBox deleteCheckbox = view.findViewById(R.id.delete_checkbox);
        boolean isChecked = !deleteCheckbox.isChecked();

        deleteCheckbox.setChecked(isChecked);

        if (isChecked)
            deleteList.add(assessment);
        else
            deleteList.remove(assessment);
    }

    /**
     * Deletes the assessments in the delete list from the database.
     */
    private void deleteAssessments() {
        for (Assessment assessment : deleteList) {
            scheduleViewModel.delete(assessment);
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
            binding.deleteAssessmentFab.setVisibility(View.VISIBLE);
        } else {
            binding.deleteAssessmentFab.setVisibility(View.GONE);
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
            startActivity(new Intent(AssessmentsActivity.this, HomePageActivity.class));
            return true;
        } else if (itemId == R.id.add_item) {
            startActivity(new Intent(AssessmentsActivity.this, AddAssessmentActivity.class));
            return true;
        } else if (itemId == R.id.delete_item) {
            isDelete = true;
            showDeleteButtons();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a message asking for confirmation to delete the selected assessments.
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
            bundle.putString(DeleteDialog.MESSAGE, getString(R.string.delete_assessment));
        } else {
            bundle.putString(DeleteDialog.MESSAGE, getString(R.string.delete_multiple_assessments));
        }

        DeleteDialog dialog = new DeleteDialog();
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "DeleteDialog");
    }

    /**
     * Deletes the selected assessments from the database when the user confirms deletion.
     *
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDeleteDialogPositive(DialogFragment dialog) {
        deleteAssessments();
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