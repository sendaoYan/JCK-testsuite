/*
 * @test
 * @summary jdk.jshell.tool.JavaShellToolBuilder conformance: tool builder interface.
 * @modules jdk.jshell
 * @run main JavaShellToolBuilderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.util.prefs.Preferences;
import jdk.jshell.tool.JavaShellToolBuilder;

public class JavaShellToolBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static void testWhiteBox() throws Exception {

        Class<?> c = Class.forName("jdk.jshell.tool.JavaShellToolBuilder");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
    }

    static void testBlackBox() throws Exception {
        // builder() is the documented static factory.
        JavaShellToolBuilder cfg = JavaShellToolBuilder.builder();
        check(cfg != null, "builder() non-null");

        ByteArrayInputStream e1 = new ByteArrayInputStream(new byte[0]);
        ByteArrayInputStream e2 = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        PrintStream po = new PrintStream(bo, true, java.nio.charset.StandardCharsets.UTF_8);

        // Every configuration method returns the builder for chaining.
        check(cfg == cfg.in(e1, e2), "in(cmdIn,userIn) returns this");
        check(cfg == cfg.out(po), "out(output) returns this");
        check(cfg == cfg.out(po, po, po), "out(cmdOut,console,userOut) returns this");
        check(cfg == cfg.err(po), "err(error) returns this");
        check(cfg == cfg.err(po, po), "err(cmdErr,userErr) returns this");
        check(cfg == cfg.persistence(new HashMap<String, String>()), "persistence(Map) returns this");
        check(cfg == cfg.persistence((Preferences) null), "persistence(Preferences) returns this");
        check(cfg == cfg.env(new HashMap<String, String>()), "env(Map) returns this");
        check(cfg == cfg.locale(Locale.US), "locale(Locale) returns this");
        check(cfg == cfg.promptCapture(true), "promptCapture(boolean) returns this");
        check(cfg == cfg.interactiveTerminal(false), "interactiveTerminal(boolean) returns this");
        check(cfg == cfg.windowSize(80, 24), "windowSize(columns,rows) returns this");

        // A real, headless run: --help prints usage without reading input.
        ByteArrayOutputStream helpOut = new ByteArrayOutputStream();
        PrintStream hps = new PrintStream(helpOut, true, java.nio.charset.StandardCharsets.UTF_8);
        JavaShellToolBuilder runner = JavaShellToolBuilder.builder()
                .in(new ByteArrayInputStream(new byte[0]), new ByteArrayInputStream(new byte[0]))
                .out(hps)
                .err(hps)
                .persistence(new HashMap<String, String>())
                .env(new HashMap<String, String>())
                .locale(Locale.US)
                .interactiveTerminal(false);
        runner.run("--help");
        hps.flush();
        String help = helpOut.toString(java.nio.charset.StandardCharsets.UTF_8);
        check(!help.isEmpty() && help.contains("Usage"), "run(--help) prints usage text");

        // start() reports the exit status; --version exits 0 and prints the tool name.
        ByteArrayOutputStream verOut = new ByteArrayOutputStream();
        PrintStream vps = new PrintStream(verOut, true, java.nio.charset.StandardCharsets.UTF_8);
        JavaShellToolBuilder starter = JavaShellToolBuilder.builder()
                .in(new ByteArrayInputStream(new byte[0]), new ByteArrayInputStream(new byte[0]))
                .out(vps)
                .err(vps)
                .persistence(new HashMap<String, String>())
                .env(new HashMap<String, String>())
                .locale(Locale.US)
                .interactiveTerminal(false);
        int code = starter.start("--version");
        vps.flush();
        String version = verOut.toString(java.nio.charset.StandardCharsets.UTF_8);
        check(code == 0, "start(--version) returns exit code 0");
        check(!version.isEmpty() && version.contains("jshell"), "start(--version) prints jshell name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
