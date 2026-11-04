/*
 * @test
 * @summary java.util.Observable API conformance: observer registration and notification.
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
        void fire() {
            setChanged();
            notifyObservers("arg");
        }
    }

    static void testObservable() {
        final Object[] seen = new Object[2];
        MyObservable obs = new MyObservable();
        Observer o = (src, arg) -> {
            seen[0] = src;
            seen[1] = arg;
        };
        obs.addObserver(o);
        check(obs.countObservers() == 1, "countObservers");
        obs.fire();
        check(seen[0] == obs, "observer receives the source");
        check("arg".equals(seen[1]), "observer receives the argument");
        check(!obs.hasChanged(), "notifyObservers clears the changed flag");
        obs.deleteObserver(o);
        check(obs.countObservers() == 0, "deleteObserver");
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

    public static void main(String[] args) {
        testObservable();
        testObservableContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
