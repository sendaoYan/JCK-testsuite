/*
 * @test
 * @summary com.sun.corba.se.impl.io.ValueHandlerImpl conformance: type shape via reflection (headless-safe).
 * @run main ValueHandlerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ValueHandlerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.io.ValueHandlerImpl", false, ValueHandlerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.rmi.CORBA.ValueHandlerMultiFormat"), "interface javax.rmi.CORBA.ValueHandlerMultiFormat");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 17, "public declared method count");
        check(mn.contains("createForAnyType"), "method createForAnyType");
        check(mn.contains("getAnyClassFromType"), "method getAnyClassFromType");
        check(mn.contains("getClassFromType"), "method getClassFromType");
        check(mn.contains("getClassName"), "method getClassName");
        check(mn.contains("getDefinedInId"), "method getDefinedInId");
        check(mn.contains("getMaximumStreamFormatVersion"), "method getMaximumStreamFormatVersion");
        check(mn.contains("getRMIRepositoryID"), "method getRMIRepositoryID");
        check(mn.contains("getRunTimeCodeBase"), "method getRunTimeCodeBase");
        check(mn.contains("getSerialVersionUID"), "method getSerialVersionUID");
        check(mn.contains("getUnqualifiedName"), "method getUnqualifiedName");
        check(mn.contains("isAbstractBase"), "method isAbstractBase");
        check(mn.contains("isCustomMarshaled"), "method isCustomMarshaled");
        check(mn.contains("isSequence"), "method isSequence");
        check(mn.contains("readValue"), "method readValue");
        check(mn.contains("useFullValueDescription"), "method useFullValueDescription");
        check(mn.contains("writeReplace"), "method writeReplace");
        check(mn.contains("writeValue"), "method writeValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
