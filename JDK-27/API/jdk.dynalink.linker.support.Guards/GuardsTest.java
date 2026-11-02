/*
 * @test
 * @summary jdk.dynalink.linker.support.Guards conformance: guard factory methods.
 * @run main GuardsTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class GuardsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(Guards.isNull() != null, "isNull");
        check(Guards.isNotNull() != null, "isNotNull");
        check(Guards.getInstanceOfGuard(String.class) != null, "instanceof guard");
        check(Guards.getIdentityGuard("x") != null, "identity guard");
        check(Guards.isOfClass(String.class, MethodType.methodType(boolean.class, Object.class)) != null, "isOfClass");
        check(Guards.getClassGuard(String.class) != null, "class guard");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
