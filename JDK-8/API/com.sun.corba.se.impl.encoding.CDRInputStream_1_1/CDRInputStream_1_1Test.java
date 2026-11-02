/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.CDRInputStream_1_1 conformance: type shape via reflection (headless-safe).
 * @run main CDRInputStream_1_1Test
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CDRInputStream_1_1Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.CDRInputStream_1_1", false, CDRInputStream_1_1Test.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.encoding.CDRInputStream_1_0"), "superclass com.sun.corba.se.impl.encoding.CDRInputStream_1_0");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("createStreamMemento"), "method createStreamMemento");
        check(mn.contains("dup"), "method dup");
        check(mn.contains("getGIOPVersion"), "method getGIOPVersion");
        check(mn.contains("read_wchar"), "method read_wchar");
        check(mn.contains("read_wstring"), "method read_wstring");
        check(mn.contains("restoreInternalState"), "method restoreInternalState");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
