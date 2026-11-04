/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.ClassBytecodes conformance: class bytecodes record.
 * @run main ClassBytecodesTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class ClassBytecodesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        ClassBytecodes cb = new ClassBytecodes("com.Foo", new byte[]{1, 2, 3});
        check(cb.name().equals("com.Foo"), "name");
        check(cb.bytecodes().length == 3, "bytecodes");
        check(cb instanceof java.io.Serializable, "serializable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
