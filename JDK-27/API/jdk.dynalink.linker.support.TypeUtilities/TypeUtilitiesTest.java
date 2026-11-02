/*
 * @test
 * @summary jdk.dynalink.linker.support.TypeUtilities conformance: type utility predicates.
 * @run main TypeUtilitiesTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class TypeUtilitiesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        check(TypeUtilities.isSubtype(String.class, Object.class), "isSubtype");
        check(TypeUtilities.getWrapperType(int.class) == Integer.class, "wrapper type");
        check(TypeUtilities.getPrimitiveTypeByName("int") == int.class, "primitive by name");
        check(TypeUtilities.isWrapperType(Integer.class), "isWrapperType");
        check(TypeUtilities.getPrimitiveType(Integer.class) == int.class, "primitive type");
        check(TypeUtilities.isMethodInvocationConvertible(int.class, Integer.class), "method invocation convertible");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
