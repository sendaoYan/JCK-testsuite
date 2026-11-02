/*
 * @test
 * @summary com.sun.corba.se.impl.util.Utility conformance: type shape via reflection (headless-safe).
 * @run main UtilityTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class UtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.util.Utility", false, UtilityTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 22, "public declared method count");
        check(mn.contains("autoConnect"), "method autoConnect");
        check(mn.contains("clearCaches"), "method clearCaches");
        check(mn.contains("dynamicStubName"), "method dynamicStubName");
        check(mn.contains("getAndForgetTie"), "method getAndForgetTie");
        check(mn.contains("getFactory"), "method getFactory");
        check(mn.contains("getHelper"), "method getHelper");
        check(mn.contains("idlStubName"), "method idlStubName");
        check(mn.contains("loadClassForClass"), "method loadClassForClass");
        check(mn.contains("loadStub"), "method loadStub");
        check(mn.contains("loadStubClass"), "method loadStubClass");
        check(mn.contains("loadTie"), "method loadTie");
        check(mn.contains("printStackTrace"), "method printStackTrace");
        check(mn.contains("purgeStubForTie"), "method purgeStubForTie");
        check(mn.contains("purgeTieAndServant"), "method purgeTieAndServant");
        check(mn.contains("readAbstractAndNarrow"), "method readAbstractAndNarrow");
        check(mn.contains("readObjectAndNarrow"), "method readObjectAndNarrow");
        check(mn.contains("stubName"), "method stubName");
        check(mn.contains("stubNameForCompiler"), "method stubNameForCompiler");
        check(mn.contains("stubNameFromRepID"), "method stubNameFromRepID");
        check(mn.contains("throwNotSerializableForCorba"), "method throwNotSerializableForCorba");
        check(mn.contains("tieName"), "method tieName");
        check(mn.contains("tieNameForCompiler"), "method tieNameForCompiler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
