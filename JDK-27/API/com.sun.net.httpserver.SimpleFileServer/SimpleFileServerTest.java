/*
 * @test
 * @summary com.sun.net.httpserver.SimpleFileServer conformance: white-box structure + black-box behavior.
 * @run main SimpleFileServerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class SimpleFileServerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.net.httpserver.SimpleFileServer");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getConstructors().length == 0, "no public constructor");
        Set<String> m = methodNames(c);
        check(m.contains("createFileServer"), "createFileServer");
        check(m.contains("createFileHandler"), "createFileHandler");
        check(m.contains("createOutputFilter"), "createOutputFilter");
        // OutputLevel enum
        Class<?> lvl = load("com.sun.net.httpserver.SimpleFileServer$OutputLevel");
        check(lvl.isEnum(), "OutputLevel is enum");
        check(Enum.valueOf((Class) lvl, "NONE") != null &&
              Enum.valueOf((Class) lvl, "INFO") != null &&
              Enum.valueOf((Class) lvl, "VERBOSE") != null, "OutputLevel has NONE/INFO/VERBOSE");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // create a temp directory with one file
        Path root = Files.createTempDirectory("jck-sfs");
        try {
            Files.writeString(root.resolve("hello.txt"), "hello world", StandardCharsets.UTF_8);

            // createFileHandler must be absolute and existing directory
            com.sun.net.httpserver.HttpHandler fh = com.sun.net.httpserver.SimpleFileServer.createFileHandler(root);
            check(fh != null, "createFileHandler non-null");

            // validation of root directory
            boolean threw = false;
            try { com.sun.net.httpserver.SimpleFileServer.createFileHandler(Path.of("relative")); }
            catch (IllegalArgumentException e) { threw = true; }
            check(threw, "createFileHandler relative path throws IAE");
            threw = false;
            try { com.sun.net.httpserver.SimpleFileServer.createFileHandler(root.resolve("missing")); }
            catch (IllegalArgumentException e) { threw = true; }
            check(threw, "createFileHandler non-existent path throws IAE");

            // round-trip serving a real file and a directory listing
            com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", fh);
            server.start();
            try {
                int port = server.getAddress().getPort();
                Resp file = request(port, "/hello.txt", "GET");
                check(file.code == 200, "GET file status 200");
                check(file.body.equals("hello world"), "GET file body");

                Resp listing = request(port, "/", "GET");
                check(listing.code == 200, "GET directory status 200");
                check(listing.body.contains("hello.txt"), "directory listing contains file name");

                Resp post = request(port, "/hello.txt", "POST");
                check(post.code == 405, "POST yields 405");
            } finally {
                server.stop(0);
            }

            // createOutputFilter: NONE rejected, INFO/VERBOSE produce a usable filter
            boolean threwNpe = false;
            try { com.sun.net.httpserver.SimpleFileServer.createOutputFilter(
                    new ByteArrayOutputStream(), com.sun.net.httpserver.SimpleFileServer.OutputLevel.NONE); }
            catch (IllegalArgumentException e) { threwNpe = true; }
            check(threwNpe, "createOutputFilter NONE throws IAE");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            com.sun.net.httpserver.Filter of = com.sun.net.httpserver.SimpleFileServer.createOutputFilter(
                    out, com.sun.net.httpserver.SimpleFileServer.OutputLevel.INFO);
            check(of != null, "createOutputFilter INFO non-null");
            check(of.description() != null, "output filter has description");

            // output filter actually logs to the given stream during a request.
            // afterHandler filters run in reverse add order, so a latch filter added
            // before the output filter counts down only after the output filter writes.
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            com.sun.net.httpserver.Filter latchFilter =
                    com.sun.net.httpserver.Filter.afterHandler("latch", ex -> latch.countDown());
            com.sun.net.httpserver.HttpServer s2 = com.sun.net.httpserver.HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 0), 0);
            com.sun.net.httpserver.HttpContext ctx = s2.createContext("/", fh);
            ctx.getFilters().add(latchFilter);
            ctx.getFilters().add(of);
            s2.start();
            try {
                request(s2.getAddress().getPort(), "/hello.txt", "GET");
                check(latch.await(10, java.util.concurrent.TimeUnit.SECONDS),
                      "afterHandler filters completed");
                String log = out.toString(StandardCharsets.UTF_8);
                check(log.contains("GET"), "output filter logged GET request");
            } finally {
                s2.stop(0);
            }

            // createFileServer returns a bound, not-started server with ephemeral port
            com.sun.net.httpserver.HttpServer fs = com.sun.net.httpserver.SimpleFileServer.createFileServer(
                    new InetSocketAddress("127.0.0.1", 0), root, com.sun.net.httpserver.SimpleFileServer.OutputLevel.NONE);
            try {
                check(fs != null, "createFileServer non-null");
                check(fs.getAddress().getPort() > 0, "createFileServer ephemeral port");
            } finally {
                fs.stop(0);
            }
        } finally {
            // cleanup temp directory
            try (var walk = Files.walk(root)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException e) { } });
            }
        }
    }

    static final class Resp { final int code; final String body;
        Resp(int c, String b){code=c;body=b;} }

    static Resp request(int port, String path, String method) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + port + path).openConnection();
        c.setRequestMethod(method);
        int code = c.getResponseCode();
        InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
        byte[] b = in == null ? new byte[0] : in.readAllBytes();
        if (in != null) in.close();
        c.disconnect();
        return new Resp(code, new String(b, StandardCharsets.UTF_8));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
