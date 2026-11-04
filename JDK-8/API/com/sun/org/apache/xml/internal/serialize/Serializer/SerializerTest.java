/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.Serializer conformance: interface hierarchy and members.
 * @run main SerializerTest
 */
public class SerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.Serializer", false, SerializerTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setOutputByteStream"), "method setOutputByteStream");
        check(mn.contains("setOutputCharStream"), "method setOutputCharStream");
        check(mn.contains("setOutputFormat"), "method setOutputFormat");
        check(mn.contains("asDocumentHandler"), "method asDocumentHandler");
        check(mn.contains("asContentHandler"), "method asContentHandler");
        check(mn.contains("asDOMSerializer"), "method asDOMSerializer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
