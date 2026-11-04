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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
