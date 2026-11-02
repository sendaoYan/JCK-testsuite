/*
 * @test
 * @summary jdk.dynalink.DynamicLinker conformance: create linker and link a call site.
 * @run main DynamicLinkerTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class DynamicLinkerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        DynamicLinkerFactory f = new DynamicLinkerFactory();
        DynamicLinker dl = f.createLinker();
        check(dl != null, "linker");
        check(dl.getLinkerServices() != null, "linker services");
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleRelinkableCallSite cs = new SimpleRelinkableCallSite(csd);
        check(dl.link(cs) == cs, "link returns site");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
