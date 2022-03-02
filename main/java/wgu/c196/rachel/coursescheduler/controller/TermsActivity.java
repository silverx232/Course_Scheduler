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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.adapter.TermRecyclerAdapter;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityTermsBinding;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.model.Term;
import wgu.c196.rachel.coursescheduler.util.DeleteDialog;

/**
 * Controller for viewing all terms.
 *
 * <p> This class lets the user view information about the terms in the database. </p>
 */
public class TermsActivity extends AppCompatActivity implements DeleteDialog.DeleteDialogListener {
    /**
     * Tag used by AddTermActivity to pass the term's ID if editing a term.
     */
    public static final String TERM_ID = "term id";

    private ActivityTermsBinding binding;
    private ScheduleViewModel scheduleViewModel;
    private TermRecyclerAdapter termRecyclerAdapter;
    private boolean isDelete = false;
    private final List<Term> deleteList = new ArrayList<>();
    private List<Course> deleteDialogCourses = new ArrayList<>();
    private List<Term> termList = new ArrayList<>();
    private static final String ASSOCIATED_COURSES = "associated courses";

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It gets a list of all terms in the
     * database and loads the Recycler Adapter with the list. Clicking on a term will bring the
     * user to a list of that term's courses. </p>
     * @param savedInstanceState contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms);

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(
                TermsActivity.this.getApplication())
                .create(ScheduleViewModel.class);

        binding.deleteFab.setVisibility(View.GONE);
        binding.termRecyclerView.setHasFixedSize(true);
        binding.termRecyclerView.setLayoutManager(new LinearLayoutManager(TermsActivity.this));

        binding.deleteFab.setOnClickListener(view -> deleteTerms());

//        checkTermSize();

        scheduleViewModel.getAllTerms().observe(this, new Observer<List<Term>>() {
            @Override
            public void onChanged(List<Term> terms) {
                // This is what happens when the data changes
                termRecyclerAdapter = new TermRecyclerAdapter(terms, (position, view) -> {  // onClick listener
                    Term term = terms.get(position);

                    if (isDelete) {
                        selectDeleteTerms(term, view);
                    } else {
                        Intent intent = new Intent(TermsActivity.this, CourseActivity.class);
                        intent.putExtra(CourseActivity.TERM_ID, term.getId());
                        startActivity(intent);
                    }
                } );

                binding.termRecyclerView.setAdapter(termRecyclerAdapter);
            }
        });
    }

