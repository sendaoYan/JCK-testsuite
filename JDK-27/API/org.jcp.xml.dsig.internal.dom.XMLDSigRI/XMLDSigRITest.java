/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.XMLDSigRI conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main XMLDSigRITest
 */
import java.lang.reflect.*;
import java.util.*;
public class XMLDSigRITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.XMLDSigRI", false, XMLDSigRITest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.XMLDSigRI"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.security.Provider"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
