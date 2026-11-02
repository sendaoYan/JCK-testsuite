/*
 * @test
 * @summary javax.naming.spi.NamingManager conformance: API type verification.
 * @run main NamingManagerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class NamingManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(!javax.naming.spi.NamingManager.hasInitialContextFactoryBuilder(), "no ICFB");
check(javax.naming.spi.NamingManager.CPE != null, "CPE constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
