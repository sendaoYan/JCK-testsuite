/*
 * @test
 * @summary javax.xml.soap.SOAPException conformance: 构造链矩阵/cause 链与 initCause 语义/getMessage 动态回退/JDK-8 方法面差异/序列化往返.
 * @run main SOAPExceptionTest
 */


public class SOAPExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        check(javax.xml.soap.SOAPException.class.getSuperclass().getName().equals("java.lang.Exception"), "extends java.lang.Exception");
        javax.xml.soap.SOAPException se = new javax.xml.soap.SOAPException();
        check(se.getCause() == null, "no cause");
        testConstructorMatrix();
        testGetMessageFallback();
        testInitCauseSemantics();
        testMethodFaceJdk8();
        testNestedChaining();
        testSerializationRoundTrip();
        testThrowCatchIdiom();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // 四构造形态精确回读（JDK-8 RI 锁定）
    static void testConstructorMatrix() {
        IllegalStateException inner = new IllegalStateException("inner-msg");
        // ()
        javax.xml.soap.SOAPException e0 = new javax.xml.soap.SOAPException();
        check(e0.getMessage() == null, "() message null");
        check(e0.getCause() == null, "() cause null");
        // (String)
        javax.xml.soap.SOAPException e1 = new javax.xml.soap.SOAPException("reason");
        check("reason".equals(e1.getMessage()), "(String) message exact");
        check(e1.getCause() == null, "(String) cause null");
        // (String, Throwable)
        javax.xml.soap.SOAPException e2 = new javax.xml.soap.SOAPException("m2", inner);
        check("m2".equals(e2.getMessage()), "(String,Throwable) message == String");
        check(e2.getCause() == inner, "(String,Throwable) cause == Throwable (same instance)");
        // (Throwable)：RI 实测 message == cause.toString()
        javax.xml.soap.SOAPException e3 = new javax.xml.soap.SOAPException(inner);
        check(inner.toString().equals(e3.getMessage()),
                "(Throwable) message == cause.toString() (RI locked)");
        check(e3.getCause() == inner, "(Throwable) cause == Throwable");
        check(e3.getMessage().contains("inner-msg"), "(Throwable) message carries cause text");
        // toString 不抛且含类名/消息
        check(e2.toString().contains("SOAPException"), "toString contains class simple name");
        check(e2.toString().contains("m2"), "toString contains message");
        check(e3.toString().length() > 0, "(Throwable) toString non-empty");
        // equals 为异常引用语义（不重写）
        check(!e1.equals(new javax.xml.soap.SOAPException("reason")), "identity equality (no value equals)");
    }

    // getMessage 动态回退：自身 message 为 null 且 cause 非空时返回 cause.getMessage()
    static void testGetMessageFallback() {
        IllegalStateException inner = new IllegalStateException("fallback-msg");
        javax.xml.soap.SOAPException e = new javax.xml.soap.SOAPException();
        check(e.getMessage() == null, "before initCause message null");
        e.initCause(inner);
        check(inner.getMessage().equals(e.getMessage()),
                "getMessage falls back to cause.getMessage() after initCause (spec behavior)");
        javax.xml.soap.SOAPException eNullMsgCause = new javax.xml.soap.SOAPException(new RuntimeException());
        check(eNullMsgCause.getMessage() != null,
                "(Throwable) message non-null even when cause has no message (uses toString)");
    }

    // initCause 语义：首次成功返回 this/重复 ISE/自引 IAE/null 参数允许
    static void testInitCauseSemantics() {
        IllegalStateException a = new IllegalStateException("a");
        RuntimeException b = new RuntimeException("b");
        javax.xml.soap.SOAPException e = new javax.xml.soap.SOAPException();
        java.lang.Throwable ret = e.initCause(a);
        check(ret == e, "initCause returns this");
        check(e.getCause() == a, "initCause cause readback");
        try {
            e.initCause(b);
            check(false, "double initCause must throw");
        } catch (IllegalStateException ise) {
            check("Can't override cause".equals(ise.getMessage()), "double initCause ISE message exact: " + ise.getMessage());
        }
        javax.xml.soap.SOAPException self = new javax.xml.soap.SOAPException();
        try {
            self.initCause(self);
            check(false, "self-cause must throw");
        } catch (IllegalArgumentException iae) {
            check(true, "initCause(this) throws IllegalArgumentException");
        }
        javax.xml.soap.SOAPException nullCause = new javax.xml.soap.SOAPException("keep");
        nullCause.initCause(null);
        check(nullCause.getCause() == null, "initCause(null) leaves cause null");
        check("keep".equals(nullCause.getMessage()), "initCause(null) keeps message");
        // (String,Throwable)/(Throwable) 构造已置 cause：再 initCause 一律 ISE
        javax.xml.soap.SOAPException preset = new javax.xml.soap.SOAPException("m", a);
        try {
            preset.initCause(b);
            check(false, "initCause after (String,Throwable) ctor must throw");
        } catch (IllegalStateException ise) {
            check(true, "initCause after ctor-set cause throws ISE");
        }
        javax.xml.soap.SOAPException preset2 = new javax.xml.soap.SOAPException(a);
        try {
            preset2.initCause(a);
            check(false, "initCause after (Throwable) ctor must throw");
        } catch (IllegalStateException ise) {
            check(true, "initCause after (Throwable) ctor throws ISE");
        }
    }

    // JDK-8 rt.jar 实测：无 getDetail/setDetail（javadoc 文本与二进制分叉，锁定二进制事实）
    static void testMethodFaceJdk8() throws Exception {
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : javax.xml.soap.SOAPException.class.getMethods()) names.add(m.getName());
        check(names.contains("getMessage"), "has getMessage");
        check(names.contains("getCause"), "has getCause");
        check(names.contains("initCause"), "has initCause");
        check(!names.contains("getDetail"), "JDK-8 binary has NO getDetail (spec-text divergence)");
        check(!names.contains("setDetail"), "JDK-8 binary has NO setDetail (spec-text divergence)");
        try {
            javax.xml.soap.SOAPException.class.getMethod("getDetail");
            check(false, "getMethod(getDetail) should be missing in JDK-8");
        } catch (NoSuchMethodException e) {
            check(true, "getMethod(getDetail) throws NoSuchMethodException");
        }
        check(javax.xml.soap.SOAPException.class.getConstructors().length == 4,
                "exactly 4 public constructors");
        boolean[] ok = new boolean[4];
        for (java.lang.reflect.Constructor<?> c : javax.xml.soap.SOAPException.class.getConstructors()) {
            Class<?>[] ps = c.getParameterTypes();
            if (ps.length == 0) ok[0] = true;
            if (ps.length == 1 && ps[0] == String.class) ok[1] = true;
            if (ps.length == 1 && java.lang.Throwable.class.isAssignableFrom(ps[0])) ok[2] = true;
            if (ps.length == 2) ok[3] = true;
        }
        check(ok[0] && ok[1] && ok[2] && ok[3], "constructor signatures (),(String),(Throwable),(String,Throwable)");
        check(java.io.Serializable.class.isAssignableFrom(javax.xml.soap.SOAPException.class),
                "Serializable via Exception");
    }

    // 嵌套 SOAPException 链逐层解包
    static void testNestedChaining() {
        javax.xml.soap.SOAPException root = new javax.xml.soap.SOAPException("root-cause");
        javax.xml.soap.SOAPException mid = new javax.xml.soap.SOAPException("mid", root);
        javax.xml.soap.SOAPException top = new javax.xml.soap.SOAPException("top", mid);
        check(top.getCause() == mid && mid.getCause() == root, "two-level cause chain exact instances");
        int depth = 0;
        Throwable t = top;
        while (t.getCause() != null) { t = t.getCause(); depth++; }
        check(depth == 2, "chain walk depth 2");
        check("root-cause".equals(t.getMessage()), "chain bottom message exact");
        check(top.toString().contains("top"), "top toString carries its own message");
    }

    // 序列化往返：Exception 通道保留 message 与 cause 类/消息
    static void testSerializationRoundTrip() throws Exception {
        javax.xml.soap.SOAPException orig = new javax.xml.soap.SOAPException("ser-msg", new IllegalStateException("ser-inner"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(orig);
        }
        Object back;
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            back = ois.readObject();
        }
        check(back instanceof javax.xml.soap.SOAPException, "deserialized is SOAPException");
        javax.xml.soap.SOAPException des = (javax.xml.soap.SOAPException) back;
        check("ser-msg".equals(des.getMessage()), "message survives serialization");
        check(des.getCause() instanceof IllegalStateException, "cause class survives");
        check("ser-inner".equals(des.getCause().getMessage()), "cause message survives");
    }

    // 作为受检异常的抛/捕惯用法
    static void testThrowCatchIdiom() {
        try {
            throw new javax.xml.soap.SOAPException("thrown-reason", new java.io.IOException("io"));
        } catch (javax.xml.soap.SOAPException e) {
            check("thrown-reason".equals(e.getMessage()), "caught message exact");
            check(e.getCause() instanceof java.io.IOException, "caught cause type");
        } catch (Exception e) {
            check(false, "must be catchable as SOAPException");
        }
        // Exception 多态捕获
        try {
            throw new javax.xml.soap.SOAPException();
        } catch (java.lang.Exception e) {
            check(e instanceof javax.xml.soap.SOAPException, "catch as java.lang.Exception polymorphic");
        }
    }
}
