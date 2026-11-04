/*
 * @test
 * @summary java.util.Observer API conformance: update is invoked with the source and argument.
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

    static void testObserver() {
        final Object[] seen = new Object[2];
        Observer o = (src, arg) -> {
            seen[0] = src;
            seen[1] = arg;
        };
        Observable src = new Observable();
        o.update(src, "x");
        check(seen[0] == src, "update receives the source");
        check("x".equals(seen[1]), "update receives the argument");
    }


    // ===================== 契约深化：update 回调参数矩阵（直接调用契约） =====================
    static void testObserverContracts() {
        final Object[] got = new Object[2];
        Observer o = (src, arg) -> { got[0] = src; got[1] = arg; };
        Observable src = new Observable();
        o.update(src, "arg");
        check(got[0] == src && "arg".equals(got[1]), "update receives (source, arg) verbatim");
        o.update(null, null);
        check(got[0] == null && got[1] == null, "update tolerates nulls (no validation)");
    }

    public static void main(String[] args) {
        testObserver();
        testObserverContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
