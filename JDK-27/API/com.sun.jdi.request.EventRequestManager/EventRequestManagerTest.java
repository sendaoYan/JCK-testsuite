/*
 * @test
 * @summary com.sun.jdi.request.EventRequestManager conformance: interface hierarchy and methods.
 * @run main EventRequestManagerTest
 */

import java.lang.reflect.*;
import java.util.*;

public class EventRequestManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.request.EventRequestManager");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Mirror"), "extends com.sun.jdi.Mirror");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("createClassPrepareRequest"), "method createClassPrepareRequest");
        check(dm.contains("createClassUnloadRequest"), "method createClassUnloadRequest");
        check(dm.contains("createThreadStartRequest"), "method createThreadStartRequest");
        check(dm.contains("createThreadDeathRequest"), "method createThreadDeathRequest");
        check(dm.contains("createExceptionRequest"), "method createExceptionRequest");
        check(dm.contains("createMethodEntryRequest"), "method createMethodEntryRequest");
        check(dm.contains("createMethodExitRequest"), "method createMethodExitRequest");
        check(dm.contains("createMonitorContendedEnterRequest"), "method createMonitorContendedEnterRequest");
        check(dm.contains("createMonitorContendedEnteredRequest"), "method createMonitorContendedEnteredRequest");
        check(dm.contains("createMonitorWaitRequest"), "method createMonitorWaitRequest");
        check(dm.contains("createMonitorWaitedRequest"), "method createMonitorWaitedRequest");
        check(dm.contains("createStepRequest"), "method createStepRequest");
        check(dm.contains("createBreakpointRequest"), "method createBreakpointRequest");
        check(dm.contains("createAccessWatchpointRequest"), "method createAccessWatchpointRequest");
        check(dm.contains("createModificationWatchpointRequest"), "method createModificationWatchpointRequest");
        check(dm.contains("createVMDeathRequest"), "method createVMDeathRequest");
        check(dm.contains("deleteEventRequest"), "method deleteEventRequest");
        check(dm.contains("deleteEventRequests"), "method deleteEventRequests");
        check(dm.contains("deleteAllBreakpoints"), "method deleteAllBreakpoints");
        check(dm.contains("stepRequests"), "method stepRequests");
        check(dm.contains("classPrepareRequests"), "method classPrepareRequests");
        check(dm.contains("classUnloadRequests"), "method classUnloadRequests");
        check(dm.contains("threadStartRequests"), "method threadStartRequests");
        check(dm.contains("threadDeathRequests"), "method threadDeathRequests");
        check(dm.contains("exceptionRequests"), "method exceptionRequests");
        check(dm.contains("breakpointRequests"), "method breakpointRequests");
        check(dm.contains("accessWatchpointRequests"), "method accessWatchpointRequests");
        check(dm.contains("modificationWatchpointRequests"), "method modificationWatchpointRequests");
        check(dm.contains("methodEntryRequests"), "method methodEntryRequests");
        check(dm.contains("methodExitRequests"), "method methodExitRequests");
        check(dm.contains("monitorContendedEnterRequests"), "method monitorContendedEnterRequests");
        check(dm.contains("monitorContendedEnteredRequests"), "method monitorContendedEnteredRequests");
        check(dm.contains("monitorWaitRequests"), "method monitorWaitRequests");
        check(dm.contains("monitorWaitedRequests"), "method monitorWaitedRequests");
        check(dm.contains("vmDeathRequests"), "method vmDeathRequests");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
