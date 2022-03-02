package wgu.c196.rachel.coursescheduler.controller;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.databinding.ActivityHomePageBinding;

/**
 * The home page for Course Scheduler.
 *
 * <p> This class acts as the home page. It lets the user select to view all terms, courses,
 * assessments, or course instructors. </p>
 */
public class HomePageActivity extends AppCompatActivity {
    private ActivityHomePageBinding binding;

    /**
     * Method that runs when the activity is created.
     *
     * <p> This method initializes the views in the layout. It sets up the onClickListeners for
     * the buttons. </p>
     * @param savedInstanceState contains data supplied to onSaveInstanceState() or null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home_page);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_page);

        binding.viewTermsButton.setOnClickListener(view -> {
            startActivity(new Intent(this, TermsActivity.class));
        });

        binding.viewCoursesButton.setOnClickListener(view -> {
            startActivity(new Intent(this, CourseActivity.class));
        });

        binding.viewAssessmentsButton.setOnClickListener(view -> {
            startActivity(new Intent(this, AssessmentsActivity.class));
        });

        binding.viewInstructorsButton.setOnClickListener(view -> {
            startActivity(new Intent(this, InstructorsActivity.class));
        });
    }
}