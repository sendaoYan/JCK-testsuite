/*
 * @test
 * @summary jdk.jshell.spi.SPIResolutionException conformance: spi resolution exception.
 * @run main SPIResolutionExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class SPIResolutionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.spi.SPIResolutionException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        { boolean f=false; for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals("id")) f=true; check(f, "has id"); }
    }

    static void testBlackBox() throws Exception {

        SPIResolutionException e = new SPIResolutionException(42);
        check(e.id() == 42, "id");
        check(e instanceof RuntimeException, "runtime exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
