/*
 * @test
 * @summary java.net.DatagramSocket conformance: API type verification.
 * @run main DatagramSocketTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class DatagramSocketTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.DatagramSocket s = new java.net.DatagramSocket();
check(s.isBound(), "bound");
check(s.getLocalPort() >= 0, "local port");
check(s.getLocalAddress() != null, "local address");
s.close();
check(s.isClosed(), "closed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
