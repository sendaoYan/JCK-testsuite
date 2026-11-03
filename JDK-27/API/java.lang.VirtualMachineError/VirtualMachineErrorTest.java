/*
 * @test
 * @summary java.lang.VirtualMachineError conformance: white-box structure + black-box behavior.
 * @run main VirtualMachineErrorTest
 */

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class VirtualMachineErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": got " + t.getClass().getName());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.VirtualMachineError");
        check(!c.isInterface(), "not an interface");
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(c.getSuperclass() == Error.class, "superclass is Error");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getDeclaredConstructors().length == 4, "4 public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("java.lang.VirtualMachineError");

        VirtualMachineError e0 = new VirtualMachineError() {};
        check(e0.getMessage() == null, "no-arg ctor has null message");
        { VirtualMachineError e = new VirtualMachineError("boom") {}; check("boom".equals(e.getMessage()), "(String) ctor message"); }
        { Exception cause = new Exception("cause"); VirtualMachineError e = new VirtualMachineError("boom", cause) {}; check("boom".equals(e.getMessage()) && e.getCause() == cause, "(String,Throwable) ctor"); }
        { Exception cause = new Exception("cause"); VirtualMachineError e = new VirtualMachineError(cause) {}; check(e.getCause() == cause, "(Throwable) ctor cause"); }

        // common Throwable contract
        VirtualMachineError t = new VirtualMachineError() {};
        check(t.getCause() == null, "fresh cause null");
        check(t.initCause(new Exception("cause")) == t, "initCause returns this");
        check(t.getCause() != null && "cause".equals(t.getCause().getMessage()), "cause set");
        expect(java.lang.IllegalStateException.class, () -> t.initCause(new Exception("x")), "initCause twice");
        Throwable sup = new Exception("suppressed");
        t.addSuppressed(sup);
        check(t.getSuppressed().length == 1 && t.getSuppressed()[0] == sup, "addSuppressed/getSuppressed");
        check(t.getStackTrace().length >= 1, "getStackTrace non-empty");
        check(t.toString().startsWith(t.getClass().getName()), "toString starts with runtime class name");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        t.printStackTrace(new PrintStream(bos));
        check(bos.size() > 0, "printStackTrace(PrintStream) writes");
        check(t.getLocalizedMessage() == t.getMessage(), "getLocalizedMessage == getMessage");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
