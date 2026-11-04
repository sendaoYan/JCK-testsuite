/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.nav.Navigator conformance: interface hierarchy and members.
 * @run main NavigatorTest
 */

public class NavigatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.nav.Navigator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.nav.Navigator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("asDecl"), "has method asDecl");
        check(mn.contains("erasure"), "has method erasure");
        check(mn.contains("getBaseClass"), "has method getBaseClass");
        check(mn.contains("getClassLocation"), "has method getClassLocation");
        check(mn.contains("getClassName"), "has method getClassName");
        check(mn.contains("getClassShortName"), "has method getClassShortName");
        check(mn.contains("getComponentType"), "has method getComponentType");
        check(mn.contains("getDeclaredField"), "has method getDeclaredField");
        check(mn.contains("getDeclaredFields"), "has method getDeclaredFields");
        check(mn.contains("getDeclaredMethods"), "has method getDeclaredMethods");
        check(mn.contains("getDeclaringClassForField"), "has method getDeclaringClassForField");
        check(mn.contains("getDeclaringClassForMethod"), "has method getDeclaringClassForMethod");
        check(mn.contains("getEnumConstants"), "has method getEnumConstants");
        check(mn.contains("getFieldLocation"), "has method getFieldLocation");
        check(mn.contains("getFieldName"), "has method getFieldName");
        check(mn.contains("getFieldType"), "has method getFieldType");
        check(mn.contains("getMethodLocation"), "has method getMethodLocation");
        check(mn.contains("getMethodName"), "has method getMethodName");
        check(mn.contains("getMethodParameters"), "has method getMethodParameters");
        check(mn.contains("getPackageName"), "has method getPackageName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
