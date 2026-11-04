/*
 * @test
 * @summary java.net.URISyntaxException conformance: both constructors, exact
 *          getMessage composition, input/reason/index round-trip, null and
 *          negative-index validation (NPE/IAE), serialization stability and
 *          serialVersionUID, real parse failures carrying exact triples.
 * @run main URISyntaxExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

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

    public static void main(String[] args) throws Exception {
        java.net.URISyntaxException e = new java.net.URISyntaxException("bad input", "missing :");
        check(e instanceof java.lang.Exception, "is Exception");
        check("bad input".equals(e.getInput()), "getInput");
        check("missing :".equals(e.getReason()), "getReason");
        check(e.getIndex() == -1, "index -1 when not set");
        check(e.getMessage() != null && e.getMessage().contains("bad input"), "getMessage includes input");
        java.net.URISyntaxException e2 = new java.net.URISyntaxException("bad input", "missing :", 3);
        check(e2.getIndex() == 3, "getIndex");

        // ===== 深化批次 A 追加：契约矩阵 =====
        check("missing :: bad input".equals(e.getMessage()), "exact 2-arg getMessage");
        check("missing : at index 3: bad input".equals(e2.getMessage()), "exact 3-arg getMessage");
        check(e2.getCause() == null, "getCause null by default");
        check("java.net.URISyntaxException: missing : at index 3: bad input".equals(e2.toString()),
                "toString = class: message");
        check(!RuntimeException.class.isInstance(e), "checked exception, not RuntimeException");
        java.net.URISyntaxException e0 = new java.net.URISyntaxException("in", "r", 0);
        check("r at index 0: in".equals(e0.getMessage()), "index 0 still prints");
        check(e0.getIndex() == 0, "index 0 kept");
        // 构造器校验：input/reason 为 null → NPE；index < -1 → IAE；NPE 优先于 IAE
        checkThrows(NullPointerException.class,
                () -> new java.net.URISyntaxException(null, "r"), "null input NPE");
        checkThrows(NullPointerException.class,
                () -> new java.net.URISyntaxException("in", null), "null reason NPE");
        checkThrows(NullPointerException.class,
                () -> new java.net.URISyntaxException(null, null), "null both NPE");
        checkThrows(NullPointerException.class,
                () -> new java.net.URISyntaxException("in", null, -5),
                "null reason wins over bad index (NPE)");
        checkThrows(IllegalArgumentException.class,
                () -> new java.net.URISyntaxException("in", "r", -2), "index < -1 IAE");
        try {
            new java.net.URISyntaxException("in", "r", -2);
            check(false, "IAE details unreachable");
        } catch (IllegalArgumentException iae) {
            check(iae.getMessage() == null, "IAE message null");
        }
        // initCause（继承自 Throwable，可一次性设置）
        java.net.URISyntaxException ec = new java.net.URISyntaxException("i", "r");
        Throwable cause = new IllegalStateException("why");
        ec.initCause(cause);
        check(ec.getCause() == cause, "initCause sets cause");
        // 真实解析失败携带精确三元组
        try {
            new java.net.URI("1abc:x");
            check(false, "1abc:x should fail");
        } catch (java.net.URISyntaxException pe) {
            check("1abc:x".equals(pe.getInput()), "parse failure input");
            check("Illegal character in scheme name".equals(pe.getReason()), "parse failure reason");
            check(pe.getIndex() == 0, "parse failure index");
        }
        // 序列化：serialVersionUID 稳定 + 字段/消息 round-trip
        check(ObjectStreamClass.lookup(java.net.URISyntaxException.class).getSerialVersionUID()
                == 2137979680897488891L, "serialVersionUID");
        check(ObjectStreamClass.lookup(java.net.URISyntaxException.class).getFields().length == 2,
                "two serial fields (input, index)");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(e2);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        java.net.URISyntaxException back = (java.net.URISyntaxException) ois.readObject();
        check("bad input".equals(back.getInput()), "serialized input");
        check("missing :".equals(back.getReason()), "serialized reason");
        check(back.getIndex() == 3, "serialized index");
        check("missing : at index 3: bad input".equals(back.getMessage()), "serialized message");
        check(back != e2, "round-trip yields new object");
        // 受检异常：必须能作为 Exception 捕获
        try {
            throw e;
        } catch (Exception caught) {
            check(caught == e, "catch as Exception");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
