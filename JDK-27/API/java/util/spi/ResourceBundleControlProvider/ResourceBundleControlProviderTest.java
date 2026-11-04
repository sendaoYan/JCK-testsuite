/*
 * @test
 * @summary java.util.spi.ResourceBundleControlProvider conformance: white-box structure + black-box behavior.
 * @run main ResourceBundleControlProviderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.spi.ResourceBundleControlProvider;

public class ResourceBundleControlProviderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.spi.ResourceBundleControlProvider");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "public interface");
        Method gc = c.getMethod("getControl", String.class);
        check(Modifier.isAbstract(gc.getModifiers()), "getControl abstract");
        check(gc.getReturnType() == ResourceBundle.Control.class, "getControl returns Control");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final ResourceBundle.Control control =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
        ResourceBundleControlProvider p = new ResourceBundleControlProvider() {
            public ResourceBundle.Control getControl(String baseName) {
                return "foo".equals(baseName) ? control : null;
            }
        };

        check(p.getControl("foo") == control, "getControl(foo) same reference");
        check(p.getControl("foo") != null, "getControl(foo) non-null");
        check(p.getControl("bar") == null, "getControl(bar) null");
        check(p.getControl("foo").getFormats("any").equals(ResourceBundle.Control.FORMAT_DEFAULT),
              "returned control is a real Control");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
