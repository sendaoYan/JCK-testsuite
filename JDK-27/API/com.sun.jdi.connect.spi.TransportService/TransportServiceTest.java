/*
 * @test
 * @summary com.sun.jdi.connect.spi.TransportService conformance: abstract class shape and methods.
 * @run main TransportServiceTest
 */

import java.lang.reflect.*;
import java.util.*;

public class TransportServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.spi.TransportService");
        check(Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = methodNames(c);
        check(dm.contains("name"), "method name");
        check(dm.contains("description"), "method description");
        check(dm.contains("capabilities"), "method capabilities");
        check(dm.contains("attach"), "method attach");
        check(dm.contains("startListening"), "method startListening");
        check(dm.contains("stopListening"), "method stopListening");
        check(dm.contains("accept"), "method accept");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
