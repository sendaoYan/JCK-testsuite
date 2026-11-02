/*
 * @test
 * @summary java.net.SocketPermission conformance: API type verification.
 * @run main SocketPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SocketPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.SocketPermission sp = new java.net.SocketPermission("localhost:80", "connect");
check(sp.getActions() != null && sp.getActions().contains("connect"), "actions");
check(sp.implies(new java.net.SocketPermission("localhost:80", "connect")), "implies");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
