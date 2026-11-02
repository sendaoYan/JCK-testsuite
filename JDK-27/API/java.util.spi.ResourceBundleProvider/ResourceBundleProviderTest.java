/*
 * @test
 * @summary java.util.spi.ResourceBundleProvider conformance: functional interface getBundle.
 * @run main ResourceBundleProviderTest
 */
import java.util.Locale;
import java.util.spi.ResourceBundleProvider;

public class ResourceBundleProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ResourceBundleProvider.class.isInterface(), "is interface");
        ResourceBundleProvider p = (b, l) -> null;
        check(p.getBundle("x", Locale.US) == null, "getBundle null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
