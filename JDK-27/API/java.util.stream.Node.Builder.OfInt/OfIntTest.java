/*
 * @test
 * @summary java.util.stream.Node.Builder.OfInt conformance: package-private SPI interface hierarchy and declared methods (reflection).
 * @run main OfIntTest
 */

import java.lang.reflect.Method;
import java.util.Arrays;

public class OfIntTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, int paramCount) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) return true;
        }
        return false;
    }

    static int countMethods(Class<?> c, String name) {
        int n = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) n++;
        return n;
    }

    static boolean hasAcceptPrimitive(Class<?> c, String prim) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("accept") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].getName().equals(prim)) return true;
        }
        return false;
    }

    static String[] interfaceNames(Class<?> c) {
        return Arrays.stream(c.getInterfaces()).map(Class::getName).sorted().toArray(String[]::new);
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$Builder$OfInt");
        check(c.isInterface(), "is an interface");
        check(Arrays.equals(interfaceNames(c), new String[]{"java.util.stream.Node$Builder", "java.util.stream.Sink$OfInt"}),
              "extends expected superinterfaces");
        check(hasMethod(c, "build", 0), "build(0-arg) declared");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
