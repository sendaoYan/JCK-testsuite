/*
 * @test
 * @summary com.sun.corba.se.impl.orbutil.RepIdDelegator conformance: type shape via reflection (headless-safe).
 * @run main RepIdDelegatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RepIdDelegatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orbutil.RepIdDelegator", false, RepIdDelegatorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.orbutil.RepositoryIdInterface"), "interface com.sun.corba.se.impl.orbutil.RepositoryIdInterface");
        check(in.contains("com.sun.corba.se.impl.orbutil.RepositoryIdStrings"), "interface com.sun.corba.se.impl.orbutil.RepositoryIdStrings");
        check(in.contains("com.sun.corba.se.impl.orbutil.RepositoryIdUtility"), "interface com.sun.corba.se.impl.orbutil.RepositoryIdUtility");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 22, "public declared method count");
        check(mn.contains("createForAnyType"), "method createForAnyType");
        check(mn.contains("createForJavaType"), "method createForJavaType");
        check(mn.contains("createSequenceRepID"), "method createSequenceRepID");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getClassDescValueRepId"), "method getClassDescValueRepId");
        check(mn.contains("getClassFromType"), "method getClassFromType");
        check(mn.contains("getClassName"), "method getClassName");
        check(mn.contains("getCodeBaseRMIChunkedId"), "method getCodeBaseRMIChunkedId");
        check(mn.contains("getCodeBaseRMIChunkedNoRepStrId"), "method getCodeBaseRMIChunkedNoRepStrId");
        check(mn.contains("getCodeBaseRMIUnchunkedId"), "method getCodeBaseRMIUnchunkedId");
        check(mn.contains("getCodeBaseRMIUnchunkedNoRepStrId"), "method getCodeBaseRMIUnchunkedNoRepStrId");
        check(mn.contains("getFromString"), "method getFromString");
        check(mn.contains("getStandardRMIChunkedId"), "method getStandardRMIChunkedId");
        check(mn.contains("getStandardRMIChunkedNoRepStrId"), "method getStandardRMIChunkedNoRepStrId");
        check(mn.contains("getStandardRMIUnchunkedId"), "method getStandardRMIUnchunkedId");
        check(mn.contains("getStandardRMIUnchunkedNoRepStrId"), "method getStandardRMIUnchunkedNoRepStrId");
        check(mn.contains("getTypeInfo"), "method getTypeInfo");
        check(mn.contains("getWStringValueRepId"), "method getWStringValueRepId");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isChunkedEncoding"), "method isChunkedEncoding");
        check(mn.contains("isCodeBasePresent"), "method isCodeBasePresent");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
