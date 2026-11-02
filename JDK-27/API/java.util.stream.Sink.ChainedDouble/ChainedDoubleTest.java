/*
 * @test
 * @summary java.util.stream.Sink.ChainedDouble conformance: package-private SPI chained sink class hierarchy and members (reflection).
 * @run main ChainedDoubleTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ChainedDoubleTest {
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

    static String[] interfaceNames(Class<?> c) {
        return Arrays.stream(c.getInterfaces()).map(Class::getName).sorted().toArray(String[]::new);
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.util.stream.Sink$ChainedDouble");
        check(!c.isInterface(), "is a class");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() == Object.class, "direct superclass is Object");
        check(Arrays.equals(interfaceNames(c), new String[]{"java.util.stream.Sink$OfDouble"}),
              "implements expected sink interface");
        check(hasMethod(c, "begin", 1), "begin(long) declared");
        check(hasMethod(c, "end", 0), "end() declared");
        check(hasMethod(c, "cancellationRequested", 0), "cancellationRequested() declared");

        boolean hasField = false;
        for (Field f : c.getDeclaredFields()) {
            if (f.getName().equals("downstream")
                    && f.getType().getName().equals("java.util.stream.Sink")) hasField = true;
        }
        check(hasField, "downstream field of type Sink declared");

        boolean hasCtor = false;
        for (Constructor<?> ct : c.getDeclaredConstructors()) {
            if (ct.getParameterCount() == 1
                    && ct.getParameterTypes()[0].getName().equals("java.util.stream.Sink")) hasCtor = true;
        }
        check(hasCtor, "constructor taking a downstream Sink declared");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
