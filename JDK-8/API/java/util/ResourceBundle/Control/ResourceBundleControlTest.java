/*
 * @test
 * @summary java.util.ResourceBundle.Control conformance: formats/candidates/bundle-name/ttl.
 * @run main ResourceBundleControlTest
 */

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleControlTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testControl() {
        ResourceBundle.Control c = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
        check(c != null, "getControl");
        List<String> formats = c.getFormats("x");
        check(formats.contains("java.class") && formats.contains("java.properties"), "getFormats");
        List<Locale> candidates = c.getCandidateLocales("base", Locale.US);
        check(!candidates.isEmpty() && candidates.get(0).equals(Locale.US),
              "getCandidateLocales starts with requested");
        check("base_en_US".equals(c.toBundleName("base", Locale.US)), "toBundleName");
        check(c.getTimeToLive("base", Locale.US) == ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL,
              "getTimeToLive default");
        check(ResourceBundle.Control.FORMAT_CLASS != ResourceBundle.Control.FORMAT_PROPERTIES,
              "format constants");
        check(ResourceBundle.Control.TTL_DONT_CACHE == -1, "TTL_DONT_CACHE");
        check(ResourceBundle.Control.TTL_NO_EXPIRATION_CONTROL == -2, "TTL_NO_EXPIRATION_CONTROL");
        ResourceBundle.Control noFallback =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);
        check(noFallback != null, "getNoFallbackControl");
    }

    public static void main(String[] args) {
        testControl();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
