/*
 * @test
 * @summary java.util.stream.Node.OfDouble conformance: interface hierarchy and members.
 * @run main OfDoubleTest
 */


public class OfDoubleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ===================== 契约深化：OfDouble 原语特化面反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.stream.Node$OfDouble");
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
        Class<?> c = Class.forName("java.util.stream.Node$OfDouble");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.util.stream.Node$OfPrimitive");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("asArray"), "has method asArray");
        check(names.contains("asPrimitiveArray"), "has method asPrimitiveArray");
        check(names.contains("copyInto"), "has method copyInto");        System.out.println("PASS (" + passed + " assertions)");
    }
}
