/*
 * @test
 * @summary java.util.stream.Node.Builder conformance: package-private SPI (reflection) + accessibility.
 * @run main BuilderTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class BuilderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, int paramCount) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) return true;
        return false;
    }

    static String[] interfaceNames(Class<?> c) {
        return Arrays.stream(c.getInterfaces()).map(Class::getName).sorted().toArray(String[]::new);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$Builder");
        check(c.isInterface(), "is an interface");
        check(Arrays.equals(interfaceNames(c), new String[]{"java.util.stream.Sink"}),
              "extends expected superinterfaces");
        check(hasMethod(c, "build", 0), "build(0-arg) declared");
        check(Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("build") && m.getReturnType().getName().equals("java.util.stream.Node")),
                "build() returns Node");
    }

    // ---- Part 2: black-box (accessibility contract) ----
    static void testBlackBox() throws Exception {
        // The nested SPI type is declared public, but its enclosing type is
        // package-private, so the SPI is unreachable from outside java.util.stream.
        Class<?> enclosing = Class.forName("java.util.stream.Node");
        check(!Modifier.isPublic(enclosing.getModifiers()),
              "enclosing type is package-private (SPI unreachable outside package)");
    }


    // ===================== 契约深化：Node.Builder 引用形态契约面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$Builder");
        check(c.isInterface(), "Node.Builder is interface");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.util.stream.Sink"), "Node.Builder extends Sink");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("build"), "build() present");
        check(names.contains("begin") && names.contains("end") && names.contains("cancellationRequested"),
              "Sink lifecycle methods inherited");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
