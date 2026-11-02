/*
 * @test
 * @summary jdk.dynalink.NamedOperation conformance: named operation.
 * @run main NamedOperationTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class NamedOperationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        NamedOperation op = new NamedOperation(StandardOperation.GET, "name");
        check(op.getBaseOperation() == StandardOperation.GET, "base op");
        check(op.getName().equals("name"), "name");
        check(op.changeName("x").getName().equals("x"), "changeName");
        check(NamedOperation.getBaseOperation(op) == StandardOperation.GET, "static getBaseOperation");
        check(NamedOperation.getName(op).equals("name"), "static getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
