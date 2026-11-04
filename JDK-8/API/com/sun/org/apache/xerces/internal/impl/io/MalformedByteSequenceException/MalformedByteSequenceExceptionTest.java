/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException conformance: class hierarchy and members.
 * @run main MalformedByteSequenceExceptionTest
 */
public class MalformedByteSequenceExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.io.MalformedByteSequenceException", false, MalformedByteSequenceExceptionTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.io.CharConversionException"), "superclass java.io.CharConversionException");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDomain"), "method getDomain");
        check(mn.contains("getKey"), "method getKey");
        check(mn.contains("getArguments"), "method getArguments");
        check(mn.contains("getMessage"), "method getMessage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
