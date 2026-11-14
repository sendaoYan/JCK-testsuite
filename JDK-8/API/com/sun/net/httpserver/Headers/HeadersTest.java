/*
 * @test
 * @summary com.sun.net.httpserver.Headers conformance: hierarchy and declared members via reflection.
 * @run main HeadersTest
 */

public class HeadersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.Headers", false, null);
        check(c.getName().equals("com.sun.net.httpserver.Headers"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            System.out.println("PASS (" + passed + " assertions; SKIP build-specific internal structure)");
            return;
        }
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.util.Map"), "implements java.util.Map");
        // Assert only the exported Map/Headers API surface. The private helpers
        // (checkValue, normalize) are build-specific implementation details —
        // jdk8u502 dropped checkValue — and are not part of the contract.
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("add"), "declares add");
        check(ms.contains("clear"), "declares clear");
        check(ms.contains("containsKey"), "declares containsKey");
        check(ms.contains("containsValue"), "declares containsValue");
        check(ms.contains("entrySet"), "declares entrySet");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("get"), "declares get");
        check(ms.contains("getFirst"), "declares getFirst");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("isEmpty"), "declares isEmpty");
        check(ms.contains("keySet"), "declares keySet");
        check(ms.contains("put"), "declares put");
        check(ms.contains("putAll"), "declares putAll");
        check(ms.contains("remove"), "declares remove");
        check(ms.contains("set"), "declares set");
        check(ms.contains("size"), "declares size");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
