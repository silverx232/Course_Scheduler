package wgu.c196.rachel.coursescheduler.util;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import wgu.c196.rachel.coursescheduler.R;

/**
 * Creates the channels needed for notifications.
 *
 * <p> This class is noted in the Application tag of the manifest so that it can be accessed. </p>
 */
public class AlertChannels extends Application {
    /**
     * Tag used for naming the course channel
     */
    public static final String COURSE = "course";
    /**
     * Tag used for naming the assessment channel
     */
    public static final String ASSESSMENT = "assessment";

    /**
     * Creates the channels for notifications.
     *
     * <p> Creates one channel for courses and one for assessments. </p>
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // Check if user OS is O or higher, because notification channels do not work before then
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel courseChannel = new NotificationChannel(COURSE, "Course Alerts",
                NotificationManager.IMPORTANCE_LOW);
        courseChannel.setDescription(getString(R.string.course_channel_description));

        NotificationChannel assessmentChannel = new NotificationChannel(ASSESSMENT,
                "Assessment Alerts", NotificationManager.IMPORTANCE_DEFAULT);
        assessmentChannel.setDescription(getString(R.string.assessment_channel_description));

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(courseChannel);
        manager.createNotificationChannel(assessmentChannel);
    }
}
