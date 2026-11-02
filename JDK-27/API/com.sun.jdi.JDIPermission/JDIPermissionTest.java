/*
 * @test
 * @summary com.sun.jdi.JDIPermission conformance: hierarchy, constructors and behavior.
 * @run main JDIPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;

public class JDIPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.JDIPermission");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.security.BasicPermission"), "extends BasicPermission");
        Object p1 = c.getConstructor(String.class).newInstance("virtualMachineManager");
        check("virtualMachineManager".equals(c.getMethod("getName").invoke(p1)), "getName");
        Object p2 = c.getConstructor(String.class, String.class).newInstance("virtualMachineManager", null);
        check("virtualMachineManager".equals(c.getMethod("getName").invoke(p2)), "getName 2-arg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
