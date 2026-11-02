/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.ClassInstallException conformance: class install exception.
 * @run main ClassInstallExceptionTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class ClassInstallExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ClassInstallException e = new ClassInstallException("msg", new boolean[]{true, false});
        check(e.getMessage().equals("msg"), "message");
        check(e.installed().length == 2 && e.installed()[0] && !e.installed()[1], "installed");
        check(e instanceof ExecutionControlException, "hierarchy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
