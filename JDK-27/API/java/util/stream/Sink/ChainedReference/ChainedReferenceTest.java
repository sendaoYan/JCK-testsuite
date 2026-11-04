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


    // ===================== 契约深化：ChainedReference 链式 Sink 契约面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Sink$ChainedReference");
        check(!c.isInterface(), "ChainedReference is an abstract class (not interface)");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        // 构造语义：单个构造器接收被包裹的下游 Sink
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single declared constructor (wraps downstream)");
        // 生命周期方法委托下游
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("begin") && names.contains("end") && names.contains("cancellationRequested"),
              "begin/end/cancellationRequested available");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
