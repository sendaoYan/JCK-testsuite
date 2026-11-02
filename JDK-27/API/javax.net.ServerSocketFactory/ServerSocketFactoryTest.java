/*
 * @test
 * @summary javax.net.ServerSocketFactory conformance: API type verification.
 * @run main ServerSocketFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ServerSocketFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ServerSocketFactory f = javax.net.ServerSocketFactory.getDefault();
check(f != null, "getDefault");
check(f instanceof javax.net.ServerSocketFactory, "instance");
java.net.ServerSocket ss = f.createServerSocket();
check(!ss.isBound(), "unbound");
ss.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
