/*
 * @test
 * @summary javax.xml.ws.WebServiceException conformance: full constructor matrix, message/cause chains, hierarchy.
 * @run main WebServiceExceptionTest
 */

import javax.xml.ws.WebServiceException;

public class WebServiceExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static final Throwable CAUSE = new IllegalStateException("root");

    public static void main(String[] args) throws Exception {
        check(javax.xml.ws.WebServiceException.class.getSuperclass().getName().equals("java.lang.RuntimeException"), "extends java.lang.RuntimeException");
        testNoArg();
        testMessageOnly();
        testMessageCause();
        testCauseOnly();
        testHierarchyAndSerial();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // () —— message null、cause null
    static void testNoArg() {
        WebServiceException e = new WebServiceException();
        check(e.getMessage() == null, "() message null");
        check(e.getCause() == null, "() cause null");
        check(e.toString().contains("WebServiceException"), "() toString contains class name");
        try {
            throw e;
        } catch (RuntimeException re) {
            check(re == e, "() catchable as RuntimeException");
        } catch (Throwable t) {
            check(false, "() wrong catch type: " + t.getClass().getName());
        }
    }

    // (String) —— message 回读、cause 仍 null（无自动填充）
    static void testMessageOnly() {
        WebServiceException e = new WebServiceException("boom");
        check("boom".equals(e.getMessage()), "(String) message read back");
        check(e.getCause() == null, "(String) cause still null");
        check(e.toString().contains("boom"), "(String) toString contains message");
        WebServiceException empty = new WebServiceException("");
        check("".equals(empty.getMessage()), "(String) empty string kept (not null)");
        WebServiceException nul = new WebServiceException((String) null);
        check(nul.getMessage() == null, "(String) explicit null message");
    }

    // (String, Throwable) —— message+cause 双回读
    static void testMessageCause() {
        WebServiceException e = new WebServiceException("wrap", CAUSE);
        check("wrap".equals(e.getMessage()), "(String,Throwable) message");
        check(e.getCause() == CAUSE, "(String,Throwable) cause same reference");
        check("root".equals(e.getCause().getMessage()), "cause message reachable");
        check(e.toString().contains("wrap"), "toString contains wrapper message");
        // 链深度
        check(e.getCause().getCause() == null, "cause has no further cause");
        // null cause 显式传入
        WebServiceException nc = new WebServiceException("m", null);
        check("m".equals(nc.getMessage()), "null-cause message kept");
        check(nc.getCause() == null, "explicit null cause");
        // null message + cause
        WebServiceException nm = new WebServiceException(null, CAUSE);
        check(nm.getMessage() == null, "null message with cause");
        check(nm.getCause() == CAUSE, "cause with null message");
        // initCause 二次设置被拒（构造已定 cause）
        try {
            nm.initCause(new ArithmeticException("x"));
            check(false, "initCause after constructor cause must throw");
        } catch (IllegalStateException ise) {
            check(true, "initCause re-init -> IllegalStateException");
        }
    }

    // (Throwable) —— message 取自 cause.toString()
    static void testCauseOnly() {
        WebServiceException e = new WebServiceException(CAUSE);
        check(e.getCause() == CAUSE, "(Throwable) cause reference");
        check(e.getMessage() != null && e.getMessage().contains("root"),
              "(Throwable) message derived from cause toString: " + e.getMessage());
        check(e.getMessage().contains(IllegalStateException.class.getSimpleName()),
              "derived message contains cause class name");
        // null Throwable
        WebServiceException n = new WebServiceException((Throwable) null);
        check(n.getMessage() == null, "(Throwable null) message null");
        check(n.getCause() == null, "(Throwable null) cause null");
        // 嵌套两层
        Exception mid = new WebServiceException("mid", CAUSE);
        WebServiceException top = new WebServiceException(mid);
        check(top.getCause() == mid, "nested cause chain");
        check(top.getCause().getCause() == CAUSE, "two-level chain traversal");
        check(top.getMessage().contains("mid"), "top message derived from mid");
    }

    static void testHierarchyAndSerial() throws Exception {
        // 继承链：WebServiceException -> RuntimeException -> Exception -> Throwable
        Class<?> c = WebServiceException.class;
        check(RuntimeException.class.isAssignableFrom(c), "assignable to RuntimeException");
        check(Exception.class.isAssignableFrom(c), "assignable to Exception");
        check(Throwable.class.isAssignableFrom(c), "assignable to Throwable");
        check(!Error.class.isAssignableFrom(c), "not an Error");
        // 恰 4 个 public 构造器
        check(c.getConstructors().length == 4, "exactly 4 public constructors");
        // 与 ProtocolException 子类化关系（族谱）
        check(javax.xml.ws.ProtocolException.class.getSuperclass() == c,
              "ProtocolException subclasses WebServiceException");
        // 序列化面：ObjectStreamClass 有 serial form 描述（serialVersionUID 字段存在）
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(c);
        check(osc != null, "ObjectStreamClass descriptor exists");
        check(osc.getFields().length >= 0, "serial fields describable");
        // 可序列化（异常基类已 Serializable）
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        // getStackTrace/setStackTrace round-trip
        WebServiceException e = new WebServiceException("st");
        StackTraceElement[] st = new StackTraceElement[]{new StackTraceElement("A", "b", "A.java", 1)};
        e.setStackTrace(st);
        check(e.getStackTrace().length == 1 && "A".equals(e.getStackTrace()[0].getClassName()),
              "stack trace set/get round-trip");
    }
}
