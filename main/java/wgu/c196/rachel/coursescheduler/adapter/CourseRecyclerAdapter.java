package wgu.c196.rachel.coursescheduler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.databinding.CourseRowBinding;
import wgu.c196.rachel.coursescheduler.model.Course;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;

/**
 * Recycler Adapter for the Course class.
 *
 * <p> Sets up the cards for the RecyclerView list of courses. Contains the public class
 * ViewHolder and the interface OnContactClickListener. </p>
 */
public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder> {
    private List<Course> courseList;
    private Map<Integer, CourseInstructor> instructorMap;
    private OnContactClickListener onContactClickListener;
    private boolean isDelete = false;

    /**
     * Constructor for CourseRecyclerAdapter.
     *
     * @param courseList The list of Courses that the RecyclerView will use
     * @param instructorMap A list of CourseInstructors mapped to their ID, provided for fast access
     * @param onContactClickListener A listener that specifies what will happen when the card is clicked in the RecyclerView
     */
    public CourseRecyclerAdapter(List<Course> courseList, Map<Integer, CourseInstructor> instructorMap,
                                 OnContactClickListener onContactClickListener) {
        this.courseList = courseList;
        this.onContactClickListener = onContactClickListener;
        this.instructorMap = instructorMap;
    }

    /**
     * Class that creates a ViewHolder.
     *
     * <p> This is called when the Adapter needs a new ViewHolder to represent an item. </p>
     * @param parent The group that the ViewHolder's view will be added to.
     * @param viewType An int specifying the type of ViewHolder to create, if there are multiple ViewHolders. (override getItemViewType())
     * @return Returns a ViewHolder object
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_row, parent, false);
        return new ViewHolder(view, onContactClickListener);
    }

    /**
     * Displays the data at the given position.
     *
     * <p> This method displays the data from the specified position. It populates the ViewHolder's
     * card with the information for the Course located at the given position in the
     * courseList. </p>
     * @param holder The ViewHolder that contains the views where the data will be displayed
     * @param position The position in the RecyclerView where the information will be displayed,
     *                 also the position in the courseList for which Course to use
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isDelete) {
            holder.binding.courseDeleteCheckbox.setVisibility(View.VISIBLE);
        } else {
            holder.binding.courseDeleteCheckbox.setChecked(false);
            holder.binding.courseDeleteCheckbox.setVisibility(View.GONE);
        }

        Course course = courseList.get(position);
        CourseInstructor instructor = instructorMap.get(course.getCourseInstructorId());

        holder.binding.courseTitleTextview.setText(course.getTitle());

        String instructorName = "Instructor: ";
        if (instructor != null && instructor.getName() != null)
            instructorName += instructor.getName();
        holder.binding.courseInstructorTextview.setText(instructorName);

        String status = "Status: ";
        if (course.getStatus() != null)
            status += course.getStatus().toString();
        holder.binding.courseStatusTextview.setText(status);
    }

    /**
     * Gets the size of the list of Courses being displayed.
     * @return Returns the size of the list of Courses being displayed
     */
    @Override
    public int getItemCount() {
        return courseList.size();
    }

    /**
     * Sets the isDelete property.
     *
     * <p> This method tells the onBindViewHolder() method whether to display the delete Checkboxes.
     * This will allow the Courses to be selected for deletion. </p>
     * @param isDelete True if the Courses are being selected for deletion
     */
    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * Class that sets up the View for the Recycler Adapter to use.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnContactClickListener onContactClickListener;
        final CourseRowBinding binding;

        /**
         * Constructor for the ViewHolder class.
         *
         * @param itemView The view that will hold the row in the Recycler Adapter
         * @param onContactClickListener The listener that specifies what will happen when the row is clicked
         */
        public ViewHolder(@NonNull View itemView, OnContactClickListener onContactClickListener) {
            super(itemView);
            this.onContactClickListener = onContactClickListener;
            binding = CourseRowBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        /**
         * Method that passes the onClick to the onContactClickListener
         *
         * @param view The view (row) that was clicked on
         */
        @Override
        public void onClick(View view) {
            onContactClickListener.onContactClick(getAdapterPosition(), view);
        }
    }

    /**
     * Interface that specifies a listener that will tell the View in ViewHolder what to do when clicked
     */
    public interface OnContactClickListener {
        void onContactClick(int position, View view);
    }

}
