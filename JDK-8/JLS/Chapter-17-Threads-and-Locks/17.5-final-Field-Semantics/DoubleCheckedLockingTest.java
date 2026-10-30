/*
 * @test
 * @summary JLS 17.5 (Java SE 8): the safe double-checked locking idiom (volatile
 *          field + synchronized re-check) returns a fully constructed object
 *          whose final fields are correctly visible, and constructs exactly one
 *          instance.
 * @run main DoubleCheckedLockingTest
 */

public class DoubleCheckedLockingTest {
    static final class Helper {
        final int value;
        Helper(int v) { value = v; }
    }

    static final class Foo {
        private volatile Helper helper;
        Helper getHelper() {
            Helper localRef = helper;
            if (localRef == null) {
                synchronized (this) {
                    localRef = helper;
                    if (localRef == null) {
                        helper = localRef = new Helper(42);
                    }
                }
            }
            return localRef;
        }
    }

    public static void main(String[] args) throws Exception {
        final Foo foo = new Foo();
        final int N = 8;
        final Thread[] threads = new Thread[N];
        final Helper[] results = new Helper[N];
        final boolean[] ok = new boolean[N];

        for (int i = 0; i < N; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                Helper h = foo.getHelper();
                results[idx] = h;
                ok[idx] = (h != null && h.value == 42);
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join(5000);
        for (Thread t : threads) {
            if (t.isAlive()) throw new AssertionError("worker did not finish");
        }

        for (int i = 0; i < N; i++) {
            if (!ok[i]) throw new AssertionError("thread " + i + " saw a bad helper");
            if (results[i] != results[0]) throw new AssertionError("not a single helper instance");
        }

        System.out.println("PASS");
    }
}
