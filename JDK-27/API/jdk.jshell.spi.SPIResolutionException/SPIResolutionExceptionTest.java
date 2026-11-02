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
    public static void main(String[] args) throws Exception {
        SPIResolutionException e = new SPIResolutionException(42);
        check(e.id() == 42, "id");
        check(e instanceof RuntimeException, "runtime exception");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
