/*
 * @test
 * @summary java.util.stream.Node.Builder.OfDouble conformance: package-private SPI (reflection) + accessibility.
 * @run main OfDoubleTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class OfDoubleTest {
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
        Class<?> c = Class.forName("java.util.stream.Node$Builder$OfDouble");
        check(c.isInterface(), "is an interface");
        check(Arrays.equals(interfaceNames(c), new String[]{"java.util.stream.Node$Builder", "java.util.stream.Sink$OfDouble"}),
              "extends expected superinterfaces");
        check(hasMethod(c, "build", 0), "build(0-arg) declared");
        check(Arrays.stream(c.getMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 1),
                "accept(1-arg) inherited from Sink.OfDouble");
    }

    static void testBlackBox() throws Exception {
        // The nested SPI type is declared public, but its enclosing type is
        // package-private, so the SPI is unreachable from outside java.util.stream.
        Class<?> enclosing = Class.forName("java.util.stream.Node");
        check(!Modifier.isPublic(enclosing.getModifiers()),
              "enclosing type is package-private (SPI unreachable outside package)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
