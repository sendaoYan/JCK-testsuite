/*
 * @test
 * @summary com.sun.jdi.connect.spi.TransportService conformance: white-box structure + black-box behavior.
 * @run main TransportServiceTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class TransportServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.spi.TransportService");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getDeclaredConstructors().length == 1, "constructor count");
        Set<String> dm = methodNames(c);
        check(dm.contains("name"), "method name");
        check(dm.contains("description"), "method description");
        check(dm.contains("capabilities"), "method capabilities");
        check(dm.contains("attach"), "method attach");
        check(dm.contains("startListening"), "method startListening");
        check(dm.contains("stopListening"), "method stopListening");
        check(dm.contains("accept"), "method accept");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TransportService ts = new TransportService() {
            public String name() { return "test"; }
            public String description() { return "d"; }
            public TransportService.Capabilities capabilities() {
                return new TransportService.Capabilities() {
                    public boolean supportsMultipleConnections() { return false; }
                    public boolean supportsAttachTimeout() { return false; }
                    public boolean supportsAcceptTimeout() { return false; }
                    public boolean supportsHandshakeTimeout() { return false; }
                };
            }
            public Connection attach(String address, long a, long h) { return null; }
            public TransportService.ListenKey startListening(String address) { return null; }
            public TransportService.ListenKey startListening() { return null; }
            public void stopListening(TransportService.ListenKey key) { }
            public Connection accept(TransportService.ListenKey key, long a, long h) { return null; }
        };
        check("test".equals(ts.name()), "name");
        check("d".equals(ts.description()), "description");
        check(ts.capabilities() != null, "capabilities");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
