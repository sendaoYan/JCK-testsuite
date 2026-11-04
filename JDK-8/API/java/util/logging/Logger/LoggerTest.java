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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
