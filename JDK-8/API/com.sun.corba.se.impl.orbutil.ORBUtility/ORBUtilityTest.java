/*
 * @test
 * @summary com.sun.corba.se.impl.orbutil.ORBUtility conformance: type shape via reflection (headless-safe).
 * @run main ORBUtilityTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ORBUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orbutil.ORBUtility", false, ORBUtilityTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 28, "public declared method count");
        check(mn.contains("bytesToInt"), "method bytesToInt");
        check(mn.contains("classNameOf"), "method classNameOf");
        check(mn.contains("compareVersion"), "method compareVersion");
        check(mn.contains("concatenateStringArrays"), "method concatenateStringArrays");
        check(mn.contains("connectAndGetIOR"), "method connectAndGetIOR");
        check(mn.contains("createValueHandler"), "method createValueHandler");
        check(mn.contains("dprint"), "method dprint");
        check(mn.contains("dprintTrace"), "method dprintTrace");
        check(mn.contains("extractSystemException"), "method extractSystemException");
        check(mn.contains("getClassSecurityInfo"), "method getClassSecurityInfo");
        check(mn.contains("getEncodingVersion"), "method getEncodingVersion");
        check(mn.contains("getIOR"), "method getIOR");
        check(mn.contains("getMaxStreamFormatVersion"), "method getMaxStreamFormatVersion");
        check(mn.contains("getThreadName"), "method getThreadName");
        check(mn.contains("hexOf"), "method hexOf");
        check(mn.contains("insertSystemException"), "method insertSystemException");
        check(mn.contains("intToBytes"), "method intToBytes");
        check(mn.contains("isForeignORB"), "method isForeignORB");
        check(mn.contains("isPrintable"), "method isPrintable");
        check(mn.contains("isSystemException"), "method isSystemException");
        check(mn.contains("makeClientDelegate"), "method makeClientDelegate");
        check(mn.contains("makeObjectReference"), "method makeObjectReference");
        check(mn.contains("operationNameAndRequestId"), "method operationNameAndRequestId");
        check(mn.contains("parseVersion"), "method parseVersion");
        check(mn.contains("readSystemException"), "method readSystemException");
        check(mn.contains("repositoryIdOf"), "method repositoryIdOf");
        check(mn.contains("throwNotSerializableForCorba"), "method throwNotSerializableForCorba");
        check(mn.contains("writeSystemException"), "method writeSystemException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
