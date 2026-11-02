/*
 * @test
 * @summary com.sun.corba.se.impl.util.RepositoryId conformance: type shape via reflection (headless-safe).
 * @run main RepositoryIdTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RepositoryIdTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.util.RepositoryId", false, RepositoryIdTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 34, "public declared method count");
        check(mn.contains("computeValueTag"), "method computeValueTag");
        check(mn.contains("convertToISOLatin1"), "method convertToISOLatin1");
        check(mn.contains("createForAnyType"), "method createForAnyType");
        check(mn.contains("createForIDLType"), "method createForIDLType");
        check(mn.contains("createForJavaType"), "method createForJavaType");
        check(mn.contains("createForSpecialCase"), "method createForSpecialCase");
        check(mn.contains("createSequenceRepID"), "method createSequenceRepID");
        check(mn.contains("fromHex"), "method fromHex");
        check(mn.contains("getActualSerialVersionUID"), "method getActualSerialVersionUID");
        check(mn.contains("getActualSerialVersionUIDAsLong"), "method getActualSerialVersionUIDAsLong");
        check(mn.contains("getAnyClassFromType"), "method getAnyClassFromType");
        check(mn.contains("getByteArray"), "method getByteArray");
        check(mn.contains("getClassFromType"), "method getClassFromType");
        check(mn.contains("getClassName"), "method getClassName");
        check(mn.contains("getDefinedInId"), "method getDefinedInId");
        check(mn.contains("getRepositoryId"), "method getRepositoryId");
        check(mn.contains("getSerialVersionUID"), "method getSerialVersionUID");
        check(mn.contains("getSerialVersionUIDAsLong"), "method getSerialVersionUIDAsLong");
        check(mn.contains("getServerURL"), "method getServerURL");
        check(mn.contains("getTypeInfo"), "method getTypeInfo");
        check(mn.contains("getTypeString"), "method getTypeString");
        check(mn.contains("getUnqualifiedName"), "method getUnqualifiedName");
        check(mn.contains("getVersionString"), "method getVersionString");
        check(mn.contains("isAbstractBase"), "method isAbstractBase");
        check(mn.contains("isAnyRequired"), "method isAnyRequired");
        check(mn.contains("isChunkedEncoding"), "method isChunkedEncoding");
        check(mn.contains("isCodeBasePresent"), "method isCodeBasePresent");
        check(mn.contains("isIDLType"), "method isIDLType");
        check(mn.contains("isRMIValueType"), "method isRMIValueType");
        check(mn.contains("isSequence"), "method isSequence");
        check(mn.contains("isSupportedFormat"), "method isSupportedFormat");
        check(mn.contains("setByteArray"), "method setByteArray");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("useFullValueDescription"), "method useFullValueDescription");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
