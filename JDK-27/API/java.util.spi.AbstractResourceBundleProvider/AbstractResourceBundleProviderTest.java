/*
 * @test
 * @summary java.util.spi.AbstractResourceBundleProvider conformance: abstract class and toBundleName.
 * @run main AbstractResourceBundleProviderTest
 */
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.spi.AbstractResourceBundleProvider;

public class AbstractResourceBundleProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class RBP extends AbstractResourceBundleProvider {
        String toName(String b, Locale l) { return toBundleName(b, l); }
    }
    public static void main(String[] args) {
        check(Modifier.isAbstract(AbstractResourceBundleProvider.class.getModifiers()), "abstract");
        RBP rbp = new RBP();
        check("com.foo".equals(rbp.toName("com.foo", Locale.ROOT)), "toBundleName ROOT");
        check("com.foo_en_US".equals(rbp.toName("com.foo", Locale.US)), "toBundleName US");
        check("com.foo_fr".equals(rbp.toName("com.foo", Locale.FRENCH)), "toBundleName fr");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
