/*
 * @test
 * @summary com.sun.jmx.remote.internal.ArrayQueue conformance: hierarchy and declared members via reflection.
 * @run main ArrayQueueTest
 */

public class ArrayQueueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.internal.ArrayQueue", false, null);
        check(c.getName().equals("com.sun.jmx.remote.internal.ArrayQueue"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractList"), "superclass java.util.AbstractList");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("add"), "declares add");
        check(ms.contains("get"), "declares get");
        check(ms.contains("newArray"), "declares newArray");
        check(ms.contains("remove"), "declares remove");
        check(ms.contains("resize"), "declares resize");
        check(ms.contains("size"), "declares size");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
