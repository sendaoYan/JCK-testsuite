/*
 * @test
 * @summary java.lang.Module conformance: java.base module inspection.
 * @run main ModuleTest
 */
import java.lang.Module;

public class ModuleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Module m = String.class.getModule();
        check(m.isNamed(), "isNamed");
        check("java.base".equals(m.getName()), "getName java.base");
        check(m.isExported("java.lang"), "isExported java.lang");
        check(m.isExported("java.lang", ModuleTest.class.getModule()), "isExported to unnamed");
        check(m.getDescriptor() != null, "descriptor non-null");
        check(m.getPackages().contains("java.lang"), "packages contains java.lang");
        check(m.getLayer() != null, "getLayer non-null");
        check(m.canRead(m), "canRead self");
        check(m.getResourceAsStream("java/lang/String.class") != null, "resourceAsStream");
        check(m.toString().contains("java.base"), "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
