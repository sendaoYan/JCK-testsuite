/*
 * @test
 * @summary com.sun.corba.se.impl.presentation.rmi.IDLTypesUtil conformance: type shape via reflection (headless-safe).
 * @run main IDLTypesUtilTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IDLTypesUtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.presentation.rmi.IDLTypesUtil", false, IDLTypesUtilTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 14, "public declared method count");
        check(mn.contains("getAttributeNameForProperty"), "method getAttributeNameForProperty");
        check(mn.contains("getPrimitiveIDLTypeMapping"), "method getPrimitiveIDLTypeMapping");
        check(mn.contains("getSpecialCaseIDLTypeMapping"), "method getSpecialCaseIDLTypeMapping");
        check(mn.contains("isArray"), "method isArray");
        check(mn.contains("isCheckedException"), "method isCheckedException");
        check(mn.contains("isEntity"), "method isEntity");
        check(mn.contains("isException"), "method isException");
        check(mn.contains("isObjectReference"), "method isObjectReference");
        check(mn.contains("isPrimitive"), "method isPrimitive");
        check(mn.contains("isPropertyAccessorMethod"), "method isPropertyAccessorMethod");
        check(mn.contains("isRemoteException"), "method isRemoteException");
        check(mn.contains("isRemoteInterface"), "method isRemoteInterface");
        check(mn.contains("isValue"), "method isValue");
        check(mn.contains("validateRemoteInterface"), "method validateRemoteInterface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
