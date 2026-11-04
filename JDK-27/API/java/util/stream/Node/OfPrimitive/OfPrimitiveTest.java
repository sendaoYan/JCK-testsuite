/*
 * @test
 * @summary java.util.stream.Node.OfPrimitive conformance: package-private SPI (reflection) + accessibility.
 * @run main OfPrimitiveTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class OfPrimitiveTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$OfPrimitive");
        check(c.isInterface(), "is an interface");
        check(Arrays.equals(interfaceNames(c), new String[]{"java.util.stream.Node"}),
              "extends expected superinterfaces");
        check(hasMethod(c, "spliterator", 0), "spliterator(0-arg) declared");
        check(hasMethod(c, "forEach", 1), "forEach(1-arg) declared");
        check(hasMethod(c, "getChild", 1), "getChild(1-arg) declared");
        check(hasMethod(c, "truncate", 3), "truncate(3-arg) declared");
        check(hasMethod(c, "asArray", 1), "asArray(1-arg) declared");
        check(hasMethod(c, "asPrimitiveArray", 0), "asPrimitiveArray(0-arg) declared");
        check(hasMethod(c, "newArray", 1), "newArray(1-arg) declared");
        check(hasMethod(c, "copyInto", 2), "copyInto(2-arg) declared");
    }

    static void testBlackBox() throws Exception {
        // The nested SPI type is declared public, but its enclosing type is
        // package-private, so the SPI is unreachable from outside java.util.stream.
        Class<?> enclosing = Class.forName("java.util.stream.Node");
        check(!Modifier.isPublic(enclosing.getModifiers()),
              "enclosing type is package-private (SPI unreachable outside package)");
    }


    // ===================== 契约深化：Node 契约面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$OfPrimitive");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.containsAll(java.util.Arrays.asList("asArray", "spliterator", "count", "forEach",
              "copyInto", "getChild", "getChildCount", "getShape", "truncate")),
              "Node full contract method set present");
        check(java.util.Arrays.stream(c.getMethods())
              .filter(m -> m.getName().equals("spliterator")).count() >= 1,
              "spliterator exposed");
        // Node.Builder 继承 Sink
        Class<?> builder = Class.forName("java.util.stream.Node$Builder");
        check(builder.isInterface(), "Node.Builder is interface");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : builder.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.util.stream.Sink"), "Node.Builder extends Sink");
        check(java.util.Arrays.stream(builder.getMethods()).anyMatch(m -> m.getName().equals("build")),
              "Node.Builder has build()");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
