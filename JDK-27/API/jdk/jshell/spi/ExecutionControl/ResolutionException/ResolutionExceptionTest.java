/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.ResolutionException conformance: resolution exception.
 * @run main ResolutionExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class ResolutionExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: message format and stack ownership ----
    static void testDeepSemantics() throws Exception {
        StackTraceElement[] st = new StackTraceElement[]{ new StackTraceElement("A", "b", "A.java", 1) };
        ResolutionException e = new ResolutionException(42, st);
        check(e.getMessage().equals("resolution exception: 42"), "message embeds the id");
        check(e.getStackTrace().length == 1 && e.getStackTrace()[0].getClassName().equals("A"),
              "stack trace installed from the constructor");
        check(new ResolutionException(0, new StackTraceElement[0]).id() == 0, "id 0 legal");
        check(new ResolutionException(-3, new StackTraceElement[0]).getMessage().equals("resolution exception: -3"),
              "negative id rendered in message");
    }

    public static void main(String[] args) throws Exception {
        ResolutionException e = new ResolutionException(42, new StackTraceElement[0]);
        check(e.id() == 42, "id");
        check(e instanceof RunException, "extends RunException");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
