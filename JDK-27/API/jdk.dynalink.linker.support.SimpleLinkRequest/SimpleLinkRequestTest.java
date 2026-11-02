/*
 * @test
 * @summary jdk.dynalink.linker.support.SimpleLinkRequest conformance: link request fields.
 * @run main SimpleLinkRequestTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class SimpleLinkRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        CallSiteDescriptor csd = new CallSiteDescriptor(MethodHandles.lookup(), StandardOperation.GET, MethodType.methodType(Object.class, Object.class));
        SimpleLinkRequest r = new SimpleLinkRequest(csd, false, "obj");
        check(r.getReceiver().equals("obj"), "receiver");
        check(r.getArguments().length == 1, "arguments");
        check(r.getCallSiteDescriptor() == csd, "descriptor");
        check(!r.isCallSiteUnstable(), "stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
