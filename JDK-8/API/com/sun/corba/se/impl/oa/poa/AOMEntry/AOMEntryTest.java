/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.AOMEntry conformance: type shape via reflection (headless-safe).
 * @run main AOMEntryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class AOMEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.AOMEntry", false, AOMEntryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.orbutil.fsm.FSMImpl"), "superclass com.sun.corba.se.spi.orbutil.fsm.FSMImpl");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("activateObject"), "method activateObject");
        check(mn.contains("enter"), "method enter");
        check(mn.contains("etherealizeComplete"), "method etherealizeComplete");
        check(mn.contains("exit"), "method exit");
        check(mn.contains("incarnateComplete"), "method incarnateComplete");
        check(mn.contains("incarnateFailure"), "method incarnateFailure");
        check(mn.contains("startEtherealize"), "method startEtherealize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
