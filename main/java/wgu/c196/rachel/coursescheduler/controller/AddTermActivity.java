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
import android.widget.CalendarView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.database.ScheduleViewModel;
import wgu.c196.rachel.coursescheduler.databinding.ActivityAddTermBinding;
import wgu.c196.rachel.coursescheduler.model.Term;
import wgu.c196.rachel.coursescheduler.util.InformationDialog;

/**
 * Controller for adding a term.
 *
 * <p> This activity lets the user add a term or edit an existing term. </p>
 */
public class AddTermActivity extends AppCompatActivity {
    private ActivityAddTermBinding binding;
    private boolean isEditTerm = false;
    private Term userTerm;
    private ScheduleViewModel scheduleViewModel;
    private LocalDate startDate;
    private LocalDate endDate;

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
//        setContentView(R.layout.activity_add_term);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_term);

        scheduleViewModel = new ViewModelProvider.AndroidViewModelFactory(this.getApplication())
                .create(ScheduleViewModel.class);

        binding.endDateCalendarview.setVisibility(View.GONE);
        binding.startDateCalendarview.setVisibility(View.GONE);


        Bundle intentData = getIntent().getExtras();
        if (intentData != null) {
            isEditTerm = true;
            scheduleViewModel.getLiveTerm(intentData.getInt(TermsActivity.TERM_ID))
                    .observe(this, new Observer<Term>() {
                @Override
                public void onChanged(Term term) {
                    userTerm = term;
                    populateInformation();
                }
            });
        } else {
            userTerm = new Term();
        }

        // Set up the calenderViews to match their select buttons
        binding.selectStartDate.setOnClickListener(view -> {
            binding.startDateCalendarview.setVisibility(View.VISIBLE);
            binding.endDateCalendarview.setVisibility(View.GONE);
        });

        binding.selectEndDate.setOnClickListener(view -> {
            binding.startDateCalendarview.setVisibility(View.GONE);
            binding.endDateCalendarview.setVisibility(View.VISIBLE);
        });

        binding.startDateCalendarview.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // The Calendar class stores months as 0 to 11, but LocalDate accepts them as 1 to 12
                month++;
                startDate = LocalDate.of(year, month, dayOfMonth);
                binding.selectStartDate.setText(startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
            }
        });

        binding.endDateCalendarview.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // The Calendar class stores months as 0 to 11, but LocalDate accepts them as 1 to 12
                month++;
                endDate = LocalDate.of(year, month, dayOfMonth);
                binding.selectEndDate.setText(endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
            }
        });

        binding.termSaveButton.setOnClickListener(view -> {
            saveTerm();
        });
    }

    /**
     * Saves a term in the database.
     *
     * <p> Creates a term with the user supplied information and saves it in the database.
     * A term title is required. After saving the term, the user is returned to viewing all terms. </p>
     */
    private void saveTerm() {
        String title = binding.termNameEdittext.getText().toString().trim();

        if (title.isEmpty()) {
            showRequiredDialog(getString(R.string.term_name_required));
            return;
        }

        userTerm.setTitle(title);
        userTerm.setStartDate(startDate);
        userTerm.setEndDate(endDate);

        if (isEditTerm) {
            scheduleViewModel.update(userTerm);
        } else {
            scheduleViewModel.insert(userTerm);
        }

        // Move to view term list
        goTermsActivity();
    }

    /**
     * Loads the information from a user selected term.
     *
     * <p> if the user is editing a term (indicated through passing an Intent), the
     * information from the indicated term is loaded into the form. </p>
     */
    private void populateInformation() {
        binding.addTermTextview.setText(R.string.edit_term);
        binding.termNameEdittext.setText(userTerm.getTitle());

        startDate = userTerm.getStartDate();
        if (startDate != null)
            binding.selectStartDate.setText(startDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));

        endDate = userTerm.getEndDate();
        if (endDate != null)
            binding.selectEndDate.setText(endDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
    }

    /**
     * Sends the user to the activity to view all terms.
     */
    private void goTermsActivity() {
        startActivity(new Intent(AddTermActivity.this, TermsActivity.class));
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
            startActivity(new Intent(AddTermActivity.this, HomePageActivity.class));
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