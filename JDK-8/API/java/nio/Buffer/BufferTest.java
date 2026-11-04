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
import java.nio.CharBuffer;
import java.nio.IntBuffer;
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

        // Version snapshot (JDK-8): the view factories live only on the concrete buffer
        // classes; they are hoisted into Buffer itself in JDK 9+. The typed subclasses
        // cannot covariantly override position/limit/flip here because the Buffer
        // declarations are final (verified by the loop above).
        check(!hasMethod(c, "slice"), "JDK-8 Buffer declares no slice() (hoisted into Buffer in JDK 9+)");
        check(!hasMethod(c, "duplicate"), "JDK-8 Buffer declares no duplicate() (hoisted into Buffer in JDK 9+)");
        check(ByteBuffer.class.getMethod("flip").getReturnType() == Buffer.class,
                "JDK-8 ByteBuffer inherits final Buffer.flip() (no covariant override)");
        check(ByteBuffer.class.getMethod("position", int.class).getReturnType() == Buffer.class,
                "JDK-8 ByteBuffer.position(int) is inherited from Buffer and returns Buffer");
    }

    /**
     * Re-checks the four-pointer invariant 0 &lt;= mark &lt;= position &lt;= limit &lt;= capacity
     * together with remaining()/hasRemaining() after every state-machine step.
     */
    static void state(Buffer b, int pos, int lim, String msg) {
        check(b.position() == pos && b.limit() == lim
                        && b.remaining() == lim - pos && b.hasRemaining() == (pos < lim)
                        && b.position() >= 0 && b.position() <= b.limit() && b.limit() <= b.capacity(),
                msg + " [expected pos=" + pos + " lim=" + lim + ", actual pos=" + b.position()
                        + " lim=" + b.limit() + " cap=" + b.capacity() + "]");
    }

    /** One relative element transfer on a concrete buffer kind. */
    interface Op { void run(Buffer b); }

    /**
     * Table-driven cursor state machine, executed verbatim over every buffer kind:
     * fill in write mode, mark/reset, boundary position/limit moves, flip to read
     * mode, drain, rewind, compact back to write mode, clear, then the mark-discard
     * rules. Running the identical table over several kinds checks polymorphic
     * consistency of the cursor contract.
     */
    static void runCursorMachine(String kind, Buffer b, Op putOne, Op getOne, Op compactOne) {
        state(b, 0, 6, kind + ": fresh buffer pos=0 lim=capacity");
        putOne.run(b);
        state(b, 1, 6, kind + ": one relative put advances position");
        putOne.run(b);
        putOne.run(b);
        state(b, 3, 6, kind + ": three relative puts");
        b.mark();
        putOne.run(b);
        state(b, 4, 6, kind + ": mark at 3, one more put");
        b.reset();
        state(b, 3, 6, kind + ": reset rewinds the cursor to the mark");
        b.position(0);
        state(b, 0, 6, kind + ": position(0) stays in write mode");
        b.position(6);
        state(b, 6, 6, kind + ": position(limit) boundary, hasRemaining false");
        b.position(3);
        b.limit(3);
        state(b, 3, 3, kind + ": limit(position) boundary, remaining 0");
        b.limit(6);
        state(b, 3, 6, kind + ": limit may move back up within capacity");
        b.flip();
        state(b, 0, 3, kind + ": flip sets pos=0, lim=old position");
        getOne.run(b);
        getOne.run(b);
        state(b, 2, 3, kind + ": two relative gets");
        b.mark();
        getOne.run(b);
        state(b, 3, 3, kind + ": reads exhausted, remaining 0");
        b.rewind();
        state(b, 0, 3, kind + ": rewind keeps limit");
        b.position(1);
        compactOne.run(b);
        state(b, 2, 6, kind + ": compact shifts the 2 remaining elements, lim=capacity");
        b.clear();
        state(b, 0, 6, kind + ": clear restores pos=0 lim=capacity");

        // Mark-discard rules, identical for every buffer kind.
        b.clear();
        b.position(2).mark();
        b.flip();
        checkThrows(InvalidMarkException.class, () -> b.reset(), kind + ": flip discards mark");
        b.clear();
        b.position(2).mark();
        b.rewind();
        checkThrows(InvalidMarkException.class, () -> b.reset(), kind + ": rewind discards mark");
        b.clear();
        b.position(2).mark();
        b.clear();
        checkThrows(InvalidMarkException.class, () -> b.reset(), kind + ": clear discards mark");
    }

    /** The same cursor machine over four kinds: heap/direct ByteBuffer, CharBuffer, IntBuffer. */
    static void testCursorMachineAcrossKinds() {
        runCursorMachine("heap ByteBuffer", ByteBuffer.allocate(6),
                b -> ((ByteBuffer) b).put((byte) 1),
                b -> ((ByteBuffer) b).get(),
                b -> ((ByteBuffer) b).compact());
        runCursorMachine("direct ByteBuffer", ByteBuffer.allocateDirect(6),
                b -> ((ByteBuffer) b).put((byte) 1),
                b -> ((ByteBuffer) b).get(),
                b -> ((ByteBuffer) b).compact());
        runCursorMachine("CharBuffer", CharBuffer.allocate(6),
                b -> ((CharBuffer) b).put('x'),
                b -> ((CharBuffer) b).get(),
                b -> ((CharBuffer) b).compact());
        runCursorMachine("IntBuffer", IntBuffer.allocate(6),
                b -> ((IntBuffer) b).put(7),
                b -> ((IntBuffer) b).get(),
                b -> ((IntBuffer) b).compact());
    }

    /** Exhaustive position/limit boundary matrix: every accepted value plus each rejection. */
    static void testPositionLimitBoundaries() {
        Buffer b = ByteBuffer.allocate(4);
        for (int p = 0; p <= 4; p++) {
            b.position(p);
            check(b.position() == p && b.remaining() == 4 - p,
                    "position(" + p + ") accepted at full limit, remaining tracks");
        }
        b.position(0);
        b.limit(2);
        check(b.position(0) == b, "position(0) accepted under limit 2");
        b.position(2);
        check(b.position() == 2, "position(limit) accepted");
        checkThrows(IllegalArgumentException.class, () -> b.position(3),
                "position(limit+1) under limit 2 -> IllegalArgumentException");
        b.clear();
        for (int l = 0; l <= 4; l++) {
            b.limit(l);
            check(b.limit() == l && b.remaining() == l, "limit(" + l + ") accepted, remaining tracks");
        }
        checkThrows(IllegalArgumentException.class, () -> b.limit(-1), "limit(-1) -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> b.limit(5),
                "limit(capacity+1) -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> b.position(-1),
                "position(-1) -> IllegalArgumentException");

        // limit below position clamps the position and discards a larger mark.
        Buffer c = ByteBuffer.allocate(6);
        c.position(4).mark();
        c.limit(2);
        check(c.position() == 2 && c.remaining() == 0, "limit(2) clamps position 4 -> 2");
        checkThrows(InvalidMarkException.class, () -> c.reset(), "limit < mark discards mark");

        // limit == mark keeps the mark (invariant 0 <= mark <= limit still holds).
        Buffer d = ByteBuffer.allocate(6);
        d.position(2).mark().position(4);
        d.limit(2);
        d.reset();
        check(d.position() == 2, "limit == mark keeps the mark usable");

        // mark < new limit < position: mark kept, position clamped.
        Buffer e = ByteBuffer.allocate(6);
        e.position(2).mark().position(4);
        e.limit(3);
        check(e.position() == 3, "limit(3) clamps position 4 -> 3");
        e.reset();
        check(e.position() == 2, "mark kept when mark <= new limit < position");

        // position moved back to exactly the mark keeps it; below the mark discards it.
        Buffer f = ByteBuffer.allocate(6);
        f.position(3).mark().position(5);
        f.position(3);
        f.reset();
        check(f.position() == 3, "position == mark keeps mark");
        f.position(5);
        f.position(2);
        checkThrows(InvalidMarkException.class, () -> f.reset(), "position < mark discards mark");

        // Degenerate geometries: zero capacity and full drain.
        Buffer z = ByteBuffer.allocate(0);
        check(z.capacity() == 0 && z.position() == 0 && z.limit() == 0
                && z.remaining() == 0 && !z.hasRemaining(), "zero-capacity buffer geometry");
        z.flip();
        check(z.limit() == 0, "flip on empty buffer keeps limit 0");
        z.clear();
        check(z.limit() == 0, "clear on empty buffer keeps limit 0");
        Buffer full = ByteBuffer.allocate(3);
        full.position(3).flip();
        check(full.position() == 0 && full.limit() == 3 && full.remaining() == 3,
                "flip from a fully written buffer exposes all elements");
        full.position(3);
        full.flip();
        check(full.position() == 0 && full.limit() == 3, "flip after a full drain restores the same state");
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
        testCursorMachineAcrossKinds();
        testPositionLimitBoundaries();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
