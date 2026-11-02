/*
 * @test
 * @summary javax.net.ssl.HandshakeCompletedEvent conformance: API type verification.
 * @run main HandshakeCompletedEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class HandshakeCompletedEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLSession sess = javax.net.ssl.SSLContext.getDefault().createSSLEngine().getSession();
javax.net.ssl.SSLSocket sock = (javax.net.ssl.SSLSocket) javax.net.ssl.SSLSocketFactory.getDefault().createSocket();
javax.net.ssl.HandshakeCompletedEvent e = new javax.net.ssl.HandshakeCompletedEvent(sock, sess);
check(e.getSocket() == sock, "socket");
check(e.getSession() == sess, "session");
sock.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
