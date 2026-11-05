/*
 * @test
 * @summary java.lang.AutoCloseable conformance: white-box structure + black-box behavior
 *          (try-with-resources closes in reverse order, close runs once, close() exception
 *          propagates).
 * @run main AutoCloseableTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoCloseableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.AutoCloseable");
        check(c.isInterface(), "AutoCloseable is an interface");
        java.lang.reflect.Method close = c.getMethod("close");
        check(close.getReturnType() == void.class, "close() returns void");
        check(close.getExceptionTypes().length == 1
              && close.getExceptionTypes()[0] == Exception.class,
              "close() throws Exception");
    }

    // ---- Part 2: black-box (behavioral) ----
    static final List<String> log = new ArrayList<>();

    static class Resource implements AutoCloseable {
        final String name;
        Resource(String name) { this.name = name; }
        public void close() throws Exception { log.add(name); }
    }

    static void testCloseOrderAndOnce() throws Exception {
        log.clear();
        try (Resource a = new Resource("a"); Resource b = new Resource("b")) {
            check(log.isEmpty(), "nothing closed before block ends");
        }
        check(log.equals(Arrays.asList("b", "a")), "close reverse declaration order");
        check(log.size() == 2, "each closed once");
    }

    static class ThrowingResource implements AutoCloseable {
        public void close() throws java.io.IOException { throw new java.io.IOException("boom"); }
    }

    static void testCloseThrows() {
        try (ThrowingResource r = new ThrowingResource()) {
            // body runs before close()
        } catch (java.io.IOException e) {
            check("boom".equals(e.getMessage()), "close() exception propagates");
            return;
        }
        throw new AssertionError("expected IOException from close()");
    }

    
    // ==================== 契约深化（2026-11）====================

    /** 记录 open/close 到静态日志：验证 TWR 的完整时序。 */
    static final List<String> twrLog = new ArrayList<>();

    static class CloseOrder implements AutoCloseable {
        final String name;
        CloseOrder(String name) { this.name = name; twrLog.add("open:" + name); }
        public void close() { twrLog.add("close:" + name); }
    }

    /** close() 抛指定异常的资源（异常实例由测试注入，便于同一性断言）。 */
    static class Boom implements AutoCloseable {
        final RuntimeException boom;
        Boom(RuntimeException boom) { this.boom = boom; }
        public void close() { throw boom; }
    }

    /** close() 计数（验证恰好一次）。 */
    static class Counting implements AutoCloseable {
        int closed = 0;
        public void close() { closed++; }
    }

    /** 签名缩窄：接口声明 throws Exception，实现可以完全不抛受检异常。 */
    static class Silent implements AutoCloseable {
        int closed = 0;
        public void close() { closed++; }   // 无 throws：合法的签名缩窄
    }

    /** 构造器抛异常的资源：验证“资源初始化失败时不 close 任何东西”。 */
    static class BadCtor implements AutoCloseable {
        BadCtor() { twrLog.add("ctor-throws"); throw new IllegalStateException("ctor"); }
        public void close() { twrLog.add("close:never"); }
    }

    static class GoodCtor implements AutoCloseable {
        final String name;
        GoodCtor(String name) { this.name = name; twrLog.add("open:" + name); }
        public void close() { twrLog.add("close:" + name); }
    }

    /** 白盒补充：恰 1 个方法、非泛型、public。 */
    static void testWhiteBoxShape() throws Exception {
        Class<?> c = Class.forName("java.lang.AutoCloseable");
        check(c.getTypeParameters().length == 0, "AutoCloseable is not generic");
        java.lang.reflect.Method close = c.getDeclaredMethod("close");
        check(java.lang.reflect.Modifier.isAbstract(close.getModifiers()), "close() is abstract");
        check(close.getParameterCount() == 0, "close() takes no parameters");
        check(c.getDeclaredMethods().length == 1, "AutoCloseable declares exactly 1 method");
        check(c.getMethods().length == 1, "AutoCloseable exposes exactly 1 public method");
        check(java.lang.reflect.Modifier.isPublic(close.getModifiers()), "close() is public");
    }

    /** 双资源 TWR：open 顺序、close 逆序，日志精确 [open:r1, open:r2, body, close:r2, close:r1]。 */
    static void testOpenCloseLogSequence() throws Exception {
        twrLog.clear();
        try (CloseOrder r1 = new CloseOrder("r1"); CloseOrder r2 = new CloseOrder("r2")) {
            twrLog.add("body");
        }
        check(twrLog.size() == 5, "5 log entries, got " + twrLog.size());
        check("open:r1".equals(twrLog.get(0)), "entry 0: open:r1");
        check("open:r2".equals(twrLog.get(1)), "entry 1: open:r2");
        check("body".equals(twrLog.get(2)), "entry 2: body (after all opens)");
        check("close:r2".equals(twrLog.get(3)), "entry 3: close:r2 (reverse order first)");
        check("close:r1".equals(twrLog.get(4)), "entry 4: close:r1");
        check(twrLog.equals(Arrays.asList("open:r1", "open:r2", "body", "close:r2", "close:r1")),
              "exact log sequence");
    }

    /** 三资源 TWR：close 严格逆序 r3,r2,r1。 */
    static void testThreeResourceReverseOrder() throws Exception {
        twrLog.clear();
        try (CloseOrder a = new CloseOrder("a"); CloseOrder b = new CloseOrder("b"); CloseOrder c = new CloseOrder("c")) {
            // 空 body
        }
        check(twrLog.equals(Arrays.asList("open:a", "open:b", "open:c", "close:c", "close:b", "close:a")),
              "3 resources close strictly in reverse");
        check(twrLog.indexOf("close:c") < twrLog.indexOf("close:b"), "close:c before close:b");
        check(twrLog.indexOf("close:b") < twrLog.indexOf("close:a"), "close:b before close:a");
        check(twrLog.indexOf("open:c") < twrLog.indexOf("close:c"), "every open precedes its close");
        check(twrLog.indexOf("open:c") < twrLog.indexOf("close:a"), "last open precedes first close");
    }

    /** 空 body：资源仍然 close。 */
    static void testEmptyBodyStillCloses() throws Exception {
        twrLog.clear();
        try (CloseOrder only = new CloseOrder("only")) {
            // 空 body
        }
        check(twrLog.equals(Arrays.asList("open:only", "close:only")), "empty body still closes");
        check(twrLog.size() == 2, "exactly open+close");
    }

    /** 异常抑制闭环：body 抛 E1 + close 抛 E2 → 捕获 E1 同一实例，E2 被抑制且同实例。 */
    static void testSuppressedExceptionLoop() {
        RuntimeException e1 = new RuntimeException("E1");
        final RuntimeException e2 = new RuntimeException("E2");
        RuntimeException caught = null;
        try (Boom r = new Boom(e2)) {
            throw e1;
        } catch (RuntimeException e) {
            caught = e;
        }
        check(caught == e1, "body exception E1 propagates (SAME instance)");
        check(caught.getSuppressed().length == 1, "exactly 1 suppressed exception");
        check(caught.getSuppressed()[0] == e2, "close exception E2 suppressed on E1 (SAME instance)");
        check("E2".equals(caught.getSuppressed()[0].getMessage()), "suppressed message intact");
        check("E1".equals(caught.getMessage()), "primary message intact");
    }

    /** close 抛异常而 body 正常：异常直接作为主异常传播，无 suppressed。 */
    static void testCloseThrowsBodyNormal() {
        final RuntimeException e2 = new RuntimeException("E2");
        RuntimeException caught = null;
        try (Boom r = new Boom(e2)) {
            // body 正常结束
        } catch (RuntimeException e) {
            caught = e;
        }
        check(caught == e2, "close exception propagates directly when body is clean");
        check(caught.getSuppressed().length == 0, "no suppressed exceptions");
        check("E2".equals(caught.getMessage()), "message intact");
    }

    /** 双资源双 close 抛：后声明资源的异常先抛成主异常，先声明的被抑制。 */
    static void testBothClosesThrow() {
        final RuntimeException ea = new RuntimeException("EA");
        final RuntimeException eb = new RuntimeException("EB");
        RuntimeException caught = null;
        try (Boom a = new Boom(ea); Boom b = new Boom(eb)) {
            // body 正常
        } catch (RuntimeException e) {
            caught = e;
        }
        check(caught == eb, "last-declared resource's close exception propagates (b closes first)");
        check(caught.getSuppressed().length == 1, "one suppressed");
        check(caught.getSuppressed()[0] == ea, "first resource's close exception suppressed");
        check("EA".equals(caught.getSuppressed()[0].getMessage()), "suppressed message EA");
    }

    /** 双资源 + body 抛：两个 close 异常都按 close 顺序抑制到 body 异常上。 */
    static void testBodyThrowsTwoClosesSuppress() {
        final RuntimeException e0 = new RuntimeException("E0");
        final RuntimeException eb = new RuntimeException("EB");
        final RuntimeException ea = new RuntimeException("EA");
        RuntimeException caught = null;
        try (Boom a = new Boom(ea); Boom b = new Boom(eb)) {
            throw e0;
        } catch (RuntimeException e) {
            caught = e;
        }
        check(caught == e0, "body exception propagates");
        check(caught.getSuppressed().length == 2, "both close exceptions suppressed");
        check(caught.getSuppressed()[0] == eb, "suppressed[0] is b's close (closes first)");
        check(caught.getSuppressed()[1] == ea, "suppressed[1] is a's close");
    }

    /** 嵌套 TWR 抑制链：body E1、inner close E2、outer close E3 → suppressed [E2,E3]。 */
    static void testNestedTwrSuppressionChain() {
        final RuntimeException e2 = new RuntimeException("E2");
        final RuntimeException e3 = new RuntimeException("E3");
        RuntimeException caught = null;
        try {
            try (Boom outer = new Boom(e3)) {
                try (Boom inner = new Boom(e2)) {
                    throw new RuntimeException("E1");
                } catch (RuntimeException innerCaught) {
                    // 内层 TWR 已把 E2 抑制到 E1；body 恢复正常后由外层 close 抛 E3
                    caught = innerCaught;
                }
            }
        } catch (RuntimeException e3thrown) {
            check(e3thrown == e3, "outer close exception escapes after body recovery");
        }
        check(caught != null, "inner exception captured");
        check("E1".equals(caught.getMessage()), "primary is E1 from inner body");
        check(caught.getSuppressed().length == 1 && caught.getSuppressed()[0] == e2,
              "inner TWR suppressed E2 on E1");
        // 再验证外层自身抑制路径：body 抛出且未被内层捕获
        RuntimeException caught2 = null;
        try {
            try (Boom outer = new Boom(e3); Boom inner = new Boom(e2)) {
                throw new RuntimeException("E0");
            }
        } catch (RuntimeException e) {
            caught2 = e;
        }
        check("E0".equals(caught2.getMessage()), "primary is E0");
        check(caught2.getSuppressed().length == 2, "both closes suppressed in reverse order");
        check(caught2.getSuppressed()[0] == e2, "suppressed[0] = inner (b) close E2");
        check(caught2.getSuppressed()[1] == e3, "suppressed[1] = outer (a) close E3");
    }

    /** body 抛 Error（非 Exception）：close 依然执行并抑制。 */
    static void testErrorBodyStillCloses() {
        final RuntimeException closeBoom = new RuntimeException("CE");
        Counting counted = new Counting();
        try (Counting c = counted; Boom b = new Boom(closeBoom)) {
            throw new AssertionError("BODY-ERROR");
        } catch (AssertionError err) {
            check("BODY-ERROR".equals(err.getMessage()), "AssertionError body propagates");
            check(err.getSuppressed().length == 1 && err.getSuppressed()[0] == closeBoom,
                  "close exception suppressed onto Error");
        }
        check(counted.closed == 1, "earlier resource closed exactly once even on Error");
    }

    /** 每资源 close 恰好一次（body 正常/抛异常两档）。 */
    static void testCloseExactlyOnce() throws Exception {
        Counting c1 = new Counting();
        try (Counting c = c1) {
            // normal
        }
        check(c1.closed == 1, "close called exactly once (normal body), got " + c1.closed);

        Counting c2 = new Counting();
        try (Counting c = c2) {
            throw new RuntimeException("boom");
        } catch (RuntimeException e) {
            check("boom".equals(e.getMessage()), "body exception seen");
        }
        check(c2.closed == 1, "close called exactly once (throwing body), got " + c2.closed);

        Counting c3 = new Counting();
        try (Counting a = c3; Counting b = new Counting()) {
            throw new RuntimeException("boom2");
        } catch (RuntimeException e) {
            check(e.getSuppressed().length == 0, "clean closes add nothing suppressed");
        }
        check(c3.closed == 1, "first resource closed once");
    }

    /** 签名缩窄实证：Silent.close 无 throws，直接调用无需 try/catch。 */
    static void testNarrowedSignature() throws Exception {
        Silent s = new Silent();
        s.close();
        check(s.closed == 1, "direct close() call without try/catch works");
        try (Silent r = new Silent()) {
            // 无需 catch 任何受检异常
        }
        check(true, "narrowed close usable inside TWR without checked handling");
        AutoCloseable wide = new Silent();
        try {
            wide.close();
            check(true, "wide-typed close() callable under try/catch Exception");
        } catch (Exception e) {
            throw new AssertionError("Silent never throws", e);
        }
        check(((Silent) wide).closed == 1, "interface-typed reference still dispatches to impl");
        Silent inner = new Silent();
        try (AutoCloseable r = inner) {
            // 接口类型的资源变量同样合法
        }
        check(inner.closed == 1, "AutoCloseable-typed TWR resource closes");
    }

    /** 资源构造器抛异常：该资源不 close；已构造成功的先行资源仍 close。 */
    static void testCtorThrowsClosesEarlierResources() {
        twrLog.clear();
        try (GoodCtor ok = new GoodCtor("ok"); BadCtor bad = new BadCtor()) {
            throw new AssertionError("unreachable body");
        } catch (IllegalStateException e) {
            check("ctor".equals(e.getMessage()), "ctor exception propagates");
        }
        check(twrLog.contains("open:ok"), "first resource constructed");
        check(!twrLog.contains("close:never"), "failing resource never closed");
        check(twrLog.contains("close:ok"), "earlier successful resource IS closed");
        check(twrLog.indexOf("ctor-throws") < twrLog.indexOf("close:ok"),
              "ctor failure precedes the compensating close");
        twrLog.clear();
        try (BadCtor bad = new BadCtor()) {
            throw new AssertionError("unreachable");
        } catch (IllegalStateException e) {
            check(true, "first-resource ctor failure propagates");
        }
        check(!twrLog.contains("close:never"), "no resource to close when first ctor fails");
    }

    /** TWR 中的 return 与 break：控制流离开仍 close。 */
    static void testControlFlowStillCloses() {
        twrLog.clear();
        boolean returned = flowReturnHelper();
        check(returned, "return path taken");
        check(twrLog.equals(Arrays.asList("open:r", "close:r")), "return still closes");

        twrLog.clear();
        int hits = 0;
        for (int i = 0; i < 2; i++) {
            try (CloseOrder r = new CloseOrder("r")) {
                if (i == 0) continue;
                hits++;
            }
        }
        check(hits == 1, "second iteration reached body end");
        check(twrLog.equals(Arrays.asList("open:r", "close:r", "open:r", "close:r")),
              "continue still closes each iteration");

        twrLog.clear();
        outer:
        for (int i = 0; i < 3; i++) {
            try (CloseOrder r = new CloseOrder("r")) {
                break outer;
            }
        }
        check(twrLog.equals(Arrays.asList("open:r", "close:r")), "labeled break still closes");
    }

    static boolean flowReturnHelper() {
        try (CloseOrder r = new CloseOrder("r")) {
            return true;
        }
    }

    /** 受检异常的 close（IOException）+ 非受检 body 异常 → 抑制类型与消息精确。 */
    static class CheckedBoom implements AutoCloseable {
        public void close() throws java.io.IOException { throw new java.io.IOException("io-boom"); }
    }

    static void testCheckedCloseSuppressedOnUncheckedBody() {
        RuntimeException caught = null;
        try (CheckedBoom r = new CheckedBoom()) {
            throw new IllegalStateException("state");
        } catch (IllegalStateException e) {
            caught = e;
        } catch (java.io.IOException e) {
            throw new AssertionError("body exception should win", e);
        }
        check(caught != null, "unchecked body exception propagates");
        check(caught.getSuppressed().length == 1, "checked close exception suppressed");
        check(caught.getSuppressed()[0] instanceof java.io.IOException,
              "suppressed is the checked IOException");
        check("io-boom".equals(caught.getSuppressed()[0].getMessage()), "suppressed message exact");
    }

    /** 子类继承 close：TWR 对子类资源照常调用（含匿名实现）。 */
    static class LoudSilent extends Silent {
        int loudCloses = 0;
        public void close() { super.close(); loudCloses++; }
    }

    static void testSubclassAndAnonymousResources() throws Exception {
        LoudSilent ls = new LoudSilent();
        try (Silent r = ls) {
            // body
        }
        check(ls.closed == 1, "super close dispatched via subclass override");
        check(ls.loudCloses == 1, "subclass close invoked exactly once");
        final int[] anonClosed = {0};
        try (AutoCloseable anon = new AutoCloseable() {
            public void close() { anonClosed[0]++; }
        }) {
            // body
        }
        check(anonClosed[0] == 1, "anonymous AutoCloseable closed once");
        Silent shared = new Silent();
        try (Silent a = shared; Silent b = shared) {
            // body
        }
        check(shared.closed == 2, "same instance as two resources closes twice");
        Silent manual = new Silent();
        manual.close();
        manual.close();
        check(manual.closed == 2, "manual double close allowed by contract");
    }

    /** 库类型实现 AutoCloseable 的事实表（不打开真实文件，仅类型关系）。 */
    static void testLibraryImplementsAutoCloseable() {
        check(AutoCloseable.class.isAssignableFrom(java.io.FileInputStream.class),
              "FileInputStream implements AutoCloseable");
        check(AutoCloseable.class.isAssignableFrom(java.io.ByteArrayInputStream.class),
              "ByteArrayInputStream implements AutoCloseable");
        check(AutoCloseable.class.isAssignableFrom(java.util.Scanner.class),
              "Scanner implements AutoCloseable");
        check(!AutoCloseable.class.isAssignableFrom(String.class),
              "String does not implement AutoCloseable");
        java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(new byte[] {1});
        try {
            in.close();
            check(true, "BAIS.close() narrow signature callable without checked handling");
        } catch (java.io.IOException e) {
            throw new AssertionError("BAIS.close never throws", e);
        }
    }

    /** JDK-9+ effectively-final 资源形态：try (r) 直接引用外部资源变量。 */
    static void testEffectivelyFinalResource() throws Exception {
        twrLog.clear();
        CloseOrder r = new CloseOrder("efr");
        try (r) {
            twrLog.add("body");
        }
        check(twrLog.equals(Arrays.asList("open:efr", "body", "close:efr")),
              "effectively-final resource form closes normally");
        // 资源变量在 TWR 后仍可读（但已关闭）
        check("efr".equals(r.name), "resource variable readable after TWR");
        // 同一变量可再次进 TWR（接口无生命周期约束，close 幂等性由实现负责）
        twrLog.clear();
        try (r) {
            // body
        }
        check(twrLog.equals(Arrays.asList("close:efr")), "re-entry only logs close (ctor ran once)");
    }

    static void testBlackBox() throws Exception {
        testCloseOrderAndOnce();
        testCloseThrows();
        testOpenCloseLogSequence();
        testThreeResourceReverseOrder();
        testEmptyBodyStillCloses();
        testSuppressedExceptionLoop();
        testCloseThrowsBodyNormal();
        testBothClosesThrow();
        testBodyThrowsTwoClosesSuppress();
        testNestedTwrSuppressionChain();
        testErrorBodyStillCloses();
        testCloseExactlyOnce();
        testNarrowedSignature();
        testCtorThrowsClosesEarlierResources();
        testControlFlowStillCloses();
        testCheckedCloseSuppressedOnUncheckedBody();
        testSubclassAndAnonymousResources();
        testLibraryImplementsAutoCloseable();
        testEffectivelyFinalResource();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testWhiteBoxShape();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
