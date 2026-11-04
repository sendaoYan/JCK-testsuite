/*
 * @test
 * @summary com.sun.xml.internal.stream.EventFilterSupport conformance: class hierarchy and members.
 * @run main EventFilterSupportTest
 */

public class EventFilterSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.EventFilterSupport", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.EventFilterSupport"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("javax.xml.stream.util.EventReaderDelegate".equals(c.getSuperclass().getName()), "extends javax.xml.stream.util.EventReaderDelegate");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("hasNext"), "has method hasNext");
        check(mn.contains("next"), "has method next");
        check(mn.contains("nextEvent"), "has method nextEvent");
        check(mn.contains("nextTag"), "has method nextTag");
        check(mn.contains("peek"), "has method peek");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
