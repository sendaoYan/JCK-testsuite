/*
 * @test
 * @summary java.rmi.server.RemoteCall conformance: white-box structure + black-box behavior.
 * @run main RemoteCallTest
 */

import java.io.*;
import java.lang.reflect.Modifier;
import java.rmi.server.RemoteCall;

public class RemoteCallTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RemoteCall");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        for (String m : new String[]{"getOutputStream", "releaseOutputStream", "getInputStream",
                                     "releaseInputStream", "getResultStream", "executeCall", "done"}) {
            check(names.contains(m), m + " declared");
        }
    }

    static void testBlackBox() throws Exception {
        final ObjectOutput sentOut = null;
        final ObjectInput sentIn = null;
        final ObjectOutput sentResult = null;
        final boolean[] executed = new boolean[1];

        RemoteCall rc = new RemoteCall() {
            public ObjectOutput getOutputStream() { return sentOut; }
            public void releaseOutputStream() { }
            public ObjectInput getInputStream() { return sentIn; }
            public void releaseInputStream() { }
            public ObjectOutput getResultStream(boolean success) { return sentResult; }
            public void executeCall() { executed[0] = true; }
            public void done() { }
        };

        check(rc instanceof RemoteCall, "anonymous instanceof RemoteCall");
        check(rc.getOutputStream() == sentOut, "getOutputStream returns sentinel");
        check(rc.getInputStream() == sentIn, "getInputStream returns sentinel");
        check(rc.getResultStream(true) == sentResult, "getResultStream returns sentinel");
        rc.releaseOutputStream();
        rc.releaseInputStream();
        rc.executeCall();
        check(executed[0], "executeCall invoked");
        rc.done();
        check(true, "release*/executeCall/done callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
