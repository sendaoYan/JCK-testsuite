/*
 * @test
 * @summary java.net.URISyntaxException conformance: white-box structure + black-box behavior
 *          + full contract matrix (exact getMessage composition, null/negative-index
 *          validation, serialization stability and serialVersionUID, real parse failures).
 * @run main URISyntaxExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.*;
import java.net.*;

public class URISyntaxExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface T { void r() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, T t, String m) {
        try {
            t.r();
        } catch (Throwable x) {
            if (expected.isInstance(x)) { passed++; return; }
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " got " + x.getClass().getName(), x);
        }
        throw new AssertionError(m + ": nothing thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URISyntaxException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(c.getConstructor(String.class, String.class) != null, "ctor(input,reason)");
        check(c.getConstructor(String.class, String.class, int.class) != null, "ctor(input,reason,index)");
        check(c.getMethod("getInput") != null, "getInput");
        check(c.getMethod("getReason") != null, "getReason");
        check(c.getMethod("getIndex") != null, "getIndex");
        check(c.getMethod("getMessage") != null, "getMessage");
    }

    static void testBlackBox() throws Exception {
        URISyntaxException e = new URISyntaxException("input string", "reason");
        check(e.getInput().equals("input string"), "input");
        check(e.getReason().equals("reason"), "reason");
        check(e.getIndex() == -1, "index default -1");
        check(e.getMessage().contains("reason"), "message contains reason");
        check(e.getMessage().contains("input string"), "message contains input");

        URISyntaxException e2 = new URISyntaxException("in", "r", 7);
        check(e2.getInput().equals("in"), "input with index");
        check(e2.getReason().equals("r"), "reason with index");
        check(e2.getIndex() == 7, "index");

        check(e instanceof Exception, "instanceof Exception");
        try { throw e; } catch (Exception ex) { passed++; }
    }

    /** 深化批次 A 追加：完整契约矩阵。 */
    static void testContractMatrix() throws Exception {
        URISyntaxException a = new URISyntaxException("bad input", "missing :");
        check("missing :: bad input".equals(a.getMessage()), "exact 2-arg getMessage");
        URISyntaxException b = new URISyntaxException("bad input", "missing :", 3);
        check("missing : at index 3: bad input".equals(b.getMessage()), "exact 3-arg getMessage");
        check(b.getCause() == null, "getCause null by default");
        check("java.net.URISyntaxException: missing : at index 3: bad input".equals(b.toString()),
                "toString = class: message");
        check(!RuntimeException.class.isInstance(a), "checked exception, not RuntimeException");
        check(a.getReason().equals("missing :"), "getReason is ctor reason");
        URISyntaxException z = new URISyntaxException("in", "r", 0);
        check("r at index 0: in".equals(z.getMessage()), "index 0 still prints");
        check(z.getIndex() == 0, "index 0 kept");
        // 构造器校验：input/reason 为 null → NPE；index < -1 → IAE；NPE 优先于 IAE
        checkThrows(NullPointerException.class,
                () -> new URISyntaxException(null, "r"), "null input NPE");
        checkThrows(NullPointerException.class,
                () -> new URISyntaxException("in", null), "null reason NPE");
        checkThrows(NullPointerException.class,
                () -> new URISyntaxException(null, null), "null both NPE");
        checkThrows(NullPointerException.class,
                () -> new URISyntaxException("in", null, -5),
                "null reason wins over bad index (NPE)");
        checkThrows(IllegalArgumentException.class,
                () -> new URISyntaxException("in", "r", -2), "index < -1 IAE");
        try {
            new URISyntaxException("in", "r", -2);
            check(false, "IAE details unreachable");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() == null, "IAE message null");
        }
        // initCause（继承自 Throwable，可一次性设置）
        URISyntaxException ec = new URISyntaxException("i", "r");
        Throwable cause = new IllegalStateException("why");
        ec.initCause(cause);
        check(ec.getCause() == cause, "initCause sets cause");
        // 真实解析失败携带精确三元组
        try {
            new URI("1abc:x");
            check(false, "1abc:x should fail");
        } catch (URISyntaxException pe) {
            check("1abc:x".equals(pe.getInput()), "parse failure input");
            check("Illegal character in scheme name".equals(pe.getReason()), "parse failure reason");
            check(pe.getIndex() == 0, "parse failure index");
        }
        // 序列化：serialVersionUID 稳定 + 字段/消息 round-trip
        check(ObjectStreamClass.lookup(URISyntaxException.class).getSerialVersionUID()
                == 2137979680897488891L, "serialVersionUID");
        check(ObjectStreamClass.lookup(URISyntaxException.class).getFields().length == 2,
                "two serial fields (input, index)");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(b);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        URISyntaxException back = (URISyntaxException) ois.readObject();
        check("bad input".equals(back.getInput()), "serialized input");
        check("missing :".equals(back.getReason()), "serialized reason");
        check(back.getIndex() == 3, "serialized index");
        check("missing : at index 3: bad input".equals(back.getMessage()), "serialized message");
        check(back != b, "round-trip yields new object");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
