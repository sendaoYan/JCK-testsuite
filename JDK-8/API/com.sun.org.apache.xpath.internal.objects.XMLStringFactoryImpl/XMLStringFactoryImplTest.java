/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.XMLStringFactoryImpl conformance: reflection.
 * @run main XMLStringFactoryImplTest
 */

public class XMLStringFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.XMLStringFactoryImpl", false, XMLStringFactoryImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XMLStringFactoryImpl"), "simple name XMLStringFactoryImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.utils.XMLStringFactory"), "superclass com.sun.org.apache.xml.internal.utils.XMLStringFactory");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getFactory"), "method getFactory");
        check(has(c, "newstr"), "method newstr");
        check(has(c, "emptystr"), "method emptystr");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
