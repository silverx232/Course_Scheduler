package wgu.c196.rachel.coursescheduler.database;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

import wgu.c196.rachel.coursescheduler.model.Assessment;
import wgu.c196.rachel.coursescheduler.model.Course;

/**
 * Converts data types for the SQLite database.
 *
 * <p> SQLite can only store null, integer, real, text, and blob in the database. This class is used
 * to convert between complex objects and these data types.</p>
 */
public class DatabaseConverter {

    @TypeConverter
    public static LocalDate fromTimestamp(Long timestamp) {
        return timestamp == null ? null : LocalDate.ofEpochDay(timestamp);
    }

    @TypeConverter
    public static Long dateToTimestamp(LocalDate date) {
        return date == null ? null : date.getLong(ChronoField.EPOCH_DAY);
    }

    @TypeConverter
    public static String dateTimeToString(LocalDateTime date) {
        return date == null ? null : date.toString();
    }

    @TypeConverter
    public static LocalDateTime stringToDateTime(String dateString) {
        return dateString == null ? null : LocalDateTime.parse(dateString);
    }

    @TypeConverter
    public static String fromStatus(Course.Status status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static Course.Status toStatus(String name) {
        return name == null ? null : Course.Status.valueOf(name);
    }

    @TypeConverter
    public static String fromType(Assessment.Type type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static Assessment.Type toType(String name) {
        return name == null ? null : Assessment.Type.valueOf(name);
    }
}
