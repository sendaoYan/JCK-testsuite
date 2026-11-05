/*
 * @test
 * @summary javax.rmi.CORBA.Tie conformance: full interface face + contract exercised via implementation.
 * @run main TieTest
 */

import java.lang.reflect.*;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import javax.rmi.CORBA.Tie;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.portable.*;

public class TieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    /** recording Tie implementation: exercises every method of the contract */
    static class RecordingTie implements Tie {
        Remote target;
        ORB orb;
        int orbCalls, deactivateCalls;
        boolean deactivateThrows;

        public Object thisObject() { return null; }
        public void deactivate() throws NoSuchObjectException {
            deactivateCalls++;
            if (deactivateThrows) throw new NoSuchObjectException("not active");
        }
        public ORB orb() { return orb; }
        public void orb(ORB orb) { orbCalls++; this.orb = orb; }
        public void setTarget(Remote target) { this.target = target; }
        public Remote getTarget() { return target; }
        public OutputStream _invoke(String method, InputStream in, ResponseHandler handler) {
            return null;
        }
        public String[] _ids() { return new String[]{"IDL:RecordingTie:1.0"}; }
    }

    static class MyRemote implements Remote {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.Tie");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Class.forName("org.omg.CORBA.portable.InvokeHandler").isAssignableFrom(c),
                "implements/extends org.omg.CORBA.portable.InvokeHandler");
        check(hasMethod(c, "deactivate"), "declares deactivate");
        check(hasMethod(c, "getTarget"), "declares getTarget");
        check(hasMethod(c, "orb"), "declares orb");
        // exact 6-method face with precise signatures
        check(c.getDeclaredMethods().length == 6, "exactly 6 declared methods (got " + c.getDeclaredMethods().length + ")");
        check(hasOwn(c, "thisObject"), "Object thisObject()");
        check(hasOwn(c, "deactivate"), "void deactivate() throws NoSuchObjectException");
        check(hasOwn(c, "orb"), "ORB orb()");
        check(hasOwn(c, "setTarget", Remote.class), "void setTarget(Remote)");
        check(hasOwn(c, "getTarget"), "Remote getTarget()");
        // every declared method is public abstract
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
        }
        // throws clauses of the checked exceptions
        check(c.getDeclaredMethod("deactivate").getExceptionTypes().length == 1
                && c.getDeclaredMethod("deactivate").getExceptionTypes()[0] == NoSuchObjectException.class,
                "deactivate() throws NoSuchObjectException only");
        // inherited from InvokeHandler: _invoke
        check(hasMethod(InvokeHandler.class, "_invoke"), "inherits _invoke from InvokeHandler");
    }

    static boolean hasOwn(Class<?> c, String name, Class<?>... params) {
        try { c.getDeclaredMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 2: black-box (contract exercised via a recording implementation) ----
    static void testBlackBox() throws Exception {
        RecordingTie t = new RecordingTie();
        // initial state: no target, no orb
        check(t.getTarget() == null, "fresh tie has no target");
        check(t.orb() == null, "fresh tie has no orb");
        check(t.thisObject() == null, "thisObject of an unconnected tie is null (no CORBA object yet)");
        check(t._ids().length == 1, "_ids from the tie");

        // setTarget/getTarget round trip retains identity
        MyRemote r = new MyRemote();
        t.setTarget(r);
        check(t.getTarget() == r, "setTarget/getTarget round trip");
        t.setTarget(null);
        check(t.getTarget() == null, "setTarget(null) clears the target");

        // orb(ORB) setter / orb() getter round trip
        ORB orb = ORB.init(new String[0], null);
        t.orb(orb);
        check(t.orbCalls == 1, "orb(ORB) invoked once");
        check(t.orb() == orb, "orb()/orb(ORB) round trip");

        // deactivate: both the quiet and the throwing path (NoSuchObjectException per contract)
        t.deactivate();
        check(t.deactivateCalls == 1, "deactivate() invoked");
        t.deactivateThrows = true;
        try {
            t.deactivate();
            check(false, "expected NoSuchObjectException from throwing deactivate");
        } catch (NoSuchObjectException expected) {
            check(t.deactivateCalls == 2, "deactivate() reached the throwing path");
        }

        // a Tie is usable wherever an InvokeHandler is required
        check(t instanceof InvokeHandler, "Tie is an InvokeHandler");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
