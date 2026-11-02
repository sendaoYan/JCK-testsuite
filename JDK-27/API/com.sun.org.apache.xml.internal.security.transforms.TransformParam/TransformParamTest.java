/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.TransformParam conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.transforms:+open
 * @run main TransformParamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TransformParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.TransformParam", false, TransformParamTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.transforms.TransformParam"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
