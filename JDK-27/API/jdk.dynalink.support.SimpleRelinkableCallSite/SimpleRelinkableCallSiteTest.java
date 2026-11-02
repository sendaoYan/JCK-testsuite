/*
 * @test
 * @summary jdk.dynalink.support.SimpleRelinkableCallSite conformance: simple relinkable call site.
 * @run main SimpleRelinkableCallSiteTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class SimpleRelinkableCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(csd);
        check(cs instanceof RelinkableCallSite, "relinkable");
        check(cs.type().equals(MethodType.methodType(Object.class, Object.class)), "type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
