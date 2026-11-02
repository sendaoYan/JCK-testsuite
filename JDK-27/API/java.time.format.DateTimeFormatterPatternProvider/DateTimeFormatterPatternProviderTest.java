/*
 * @test
 * @summary java.time.format.DateTimeFormatterPatternProvider conformance: abstract SPI class reflection.
 * @run main DateTimeFormatterPatternProviderTest
 */
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.format.DateTimeFormatterPatternProvider;
import java.util.spi.LocaleServiceProvider;

public class DateTimeFormatterPatternProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Modifier.isAbstract(DateTimeFormatterPatternProvider.class.getModifiers()), "abstract");
        check(LocaleServiceProvider.class.isAssignableFrom(DateTimeFormatterPatternProvider.class), "extends LocaleServiceProvider");
        boolean has = false;
        for (Method m : DateTimeFormatterPatternProvider.class.getDeclaredMethods()) if (m.getName().equals("getDateTimeFormatterPattern")) has = true;
        check(has, "getDateTimeFormatterPattern present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
