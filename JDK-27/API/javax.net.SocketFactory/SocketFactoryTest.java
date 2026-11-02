/*
 * @test
 * @summary javax.net.SocketFactory conformance: API type verification.
 * @run main SocketFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SocketFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.SocketFactory f = javax.net.SocketFactory.getDefault();
check(f != null, "getDefault");
check(f instanceof javax.net.SocketFactory, "instance");
java.net.Socket s = f.createSocket();
check(!s.isConnected(), "unconnected");
s.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
