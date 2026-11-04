/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.StoppedException conformance: stopped exception.
 * @run main StoppedExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class StoppedExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        StoppedException e = new StoppedException();
        check(e instanceof RunException, "extends RunException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
