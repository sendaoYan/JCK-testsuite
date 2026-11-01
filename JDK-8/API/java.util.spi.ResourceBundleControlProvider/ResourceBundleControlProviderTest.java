/*
 * @test
 * @summary java.util.spi.ResourceBundleControlProvider conformance: getControl.
 * @run main ResourceBundleControlProviderTest
 */

import java.util.ResourceBundle;
import java.util.spi.ResourceBundleControlProvider;

public class ResourceBundleControlProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testResourceBundleControlProvider() {
        ResourceBundleControlProvider p = new ResourceBundleControlProvider() {
            public ResourceBundle.Control getControl(String baseName) {
                return ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
            }
        };
        check(p.getControl("foo") != null, "getControl");
    }

    public static void main(String[] args) {
        testResourceBundleControlProvider();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
