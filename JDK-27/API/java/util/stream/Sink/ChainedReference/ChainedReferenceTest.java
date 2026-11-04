/*
 * @test
 * @summary java.util.stream.Sink.ChainedReference conformance: package-private SPI (reflection) + accessibility.
 * @run main ChainedReferenceTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ChainedReferenceTest {
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
        Class<?> c = Class.forName("java.util.stream.Sink$ChainedReference");
        check(!c.isInterface(), "is a class");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() == Object.class, "direct superclass is Object");
        check(Arrays.equals(interfaceNames(c), new String[]{"java.util.stream.Sink"}),
              "implements expected sink interface");
        check(hasMethod(c, "begin", 1), "begin(long) declared");
        check(hasMethod(c, "end", 0), "end() declared");
        check(hasMethod(c, "cancellationRequested", 0), "cancellationRequested() declared");

        boolean hasField = false;
        for (Field f : c.getDeclaredFields())
            if (f.getName().equals("downstream") && f.getType().getName().equals("java.util.stream.Sink"))
                hasField = true;
        check(hasField, "downstream field of type Sink declared");

        boolean hasCtor = false;
        for (Constructor<?> ct : c.getDeclaredConstructors())
            if (ct.getParameterCount() == 1
                    && ct.getParameterTypes()[0].getName().equals("java.util.stream.Sink")) hasCtor = true;
        check(hasCtor, "constructor taking a downstream Sink declared");
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
