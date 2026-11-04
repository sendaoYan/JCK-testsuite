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


    // ===================== 契约深化：函数式接口（lambda 实现）+ 直接调用（JDK-27 侧锁定） =====================
    static void testContracts() {
        ResourceBundleProvider p = (baseName, locale) -> null;
        check(p.getBundle("mypkg.Messages", Locale.US) == null, "getBundle lambda behavior (null for absent module bundles)");
        // 接口面：getBundle(String, Locale) 单抽象方法
        // 接口面：单抽象方法
        long abstractCount = java.util.Arrays.stream(ResourceBundleProvider.class.getMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers())).count();
        check(abstractCount == 1, "exactly one abstract method (functional interface)");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
