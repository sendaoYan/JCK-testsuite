/*
 * @test
 * @summary java.util.logging.Logger conformance: white-box structure + black-box behavior.
 * @run main LoggerTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.Logger");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "direct superclass Object");
        check(Modifier.isStatic(c.getMethod("getLogger", String.class).getModifiers()), "getLogger static");
        check(Modifier.isStatic(c.getMethod("getGlobal").getModifiers()), "getGlobal static");
        check(Modifier.isStatic(c.getMethod("getAnonymousLogger").getModifiers()), "getAnonymousLogger static");
        String[] methods = {"log","logp","logrb","entering","exiting","throwing",
                "severe","warning","info","config","fine","finer","finest",
                "setLevel","getLevel","isLoggable","getName","addHandler","removeHandler",
                "getHandlers","setUseParentHandlers","getUseParentHandlers","getParent",
                "setFilter","getFilter","getResourceBundle","getResourceBundleName",
                "setResourceBundle","setParent"};
        for (String m : methods) check(hasMethod(c, m), "has method " + m);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // named hierarchy: getLogger("a.b") parents getLogger("a")
        Logger child = Logger.getLogger("jck.logging.h.A.B");
        Logger parent = Logger.getLogger("jck.logging.h.A");
        child.setUseParentHandlers(false);
        parent.setUseParentHandlers(false);
        check(child.getParent() == parent, "hierarchical parent");
        check("jck.logging.h.A.B".equals(child.getName()), "getName child");
        check(parent.getParent() != null, "parent has its own parent (root)");

        // level get/set (null means inherit)
        child.setLevel(Level.INFO);
        check(child.getLevel() == Level.INFO, "get/setLevel");
        child.setLevel(null);
        check(child.getLevel() == null, "setLevel(null) clears");

        // isLoggable reflects set level
        child.setLevel(Level.WARNING);
        check(child.isLoggable(Level.WARNING), "isLoggable WARNING true");
        check(child.isLoggable(Level.SEVERE), "isLoggable SEVERE true");
        check(!child.isLoggable(Level.INFO), "isLoggable INFO false");

        // capture handler + useParentHandlers(false) -> no console output
        CapturingHandler cap = new CapturingHandler();
        child.addHandler(cap);
        check(child.getHandlers().length == 1, "getHandlers length after add");
        child.setUseParentHandlers(false);
        check(!child.getUseParentHandlers(), "getUseParentHandlers false");

        // log(Level, String) composes through to handler
        child.log(Level.SEVERE, "plain");
        check(cap.records.size() == 1, "log(Level,String) captured");
        check("plain".equals(cap.records.get(0).getMessage()), "captured message");

        // convenience methods produce correct levels
        child.setLevel(Level.ALL);
        cap.records.clear();
        child.severe("s"); child.warning("w"); child.info("i"); child.config("c");
        child.fine("f"); child.finer("fr"); child.finest("fs");
        check(cap.records.size() == 7, "seven convenience methods captured");
        check(cap.records.get(0).getLevel() == Level.SEVERE, "severe level");
        check(cap.records.get(1).getLevel() == Level.WARNING, "warning level");
        check(cap.records.get(2).getLevel() == Level.INFO, "info level");
        check(cap.records.get(3).getLevel() == Level.CONFIG, "config level");
        check(cap.records.get(4).getLevel() == Level.FINE, "fine level");
        check(cap.records.get(5).getLevel() == Level.FINER, "finer level");
        check(cap.records.get(6).getLevel() == Level.FINEST, "finest level");

        // log(Level, String, Object) keeps format + parameters
        cap.records.clear();
        child.log(Level.INFO, "value={0}", Integer.valueOf(7));
        check(cap.records.size() == 1, "log(Level,String,Object) captured");
        check("value={0}".equals(cap.records.get(0).getMessage()), "param format message preserved");
        check(cap.records.get(0).getParameters().length == 1
                && Integer.valueOf(7).equals(cap.records.get(0).getParameters()[0]),
                "param object preserved");

        // log(Level, String, Object[])
        cap.records.clear();
        child.log(Level.INFO, "{0}+{1}", new Object[] {"a", "b"});
        check(cap.records.get(0).getParameters().length == 2, "object[] params preserved");

        // log(Level, String, Throwable)
        cap.records.clear();
        RuntimeException boom = new RuntimeException("boom");
        child.log(Level.SEVERE, "oops", boom);
        check(cap.records.get(0).getThrown() == boom, "log(Level,String,Throwable) sets thrown");

        // log(Level, Supplier<String>) lazily resolves
        cap.records.clear();
        Supplier<String> sup = () -> "supplied";
        child.log(Level.INFO, sup);
        check("supplied".equals(cap.records.get(0).getMessage()), "log(Level,Supplier) message");

        // entering/exiting at FINER
        cap.records.clear();
        child.entering("LoggerTest", "run");
        child.exiting("LoggerTest", "run");
        check(cap.records.size() == 2, "entering/exiting produce records");
        check(cap.records.get(0).getLevel() == Level.FINER, "entering level FINER");
        check(cap.records.get(1).getLevel() == Level.FINER, "exiting level FINER");
        check(cap.records.get(0).getMessage().contains("ENTRY"), "entering message ENTRY");
        check(cap.records.get(1).getMessage().contains("RETURN"), "exiting message RETURN");

        // removeHandler
        child.removeHandler(cap);
        check(child.getHandlers().length == 0, "getHandlers length after remove");

        // global + anonymous loggers
        Logger global = Logger.getGlobal();
        check(global != null, "getGlobal non-null");
        check("global".equals(global.getName()), "global logger named global");
        check(Logger.getGlobal() == global, "getGlobal returns same instance");

        Logger anon = Logger.getAnonymousLogger();
        check(anon.getName() == null, "anonymous logger has null name");
        check(anon.getParent() != null, "anonymous logger has parent");
        check(anon.getUseParentHandlers(), "anonymous logger uses parent handlers by default");

        // resource bundle accessors default null
        check(child.getResourceBundle() == null, "getResourceBundle null");
        check(child.getResourceBundleName() == null, "getResourceBundleName null");
    }

    static final class CapturingHandler extends Handler {
        final List<LogRecord> records = new ArrayList<>();
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;
            records.add(record);
        }
        public void flush() { }
        public void close() { }
    }


    // 捕获 Handler：记录 publish 的 LogRecord（行为断言用）
    static final class Capture extends java.util.logging.Handler {
        final java.util.List<java.util.logging.LogRecord> records = new java.util.ArrayList<java.util.logging.LogRecord>();
        boolean closed;
        public void publish(java.util.logging.LogRecord r) { records.add(r); }
        public void flush() {}
        public void close() { closed = true; }
    }
    static java.lang.String drain(java.util.logging.Logger l, java.util.logging.Level level,
                                  Runnable... actions) throws Exception {
        final java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.util.logging.StreamHandler h = new java.util.logging.StreamHandler(bos, new java.util.logging.SimpleFormatter());
        h.setLevel(java.util.logging.Level.ALL);
        l.setUseParentHandlers(false);
        l.addHandler(h);
        java.util.logging.Level old = l.getLevel();
        l.setLevel(level);
        for (Runnable r : actions) r.run();
        h.flush();
        l.removeHandler(h);
        if (old != null) l.setLevel(old);
        return bos.toString();
    }

    // ===================== 契约深化：同名单例/entering-exiting-throwing 捕获/参数格式化/Filter 门控/handler 数组快照 =====================
    static void testContracts() throws Exception {
        // 同名 getLogger 返回同一实例；匿名 logger 名为 null
        check(java.util.logging.Logger.getLogger("jck.logging.same") == java.util.logging.Logger.getLogger("jck.logging.same"),
              "getLogger same name -> same instance");
        java.util.logging.Logger anon = java.util.logging.Logger.getAnonymousLogger();
        check(anon.getName() == null && anon.getUseParentHandlers(), "anonymous logger: null name, parent handlers on");
        // entering/exiting/throwing 输出 ENTRY/RETURN/THROW
        java.util.logging.Logger l = java.util.logging.Logger.getLogger("jck.logging.enter");
        String s = drain(l, java.util.logging.Level.FINER,
                () -> l.entering("C", "m", new Object[] {"p1", 2}),
                () -> l.exiting("C", "m", "ret"),
                () -> l.throwing("C", "m", new RuntimeException("boom")));
        check(s.contains("ENTRY") && s.contains("p1"), "entering logs ENTRY with params");
        check(s.contains("RETURN") && s.contains("ret"), "exiting logs RETURN with result");
        check(s.contains("THROW") && s.contains("boom"), "throwing logs THROW with exception");
        // MessageFormat 参数格式化
        java.util.logging.Logger lf = java.util.logging.Logger.getLogger("jck.logging.fmt");
        String f = drain(lf, java.util.logging.Level.INFO,
                () -> lf.log(java.util.logging.Level.INFO, "code {0} and {1}", new Object[] {"x", 7}));
        check(f.contains("code x and 7"), "log params MessageFormat interpolation");
        // 级别门控：FINE 不发布到 INFO 级 logger
        java.util.logging.Logger li = java.util.logging.Logger.getLogger("jck.logging.gate");
        String gated = drain(li, java.util.logging.Level.INFO, () -> li.fine("dropped"));
        check(!gated.contains("dropped"), "FINE filtered at INFO logger");
        String passed = drain(li, java.util.logging.Level.INFO, () -> li.warning("kept"));
        check(passed.contains("kept"), "WARNING passes at INFO logger");
        check(!li.isLoggable(java.util.logging.Level.FINER) && li.isLoggable(java.util.logging.Level.SEVERE),
              "isLoggable is threshold on record level (FINER<INFO false, SEVERE>=INFO true)");
        // Filter 门控
        final int[] fcount = {0};
        li.setFilter(r -> { fcount[0]++; return r.getMessage().startsWith("ok"); });
        String filtered = drain(li, java.util.logging.Level.INFO,
                () -> li.info("ok-1"), () -> li.info("no-1"));
        check(filtered.contains("ok-1") && !filtered.contains("no-1"), "Filter gates by record");
        check(fcount[0] >= 2, "Filter consulted for each record");
        li.setFilter(null);
        // handler 数组：add/remove 即时反映（返回快照）
        java.util.logging.Logger lh = java.util.logging.Logger.getLogger("jck.logging.handlers");
        Capture c1 = new Capture();
        Capture c2 = new Capture();
        lh.addHandler(c1);
        check(lh.getHandlers().length == 1, "one handler added");
        lh.addHandler(c2);
        check(lh.getHandlers().length == 2, "two handlers added");
        lh.removeHandler(c1);
        check(lh.getHandlers().length == 1 && lh.getHandlers()[0] == c2, "removeHandler removes exact handler");
        lh.removeHandler(c1);
        check(lh.getHandlers().length == 1, "removing absent handler is no-op");
        lh.removeHandler(c2);
        // severe/warning/info 便捷方法级别正确
        Capture cc = new Capture();
        java.util.logging.Logger ls = java.util.logging.Logger.getLogger("jck.logging.conv");
        ls.setUseParentHandlers(false);
        ls.addHandler(cc);
        cc.setLevel(java.util.logging.Level.ALL);
        ls.setLevel(java.util.logging.Level.ALL);
        ls.severe("S"); ls.warning("W"); ls.info("I"); ls.config("C"); ls.fine("F"); ls.finer("R"); ls.finest("T");
        check(cc.records.size() == 7, "all 7 convenience methods publish at ALL");
        check(cc.records.get(0).getLevel() == java.util.logging.Level.SEVERE
              && cc.records.get(1).getLevel() == java.util.logging.Level.WARNING
              && cc.records.get(2).getLevel() == java.util.logging.Level.INFO
              && cc.records.get(3).getLevel() == java.util.logging.Level.CONFIG
              && cc.records.get(4).getLevel() == java.util.logging.Level.FINE
              && cc.records.get(5).getLevel() == java.util.logging.Level.FINER
              && cc.records.get(6).getLevel() == java.util.logging.Level.FINEST,
              "convenience methods map to correct levels");
        // log(Level, null) NPE / 空消息允许
        try {
            ls.log(null, "x");
            check(false, "log(null Level) NPE");
        } catch (NullPointerException e) { check(true, "log(null Level) NPE"); }
        cc.records.clear();
        ls.log(java.util.logging.Level.INFO, "");
        check(cc.records.size() == 1 && "".equals(cc.records.get(0).getMessage()), "empty message allowed");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
