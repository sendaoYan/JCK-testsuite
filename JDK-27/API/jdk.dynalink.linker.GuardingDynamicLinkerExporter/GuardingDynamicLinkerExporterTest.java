/*
 * @test
 * @summary jdk.dynalink.linker.GuardingDynamicLinkerExporter conformance: abstract exporter class.
 * @run main GuardingDynamicLinkerExporterTest
 */

import java.lang.reflect.*;

public class GuardingDynamicLinkerExporterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.GuardingDynamicLinkerExporter");
        check(!c.isInterface() && java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(java.util.function.Supplier.class.isAssignableFrom(c), "implements Supplier");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
