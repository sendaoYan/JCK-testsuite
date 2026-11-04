/*
 * @test
 * @summary java.util.stream.Node.OfLong conformance: package-private SPI (reflection) + accessibility.
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
        Class<?> c = Class.forName("java.util.stream.Node$OfLong");
        check(c.isInterface(), "is an interface");
        check(Arrays.equals(interfaceNames(c), new String[]{"java.util.stream.Node$OfPrimitive"}),
              "extends expected superinterfaces");
        check(hasMethod(c, "forEach", 1), "forEach(1-arg) declared");
        check(hasMethod(c, "copyInto", 2), "copyInto(2-arg) declared");
        check(hasMethod(c, "truncate", 3), "truncate(3-arg) declared");
        check(hasMethod(c, "newArray", 1), "newArray(1-arg) declared");
        check(hasMethod(c, "getShape", 0), "getShape(0-arg) declared");
    }

    static void testBlackBox() throws Exception {
        // The nested SPI type is declared public, but its enclosing type is
        // package-private, so the SPI is unreachable from outside java.util.stream.
        Class<?> enclosing = Class.forName("java.util.stream.Node");
        check(!Modifier.isPublic(enclosing.getModifiers()),
              "enclosing type is package-private (SPI unreachable outside package)");
    }


    // ===================== 契约深化：OfLong 原语特化面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$OfLong");
        check(c.isInterface(), "is interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("asArray"), "asArray present");
        check(names.contains("spliterator"), "spliterator present");
        // 原语数组形态：asPrimitiveArray() 无参（泛型擦除后返回 Object，具体类型由子接口绑定）
        java.lang.reflect.Method apa = null;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals("asPrimitiveArray") && m.getParameterCount() == 0) apa = m;
        }
        check(apa != null, "asPrimitiveArray() declared (erased return " + apa.getReturnType().getName() + ")");
        // 父类型链：直接父接口是 OfPrimitive（其再 extends Node）
        boolean extendsOfPrimitive = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals("java.util.stream.Node$OfPrimitive")) extendsOfPrimitive = true;
        check(extendsOfPrimitive, "directly extends Node$OfPrimitive");
        Class<?> node = Class.forName("java.util.stream.Node");
        check(node.isAssignableFrom(c), "assignable to Node");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
