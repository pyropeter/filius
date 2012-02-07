package filius.software.rip;

import java.util.Calendar;

public class RIPUtil {
	public RIPUtil() {
	}

	public static long getTime() {
		return Calendar.getInstance().getTimeInMillis();
	}
}

