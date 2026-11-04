/*
 * @test
 * @summary java.util.Observable conformance: white-box structure + black-box behavior.
 * @run main ObservableTest
 */

import java.util.Observable;
import java.util.Observer;

public class ObservableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class MyObservable extends Observable {
        public void doSetChanged() { setChanged(); }
        public void doClearChanged() { clearChanged(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Observable");
        check(c.getSuperclass() == Object.class, "extends Object");
        c.getConstructor();
        c.getDeclaredMethod("addObserver", Observer.class);
        c.getDeclaredMethod("deleteObserver", Observer.class);
        c.getDeclaredMethod("deleteObservers");
        c.getDeclaredMethod("notifyObservers");
        c.getDeclaredMethod("notifyObservers", Object.class);
        c.getDeclaredMethod("countObservers");
        c.getDeclaredMethod("hasChanged");
        c.getDeclaredMethod("setChanged");
        c.getDeclaredMethod("clearChanged");
        check(true, "all Observable methods present");
    }

    static void testBlackBox() {
        MyObservable o = new MyObservable();
        final String[] got = {null};
        Observer obs = (src, arg) -> got[0] = (arg == null ? "<null>" : String.valueOf(arg));
        o.addObserver(obs);
        check(o.countObservers() == 1, "countObservers after add");

        o.notifyObservers("noop");
        check(got[0] == null, "not notified without setChanged");

        o.doSetChanged();
        check(o.hasChanged(), "hasChanged after setChanged");
        o.notifyObservers();
        check("<null>".equals(got[0]), "notifyObservers() passes null arg");
        check(!o.hasChanged(), "hasChanged reset after notify");

        o.doSetChanged();
        o.notifyObservers("payload");
        check("payload".equals(got[0]), "notifyObservers(arg) passes arg");

        o.deleteObserver(obs);
        check(o.countObservers() == 0, "deleteObserver");
        o.addObserver(obs);
        o.deleteObservers();
        check(o.countObservers() == 0, "deleteObservers");

        o.doSetChanged();
        o.doClearChanged();
        check(!o.hasChanged(), "clearChanged");
    }


    // ===================== 契约深化：未 setChanged 不通知/参数 null 语义/deleteObservers/NPE =====================
    static class MyObservable2 extends Observable {
        void markChanged() { setChanged(); }
        void unmark() { clearChanged(); }
    }

    static void testObservableContracts() {
        final java.util.List<String> log = new java.util.ArrayList<String>();
        MyObservable2 ob = new MyObservable2();
        Observer obs = (o, arg) -> log.add(String.valueOf(arg));
        ob.addObserver(obs);
        check(ob.countObservers() == 1, "one observer registered");
        try { ob.addObserver(null); check(false, "addObserver(null) NPE"); }
        catch (NullPointerException npe) { check(true, "addObserver(null) NPE"); }
        ob.notifyObservers("without-changed");
        check(log.isEmpty(), "notifyObservers without setChanged() delivers nothing");
        ob.markChanged();
        check(ob.hasChanged(), "setChanged visible via hasChanged");
        ob.notifyObservers("payload");
        check(log.size() == 1 && "payload".equals(log.get(0)), "setChanged+notify delivers arg");
        check(!ob.hasChanged(), "notification clears the changed flag");
        ob.markChanged();
        ob.notifyObservers(null);
        check(log.size() == 2 && "null".equals(log.get(1)), "notifyObservers(null) delivers null arg");
        ob.deleteObserver(obs);
        ob.markChanged();
        ob.notifyObservers("after-delete");
        check(log.size() == 2, "deleted observer not notified");
        Observer second = (o, arg) -> log.add("second");
        ob.addObserver(obs);
        ob.addObserver(second);
        check(ob.countObservers() == 2, "two observers");
        ob.markChanged();
        ob.notifyObservers("both");
        check(log.contains("both") && log.contains("second"), "all observers notified");
        ob.deleteObservers();
        check(ob.countObservers() == 0, "deleteObservers clears all");
        ob.markChanged();
        ob.unmark();
        ob.notifyObservers("suppressed");
        check(!log.contains("suppressed"), "clearChanged suppresses notification");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testObservableContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
