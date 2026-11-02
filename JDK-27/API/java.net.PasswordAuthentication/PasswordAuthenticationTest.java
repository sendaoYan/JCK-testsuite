/*
 * @test
 * @summary java.net.PasswordAuthentication conformance: API type verification.
 * @run main PasswordAuthenticationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class PasswordAuthenticationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.PasswordAuthentication pa = new java.net.PasswordAuthentication("user", new char[]{'p','w'});
check(pa.getUserName().equals("user"), "username");
check(java.util.Arrays.equals(pa.getPassword(), new char[]{'p','w'}), "password");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
