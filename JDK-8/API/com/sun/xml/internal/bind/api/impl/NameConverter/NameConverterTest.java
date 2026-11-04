/*
 * @test
 * @summary com.sun.xml.internal.bind.api.impl.NameConverter conformance: interface hierarchy and members.
 * @run main NameConverterTest
 */

public class NameConverterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.api.impl.NameConverter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.api.impl.NameConverter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("toClassName"), "has method toClassName");
        check(mn.contains("toConstantName"), "has method toConstantName");
        check(mn.contains("toInterfaceName"), "has method toInterfaceName");
        check(mn.contains("toPackageName"), "has method toPackageName");
        check(mn.contains("toPropertyName"), "has method toPropertyName");
        check(mn.contains("toVariableName"), "has method toVariableName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
