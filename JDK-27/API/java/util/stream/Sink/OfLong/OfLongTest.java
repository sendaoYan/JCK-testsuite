/*
 * @test
 * @summary java.util.stream.Sink.OfLong conformance: package-private SPI (reflection) + accessibility.
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

    static int countMethods(Class<?> c, String name) {
        int n = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) n++;
        return n;
    }

    static boolean hasAcceptPrimitive(Class<?> c, String prim) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("accept") && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].getName().equals(prim)) return true;
        return false;
    }

    static String[] interfaceNames(Class<?> c) {
        return Arrays.stream(c.getInterfaces()).map(Class::getName).sorted().toArray(String[]::new);
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Sink$OfLong");
        check(c.isInterface(), "is an interface");
        check(Arrays.equals(interfaceNames(c),
              new String[]{"java.util.function.LongConsumer", "java.util.stream.Sink"}),
              "extends expected superinterfaces");
        check(countMethods(c, "accept") == 3, "accept overloaded 3 ways");
        check(hasAcceptPrimitive(c, "long"), "accept(long) declared");
    }

    static void testBlackBox() throws Exception {
        // The nested SPI type is declared public, but its enclosing type is
        // package-private, so the SPI is unreachable from outside java.util.stream.
        Class<?> enclosing = Class.forName("java.util.stream.Sink");
        check(!Modifier.isPublic(enclosing.getModifiers()),
              "enclosing type is package-private (SPI unreachable outside package)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
