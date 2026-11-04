/*
 * @test
 * @summary java.nio.Buffer conformance: white-box structure (abstract, cursor methods
 *          final) plus black-box cursor state machine (position/limit/mark invariants,
 *          flip/rewind/clear, mark discard rules, exception bounds, fluent chaining).
 * @run main BufferTest
 */

import java.lang.reflect.Modifier;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

public class BufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.Buffer");
        check(Modifier.isAbstract(c.getModifiers()), "Buffer is abstract");
        check(c.getSuperclass() == Object.class, "Buffer extends Object");

        // Cursor methods are final (documented): position/limit/mark/reset/clear/flip/
        // rewind/remaining/hasRemaining/capacity.
        String[] finals = {"capacity", "position", "limit", "mark", "reset", "clear",
                "flip", "rewind", "remaining", "hasRemaining"};
        for (String n : finals) {
            java.lang.reflect.Method m;
            try {
                if (n.equals("capacity")) m = c.getMethod("capacity");
                else if (n.equals("remaining")) m = c.getMethod("remaining");
                else if (n.equals("hasRemaining")) m = c.getMethod("hasRemaining");
                else if (n.equals("mark") || n.equals("reset") || n.equals("clear")
                        || n.equals("flip") || n.equals("rewind")) m = c.getMethod(n);
                else if (n.equals("position") || n.equals("limit")) m = c.getMethod(n, int.class);
                else m = null;
            } catch (NoSuchMethodException e) {
                m = null;
            }
            check(m != null && Modifier.isFinal(m.getModifiers()), n + " is final");
        }

        // Abstract query/attribute methods.
        check(hasMethod(c, "isReadOnly"), "has isReadOnly()");
        check(hasMethod(c, "hasArray"), "has hasArray()");
        check(hasMethod(c, "array"), "has array()");
        check(hasMethod(c, "arrayOffset"), "has arrayOffset()");
        check(hasMethod(c, "isDirect"), "has isDirect()");
    }

    static void testBlackBox() {
        Buffer buf = ByteBuffer.allocate(4);
        check(buf.capacity() == 4, "capacity");
        check(buf.position() == 0, "initial position");
        check(buf.limit() == 4, "initial limit");
        check(buf.remaining() == 4, "remaining == limit - position");
        check(buf.hasRemaining(), "hasRemaining initially true");

        // position(int): valid bounds.
        check(buf.position(2) == buf, "position(int) returns this");
        check(buf.position() == 2, "position set to 2");
        check(buf.remaining() == 2, "remaining after position 2");
        checkThrows(IllegalArgumentException.class, () -> buf.position(-1), "position(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> buf.position(5), "position > limit IAE");

        // limit(int): valid bounds; position clamped when limit < position.
        check(buf.limit(3) == buf, "limit(int) returns this");
        check(buf.limit() == 3, "limit set to 3");
        checkThrows(IllegalArgumentException.class, () -> buf.limit(-1), "limit(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> buf.limit(5), "limit > capacity IAE");

        Buffer clamp = ByteBuffer.allocate(4).position(3);
        clamp.limit(1);
        check(clamp.position() == 1, "position clamped when limit < position");

        // mark/reset.
        Buffer mk = ByteBuffer.allocate(4);
        checkThrows(InvalidMarkException.class, () -> mk.reset(), "reset without mark -> InvalidMarkException");
        mk.position(2).mark();
        check(mk.position() == 2, "position after mark unchanged");
        mk.position(3);
        check(mk.reset() == mk, "reset returns this");
        check(mk.position() == 2, "reset restores marked position");

        // mark discarded when position set below mark.
        Buffer md = ByteBuffer.allocate(4).position(3).mark().position(1);
        checkThrows(InvalidMarkException.class, () -> md.reset(), "mark discarded when position set below mark");

        // mark discarded when limit set below mark.
        Buffer ml = ByteBuffer.allocate(4).position(3).mark().limit(2);
        checkThrows(InvalidMarkException.class, () -> ml.reset(), "mark discarded when limit set below mark");

        // flip: limit = position, position = 0, mark discarded.
        Buffer f = ByteBuffer.allocate(4);
        f.position(3).mark();
        f.flip();
        check(f.position() == 0 && f.limit() == 3, "flip sets position 0 and limit old position");
        checkThrows(InvalidMarkException.class, () -> f.reset(), "flip discards mark");

        // rewind: position = 0, limit unchanged, mark discarded.
        Buffer rw = ByteBuffer.allocate(4).position(2).mark();
        rw.limit(3);
        rw.rewind();
        check(rw.position() == 0 && rw.limit() == 3, "rewind keeps limit, resets position");
        checkThrows(InvalidMarkException.class, () -> rw.reset(), "rewind discards mark");

        // clear: position = 0, limit = capacity, mark discarded.
        Buffer cl = ByteBuffer.allocate(4).position(2).mark().limit(2);
        cl.clear();
        check(cl.position() == 0 && cl.limit() == 4, "clear sets position 0 and limit capacity");
        checkThrows(InvalidMarkException.class, () -> cl.reset(), "clear discards mark");

        // remaining clamps at zero (never negative via public API since position<=limit).
        Buffer z = ByteBuffer.allocate(4).position(4);
        check(z.remaining() == 0 && !z.hasRemaining(), "remaining zero at limit");
        check(z.remaining() >= 0, "remaining never negative");

        // Fluent chaining.
        Buffer chain = ByteBuffer.allocate(8);
        check(chain.position(2).limit(6).mark().reset() == chain, "fluent chaining returns same buffer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
