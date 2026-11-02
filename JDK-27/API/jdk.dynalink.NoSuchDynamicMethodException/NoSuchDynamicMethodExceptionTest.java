/*
 * @test
 * @summary jdk.dynalink.NoSuchDynamicMethodException conformance: exception message and hierarchy.
 * @run main NoSuchDynamicMethodExceptionTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class NoSuchDynamicMethodExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        NoSuchDynamicMethodException e = new NoSuchDynamicMethodException("msg");
        check(e.getMessage().equals("msg"), "message");
        check(e instanceof RuntimeException, "runtime exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
