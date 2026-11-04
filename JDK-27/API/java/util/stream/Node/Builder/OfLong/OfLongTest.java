/*
 * @test
 * @summary java.util.stream.Node.Builder.OfLong conformance: package-private SPI (reflection) + accessibility.
 * @run main OfLongTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class OfLongTest {
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
        Class<?> c = Class.forName("java.util.stream.Node$Builder$OfLong");
        check(c.isInterface(), "is an interface");
        check(Arrays.equals(interfaceNames(c), new String[]{"java.util.stream.Node$Builder", "java.util.stream.Sink$OfLong"}),
              "extends expected superinterfaces");
        check(hasMethod(c, "build", 0), "build(0-arg) declared");
        check(Arrays.stream(c.getMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 1),
                "accept(1-arg) inherited from Sink.OfLong");
    }

    static void testBlackBox() throws Exception {
        // The nested SPI type is declared public, but its enclosing type is
        // package-private, so the SPI is unreachable from outside java.util.stream.
        Class<?> enclosing = Class.forName("java.util.stream.Node");
        check(!Modifier.isPublic(enclosing.getModifiers()),
              "enclosing type is package-private (SPI unreachable outside package)");
    }


    // ===================== 契约深化：Node.Builder 原语特化面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$Builder$OfLong");
        check(c.isInterface(), "is interface");
        // accept(原语) 重载存在
        boolean primAccept = false;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals("accept") && m.getParameterCount() == 1
                && m.getParameterTypes()[0] == long.class) primAccept = true;
        }
        check(primAccept, "accept(class) primitive overload present");
        check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals("build")),
              "build() present");
        boolean extendsSink = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().startsWith("java.util.stream.Sink")) extendsSink = true;
        check(extendsSink, "extends a Sink specialization");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
