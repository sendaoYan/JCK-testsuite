/*
 * @test
 * @summary java.lang.Module conformance: white-box structure + black-box behavior.
 * @run main ModuleTest
 */

import java.lang.Module;
import java.lang.reflect.Modifier;

public class ModuleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Module");
        check(Modifier.isFinal(c.getModifiers()), "Module is final");
        check(!c.isInterface(), "Module is a class");
        check(java.lang.reflect.AnnotatedElement.class.isAssignableFrom(c),
              "Module implements AnnotatedElement");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testJavaBase() throws Exception {
        Module m = String.class.getModule();
        check(m.isNamed(), "isNamed");
        check("java.base".equals(m.getName()), "getName java.base");
        check(m.isExported("java.lang"), "isExported java.lang (unqualified)");
        check(m.isExported("java.lang", ModuleTest.class.getModule()), "isExported to unnamed");
        check(!m.isOpen("java.lang"), "java.lang not open (no open directive in java.base)");
        check(m.getDescriptor() != null, "descriptor non-null");
        check(m.getPackages().contains("java.lang"), "packages contains java.lang");
        check(m.getLayer() != null, "getLayer non-null");
        check(m.canRead(m), "canRead self");
        check(m.getResourceAsStream("java/lang/String.class") != null, "resourceAsStream");
        check(m.toString().contains("java.base"), "toString");
        check(m.getAnnotation(Deprecated.class) == null, "no Deprecated annotation");
        check(m.getDeclaredAnnotations().length == 0, "no declared annotations");
        check(m.getClassLoader() == null, "bootstrap module loader is null");
        try {
            m.addReads(m);
            throw new AssertionError("addReads on java.base from unnamed caller: expected IllegalCallerException");
        } catch (IllegalCallerException expected) {
            passed++;
        }
        m.isNativeAccessEnabled(); // callable
        passed++;
    }

    static void testUnnamedModule() {
        Module unnamed = ModuleTest.class.getModule();
        Module m = String.class.getModule();
        check(!unnamed.isNamed(), "test class module is unnamed");
        check(unnamed.getName() == null, "unnamed module name null");
        check(unnamed.getClassLoader() != null, "unnamed module class loader non-null");
        check(unnamed.canRead(String.class.getModule()), "unnamed reads java.base");
        check(unnamed.isExported("com.example.x") && unnamed.isOpen("com.example.x"),
              "unnamed module exports/opens every package");
        check(unnamed.addReads(m) == unnamed, "unnamed addReads is a no-op returning this");
        check(unnamed.addExports("p.q", m) == unnamed, "unnamed addExports is a no-op returning this");
        check(unnamed.addOpens("p.q", m) == unnamed, "unnamed addOpens is a no-op returning this");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testJavaBase();
        testUnnamedModule();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
