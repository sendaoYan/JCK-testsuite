/*
 * @test
 * @summary com.sun.org.omg.CORBA.AttributeMode conformance: reflection.
 * @run main AttributeModeTest
 */

public class AttributeModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.omg.CORBA.AttributeMode", false, AttributeModeTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("AttributeMode"), "simple name AttributeMode");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.omg.CORBA.portable.IDLEntity"), "interface org.omg.CORBA.portable.IDLEntity");
        check(has(c, "value"), "method value");
        check(has(c, "from_int"), "method from_int");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
