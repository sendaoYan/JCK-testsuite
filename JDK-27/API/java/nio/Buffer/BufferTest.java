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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
