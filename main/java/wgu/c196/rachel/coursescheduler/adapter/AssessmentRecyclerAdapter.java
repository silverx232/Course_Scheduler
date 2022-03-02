package wgu.c196.rachel.coursescheduler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import wgu.c196.rachel.coursescheduler.R;
import wgu.c196.rachel.coursescheduler.databinding.TermRowBinding;
import wgu.c196.rachel.coursescheduler.model.Assessment;

/**
 * Recycler Adapter for the Assessment class.
 *
 * <p> Sets up the cards for the RecyclerView list of Assessments. Contains the public class
 * ViewHolder and the interface OnContactClickListener. </p>
 */
public class AssessmentRecyclerAdapter extends RecyclerView.Adapter<AssessmentRecyclerAdapter.ViewHolder> {
    private List<Assessment> assessmentList;
    private OnContactClickListener onContactClickListener;
    private boolean isDelete = false;

    /**
     * Constructor for AssessmentRecyclerAdapter.
     *
     * @param assessmentList The list of Assessments that the RecyclerView will use
     * @param onContactClickListener A listener that specifies what will happen when the card is clicked in the RecyclerView
     */
    public AssessmentRecyclerAdapter(List<Assessment> assessmentList, OnContactClickListener onContactClickListener) {
        this.assessmentList = assessmentList;
        this.onContactClickListener = onContactClickListener;
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
        return new ViewHolder(view, onContactClickListener);
    }

    /**
     * Displays the data at the given position.
     *
     * <p> This method displays the data from the specified position. It populates the ViewHolder's
     * card with the information for the Assessment located at the given position in the
     * assessmentList. </p>
     * @param holder The ViewHolder that contains the views where the data will be displayed
     * @param position The position in the RecyclerView where the information will be displayed,
     *                 also the position in the assessmentList for which Assessment to use
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isDelete) {
            holder.binding.deleteCheckbox.setVisibility(View.VISIBLE);
        } else {
            holder.binding.deleteCheckbox.setChecked(false);
            holder.binding.deleteCheckbox.setVisibility(View.GONE);
        }

        Assessment assessment = assessmentList.get(position);
        holder.binding.titleTextview.setText(assessment.getTitle());

        String startDate = "Start Date: ";
        if (assessment.getStartDate() != null)
            startDate += assessment.getStartDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        holder.binding.startDateTextview.setText(startDate);

        String endDate = "End Date: ";
        if (assessment.getEndDate() != null)
            endDate += assessment.getEndDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        holder.binding.endDateTextview.setText(endDate);
    }

    /**
     * Gets the size of the list of Assessments being displayed.
     * @return Returns the size of the list of Assessments being displayed
     */
    @Override
    public int getItemCount() {
        return assessmentList.size();
    }

    /**
     * Sets the isDelete property.
     *
     * <p> This method tells the onBindViewHolder() method whether to display the delete Checkboxes.
     * This will allow the Assessments to be selected for deletion. </p>
     * @param isDelete True if the Assessments are being selected for deletion
     */
    public void setDelete(boolean isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * Class that sets up the View for the Recycler Adapter to use.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private OnContactClickListener onContactClickListener;

        // Uses the term_row layout
        final TermRowBinding binding;

        /**
         * Constructor for the ViewHolder class.
         *
         * @param itemView The view that will hold the row in the Recycler Adapter
         * @param onContactClickListener The listener that specifies what will happen when the row is clicked
         */
        public ViewHolder(@NonNull View itemView, OnContactClickListener onContactClickListener) {
            super(itemView);
            this.onContactClickListener = onContactClickListener;
            binding = TermRowBinding.bind(itemView);
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
