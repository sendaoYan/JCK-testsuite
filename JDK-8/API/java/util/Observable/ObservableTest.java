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

    public static void main(String[] args) {
        testObservable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
