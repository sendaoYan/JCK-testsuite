/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xpath.regex.Match conformance: class hierarchy and members.
 * @run main MatchTest
 */
public class MatchTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xpath.regex.Match", false, MatchTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "java.lang.Cloneable"), "has interface java.lang.Cloneable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clone"), "method clone");
        check(mn.contains("setNumberOfGroups"), "method setNumberOfGroups");
        check(mn.contains("setSource"), "method setSource");
        check(mn.contains("setBeginning"), "method setBeginning");
        check(mn.contains("setEnd"), "method setEnd");
        check(mn.contains("getNumberOfGroups"), "method getNumberOfGroups");
        check(mn.contains("getBeginning"), "method getBeginning");
        check(mn.contains("getEnd"), "method getEnd");
        check(mn.contains("getCapturedText"), "method getCapturedText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
