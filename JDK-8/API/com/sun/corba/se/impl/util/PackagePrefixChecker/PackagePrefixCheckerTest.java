/*
 * @test
 * @summary com.sun.corba.se.impl.util.PackagePrefixChecker conformance: type shape via reflection (headless-safe).
 * @run main PackagePrefixCheckerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PackagePrefixCheckerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.util.PackagePrefixChecker", false, PackagePrefixCheckerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("correctPackageName"), "method correctPackageName");
        check(mn.contains("hasBeenPrefixed"), "method hasBeenPrefixed");
        check(mn.contains("hasOffendingPrefix"), "method hasOffendingPrefix");
        check(mn.contains("isOffendingPackage"), "method isOffendingPackage");
        check(mn.contains("packagePrefix"), "method packagePrefix");
        check(mn.contains("withoutPackagePrefix"), "method withoutPackagePrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
