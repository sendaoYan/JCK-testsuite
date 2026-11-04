/*
 * @test
 * @summary java.net.Authenticator.RequestorType conformance: API type verification.
 * @run main RequestorTypeTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class RequestorTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(java.net.Authenticator.RequestorType.PROXY.ordinal() == 0, "PROXY ordinal");
check(java.net.Authenticator.RequestorType.valueOf("SERVER") == java.net.Authenticator.RequestorType.SERVER, "valueOf");
check(java.net.Authenticator.RequestorType.values().length == 2, "2 values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
