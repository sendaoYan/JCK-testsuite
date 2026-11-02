/*
 * @test
 * @summary com.sun.corba.se.impl.javax.rmi.CORBA.Util conformance: type shape via reflection (headless-safe).
 * @run main UtilTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.javax.rmi.CORBA.Util", false, UtilTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.rmi.CORBA.UtilDelegate"), "interface javax.rmi.CORBA.UtilDelegate");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 18, "public declared method count");
        check(mn.contains("copyObject"), "method copyObject");
        check(mn.contains("copyObjects"), "method copyObjects");
        check(mn.contains("createValueHandler"), "method createValueHandler");
        check(mn.contains("getCodebase"), "method getCodebase");
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("getTie"), "method getTie");
        check(mn.contains("isInstanceDefined"), "method isInstanceDefined");
        check(mn.contains("isLocal"), "method isLocal");
        check(mn.contains("loadClass"), "method loadClass");
        check(mn.contains("mapSystemException"), "method mapSystemException");
        check(mn.contains("readAny"), "method readAny");
        check(mn.contains("registerTarget"), "method registerTarget");
        check(mn.contains("unexportObject"), "method unexportObject");
        check(mn.contains("unregisterTargetsForORB"), "method unregisterTargetsForORB");
        check(mn.contains("wrapException"), "method wrapException");
        check(mn.contains("writeAbstractObject"), "method writeAbstractObject");
        check(mn.contains("writeAny"), "method writeAny");
        check(mn.contains("writeRemoteObject"), "method writeRemoteObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
