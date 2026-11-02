/*
 * @test
 * @summary javax.naming.spi.DirStateFactory.Result conformance: API type verification.
 * @run main ResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.naming.directory.BasicAttributes as = new javax.naming.directory.BasicAttributes("cn", "v");
javax.naming.spi.DirStateFactory.Result r = new javax.naming.spi.DirStateFactory.Result("obj", as);
check("obj".equals(r.getObject()), "object");
check(r.getAttributes() == as, "attributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
