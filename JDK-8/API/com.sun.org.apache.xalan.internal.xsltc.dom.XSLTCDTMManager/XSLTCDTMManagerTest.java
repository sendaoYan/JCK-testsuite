/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager conformance: reflection.
 * @run main XSLTCDTMManagerTest
 */

public class XSLTCDTMManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.XSLTCDTMManager", false, XSLTCDTMManagerTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XSLTCDTMManager"), "simple name XSLTCDTMManager");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault"), "superclass com.sun.org.apache.xml.internal.dtm.ref.DTMManagerDefault");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "newInstance"), "method newInstance");
        check(has(c, "createNewDTMManagerInstance"), "method createNewDTMManagerInstance");
        check(has(c, "getDTM"), "method getDTM");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
