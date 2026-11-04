/*
 * @test
 * @summary java.nio.Buffer conformance: white-box structure (abstract class, key methods)
 *          plus black-box position/limit/capacity state machine, mark/reset, clear/flip/
 *          rewind, validation and views.
 * @run main BufferTest
 *
 * 黑盒设计案:
 * 1. 契约: Buffer 是所有缓冲区的抽象基类, 持有 position/limit/capacity/mark 四个游标,
 *    不变量 0 <= mark <= position <= limit <= capacity; remaining() == limit() - position().
 * 2. 场景: 用户通过具体子类工厂 (ByteBuffer.allocate) 拿到 Buffer, 先写入 (position 前进),
 *    再 flip() 切换为读模式, 读完用 clear()/rewind() 复位, 或 mark/reset 回退到标记点.
 * 3. 组合链路: allocate -> put -> position/limit -> mark -> reset -> flip -> read ->
 *    rewind -> clear; 以及 slice/duplicate 视图共享底层内容但游标独立.
 * 4. 不变量: 视图 (slice/duplicate) 与源共享存储; capacity 不可变; remaining 恒等于
 *    limit-position; 只读/数组直连属性 (isReadOnly/hasArray/isDirect) 与实际存储一致.
 * 5. 边界/异常: position/limit 越界 (负值或 >capacity) 抛 IllegalArgumentException;
 *    reset() 无 mark 抛 InvalidMarkException; position/limit 被调小时丢弃 mark.
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

    // ---- 白盒: 结构/反射 ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.Buffer");
        check(Modifier.isAbstract(c.getModifiers()), "Buffer is abstract");
        check(!c.isInterface(), "Buffer is a class");
        check(c.getSuperclass() == Object.class, "Buffer extends Object");
        check(c.getInterfaces().length == 0, "Buffer implements no interfaces");

        check(hasMethod(c, "capacity"), "has capacity()");
        check(hasMethod(c, "position"), "has position()");
        check(hasMethod(c, "position", int.class), "has position(int)");
        check(hasMethod(c, "limit"), "has limit()");
        check(hasMethod(c, "limit", int.class), "has limit(int)");
        check(hasMethod(c, "mark"), "has mark()");
        check(hasMethod(c, "reset"), "has reset()");
        check(hasMethod(c, "clear"), "has clear()");
        check(hasMethod(c, "flip"), "has flip()");
        check(hasMethod(c, "rewind"), "has rewind()");
        check(hasMethod(c, "remaining"), "has remaining()");
        check(hasMethod(c, "hasRemaining"), "has hasRemaining()");
        check(hasMethod(c, "isReadOnly"), "has isReadOnly()");
        check(hasMethod(c, "hasArray"), "has hasArray()");
        check(hasMethod(c, "array"), "has array()");
        check(hasMethod(c, "arrayOffset"), "has arrayOffset()");
        check(hasMethod(c, "isDirect"), "has isDirect()");
        check(hasMethod(c, "slice"), "has slice()");
        check(hasMethod(c, "slice", int.class, int.class), "has slice(int,int)");
        check(hasMethod(c, "duplicate"), "has duplicate()");

        // Version snapshot (JDK-9+): the cursor mutators on Buffer are non-final so the
        // typed subclasses can covariantly override them, and they do.
        check(!Modifier.isFinal(c.getMethod("position", int.class).getModifiers()),
                "Buffer.position(int) is non-final to permit covariant overrides (JDK-9+)");
        check(ByteBuffer.class.getMethod("position", int.class).getReturnType() == ByteBuffer.class,
                "ByteBuffer.position(int) covariantly returns ByteBuffer (JDK-9+)");
        check(ByteBuffer.class.getMethod("limit", int.class).getReturnType() == ByteBuffer.class,
                "ByteBuffer.limit(int) covariantly returns ByteBuffer (JDK-9+)");
        check(ByteBuffer.class.getMethod("flip").getReturnType() == ByteBuffer.class,
                "ByteBuffer.flip() covariantly returns ByteBuffer (JDK-9+)");
        check(ByteBuffer.class.getMethod("rewind").getReturnType() == ByteBuffer.class,
                "ByteBuffer.rewind() covariantly returns ByteBuffer (JDK-9+)");
        check(ByteBuffer.class.getMethod("clear").getReturnType() == ByteBuffer.class,
                "ByteBuffer.clear() covariantly returns ByteBuffer (JDK-9+)");
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

    // ---- 黑盒: 行为 ----
    static void testBlackBox() {
        Buffer buf = ByteBuffer.allocate(8);
        check(buf.capacity() == 8, "capacity 8");
        check(buf.position() == 0, "initial position 0");
        check(buf.limit() == 8, "initial limit == capacity");
        check(buf.remaining() == 8, "remaining == limit-position");
        check(buf.hasRemaining(), "hasRemaining true");

        buf.position(2);
        buf.limit(6);
        check(buf.remaining() == 4, "remaining == limit(6) - position(2)");
        check(buf.position() == 2 && buf.limit() == 6, "position/limit setters");

        buf.mark();
        buf.position(5);
        check(buf.position() == 5, "position advanced");
        buf.reset();
        check(buf.position() == 2, "reset restores marked position");

        buf.position(4);
        buf.flip();
        check(buf.position() == 0, "flip position -> 0");
        check(buf.limit() == 4, "flip limit -> old position");
        check(buf.remaining() == 4, "flip remaining");

        buf.position(3);
        buf.rewind();
        check(buf.position() == 0 && buf.limit() == 4, "rewind keeps limit");

        buf.clear();
        check(buf.position() == 0 && buf.limit() == 8, "clear restores full capacity");

        Buffer v = ByteBuffer.allocate(4);
        checkThrows(IllegalArgumentException.class, () -> v.position(-1), "position(-1)");
        checkThrows(IllegalArgumentException.class, () -> v.position(5), "position(>capacity)");
        checkThrows(IllegalArgumentException.class, () -> v.limit(-1), "limit(-1)");
        checkThrows(IllegalArgumentException.class, () -> v.limit(5), "limit(>capacity)");

        v.position(2).mark().position(3);
        v.reset();
        check(v.position() == 2, "reset to mark");
        v.position(1); // 小于 mark(2) -> 丢弃 mark
        checkThrows(InvalidMarkException.class, () -> v.reset(), "reset after discard mark");

        ByteBuffer bb = ByteBuffer.allocate(4);
        Buffer b2 = bb;
        check(!b2.isDirect(), "heap buffer not direct");
        check(b2.hasArray(), "heap buffer hasArray");
        check(b2.array() != null, "array non-null");
        check(b2.arrayOffset() == 0, "arrayOffset 0");

        bb.put((byte) 10).put((byte) 20).put((byte) 30).put((byte) 40);
        bb.position(1).limit(3);
        Buffer slice = b2.slice();
        check(slice.position() == 0 && slice.capacity() == 2, "slice resets position, shrinks capacity");
        check(((ByteBuffer) slice).get() == 20, "slice first element");

        bb.position(0).limit(4);
        Buffer dup = b2.duplicate();
        ((ByteBuffer) dup).put(0, (byte) 99);
        check(bb.get(0) == 99, "duplicate shares storage");
        check(dup.position() == 0 && bb.position() == 0, "duplicate independent cursor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCursorMachineAcrossKinds();
        testPositionLimitBoundaries();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
