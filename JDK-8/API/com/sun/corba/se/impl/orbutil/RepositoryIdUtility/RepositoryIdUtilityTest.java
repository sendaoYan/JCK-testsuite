/*
 * @test
 * @summary com.sun.corba.se.impl.orbutil.RepositoryIdUtility conformance: type shape via reflection (headless-safe).
 * @run main RepositoryIdUtilityTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RepositoryIdUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orbutil.RepositoryIdUtility", false, RepositoryIdUtilityTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("getCodeBaseRMIChunkedId"), "method getCodeBaseRMIChunkedId");
        check(mn.contains("getCodeBaseRMIChunkedNoRepStrId"), "method getCodeBaseRMIChunkedNoRepStrId");
        check(mn.contains("getCodeBaseRMIUnchunkedId"), "method getCodeBaseRMIUnchunkedId");
        check(mn.contains("getCodeBaseRMIUnchunkedNoRepStrId"), "method getCodeBaseRMIUnchunkedNoRepStrId");
        check(mn.contains("getStandardRMIChunkedId"), "method getStandardRMIChunkedId");
        check(mn.contains("getStandardRMIChunkedNoRepStrId"), "method getStandardRMIChunkedNoRepStrId");
        check(mn.contains("getStandardRMIUnchunkedId"), "method getStandardRMIUnchunkedId");
        check(mn.contains("getStandardRMIUnchunkedNoRepStrId"), "method getStandardRMIUnchunkedNoRepStrId");
        check(mn.contains("getTypeInfo"), "method getTypeInfo");
        check(mn.contains("isChunkedEncoding"), "method isChunkedEncoding");
        check(mn.contains("isCodeBasePresent"), "method isCodeBasePresent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
