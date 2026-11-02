/*
 * @test
 * @summary jdk.dynalink.StandardOperation conformance: operation enum constants.
 * @run main StandardOperationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class StandardOperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(StandardOperation.GET != null, "GET");
        check(StandardOperation.SET != null, "SET");
        check(StandardOperation.REMOVE != null, "REMOVE");
        check(StandardOperation.CALL != null, "CALL");
        check(StandardOperation.NEW != null, "NEW");
        check(StandardOperation.values().length == 5, "5 constants");
        check(StandardOperation.valueOf("CALL") == StandardOperation.CALL, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
