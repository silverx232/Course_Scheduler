package wgu.c196.rachel.coursescheduler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.databinding.TermRowBinding;
import wgu.c196.rachel.coursescheduler.model.CourseInstructor;

/**
 * Recycler Adapter for the CourseInstructor class.
 *
 * <p> Sets up the cards for the RecyclerView list of course instructors. Contains the public class
 * ViewHolder and the interface OnContactClickListener. </p>
 */
public class InstructorRecyclerAdapter extends RecyclerView.Adapter<InstructorRecyclerAdapter.ViewHolder> {
    private List<CourseInstructor> instructorList;
    private OnContactClickListener onContactClickListener;
    private boolean isDelete = false;

    /**
     * Constructor for InstructorRecyclerAdapter.
     *
     * @param instructorList The list of Course Instructors that the RecyclerView will use
     * @param onContactClickListener A listener that specifies what will happen when the card is clicked in the RecyclerView
     */
    public InstructorRecyclerAdapter(List<CourseInstructor> instructorList, OnContactClickListener onContactClickListener) {
        this.instructorList = instructorList;
        this.onContactClickListener = onContactClickListener;
    }

    /**
     * Sets the isDelete property.
     *
     * <p> This method tells the onBindViewHolder() method whether to display the delete Checkboxes.
     * This will allow the Course Instructors to be selected for deletion. </p>
     * @param isDelete True if the Course Instructors are being selected for deletion
     */
    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
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
                .inflate(R.layout.term_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Displays the data at the given position.
     *
     * <p> This method displays the data from the specified position. It populates the ViewHolder's
     * card with the information for the Course Instructor located at the given position in the
     * instructorList. </p>
     * @param holder The ViewHolder that contains the views where the data will be displayed
     * @param position The position in the RecyclerView where the information will be displayed,
     *                 also the position in the instructorList for which Course Instructor to use
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isDelete) {
            holder.binding.deleteCheckbox.setVisibility(View.VISIBLE);
        } else {
            holder.binding.deleteCheckbox.setChecked(false);
            holder.binding.deleteCheckbox.setVisibility(View.GONE);
        }

        CourseInstructor instructor = instructorList.get(position);
        holder.binding.titleTextview.setText(instructor.getName());
        holder.binding.startDateTextview.setText("Phone Number: " + instructor.getPhoneNumber());
        holder.binding.endDateTextview.setText("Email: " + instructor.getEmail());
    }

    /**
     * Gets the size of the list of Course Instructors being displayed.
     *
     * @return Returns the size of the list of Course Instructors being displayed
     */
    @Override
    public int getItemCount() {
        return instructorList.size();
    }

    /**
     * Class that sets up the View for the Recycler Adapter to use.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TermRowBinding binding;

        /**
         * Constructor for the ViewHolder class.
         *
         * @param itemView The view that will hold the row in the Recycler Adapter
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = TermRowBinding.bind(itemView);
            itemView.setOnClickListener(this);
        }

        /**
         * Method that passes the onClick to the onContactClickListener.
         *
         * @param view The view (row) that was clicked on
         */
        @Override
        public void onClick(View view) {
            onContactClickListener.onContactClick(getAdapterPosition(), view);
        }
    }

    /**
     * Interface that specifies a listener that will tell the View in ViewHolder what to do when clicked.
     */
    public interface OnContactClickListener {
        void onContactClick(int position, View view);
    }
}
