/*
 * @test
 * @summary javax.net.ssl.SSLSessionBindingEvent conformance: API type verification.
 * @run main SSLSessionBindingEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLSessionBindingEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLSession sess = javax.net.ssl.SSLContext.getDefault().createSSLEngine().getSession();
javax.net.ssl.SSLSessionBindingEvent e = new javax.net.ssl.SSLSessionBindingEvent(sess, "name");
check(e.getName().equals("name"), "name");
check(e.getSession() == sess, "session");
check(e instanceof java.util.EventObject, "EventObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
