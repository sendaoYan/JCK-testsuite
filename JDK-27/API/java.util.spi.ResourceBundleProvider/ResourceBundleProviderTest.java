/*
 * @test
 * @summary java.util.spi.ResourceBundleProvider conformance: white-box structure + black-box behavior.
 * @run main ResourceBundleProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.spi.ResourceBundleProvider;

public class ResourceBundleProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.ResourceBundleProvider");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public interface");
        Method gb = c.getMethod("getBundle", String.class, Locale.class);
        check(Modifier.isAbstract(gb.getModifiers()) && gb.getReturnType() == ResourceBundle.class,
              "getBundle abstract ResourceBundle");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final ResourceBundle rb = new ListResourceBundle() {
            protected Object[][] getContents() {
                return new Object[][]{{"k", "v"}, {"n", Integer.valueOf(42)}};
            }
        };
        ResourceBundleProvider p = (b, l) -> "foo".equals(b) ? rb : null;

        ResourceBundle got = p.getBundle("foo", Locale.US);
        check(got != null, "getBundle(foo) non-null");
        check("v".equals(got.getString("k")), "bundle string value");
        check(((Integer) got.getObject("n")).intValue() == 42, "bundle integer value");
        check(got.containsKey("k") && got.containsKey("n"), "bundle keys present");
        check(p.getBundle("bar", Locale.US) == null, "getBundle(bar) null");
        check(p.getBundle("foo", Locale.FRANCE) == rb, "getBundle(foo, FR) same reference");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
