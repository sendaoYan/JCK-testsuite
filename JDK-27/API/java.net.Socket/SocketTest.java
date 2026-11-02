/*
 * @test
 * @summary java.net.Socket conformance: API type verification.
 * @run main SocketTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SocketTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.Socket s = new java.net.Socket();
check(!s.isConnected(), "not connected");
check(!s.isBound(), "not bound");
check(!s.isClosed(), "not closed");
s.setTcpNoDelay(true);
check(s.getTcpNoDelay(), "tcpNoDelay");
s.close();
check(s.isClosed(), "closed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
