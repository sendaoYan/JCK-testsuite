/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.JavaUtils conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main JavaUtilsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class JavaUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.JavaUtils", false, JavaUtilsTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.JavaUtils"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("checkRegisterPermission"), "method:checkRegisterPermission");
        check(methods.contains("convertDsaASN1toXMLDSIG"), "method:convertDsaASN1toXMLDSIG");
        check(methods.contains("convertDsaXMLDSIGtoASN1"), "method:convertDsaXMLDSIGtoASN1");
        check(methods.contains("getBytesFromFile"), "method:getBytesFromFile");
        check(methods.contains("getBytesFromStream"), "method:getBytesFromStream");
        check(methods.contains("newInstanceWithEmptyConstructor"), "method:newInstanceWithEmptyConstructor");
        check(methods.contains("writeBytesToFilename"), "method:writeBytesToFilename");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
