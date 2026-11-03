/*
 * @test
 * @summary java.util.Observer conformance: white-box structure + black-box behavior.
 * @run main ObserverTest
 */

import java.util.Observable;
import java.util.Observer;

public class ObserverTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Src extends Observable {
        public void fire(Object arg) { setChanged(); notifyObservers(arg); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Observer");
        check(c.isInterface(), "is an interface");
        check(c.getDeclaredMethods().length == 1, "single abstract method");
        check(c.getDeclaredMethod("update", Observable.class, Object.class).getReturnType()
                == void.class, "update returns void");
    }

    static void testBlackBox() {
        Src src = new Src();
        final Observable[] gotO = {null};
        final Object[] gotA = {null};
        Observer o1 = (o, a) -> { gotO[0] = o; gotA[0] = a; };
        final int[] count = {0};
        Observer o2 = (o, a) -> count[0]++;
        src.addObserver(o1);
        src.addObserver(o2);
        src.fire("x");
        check(gotO[0] == src, "update receives the Observable");
        check("x".equals(gotA[0]), "update receives the arg");
        check(count[0] == 1, "all observers notified");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
