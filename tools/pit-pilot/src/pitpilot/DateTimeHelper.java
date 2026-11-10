package pitpilot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 测试辅助「等价逻辑」：java.time 日期时间不变量校验器。
 * 全部使用固定日期，避免墙钟/时区带来的不确定性。
 */
public final class DateTimeHelper {

    private DateTimeHelper() {
    }

    /** 判断 date 是否为闰年（JDK LocalDate.isLeapYear 不变量）。 */
    public static boolean isLeapYear(LocalDate date) {
        return date.isLeapYear();
    }

    /** date 加 months 月后再减 months 月，是否回到原值（月末钳制时此不变量会破）。 */
    public static boolean roundTripsMonth(LocalDate date, long months) {
        return date.plusMonths(months).minusMonths(months).equals(date);
    }

    /** 判断 a 到 b 的整天数是否等于 expectedDays（ChronoUnit.DAYS.between）。 */
    public static boolean daysBetweenEquals(LocalDate a, LocalDate b, long expectedDays) {
        return ChronoUnit.DAYS.between(a, b) == expectedDays;
    }

    /** 判断 day 是否为周末。 */
    public static boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
