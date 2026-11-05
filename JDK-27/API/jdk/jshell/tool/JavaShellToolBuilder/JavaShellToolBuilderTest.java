/*
 * @test
 * @summary jdk.jshell.tool.JavaShellToolBuilder conformance: tool builder interface.
 * @modules jdk.jshell
 * @run main/othervm/timeout=240 JavaShellToolBuilderTest
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


    // ---- deep contract: a real headless REPL session through the tool ----
    static String session(String input, boolean promptCapture) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(o, true, java.nio.charset.StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        JavaShellToolBuilder b = JavaShellToolBuilder.builder()
                .in(in, new ByteArrayInputStream(new byte[0]))
                .out(ps).err(ps)
                .persistence(new HashMap<String, String>())
                .env(new HashMap<String, String>())
                .locale(Locale.US)
                .interactiveTerminal(false)
                .promptCapture(promptCapture);
        b.run("-q");
        ps.flush();
        return o.toString(java.nio.charset.StandardCharsets.UTF_8);
    }

    static void testReplSession() throws Exception {
        // expressions are evaluated and echoed as $N ==> value
        String out = session("1 + 2;\n/exit\n", false);
        check(out.contains("jshell>"), "prompt printed");
        check(out.contains("$1 ==> 3"), "expression result echoed: $1 ==> 3");
        // declarations persist across inputs in one session
        String out2 = session("int vx = 10;\nvx + 5;\n/exit\n", false);
        check(out2.contains("==> 15"), "second snippet sees the declared variable: " + out2.replace("\n", "|"));
        // promptCapture suppresses prompt decorations
        String out3 = session("2 + 2;\n/exit\n", true);
        check(!out3.contains("jshell>"), "promptCapture suppresses prompts");
        check(out3.contains("$1 ==> 4"), "promptCapture keeps results");
    }

    static void testSeparatedChannels() throws Exception {
        // out(cmdOut, console, userOut): user-code stdout goes only to userOut
        ByteArrayOutputStream cmdB = new ByteArrayOutputStream(), conB = new ByteArrayOutputStream(), usrB = new ByteArrayOutputStream();
        PrintStream cmdOut = new PrintStream(cmdB, true, java.nio.charset.StandardCharsets.UTF_8);
        PrintStream console = new PrintStream(conB, true, java.nio.charset.StandardCharsets.UTF_8);
        PrintStream userOut = new PrintStream(usrB, true, java.nio.charset.StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(
                "System.out.println(\"user-side\");\n/exit\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        HashMap<String, String> prefs = new HashMap<String, String>();
        JavaShellToolBuilder.builder()
                .in(in, new ByteArrayInputStream(new byte[0]))
                .out(cmdOut, console, userOut)
                .err(cmdOut)
                .persistence(prefs)
                .env(new HashMap<String, String>())
                .locale(Locale.US)
                .interactiveTerminal(false)
                .run("-q");
        cmdOut.flush(); console.flush(); userOut.flush();
        check(usrB.toString(java.nio.charset.StandardCharsets.UTF_8).contains("user-side"),
              "user stdout routed to the userOut channel");
        check(conB.toString(java.nio.charset.StandardCharsets.UTF_8).contains("jshell>"),
              "prompts routed to the console channel");
        check(cmdB.size() == 0, "command channel stays quiet for ordinary snippets");
        // the persistence map receives the session history and replay state
        java.util.Set<String> keys = new java.util.TreeSet<>(prefs.keySet());
        check(keys.contains("HISTORY_LINE_0"), "history line 0 persisted");
        check(keys.contains("HISTORY_LINE_1"), "history line 1 persisted");
        check(keys.contains("REPLAY_RESTORE"), "replay state persisted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReplSession();
        testSeparatedChannels();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
