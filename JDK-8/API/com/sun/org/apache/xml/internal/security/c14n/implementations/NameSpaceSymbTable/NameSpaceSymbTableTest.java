/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.NameSpaceSymbTable conformance: class hierarchy and members.
 * @run main NameSpaceSymbTableTest
 */
public class NameSpaceSymbTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.NameSpaceSymbTable", false, NameSpaceSymbTableTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getUnrenderedNodes"), "method getUnrenderedNodes");
        check(mn.contains("outputNodePush"), "method outputNodePush");
        check(mn.contains("outputNodePop"), "method outputNodePop");
        check(mn.contains("push"), "method push");
        check(mn.contains("pop"), "method pop");
        check(mn.contains("needsClone"), "method needsClone");
        check(mn.contains("getMapping"), "method getMapping");
        check(mn.contains("getMappingWithoutRendered"), "method getMappingWithoutRendered");
        check(mn.contains("addMapping"), "method addMapping");
        check(mn.contains("addMappingAndRender"), "method addMappingAndRender");
        check(mn.contains("getLevel"), "method getLevel");
        check(mn.contains("removeMapping"), "method removeMapping");
        check(mn.contains("removeMappingIfNotRender"), "method removeMappingIfNotRender");
        check(mn.contains("removeMappingIfRender"), "method removeMappingIfRender");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
