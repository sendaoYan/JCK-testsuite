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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
