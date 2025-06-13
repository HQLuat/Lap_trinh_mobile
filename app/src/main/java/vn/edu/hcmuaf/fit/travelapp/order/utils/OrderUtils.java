package vn.edu.hcmuaf.fit.travelapp.order.utils;

import android.os.Parcel;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderUtils {

    // === Date Utilities ===
    public static class DateUtils {
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public static String formatDate(Date date) {
            return date != null ? DATE_FORMAT.format(date) : "";
        }

        public static String formatDateTime(Date date) {
            return date != null ? DATETIME_FORMAT.format(date) : "";
        }
    }

    // === Parcel & Timestamp Utilities ===
    public static class ParcelUtils {
        public static void writeTimestamp(Parcel dest, Timestamp ts) {
            dest.writeLong(ts != null ? ts.toDate().getTime() : -1);
        }

        public static Timestamp readTimestamp(Parcel in) {
            long time = in.readLong();
            return time != -1 ? new Timestamp(new Date(time)) : null;
        }
    }

    // === Enum Utilities ===
    public static class EnumUtils {
        public static <T extends Enum<T>> T safeValueOf(Class<T> enumType, String value) {
            try {
                return Enum.valueOf(enumType, value);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
