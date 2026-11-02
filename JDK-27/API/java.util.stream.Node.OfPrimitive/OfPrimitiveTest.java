/*
 * @test
 * @summary java.util.stream.Node.OfPrimitive conformance: package-private SPI interface hierarchy and declared methods (reflection).
 * @run main OfPrimitiveTest
 */

import java.lang.reflect.Method;
import java.util.Arrays;

public class OfPrimitiveTest {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
