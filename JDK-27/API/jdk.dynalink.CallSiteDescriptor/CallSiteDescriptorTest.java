/*
 * @test
 * @summary jdk.dynalink.CallSiteDescriptor conformance: construct and inspect descriptor.
 * @run main CallSiteDescriptorTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.util.*;

public class CallSiteDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        MethodType mt = MethodType.methodType(Object.class, Object.class);
        CallSiteDescriptor csd = new CallSiteDescriptor(lk, StandardOperation.GET, mt);
        check(csd.getOperation() == StandardOperation.GET, "operation");
        check(csd.getMethodType().equals(mt), "method type");
        check(csd.changeOperation(StandardOperation.SET).getOperation() == StandardOperation.SET, "changeOperation");
        check(csd.changeMethodType(mt).getMethodType().equals(mt), "changeMethodType");
        check(csd.hashCode() == csd.hashCode(), "hashCode stable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
