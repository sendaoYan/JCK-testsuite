import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.zone.ZoneOffsetTransitionRule;
import java.util.Locale;
import java.util.TimeZone;

public class ZoneOffsetTransitionRuleProbe {
    interface Fn { void run() throws Exception; }
    static void emit(String k, Object v) { System.out.println(k + "=" + v); }
    static void ex(String k, Fn f) {
        try { f.run(); }
        catch (Throwable t) { System.out.println(k + "=" + t.getClass().getName() + "|msg=" + t.getMessage()); }
    }
    public static void main(String[] a) {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        try {
            // null timeDefinition: JDK-8 NPE message has the typo "timeDefnition";
            // JDK-27+ fixed it to "timeDefinition".
            ex("of.nullTimeDef", () -> emit("of.nullTimeDef",
                    ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY,
                            LocalTime.MIDNIGHT, false, null,
                            ZoneOffset.UTC, ZoneOffset.UTC, ZoneOffset.ofHours(1))));
            // Valid construction is consistent on both (control).
            emit("of.valid", ZoneOffsetTransitionRule.of(Month.MARCH, 20, DayOfWeek.SUNDAY,
                    LocalTime.MIDNIGHT, false, ZoneOffsetTransitionRule.TimeDefinition.WALL,
                    ZoneOffset.UTC, ZoneOffset.UTC, ZoneOffset.ofHours(1)).toString());
        } catch (Throwable t) { System.out.println("fatal=exception=" + t.getClass().getName()); }
    }
}
