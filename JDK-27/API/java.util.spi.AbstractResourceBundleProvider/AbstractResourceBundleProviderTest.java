/*
 * @test
 * @summary java.util.spi.AbstractResourceBundleProvider conformance: white-box structure + black-box behavior.
 * @run main AbstractResourceBundleProviderTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.spi.AbstractResourceBundleProvider;
import java.util.spi.ResourceBundleProvider;

public class AbstractResourceBundleProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static <T extends Throwable> T expectThrows(Class<T> type, Runnable r, String m) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return type.cast(t); }
            throw new AssertionError(m + ": expected " + type.getName() + " but got " + t.getClass().getName());
        }
        throw new AssertionError(m + ": expected " + type.getName() + " but nothing thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.AbstractResourceBundleProvider");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()),
              "public abstract class");
        check(ResourceBundleProvider.class.isAssignableFrom(c), "implements ResourceBundleProvider");

        Constructor<?> c0 = c.getDeclaredConstructor();
        check(Modifier.isProtected(c0.getModifiers()), "no-arg ctor protected");
        Constructor<?> c1 = c.getDeclaredConstructor(String[].class);
        check(Modifier.isProtected(c1.getModifiers()), "String... ctor protected");

        Method tb = c.getDeclaredMethod("toBundleName", String.class, Locale.class);
        check(Modifier.isProtected(tb.getModifiers()) && tb.getReturnType() == String.class,
              "toBundleName protected String");
        Method gb = c.getMethod("getBundle", String.class, Locale.class);
        check(Modifier.isPublic(gb.getModifiers()) && gb.getReturnType() == ResourceBundle.class,
              "getBundle public ResourceBundle");
    }

    static class RBP extends AbstractResourceBundleProvider {
        RBP() { super(); }
        RBP(String... formats) { super(formats); }
        String toName(String b, Locale l) { return toBundleName(b, l); }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RBP rbp = new RBP();
        check("com.foo".equals(rbp.toName("com.foo", Locale.ROOT)), "toBundleName ROOT");
        check("com.foo_en_US".equals(rbp.toName("com.foo", Locale.US)), "toBundleName en_US");
        check("com.foo_fr".equals(rbp.toName("com.foo", Locale.FRENCH)), "toBundleName fr");
        check("com.foo_zh_CN".equals(rbp.toName("com.foo", Locale.SIMPLIFIED_CHINESE)),
              "toBundleName zh_CN");

        // no such bundle in the unnamed module -> null
        check(rbp.getBundle("com.foo", Locale.US) == null, "getBundle missing -> null");
        check(rbp.getBundle("com.foo", Locale.ROOT) == null, "getBundle missing ROOT -> null");

        // explicit formats constructors work
        check(new RBP("java.properties").getBundle("com.foo", Locale.US) == null,
              "ctor java.properties -> getBundle null");
        check(new RBP("java.class").getBundle("com.foo", Locale.US) == null,
              "ctor java.class -> getBundle null");

        // constructor validation
        expectThrows(IllegalArgumentException.class, () -> new RBP(new String[0]),
                     "ctor empty formats");
        expectThrows(IllegalArgumentException.class, () -> new RBP(new String[]{"bogus"}),
                     "ctor unknown format");
        expectThrows(NullPointerException.class, () -> new RBP((String[]) null),
                     "ctor null formats");

        // getBundle null-argument validation
        expectThrows(NullPointerException.class, () -> rbp.getBundle(null, Locale.US),
                     "getBundle null baseName");
        expectThrows(NullPointerException.class, () -> rbp.getBundle("com.foo", null),
                     "getBundle null locale");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
