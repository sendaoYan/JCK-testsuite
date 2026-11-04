/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException conformance: class hierarchy and members.
 * @run main WrappedRuntimeExceptionTest
 */
public class WrappedRuntimeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.utils.WrappedRuntimeException", false, WrappedRuntimeExceptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass java.lang.RuntimeException");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getException"), "method getException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
