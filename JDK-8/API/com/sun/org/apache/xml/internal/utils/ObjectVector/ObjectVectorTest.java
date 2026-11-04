/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.ObjectVector conformance: class hierarchy and members.
 * @run main ObjectVectorTest
 */
public class ObjectVectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.ObjectVector", false, ObjectVectorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "java.lang.Cloneable"), "has interface java.lang.Cloneable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("size"), "method size");
        check(mn.contains("setSize"), "method setSize");
        check(mn.contains("addElement"), "method addElement");
        check(mn.contains("addElements"), "method addElements");
        check(mn.contains("insertElementAt"), "method insertElementAt");
        check(mn.contains("removeAllElements"), "method removeAllElements");
        check(mn.contains("removeElement"), "method removeElement");
        check(mn.contains("removeElementAt"), "method removeElementAt");
        check(mn.contains("setElementAt"), "method setElementAt");
        check(mn.contains("elementAt"), "method elementAt");
        check(mn.contains("contains"), "method contains");
        check(mn.contains("indexOf"), "method indexOf");
        check(mn.contains("lastIndexOf"), "method lastIndexOf");
        check(mn.contains("setToSize"), "method setToSize");
        check(mn.contains("clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