//    // This only works if database is already built. If you populate the database in ScheduleRoomDatabase
//    // it reads the size as 0 the first time the program is run.
//    private void checkTermSize() {
//        ExecutorService executor = Executors.newSingleThreadExecutor();
//        executor.execute(() -> {
//            // do in background
//            int termSize = scheduleViewModel.getTermSize();
//
//            runOnUiThread(() -> {
//                if (termSize == 0) {
//                    //notify user
//                    Intent intent = new Intent(TermsActivity.this, AddTermActivity.class);
//                    startActivity(intent);
//                }
//            });
//        });
//    }

    /**
     * Checks or unchecks a term as being selected for deletion.
     *
     * <p> This method sets the view's delete checkbox to the opposite of what it currently is.
     * It then uses that boolean value to either add or remove the view's term from a list of
     * terms to be deleted. </p>
     * @param term The term whose information in located in the view
     * @param view The ViewHolder view from the recycler adapter
     */
    private void selectDeleteTerms(Term term, View view) {
        CheckBox deleteCheckBox = view.findViewById(R.id.delete_checkbox);
        boolean isChecked = !deleteCheckBox.isChecked();

        deleteCheckBox.setChecked(isChecked);

        if (isChecked)
            deleteList.add(term);
        else
            deleteList.remove(term);
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
            startActivity(new Intent(TermsActivity.this, HomePageActivity.class));
            return true;
        } else if (itemId == R.id.add_item) {
            startActivity(new Intent(TermsActivity.this, AddTermActivity.class));
            return true;
        } else if (itemId == R.id.delete_item) {
            isDelete = true;
            showDeleteButtons();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *  Checks each term selected for deletion for associated courses.
     *
     *  <p> This method is called when the delete FAB is clicked, and checks each term in the delete
     *  list for associated courses. It shows the correct dialog for if there are associated
     *  courses or not. </p>
     */
    private void deleteTerms() {
        // Check that there is a term selected
        if (deleteList.size() == 0) {
            clearDelete();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            for (Term term : deleteList) {
                boolean courseAdded = false;
                List<Course> termCourses = scheduleViewModel.getCoursesForTermNotLive(term.getId());

                if (termCourses != null && termCourses.size() > 0) {
                    deleteDialogCourses.addAll(termCourses);
                    courseAdded = true;
                }

                if (courseAdded)
                    termList.add(term);
            }

            if (deleteDialogCourses == null)
                deleteDialogCourses = new ArrayList<>();

            runOnUiThread(() -> {


                if (deleteDialogCourses.size() == 0) {
                    showDeleteDialog();
                } else {
                    // There are associated courses that need to be deleted first
                    showDeleteDialogCourses();
                }
            });
        });
    }

    /**
     * Tells the recycler adapter to show or hide the delete checkboxes in the ViewHolders, and
     * hides the delete FAB.
     */
    private void showDeleteButtons() {
        if (isDelete) {
            binding.deleteFab.setVisibility(View.VISIBLE);
        } else {
            binding.deleteFab.setVisibility(View.GONE);
        }
        termRecyclerAdapter.setDelete(isDelete);
        termRecyclerAdapter.notifyDataSetChanged();
    }

    /**
     * Shows a message asking for confirmation to delete the selected terms.
     */
    private void showDeleteDialog() {
        StringBuilder message = new StringBuilder();
        Bundle bundle = new Bundle();

        if (deleteList.size() == 1) {
            message.append(getString(R.string.delete_term, deleteList.get(0).getTitle()));
        } else {
            message.append(getString(R.string.delete_multiple_terms));

            for (int i = 0; i < deleteList.size(); i++) {
                message.append(deleteList.get(i).getTitle());

                if (i == deleteList.size() - 1)
                    message.append("?");
                else
                    message.append(", ");
            }
        }

        bundle.putString(DeleteDialog.MESSAGE, message.toString());

        DeleteDialog dialog = new DeleteDialog();
        dialog.setArguments(bundle);
        dialog.showNow(getSupportFragmentManager(), "DeleteDialog_term");
    }

    /**
     * Informs the user they cannot delete a term with associated courses.
     *
     * <p> Shows a message informing the user that there are courses associated with at least one
     * of the terms they selected for deletion. A term cannot be deleted if it has associated
     * courses. Confirming the deletion will bring the user to a list of courses for the first
     * term that has associated courses which they can then delete. </p>
     */
    private void showDeleteDialogCourses() {
        StringBuilder message = new StringBuilder();

        int size = termList.size();
        if (size == 1) {
            message.append(": ").append(termList.get(0).getTitle());
        } else {
            message.append("s: ");
            for (int i = 0; i < size; i++) {
                message.append(termList.get(i).getTitle());

                if (i < size - 1)
                    message.append(", ");
            }
        }

        message = new StringBuilder(getString(R.string.delete_term_with_course, message));

        for (int i = 0; i < deleteDialogCourses.size(); i++) {
            message.append(deleteDialogCourses.get(i).getTitle());

            if (i == (deleteDialogCourses.size() - 1)) {
                message.append(".");
            } else {
                message.append(", ");
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString(DeleteDialog.MESSAGE, message.toString());

        DeleteDialog dialog = new DeleteDialog();
        dialog.setArguments(bundle);
        dialog.showNow(getSupportFragmentManager(), ASSOCIATED_COURSES);
    }

    /**
     * Deletes the selected terms from the database if there are no associated courses.
     *
     * <p> This method checks if there are associated courses. If there are, it moves the user to
     * the view the list of courses for the first selected term with associated courses. If there
     * are no associated courses, it deletes the selected terms from the database. </p>
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDeleteDialogPositive(DialogFragment dialog) {
        if (deleteDialogCourses.size() > 0) {
            // Move to view courses for first term in termList
            Intent intent = new Intent(dialog.getActivity(), CourseActivity.class);
            intent.putExtra(CourseActivity.TERM_ID, termList.get(0).getId());

            clearDelete();

            startActivity(intent);
        } else {
            // There are no associated courses
            for (Term term : deleteList)
                scheduleViewModel.delete(term);

            clearDelete();
        }
    }

    /**
     * Tells the activity to clear the deletion list and buttons when the user cancels deletion.
     *
     * @param dialog The dialog that was clicked on
     */
    @Override
    public void onDeleteDialogNegative(DialogFragment dialog) {
        clearDelete();
    }

    /**
     * Clears the deletion lists and hides the deletion buttons
     */
    private void clearDelete() {
        deleteList.clear();
        termList.clear();
        deleteDialogCourses.clear();
        isDelete = false;
        showDeleteButtons();
    }
}