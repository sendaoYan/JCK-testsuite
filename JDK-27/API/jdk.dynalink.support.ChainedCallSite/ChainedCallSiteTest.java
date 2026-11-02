/*
 * @test
 * @summary jdk.dynalink.support.ChainedCallSite conformance: chained call site.
 * @run main ChainedCallSiteTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class ChainedCallSiteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        ChainedCallSite cs = new ChainedCallSite(csd);
        check(cs instanceof RelinkableCallSite, "relinkable");
        check(cs.type().equals(MethodType.methodType(Object.class, Object.class)), "type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
