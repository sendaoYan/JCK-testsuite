import java.util.concurrent.CountDownLatch;
/*
 * @test
 * @summary java.lang.InheritableThreadLocal API conformance: extends ThreadLocal, child
 *          threads inherit the parent's value at creation time, and childValue override
 *          transforms the inherited value.
 * @run main InheritableThreadLocalTest
 */

public class InheritableThreadLocalTest {
    static int passed;

    // Exposes the protected childValue() so we can assert the default identity behavior.
    static class DefaultITL extends InheritableThreadLocal<String> {
        String child(String parentValue) { return childValue(parentValue); }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.InheritableThreadLocal");
        check(c.getSuperclass() == ThreadLocal.class, "extends ThreadLocal");
        check(!c.isInterface(), "is a class");
    }

    static void testInheritance() throws Exception {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("parent");
        final String[] childVal = {null};
        Thread t = new Thread(() -> childVal[0] = itl.get());
        t.setDaemon(true);
        t.start();
        t.join();
        check("parent".equals(childVal[0]), "child inherits parent's value");
        check("parent".equals(itl.get()), "parent value unchanged");
    }

    static void testChildValueOverride() throws Exception {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            protected String childValue(String parentValue) { return parentValue + "-child"; }
        };
        itl.set("p");
        final String[] childVal = {null};
        Thread t = new Thread(() -> childVal[0] = itl.get());
        t.setDaemon(true);
        t.start();
        t.join();
        check("p-child".equals(childVal[0]), "childValue transforms parent value");
    }

    static void testIsThreadLocal() {
        check(new InheritableThreadLocal<>() instanceof ThreadLocal,
              "InheritableThreadLocal extends ThreadLocal");
    }


    // ===================== 契约深化（2026-11）=====================

    static final class Payload {
        final String tag;
        Payload(String tag) { this.tag = tag; }
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testIdentityInheritance() throws Exception {
        final InheritableThreadLocal<Payload> itl = new InheritableThreadLocal<>();
        final Payload parentValue = new Payload("parent");
        itl.set(parentValue);
        final Payload[] childSeen = {null};
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread child = new Thread(() -> {
            childSeen[0] = itl.get();
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "itl-identity");
        child.setDaemon(true);
        child.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "child started");
        // Default childValue is identity: the child holds the very same object.
        check(childSeen[0] == parentValue, "child inherits the exact same instance");
        // Child-side set does not touch the parent while both are alive.
        final boolean[] childOwnSet = {false};
        // (set happens below after release, sequentially, so use a second round)
        release.countDown();
        child.join(10_000);
        check(itl.get() == parentValue, "parent value unchanged after child ran");
        check(childSeen[0] == parentValue, "observed inheritance was stable");
    }

    static void testChildSetIsolation() throws Exception {
        final InheritableThreadLocal<Payload> itl = new InheritableThreadLocal<>();
        final Payload parentValue = new Payload("p");
        itl.set(parentValue);
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final Payload[] childReadAfterSet = {null};
        final Payload[] childReadAfterRemove = {null};
        Thread child = new Thread(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Payload own = new Payload("child-own");
            itl.set(own);
            childReadAfterSet[0] = itl.get();
            itl.remove();
            childReadAfterRemove[0] = itl.get();
        }, "itl-isolation");
        child.setDaemon(true);
        child.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "child started");
        release.countDown();
        child.join(10_000);
        check(childReadAfterSet[0] != null && childReadAfterSet[0].tag.equals("child-own"),
              "child set/read its own value");
        check(itl.get() == parentValue, "parent value untouched by child set/remove");
        check(childReadAfterRemove[0] == null, "child remove clears only the child's value");
    }

    static void testConstructionTimeSnapshot() throws Exception {
        final InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("before-construction");
        final String[] childSeen = {null};
        // Values are captured when the Thread object is constructed, NOT when
        // it is started (locked by probing both JDKs).
        Thread child = new Thread(() -> childSeen[0] = itl.get());
        itl.set("after-construction");
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check("before-construction".equals(childSeen[0]),
              "child inherits the value held at construction time");
        check("after-construction".equals(itl.get()), "parent keeps its later value");
    }

    static void testParentSetAfterConstructionInvisible() throws Exception {
        final InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("v1");
        final String[] childSeen = {null};
        Thread child = new Thread(() -> childSeen[0] = itl.get());
        itl.set("v2");
        itl.set("v3");
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check("v1".equals(childSeen[0]), "only the construction-time value is inherited");
        check("v3".equals(itl.get()), "parent advanced to v3");
    }

    static void testRemoveBeforeConstruction() throws Exception {
        final InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("doomed");
        itl.remove();
        final String[] childSeen = {"sentinel"};
        Thread child = new Thread(() -> childSeen[0] = itl.get());
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check(childSeen[0] == null, "removed parent value is not inherited");
    }

    static void testNullValueInheritance() throws Exception {
        final String[] childSeen = {"sentinel"};
        final String[] childValueArg = {"unset"};
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            @Override protected String childValue(String parentValue) {
                childValueArg[0] = String.valueOf(parentValue);
                return parentValue;
            }
        };
        itl.set(null);
        Thread child = new Thread(() -> childSeen[0] = itl.get());
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check(childSeen[0] == null, "explicit null parent value inherited as null");
        check("null".equals(childValueArg[0]), "childValue invoked with null");
    }

    static void testChildValueTransform() throws Exception {
        final InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            @Override protected String childValue(String parentValue) {
                return parentValue + "-child";
            }
        };
        itl.set("base");
        final String[] childSeen = {null};
        Thread child = new Thread(() -> childSeen[0] = itl.get());
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check("base-child".equals(childSeen[0]), "child sees the transformed value");
        check("base".equals(itl.get()), "parent keeps the untransformed value");
        // A second child gets a fresh transform of the (unchanged) parent value.
        final String[] secondSeen = {null};
        Thread child2 = new Thread(() -> secondSeen[0] = itl.get());
        child2.setDaemon(true);
        child2.start();
        child2.join(10_000);
        check("base-child".equals(secondSeen[0]), "second child sees the same transform");
    }

    static void testChildValueNotCalledForParent() {
        final int[] calls = {0};
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            @Override protected String childValue(String parentValue) {
                calls[0]++;
                return parentValue;
            }
        };
        itl.set("v");
        check(calls[0] == 0, "childValue not called when the parent gets/sets");
        itl.get();
        check(calls[0] == 0, "childValue not called for the parent's own get");
    }

    static void testGrandchildChain() throws Exception {
        final InheritableThreadLocal<StringBuilder> itl = new InheritableThreadLocal<StringBuilder>() {
            @Override protected StringBuilder childValue(StringBuilder parentValue) {
                return new StringBuilder(parentValue).append(">");
            }
        };
        itl.set(new StringBuilder("root"));
        final String[] grandchildSeen = {null};
        final CountDownLatch grandchildDone = new CountDownLatch(1);
        Thread child = new Thread(() -> {
            // child.get() is "root>" (transformed); the grandchild captures the
            // child's CURRENT value at its construction time.
            StringBuilder childValue = itl.get();
            Thread grandchild = new Thread(() -> {
                grandchildSeen[0] = itl.get().toString();
                grandchildDone.countDown();
            });
            grandchild.setDaemon(true);
            grandchild.start();
            try {
                grandchildDone.await(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "itl-child");
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check(grandchildSeen[0] != null && grandchildSeen[0].equals("root>>"),
              "grandchild transforms the child's inherited value again");
        check(itl.get().toString().equals("root"), "root value untouched");
    }

    static void testPlainThreadLocalNotInherited() throws Exception {
        final ThreadLocal<String> plain = new ThreadLocal<>();
        plain.set("plain-value");
        final InheritableThreadLocal<String> inheritable = new InheritableThreadLocal<>();
        inheritable.set("inheritable-value");
        final String[] plainSeen = {"sentinel"};
        final String[] inheritableSeen = {null};
        Thread child = new Thread(() -> {
            plainSeen[0] = plain.get();
            inheritableSeen[0] = inheritable.get();
        });
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check(plainSeen[0] == null, "plain ThreadLocal value is NOT inherited");
        check("inheritable-value".equals(inheritableSeen[0]), "InheritableThreadLocal value IS inherited");
    }

    static void testMultiKeyInheritance() throws Exception {
        final InheritableThreadLocal<String> a = new InheritableThreadLocal<>();
        final InheritableThreadLocal<String> b = new InheritableThreadLocal<>();
        a.set("A");
        b.set("B");
        final String[] seenA = {null};
        final String[] seenB = {null};
        Thread child = new Thread(() -> {
            seenA[0] = a.get();
            seenB[0] = b.get();
        });
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check("A".equals(seenA[0]) && "B".equals(seenB[0]), "all inheritable keys inherited");
        a.remove();
        final String[] secondA = {"sentinel"};
        final String[] secondB = {null};
        Thread child2 = new Thread(() -> {
            secondA[0] = a.get();
            secondB[0] = b.get();
        });
        child2.setDaemon(true);
        child2.start();
        child2.join(10_000);
        check(secondA[0] == null, "removed key not inherited by the second child");
        check("B".equals(secondB[0]), "other key still inherited");
    }

    static void testWorkerLifecycle() throws Exception {
        final InheritableThreadLocal<Payload> itl = new InheritableThreadLocal<>();
        final Payload p = new Payload("lifecycle");
        itl.set(p);
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final Payload[] beforeSet = {null};
        final Payload[] afterSet = {null};
        Thread child = new Thread(() -> {
            beforeSet[0] = itl.get();
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            itl.set(new Payload("own"));
            afterSet[0] = itl.get();
        }, "itl-lifecycle");
        child.setDaemon(true);
        child.start();
        check(entered.await(10, java.util.concurrent.TimeUnit.SECONDS), "child waiting");
        check(beforeSet[0] == p, "child reads the inherited value while alive");
        release.countDown();
        child.join(10_000);
        check(afterSet[0] != null && afterSet[0].tag.equals("own"), "child overwrote after release");
        check(itl.get() == p, "parent still holds its original value");
    }

    static void testIdentitySemantics() {
        InheritableThreadLocal<String> a = new InheritableThreadLocal<>();
        InheritableThreadLocal<String> b = new InheritableThreadLocal<>();
        check(a.equals(a), "ITL equals itself");
        check(!a.equals(b), "distinct ITLs not equal");
        check(a.toString().startsWith("java.lang.InheritableThreadLocal@"), "toString format");
        int h = a.hashCode();
        check(a.hashCode() == h, "hashCode stable");
        a.set("x");
        check(a.hashCode() == h, "hashCode unaffected by set");
        a.remove();
        check(a.hashCode() == h, "hashCode unaffected by remove");
    }

    static void testDefaultChildValueMore() {
        DefaultITL d = new DefaultITL();
        check(d.child("abc") == "abc", "default childValue identity (interned)");
        check(d.child(null) == null, "default childValue of null is null");
        StringBuilder sb = new StringBuilder("x");
        check(d.child(sb.toString()).equals("x"), "default childValue value equality");
        // The ITL itself behaves as a plain ThreadLocal in the defining thread.
        d.set("own");
        check("own".equals(d.get()), "ITL set/get in the defining thread");
        check(d.get() == d.get(), "same instance across gets");
    }

    static void testWhiteBoxMore() throws Exception {
        Class<?> c = Class.forName("java.lang.InheritableThreadLocal");
        check(c.getSuperclass() == ThreadLocal.class, "extends ThreadLocal (reflection)");
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredMethod("childValue", Object.class).getModifiers()),
              "childValue is protected");
        check(!java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("childValue", Object.class).getModifiers()),
              "childValue is an instance method");
        check(java.lang.reflect.Modifier.isPublic(c.getConstructor().getModifiers()),
              "public no-arg constructor");
    }

    static void testTwoChildrenIndependent() throws Exception {
        final InheritableThreadLocal<Payload> itl = new InheritableThreadLocal<>();
        final Payload base = new Payload("base");
        itl.set(base);
        final CountDownLatch bothIn = new CountDownLatch(2);
        final CountDownLatch release = new CountDownLatch(1);
        final Payload[] readA = {null};
        final Payload[] readB = {null};
        Thread a = new Thread(() -> {
            itl.set(new Payload("A-own"));
            bothIn.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            readA[0] = itl.get();
        }, "itl-two-a");
        Thread b = new Thread(() -> {
            bothIn.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            readB[0] = itl.get();
        }, "itl-two-b");
        a.setDaemon(true);
        b.setDaemon(true);
        a.start();
        b.start();
        check(bothIn.await(10, java.util.concurrent.TimeUnit.SECONDS), "both children alive");
        release.countDown();
        a.join(10_000);
        b.join(10_000);
        check(readA[0] != null && readA[0].tag.equals("A-own"), "child A kept its own value");
        check(readB[0] == base, "child B kept the inherited value");
        check(itl.get() == base, "parent value unaffected by both children");
    }

    static void testUnsetParentValue() throws Exception {
        final InheritableThreadLocal<Payload> itl = new InheritableThreadLocal<>();
        final Payload[] childSeen = {new Payload("sentinel")};
        Thread child = new Thread(() -> childSeen[0] = itl.get());
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check(childSeen[0] == null, "unset ITL inherits nothing (get null)");
        check(itl.get() == null, "parent get null too");
    }

    static void testInitialValueIsInheritanceSource() throws Exception {
        final InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            @Override protected String initialValue() { return "from-initialValue"; }
        };
        check("from-initialValue".equals(itl.get()), "parent reads initialValue");
        final String[] childSeen = {null};
        Thread child = new Thread(() -> childSeen[0] = itl.get());
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check("from-initialValue".equals(childSeen[0]),
              "child inherits the parent's initialValue result");
        itl.set("overridden");
        final String[] child2Seen = {null};
        Thread child2 = new Thread(() -> child2Seen[0] = itl.get());
        child2.setDaemon(true);
        child2.start();
        child2.join(10_000);
        check("overridden".equals(child2Seen[0]), "explicit set becomes the inheritance source");
    }

    static void testWithInitialNotInheritable() throws Exception {
        // ThreadLocal.withInitial returns a plain ThreadLocal: children do not
        // inherit the parent's instance; the supplier runs again in the child.
        final int[] calls = {0};
        final ThreadLocal<Object> tl = ThreadLocal.withInitial(() -> {
            calls[0]++;
            return new Object();
        });
        final Object parentInstance = tl.get();
        final Object[] childSeen = {null};
        Thread child = new Thread(() -> childSeen[0] = tl.get());
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check(childSeen[0] != null, "child got a value from its own supplier run");
        check(childSeen[0] != parentInstance, "withInitial instance is not inherited");
        check(calls[0] == 2, "supplier ran once per thread");
        check(tl.get() == parentInstance, "parent instance cached");
    }

    static void testGrandchildSnapshot() throws Exception {
        final InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        itl.set("root");
        final String[] grandchildSeen = {null};
        final CountDownLatch grandchildConstructed = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread child = new Thread(() -> {
            itl.set("child-late");
            Thread grandchild = new Thread(() -> grandchildSeen[0] = itl.get());
            grandchild.setDaemon(true);
            grandchildConstructed.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            grandchild.start();
            try {
                grandchild.join(10_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "itl-snapshot-child");
        child.setDaemon(true);
        child.start();
        check(grandchildConstructed.await(10, java.util.concurrent.TimeUnit.SECONDS), "grandchild constructed");
        release.countDown();
        child.join(10_000);
        check("child-late".equals(grandchildSeen[0]),
              "grandchild captures the child's value at its construction time");
        check("root".equals(itl.get()), "root value untouched");
    }

    static void testThreeKeysTwoChildren() throws Exception {
        final InheritableThreadLocal<String> k1 = new InheritableThreadLocal<>();
        final InheritableThreadLocal<String> k2 = new InheritableThreadLocal<>();
        final InheritableThreadLocal<String> k3 = new InheritableThreadLocal<>();
        k1.set("one");
        k2.set("two");
        k3.set("three");
        final String[] c1 = {null, null, null};
        final String[] c2 = {null, null, null};
        Thread a = new Thread(() -> {
            c1[0] = k1.get();
            c1[1] = k2.get();
            c1[2] = k3.get();
        });
        Thread b = new Thread(() -> {
            c2[0] = k1.get();
            c2[1] = k2.get();
            c2[2] = k3.get();
        });
        a.setDaemon(true);
        b.setDaemon(true);
        a.start();
        b.start();
        a.join(10_000);
        b.join(10_000);
        check("one".equals(c1[0]) && "two".equals(c1[1]) && "three".equals(c1[2]),
              "first child inherits all three keys");
        check("one".equals(c2[0]) && "two".equals(c2[1]) && "three".equals(c2[2]),
              "second child inherits all three keys");
        k2.remove();
        final String[] late = {null, "sentinel", null};
        Thread lateChild = new Thread(() -> {
            late[0] = k1.get();
            late[1] = k2.get();
            late[2] = k3.get();
        });
        lateChild.setDaemon(true);
        lateChild.start();
        lateChild.join(10_000);
        check("one".equals(late[0]) && late[1] == null && "three".equals(late[2]),
              "removed key invisible to a later child");
    }

    static void testChildValueOncePerChild() throws Exception {
        final int[] calls = {0};
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<String>() {
            @Override protected String childValue(String parentValue) {
                calls[0]++;
                return parentValue;
            }
        };
        itl.set("v");
        check(calls[0] == 0, "no childValue call before any child");
        Thread c1 = new Thread(() -> itl.get());
        Thread c2 = new Thread(() -> itl.get());
        check(calls[0] == 2, "childValue ran once per constructed child");
        c1.setDaemon(true);
        c2.setDaemon(true);
        c1.start();
        c2.start();
        c1.join(10_000);
        c2.join(10_000);
        check(calls[0] == 2, "no extra childValue calls while children run");
        check("v".equals(itl.get()), "parent value readable after children ran");
    }

    static void testChildRemovalStopsLineage() throws Exception {
        final InheritableThreadLocal<Payload> itl = new InheritableThreadLocal<>();
        final Payload p = new Payload("lineage");
        itl.set(p);
        final Payload[] grandchildSeen = {new Payload("sentinel")};
        final CountDownLatch grandchildDone = new CountDownLatch(1);
        Thread child = new Thread(() -> {
            check(itl.get() == p, "child inherited before removal");
            itl.remove();
            Thread grandchild = new Thread(() -> {
                grandchildSeen[0] = itl.get();
                grandchildDone.countDown();
            });
            grandchild.setDaemon(true);
            grandchild.start();
            try {
                grandchildDone.await(10, java.util.concurrent.TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "itl-lineage");
        child.setDaemon(true);
        child.start();
        child.join(10_000);
        check(grandchildSeen[0] == null, "grandchild inherits nothing after child removal");
        check(itl.get() == p, "root lineage value still intact");
    }

    static void testRepeatedInheritanceIdentity() throws Exception {
        final InheritableThreadLocal<Payload> itl = new InheritableThreadLocal<>();
        final Payload p = new Payload("repeat");
        itl.set(p);
        for (int i = 1; i <= 3; i++) {
            final Payload[] seen = {null};
            Thread child = new Thread(() -> seen[0] = itl.get());
            child.setDaemon(true);
            child.start();
            child.join(10_000);
            check(seen[0] == p, "child " + i + " inherits the same instance");
        }
        check(itl.get() == p, "parent value stable across children");
    }

    static void testFreshITLBasics() {
        InheritableThreadLocal<String> itl = new InheritableThreadLocal<>();
        check(itl.get() == null, "fresh ITL get null");
        itl.set("solo");
        check("solo".equals(itl.get()), "ITL set/get");
        itl.remove();
        check(itl.get() == null, "ITL remove");
        DefaultITL d1 = new DefaultITL();
        DefaultITL d2 = new DefaultITL();
        check(d1.child("x").equals("x") && d2.child("y").equals("y"),
              "default childValue independent per instance");
        check(itl.toString().startsWith("java.lang.InheritableThreadLocal@"),
              "fresh ITL toString format");
        check(itl.equals(itl) && !itl.equals(d1), "ITL equals semantics");
        check(itl.hashCode() == itl.hashCode(), "ITL hashCode stable");
    }
    // ---- Part 2b: default childValue(Object) ----
    static void testBlackBox() {
        DefaultITL itl = new DefaultITL();
        String parent = "parent-value";
        check(parent == itl.child(parent), "default childValue returns the parent value unchanged");
        check("x" == itl.child("x"), "default childValue is identity for any value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testIsThreadLocal();
        testInheritance();
        testChildValueOverride();
        testBlackBox();
        // ---- 契约深化（2026-11）----
        testIdentityInheritance();
        testChildSetIsolation();
        testConstructionTimeSnapshot();
        testParentSetAfterConstructionInvisible();
        testRemoveBeforeConstruction();
        testNullValueInheritance();
        testChildValueTransform();
        testChildValueNotCalledForParent();
        testGrandchildChain();
        testPlainThreadLocalNotInherited();
        testMultiKeyInheritance();
        testWorkerLifecycle();
        testIdentitySemantics();
        testDefaultChildValueMore();
        testWhiteBoxMore();
        testTwoChildrenIndependent();
        testUnsetParentValue();
        testInitialValueIsInheritanceSource();
        testWithInitialNotInheritable();
        testGrandchildSnapshot();
        testThreeKeysTwoChildren();
        testChildValueOncePerChild();
        testChildRemovalStopsLineage();
        testRepeatedInheritanceIdentity();
        testFreshITLBasics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
