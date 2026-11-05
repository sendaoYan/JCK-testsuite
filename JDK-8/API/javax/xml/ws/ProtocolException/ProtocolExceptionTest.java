/*
 * @test
 * @summary javax.xml.ws.ProtocolException conformance: full constructor matrix, message/cause chains, protocol-level hierarchy.
 * @run main ProtocolExceptionTest
 */

import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;

public class ProtocolExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    static final Throwable CAUSE = new IllegalArgumentException("bad-arg");

    public static void main(String[] args) throws Exception {
        check(javax.xml.ws.ProtocolException.class.getSuperclass().getName().equals("javax.xml.ws.WebServiceException"), "extends javax.xml.ws.WebServiceException");
        testNoArg();
        testMessageOnly();
        testMessageCause();
        testCauseOnly();
        testFamilyAndUsage();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testNoArg() {
        ProtocolException e = new ProtocolException();
        check(e.getMessage() == null, "() message null");
        check(e.getCause() == null, "() cause null");
        check(e.toString().startsWith("javax.xml.ws.ProtocolException"), "() toString starts with FQCN");
    }

    static void testMessageOnly() {
        ProtocolException e = new ProtocolException("proto-fault");
        check("proto-fault".equals(e.getMessage()), "(String) message");
        check(e.getCause() == null, "(String) cause null");
        // catch 形态：WebServiceException / RuntimeException 双通道
        try {
            throw e;
        } catch (WebServiceException wse) {
            check(wse == e, "catchable as WebServiceException");
        }
        try {
            throw e;
        } catch (RuntimeException re) {
            check(re == e, "catchable as RuntimeException");
        }
        ProtocolException empty = new ProtocolException("");
        check("".equals(empty.getMessage()), "empty string message kept");
    }

    static void testMessageCause() {
        ProtocolException e = new ProtocolException("wrap", CAUSE);
        check("wrap".equals(e.getMessage()), "(String,Throwable) message");
        check(e.getCause() == CAUSE, "(String,Throwable) cause reference");
        check("bad-arg".equals(e.getCause().getMessage()), "cause message reachable");
        // cause 链不重复
        check(e.getCause().getCause() == null, "single-level cause");
        // null 变体
        ProtocolException nc = new ProtocolException("m", null);
        check(nc.getCause() == null, "explicit null cause");
        ProtocolException nm = new ProtocolException(null, CAUSE);
        check(nm.getMessage() == null && nm.getCause() == CAUSE, "null message with cause");
    }

    static void testCauseOnly() {
        ProtocolException e = new ProtocolException(CAUSE);
        check(e.getCause() == CAUSE, "(Throwable) cause");
        check(e.getMessage() != null && e.getMessage().contains("bad-arg"),
              "(Throwable) message derived: " + e.getMessage());
        ProtocolException n = new ProtocolException((Throwable) null);
        check(n.getMessage() == null && n.getCause() == null, "(Throwable null) both null");
    }

    // 族谱与典型用法：作为协议错误被 Provider/Handler 抛出
    static void testFamilyAndUsage() throws Exception {
        Class<?> c = ProtocolException.class;
        check(WebServiceException.class.isAssignableFrom(c), "assignable to WebServiceException");
        check(RuntimeException.class.isAssignableFrom(c), "assignable to RuntimeException");
        check(!Error.class.isAssignableFrom(c), "not an Error");
        // 恰 4 个 public 构造器（与父类同构）
        check(c.getConstructors().length == 4, "exactly 4 public constructors");
        // 典型用法：Handler.handleMessage 抛出协议异常（消息处理中断契约）
        javax.xml.ws.handler.LogicalHandler<javax.xml.ws.handler.LogicalMessageContext> h =
            new javax.xml.ws.handler.LogicalHandler<javax.xml.ws.handler.LogicalMessageContext>() {
                @Override public boolean handleMessage(javax.xml.ws.handler.LogicalMessageContext ctx) {
                    throw new ProtocolException("must-understand");
                }
                @Override public boolean handleFault(javax.xml.ws.handler.LogicalMessageContext ctx) { return true; }
                @Override public void close(javax.xml.ws.handler.MessageContext ctx) { }
            };
        try {
            h.handleMessage(null);
            check(false, "handler protocol exception must propagate");
        } catch (ProtocolException pe) {
            check("must-understand".equals(pe.getMessage()), "protocol exception propagated from handler");
        }
        // 序列化面
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        // 真实序列化 round-trip：message 复活
        try {
            ProtocolException orig = new ProtocolException("ser-proto");
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(orig);
            oos.close();
            Object back = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
            check(back instanceof ProtocolException, "deserialized is ProtocolException");
            check("ser-proto".equals(((ProtocolException) back).getMessage()),
                  "message survives serialization round-trip");
        } catch (java.io.IOException e1) {
            check(false, "serialization failed: " + e1);
        }
        // getLocalizedMessage 默认同 getMessage
        ProtocolException lm = new ProtocolException("loc");
        check("loc".equals(lm.getLocalizedMessage()), "getLocalizedMessage defaults to message");
        // suppressed 异常面
        ProtocolException sup = new ProtocolException("sup", CAUSE);
        ArithmeticException suppressed = new ArithmeticException("div0");
        sup.addSuppressed(suppressed);
        check(sup.getSuppressed().length == 1, "suppressed count");
        check(sup.getSuppressed()[0] == suppressed, "suppressed reference");
        // 栈可写
        ProtocolException e = new ProtocolException("s");
        e.setStackTrace(new StackTraceElement[]{new StackTraceElement("X", "y", "X.java", 9)});
        check(e.getStackTrace().length == 1, "stack trace set/get");
        // fillInStackTrace 返回 this（记录点可重置）
        ProtocolException f = new ProtocolException("f");
        check(f.fillInStackTrace() == f, "fillInStackTrace returns this");
        // 三层 cause 链
        WebServiceException l3 = new WebServiceException("l3", new ProtocolException("l2", CAUSE));
        check(l3.getCause() instanceof ProtocolException, "mixed family chain");
        check(l3.getCause().getCause() == CAUSE, "three-level traversal");
    }
}
