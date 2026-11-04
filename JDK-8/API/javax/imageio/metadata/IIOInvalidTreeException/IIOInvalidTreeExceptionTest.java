/*
 * @test
 * @summary javax.imageio.metadata.IIOInvalidTreeException conformance: behavioral.
 * @run main IIOInvalidTreeExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.IIOException;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Node;

public class IIOInvalidTreeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        org.w3c.dom.Node n = new javax.imageio.metadata.IIOMetadataNode("root");
        javax.imageio.metadata.IIOInvalidTreeException e = new javax.imageio.metadata.IIOInvalidTreeException("bad", n);
        check(e.getOffendingNode() == n, "offendingNode");
        check(e instanceof javax.imageio.IIOException, "is IIOException");
        javax.imageio.metadata.IIOInvalidTreeException e2 = new javax.imageio.metadata.IIOInvalidTreeException("m", new Exception(), n);
        check(e2.getOffendingNode() == n, "offendingNode2");
        testStructure();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // structural contract: hierarchy, protected field, exactly two public constructors (javap)
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.imageio.metadata.IIOInvalidTreeException");
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(c.getSuperclass().getName().equals("javax.imageio.IIOException"), "extends IIOException");
        check(c.getSuperclass().getSuperclass().getName().equals("java.io.IOException"), "IIOException extends IOException");
        Field f = c.getDeclaredField("offendingNode");
        check(Modifier.isProtected(f.getModifiers()), "offendingNode is protected");
        check(f.getType() == Node.class, "offendingNode type is org.w3c.dom.Node");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "exactly two public constructors (no single-String ctor)");
        boolean twoArg = false, threeArg = false;
        for (Constructor<?> k : ctors) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 2 && p[0] == String.class && p[1] == Node.class) twoArg = true;
            if (p.length == 3 && p[0] == String.class && p[1] == Throwable.class && p[2] == Node.class) threeArg = true;
        }
        check(twoArg, "(String, Node) constructor");
        check(threeArg, "(String, Throwable, Node) constructor");
        check(c.getMethod("getOffendingNode").getReturnType() == Node.class, "getOffendingNode returns Node");
        check(IIOException.class.isAssignableFrom(c), "assignable to IIOException");
        check(java.io.IOException.class.isAssignableFrom(c), "assignable to IOException");
    }

    static void testBehavior() throws Exception {
        IIOMetadataNode node = new IIOMetadataNode("broken");
        IIOMetadataNode other = new IIOMetadataNode("other");
        String msg = "bad tree shape";

        // (String, Node)
        IIOInvalidTreeException e1 = new IIOInvalidTreeException(msg, node);
        check(e1 instanceof IIOException, "is an IIOException");
        check(e1 instanceof java.io.IOException, "is an IOException");
        check(msg.equals(e1.getMessage()), "getMessage round-trip");
        check(e1.getLocalizedMessage().equals(msg), "getLocalizedMessage equals message");
        check(e1.getOffendingNode() == node, "getOffendingNode identity (2-arg ctor)");
        check(e1.getCause() == null, "no cause (2-arg ctor)");
        check(e1.toString().startsWith("javax.imageio.metadata.IIOInvalidTreeException: " + msg),
              "toString format class: message");
        // cause may be supplied later via initCause (not fixed by the constructor)
        RuntimeException rc = new RuntimeException("late");
        check(e1.initCause(rc) == e1, "initCause returns this");
        check(e1.getCause() == rc, "initCause round-trip");
        try {
            e1.initCause(new Exception());
            check(false, "second initCause should throw IllegalStateException");
        } catch (IllegalStateException expected) {
            check(true, "second initCause throws IllegalStateException");
        }

        // (String, Throwable, Node)
        Throwable cause = new IllegalStateException("root cause");
        IIOInvalidTreeException e2 = new IIOInvalidTreeException(msg, cause, node);
        check(msg.equals(e2.getMessage()), "getMessage round-trip (3-arg ctor)");
        check(e2.getCause() == cause, "getCause identity (3-arg ctor)");
        check(e2.getOffendingNode() == node, "getOffendingNode identity (3-arg ctor)");
        try {
            e2.initCause(null);
            check(false, "initCause after ctor-supplied cause should throw");
        } catch (IllegalStateException expected) {
            check(true, "initCause after constructor-supplied cause throws IllegalStateException");
        }
        // stack trace and suppressed behave like any Throwable
        StackTraceElement[] st = e2.getStackTrace();
        check(st != null, "stack trace accessible");
        IIOInvalidTreeException sup = new IIOInvalidTreeException("s", node);
        sup.addSuppressed(new RuntimeException("r1"));
        check(sup.getSuppressed().length == 1, "addSuppressed/getSuppressed round-trip");

        // null arguments are legal and round-trip
        IIOInvalidTreeException e3 = new IIOInvalidTreeException("null node", (Node) null);
        check(e3.getOffendingNode() == null, "null offending node stays null");
        check("null node".equals(e3.getMessage()), "message with null node");
        IIOInvalidTreeException e4 = new IIOInvalidTreeException("n", null, null);
        check(e4.getOffendingNode() == null, "null offending node (3-arg ctor)");
        check(e4.getCause() == null, "null cause stays null");
        IIOInvalidTreeException e5 = new IIOInvalidTreeException(null, node);
        check(e5.getMessage() == null, "null message round-trips");
        check(e5.getOffendingNode() == node, "offending node with null message");
        // the offending node is mutable state shared with the caller
        e3.getOffendingNode();
        IIOInvalidTreeException e6 = new IIOInvalidTreeException("m", other);
        other.setAttribute("k", "v");
        check(e6.getOffendingNode() == other && "v".equals(((org.w3c.dom.Element) e6.getOffendingNode()).getAttribute("k")),
              "offending node is the live caller node");
        // two instances with the same arguments are distinct but report equal content
        IIOInvalidTreeException a = new IIOInvalidTreeException(msg, node);
        IIOInvalidTreeException b = new IIOInvalidTreeException(msg, node);
        check(a != b, "distinct instances");
        check(a.getMessage().equals(b.getMessage()), "same message");
        check(a.getOffendingNode() == b.getOffendingNode(), "same offending node reference");
        check("root cause".equals(e2.getCause().getMessage()), "cause message readable");
        check(e1.fillInStackTrace() == e1, "fillInStackTrace returns this");
    }
}
