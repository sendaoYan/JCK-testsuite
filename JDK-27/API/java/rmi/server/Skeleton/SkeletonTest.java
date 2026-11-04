/*
 * @test
 * @summary java.rmi.server.Skeleton conformance: white-box structure + black-box behavior.
 * @run main SkeletonTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.Operation;
import java.rmi.server.Skeleton;

public class SkeletonTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.Skeleton");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
        check(c.getMethod("getOperations") != null, "getOperations() declared");
        check(c.getMethod("dispatch", java.rmi.Remote.class, java.rmi.server.RemoteCall.class, int.class, long.class) != null, "dispatch declared");
    }

    static void testBlackBox() throws Exception {
        final Operation[] ops = { new Operation("opA"), new Operation("opB") };
        final boolean[] dispatched = new boolean[1];
        final Object[] dispatchArgs = new Object[4];

        Skeleton sk = new Skeleton() {
            public Operation[] getOperations() { return ops; }
            public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int op, long hash) {
                dispatched[0] = true;
                dispatchArgs[0] = obj; dispatchArgs[1] = call; dispatchArgs[2] = op; dispatchArgs[3] = hash;
            }
        };

        check(sk instanceof Skeleton, "anonymous instanceof Skeleton");
        check(sk.getOperations() == ops, "getOperations returns the array");
        check(sk.getOperations().length == 2, "getOperations length 2");

        java.rmi.Remote target = new java.rmi.Remote() { };
        sk.dispatch(target, null, 1, 12345L);
        check(dispatched[0], "dispatch invoked");
        check(dispatchArgs[0] == target, "dispatch receives remote object");
        check(((Integer) dispatchArgs[2]) == 1, "dispatch receives op number");
        check(((Long) dispatchArgs[3]) == 12345L, "dispatch receives hash");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
