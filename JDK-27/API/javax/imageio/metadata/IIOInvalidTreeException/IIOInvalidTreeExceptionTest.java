/*
 * @test
 * @summary javax.imageio.metadata.IIOInvalidTreeException conformance: white-box structure + black-box behavior.
 * @run main IIOInvalidTreeExceptionTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javax.imageio.IIOException;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;

public class IIOInvalidTreeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOInvalidTreeException");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is not interface");
        check(!c.isAnnotation(), "is not annotation");
        check(!c.isEnum(), "is not enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.imageio.IIOException"),
              "superclass is IIOException");
        // protected offendingNode field, type org.w3c.dom.Node
        Field f = c.getDeclaredField("offendingNode");
        check(Modifier.isProtected(f.getModifiers()), "offendingNode is protected");
        check(f.getType() == org.w3c.dom.Node.class, "offendingNode type is Node");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IIOMetadataNode node = new IIOMetadataNode("broken");
        String msg = "bad tree shape";

        // (String, Node) constructor: message, offending node, IIOException supertype
        IIOInvalidTreeException e1 = new IIOInvalidTreeException(msg, node);
        check(e1 instanceof IIOException, "is an IIOException");
        check(msg.equals(e1.getMessage()), "getMessage round-trip");
        check(e1.getOffendingNode() == node, "getOffendingNode identity (2-arg ctor)");
        check(e1.getCause() == null, "no cause (2-arg ctor)");
        check(e1.toString().contains(msg), "toString contains message");

        // (String, Throwable, Node) constructor: message, cause, offending node
        Throwable cause = new IllegalStateException("root cause");
        IIOInvalidTreeException e2 = new IIOInvalidTreeException(msg, cause, node);
        check(msg.equals(e2.getMessage()), "getMessage round-trip (3-arg ctor)");
        check(e2.getCause() == cause, "getCause identity (3-arg ctor)");
        check(e2.getOffendingNode() == node, "getOffendingNode identity (3-arg ctor)");

        // null offending node is legal and reported as null
        IIOInvalidTreeException e3 = new IIOInvalidTreeException("null node", (Node) null);
        check(e3.getOffendingNode() == null, "null offending node stays null");
        check(e3.getMessage().equals("null node"), "message with null node");

        IIOInvalidTreeException e4 = new IIOInvalidTreeException("null node w/ cause", null, null);
        check(e4.getOffendingNode() == null, "null offending node (3-arg ctor)");
        check(e4.getCause() == null, "null cause stays null");
        // structural contract: exactly two public constructors, no single-String variant (javap)
        java.lang.reflect.Constructor<?>[] ctors = IIOInvalidTreeException.class.getConstructors();
        check(ctors.length == 2, "exactly two public constructors");
        boolean twoArg = false, threeArg = false;
        for (java.lang.reflect.Constructor<?> k : ctors) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 2 && p[0] == String.class && p[1] == Node.class) twoArg = true;
            if (p.length == 3 && p[0] == String.class && p[1] == Throwable.class && p[2] == Node.class) threeArg = true;
        }
        check(twoArg, "(String, Node) constructor");
        check(threeArg, "(String, Throwable, Node) constructor");
        check(java.io.IOException.class.isAssignableFrom(IIOInvalidTreeException.class), "assignable to IOException");
        // cause lifecycle
        RuntimeException late = new RuntimeException("late");
        check(e1.initCause(late) == e1, "initCause returns this (2-arg ctor leaves cause unset)");
        check(e1.getCause() == late, "initCause round-trip");
        try {
            e1.initCause(new Exception());
            check(false, "second initCause should throw IllegalStateException");
        } catch (IllegalStateException expected) {
            check(true, "second initCause throws IllegalStateException");
        }
        try {
            e2.initCause(null);
            check(false, "initCause after ctor-supplied cause should throw");
        } catch (IllegalStateException expected) {
            check(true, "initCause after constructor-supplied cause throws IllegalStateException");
        }
        // Throwable mechanics
        IIOInvalidTreeException sup = new IIOInvalidTreeException("s", node);
        sup.addSuppressed(new RuntimeException("r1"));
        check(sup.getSuppressed().length == 1, "addSuppressed/getSuppressed round-trip");
        check(e1.toString().startsWith("javax.imageio.metadata.IIOInvalidTreeException: " + msg),
              "toString format class: message");
        check(e1.getLocalizedMessage().equals(msg), "getLocalizedMessage equals message");
        IIOInvalidTreeException nullMsg = new IIOInvalidTreeException(null, node);
        check(nullMsg.getMessage() == null, "null message round-trips");
        check(e2.getCause().getMessage().equals("root cause"), "cause message readable");
        // hierarchy and protected field
        check(IIOInvalidTreeException.class.getSuperclass() == IIOException.class, "direct superclass IIOException");
        check(IIOException.class.getSuperclass() == java.io.IOException.class, "IIOException extends IOException");
        try {
            java.lang.reflect.Field of = IIOInvalidTreeException.class.getDeclaredField("offendingNode");
            check(java.lang.reflect.Modifier.isProtected(of.getModifiers()), "offendingNode is protected");
            check(of.getType() == Node.class, "offendingNode type is Node");
        } catch (NoSuchFieldException bad) {
            check(false, "offendingNode field exists: " + bad);
        }
        IIOInvalidTreeException a = new IIOInvalidTreeException(msg, node);
        IIOInvalidTreeException b = new IIOInvalidTreeException(msg, node);
        check(a != b, "distinct instances");
        check(a.getMessage().equals(b.getMessage()), "same message");
        check(a.getOffendingNode() == b.getOffendingNode(), "same offending node reference");
        check(e1.fillInStackTrace() == e1, "fillInStackTrace returns this");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
