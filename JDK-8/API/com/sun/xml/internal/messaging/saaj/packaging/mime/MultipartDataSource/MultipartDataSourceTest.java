/*
 * @test
 * @summary com.sun.xml.internal.messaging.saaj.packaging.mime.MultipartDataSource conformance: interface hierarchy and members.
 * @run main MultipartDataSourceTest
 */

public class MultipartDataSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.messaging.saaj.packaging.mime.MultipartDataSource", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.messaging.saaj.packaging.mime.MultipartDataSource"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.activation.DataSource"), "has interface javax.activation.DataSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBodyPart"), "has method getBodyPart");
        check(mn.contains("getCount"), "has method getCount");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
