/*
 * @test
 * @summary java.nio.file.WatchService conformance: white-box structure + black-box behavior (JDK-8).
 * @run main WatchServiceTest
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

public class WatchServiceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.WatchService");
        check(c.isInterface(), "WatchService is an interface");
        check(java.io.Closeable.class.isAssignableFrom(c), "extends Closeable");
        for (String n : new String[] {"close", "poll", "take"})
            check(java.util.Arrays.stream(c.getMethods())
                  .anyMatch(m -> m.getName().equals(n)), "has method " + n);
        long pollVariants = java.util.Arrays.stream(c.getMethods())
                .filter(m -> m.getName().equals("poll")).count();
        check(pollVariants == 2, "two poll overloads");
    }

    // ---- black-box: behavior ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            check(ws.poll() == null, "immediate poll null when idle");
            long t0 = System.nanoTime();
            check(ws.poll(150, TimeUnit.MILLISECONDS) == null,
                  "timed poll null when idle");
            check(System.nanoTime() - t0 >= 140_000_000L, "timeout elapsed");

            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            Files.write(dir.resolve("t.txt"), "t".getBytes(StandardCharsets.UTF_8));
            java.nio.file.WatchKey pKey = ws.poll(10, TimeUnit.SECONDS);
            check(pKey != null && !pKey.pollEvents().isEmpty(), "poll returns the signalled key");
            pKey.reset();

            Files.delete(dir.resolve("t.txt"));
            java.nio.file.WatchKey dKey = ws.poll(10, TimeUnit.SECONDS);
            boolean sawDelete = false;
            for (java.nio.file.WatchEvent<?> e : dKey.pollEvents())
                if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) sawDelete = true;
            check(dKey != null && sawDelete, "delete key delivered");
            dKey.reset();

            WatchService doomed = FileSystems.getDefault().newWatchService();
            doomed.close();
            boolean closed = false;
            try { doomed.poll(); }
            catch (java.nio.file.ClosedWatchServiceException e) { closed = true; }
            check(closed, "poll after close -> ClosedWatchServiceException");
            boolean closedTake = false;
            try { doomed.take(); }
            catch (java.nio.file.ClosedWatchServiceException e) { closedTake = true; }
            check(closedTake, "take after close -> ClosedWatchServiceException");
            doomed.close();
            check(true, "double close tolerated");
        } finally {
            try { Files.deleteIfExists(dir.resolve("t.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: take() blocking contract ----
    static void testTakeBlocking() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            // take() returns once an event has queued
            Files.write(dir.resolve("tk.txt"), "t".getBytes(StandardCharsets.UTF_8));
            java.nio.file.WatchKey k = ws.take();
            check(k != null && k.watchable() == dir, "take returns the signalled key");
            k.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("tk.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }

        // take() blocked in another thread is woken by close() (daemon, bounded)
        final WatchService ws = FileSystems.getDefault().newWatchService();
        final java.util.concurrent.CountDownLatch started =
                new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch done =
                new java.util.concurrent.CountDownLatch(1);
        final String[] outcome = new String[1];
        Thread taker = new Thread(new Runnable() {
            public void run() {
                started.countDown();
                try { ws.take(); outcome[0] = "returned"; }
                catch (java.nio.file.ClosedWatchServiceException e) {
                    outcome[0] = "ClosedWatchServiceException";
                } catch (Exception e) { outcome[0] = "other:" + e; }
                done.countDown();
            }
        }, "jck-ws-taker");
        taker.setDaemon(true);
        taker.start();
        check(started.await(5, TimeUnit.SECONDS), "taker started (must)");
        // give the taker a bounded moment to enter take(), then close
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (taker.getState() != Thread.State.WAITING
                && System.nanoTime() < deadline) Thread.yield();
        ws.close();
        check(done.await(5, TimeUnit.SECONDS), "take() woken within bounds (must)");
        taker.join(TimeUnit.SECONDS.toMillis(5));
        check("ClosedWatchServiceException".equals(outcome[0]),
              "blocked take() + close -> ClosedWatchServiceException (measured)");
    }

    // ---- appended: registration failure matrix (exact exception types) ----
    static void testRegistrationFailures() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            boolean iae = false;
            try { dir.register(ws); }
            catch (IllegalArgumentException e) { iae = true; }
            check(iae, "register with no events -> IllegalArgumentException");
            boolean nsfe = false;
            try { dir.resolve("ghost").register(ws, StandardWatchEventKinds.ENTRY_CREATE); }
            catch (java.nio.file.NoSuchFileException e) { nsfe = true; }
            check(nsfe, "register missing directory -> NoSuchFileException");
            Path plain = dir.resolve("plain.txt");
            Files.write(plain, "p".getBytes(StandardCharsets.UTF_8));
            boolean nde = false;
            try { plain.register(ws, StandardWatchEventKinds.ENTRY_CREATE); }
            catch (java.nio.file.NotDirectoryException e) { nde = true; }
            check(nde, "register a regular file -> NotDirectoryException");
            java.nio.file.WatchEvent.Kind<java.nio.file.Path> bogus =
                    new java.nio.file.WatchEvent.Kind<java.nio.file.Path>() {
                        public String name() { return "JCK_BOGUS"; }
                        public Class<java.nio.file.Path> type() { return java.nio.file.Path.class; } };
            boolean uoe = false;
            try { dir.register(ws, bogus); }
            catch (UnsupportedOperationException e) { uoe = true; }
            check(uoe, "register unknown kind -> UnsupportedOperationException");
            // registering OVERFLOW alone degenerates to "no events" (measured)
            boolean ovf = false;
            try { dir.register(ws, StandardWatchEventKinds.OVERFLOW); }
            catch (IllegalArgumentException e) { ovf = true; }
            check(ovf, "register(OVERFLOW) alone -> IllegalArgumentException (measured)");
            // OVERFLOW is accepted alongside real events
            java.nio.file.WatchKey ok = dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.OVERFLOW);
            check(ok != null && ok.isValid(), "OVERFLOW alongside ENTRY_CREATE accepted");
            ok.cancel();
        } finally {
            try { Files.deleteIfExists(dir.resolve("plain.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
        // a closed service refuses registration
        WatchService closed = FileSystems.getDefault().newWatchService();
        closed.close();
        boolean cwse = false;
        try { dir.register(closed, StandardWatchEventKinds.ENTRY_CREATE); }
        catch (java.nio.file.ClosedWatchServiceException e) { cwse = true; }
        check(cwse, "register on closed service -> ClosedWatchServiceException (measured)");
    }

    // ---- appended: poll edge cases + service isolation ----
    static void testPollEdgesAndIsolation() throws Exception {
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            check(ws.poll(0, TimeUnit.SECONDS) == null,
                  "poll(0,SECONDS) idle returns null without blocking (measured)");
            boolean npe = false;
            try { ws.poll(1, null); }
            catch (NullPointerException e) { npe = true; }
            check(npe, "poll(1, null unit) -> NullPointerException (measured)");
        }

        Path dir = Files.createTempDirectory("jck");
        try (WatchService ws1 = FileSystems.getDefault().newWatchService();
             WatchService ws2 = FileSystems.getDefault().newWatchService()) {
            java.nio.file.WatchKey k1 = dir.register(ws1, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            Files.write(dir.resolve("iso.txt"), "i".getBytes(StandardCharsets.UTF_8));
            java.nio.file.WatchKey got1 = ws1.poll(5, TimeUnit.SECONDS);
            check(got1 == k1, "registered service receives the key (must)");
            check(got1.pollEvents().stream()
                    .anyMatch(e -> e.kind() == StandardWatchEventKinds.ENTRY_CREATE),
                  "ENTRY_CREATE delivered to ws1 (must)");
            got1.reset();
            check(ws2.poll(300, TimeUnit.MILLISECONDS) == null,
                  "idle service receives nothing (isolation)");
            // closing one service leaves the other fully functional
            ws2.close();
            Files.delete(dir.resolve("iso.txt"));
            java.nio.file.WatchKey got2 = ws1.poll(5, TimeUnit.SECONDS);
            check(got2 == k1, "ws1 still delivers after ws2.close()");
            boolean sawDelete = false;
            for (java.nio.file.WatchEvent<?> e : got2.pollEvents())
                if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) sawDelete = true;
            check(sawDelete, "ENTRY_DELETE still delivered to ws1 (must)");
            got2.reset();
        } finally {
            try { Files.deleteIfExists(dir.resolve("iso.txt")); } catch (Exception ignore) { }
            Files.deleteIfExists(dir);
        }
    }

    // ---- appended: exact interface shape + closed-service edges (measured) ----
    static void testServiceShape() throws Exception {
        Class<?> c = WatchService.class;
        // the interface declares exactly poll()/poll(long,TimeUnit)/take()/close()
        check(c.getDeclaredMethods().length == 4,
              "declares exactly four methods (measured)");
        java.lang.reflect.Method p0 = c.getMethod("poll");
        check(p0.getReturnType() == java.nio.file.WatchKey.class
              && p0.getParameterTypes().length == 0,
              "poll() returns WatchKey with no arguments");
        java.lang.reflect.Method p1 = c.getMethod("poll", long.class, TimeUnit.class);
        check(p1.getReturnType() == java.nio.file.WatchKey.class,
              "poll(long,TimeUnit) returns WatchKey");
        check(java.util.Arrays.equals(p1.getParameterTypes(),
                  new Class<?>[] {long.class, TimeUnit.class}),
              "poll(long,TimeUnit) exact parameter types");
        check(c.getMethod("take").getReturnType() == java.nio.file.WatchKey.class,
              "take() returns WatchKey");
        check(c.getMethod("close").getReturnType() == void.class,
              "close() returns void");
        // each newWatchService() call yields a fresh, distinct service
        WatchService a = FileSystems.getDefault().newWatchService();
        WatchService b = FileSystems.getDefault().newWatchService();
        check(a != b, "newWatchService returns distinct instances");
        a.close(); b.close();
        // the timed poll on a closed service fails like the untimed one
        boolean timedClosed = false;
        try { a.poll(0, TimeUnit.SECONDS); }
        catch (java.nio.file.ClosedWatchServiceException e) { timedClosed = true; }
        check(timedClosed, "timed poll after close -> ClosedWatchServiceException (measured)");
        a.close();
        check(true, "close after close again tolerated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTakeBlocking();
        testRegistrationFailures();
        testPollEdgesAndIsolation();
        testServiceShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
