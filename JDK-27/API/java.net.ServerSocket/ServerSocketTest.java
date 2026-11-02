/*
 * @test
 * @summary java.net.ServerSocket conformance: API type verification.
 * @run main ServerSocketTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ServerSocketTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.ServerSocket ss = new java.net.ServerSocket();
check(!ss.isBound(), "unbound");
check(!ss.isClosed(), "not closed");
ss.setReuseAddress(false);
check(!ss.getReuseAddress(), "reuse");
ss.close();
check(ss.isClosed(), "closed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
