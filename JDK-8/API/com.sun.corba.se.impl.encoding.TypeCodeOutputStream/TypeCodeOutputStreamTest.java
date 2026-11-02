/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.TypeCodeOutputStream conformance: type shape via reflection (headless-safe).
 * @run main TypeCodeOutputStreamTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TypeCodeOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.TypeCodeOutputStream", false, TypeCodeOutputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.encoding.EncapsOutputStream"), "superclass com.sun.corba.se.impl.encoding.EncapsOutputStream");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 13, "public declared method count");
        check(mn.contains("addIDAtPosition"), "method addIDAtPosition");
        check(mn.contains("createEncapsulation"), "method createEncapsulation");
        check(mn.contains("create_input_stream"), "method create_input_stream");
        check(mn.contains("getPosition"), "method getPosition");
        check(mn.contains("getPositionForID"), "method getPositionForID");
        check(mn.contains("getRealIndex"), "method getRealIndex");
        check(mn.contains("getTopLevelPosition"), "method getTopLevelPosition");
        check(mn.contains("getTopLevelStream"), "method getTopLevelStream");
        check(mn.contains("getTypeCodeBuffer"), "method getTypeCodeBuffer");
        check(mn.contains("printTypeMap"), "method printTypeMap");
        check(mn.contains("setEnclosingOutputStream"), "method setEnclosingOutputStream");
        check(mn.contains("wrapOutputStream"), "method wrapOutputStream");
        check(mn.contains("writeRawBuffer"), "method writeRawBuffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
