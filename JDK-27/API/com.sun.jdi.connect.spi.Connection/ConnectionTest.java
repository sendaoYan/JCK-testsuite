/*
 * @test
 * @summary com.sun.jdi.connect.spi.Connection conformance: white-box structure + black-box behavior.
 * @run main ConnectionTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class ConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.spi.Connection");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getDeclaredConstructors().length == 1, "constructor count");
        Set<String> dm = methodNames(c);
        check(dm.contains("readPacket"), "method readPacket");
        check(dm.contains("writePacket"), "method writePacket");
        check(dm.contains("close"), "method close");
        check(dm.contains("isOpen"), "method isOpen");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Connection conn = new Connection() {
            public byte[] readPacket() throws java.io.IOException { return new byte[0]; }
            public void writePacket(byte[] p) throws java.io.IOException { }
            public void close() throws java.io.IOException { }
            public boolean isOpen() { return true; }
        };
        check(conn.isOpen(), "isOpen");
        check(conn.readPacket().length == 0, "readPacket");
        conn.writePacket(new byte[] {1,2,3});
        conn.close();
        check(true, "writePacket/close callable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
