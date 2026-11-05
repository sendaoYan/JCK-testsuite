/*
 * @test
 * @summary jdk.jfr.Configuration conformance: type shape via reflection (headless-safe).
 * @run main ConfigurationTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import jdk.jfr.Configuration;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.*;
import java.text.ParseException;
import java.util.*;

public class ConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.Configuration", false, ConfigurationTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "create"), "declares create");
        check(hasMethod(c, "getConfiguration"), "declares getConfiguration");
        check(hasMethod(c, "getConfigurations"), "declares getConfigurations");
        check(hasMethod(c, "getContents"), "declares getContents");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getLabel"), "declares getLabel");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "getProvider"), "declares getProvider");
        testDeepBuiltInConfigs();
        testDeepCreateFromContent();
        testDeepNegatives();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepening: contract coverage =================
    static final String MINIMAL_JFC =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<configuration version=\"2.0\" label=\"ML\" description=\"MD\" provider=\"MP\">\n"
        + "  <event name=\"test.Configuration.Ev\">\n"
        + "    <setting name=\"enabled\">true</setting>\n"
        + "    <setting name=\"threshold\">3 ms</setting>\n"
        + "  </event>\n"
        + "</configuration>\n";

    @jdk.jfr.Name("test.Configuration.Ev")
    static class CfgEvent extends jdk.jfr.Event { long x; }

    static void testDeepBuiltInConfigs() throws Exception {
        Configuration d = Configuration.getConfiguration("default");
        check("default".equals(d.getName()), "cfg default name");
        check("Oracle".equals(d.getProvider()), "cfg default provider Oracle");
        check(d.getLabel() != null, "cfg default label non-null");
        check(d.getDescription() != null && !d.getDescription().isEmpty(), "cfg default description non-empty");
        check(d.getContents() != null && !d.getContents().isEmpty(), "cfg default contents non-empty");
        check(!d.getSettings().isEmpty(), "cfg default settings non-empty");
        boolean hasJdkKey = false;
        for (String k : d.getSettings().keySet()) if (k.startsWith("jdk.") && k.endsWith("#enabled")) hasJdkKey = true;
        check(hasJdkKey, "cfg default settings contain jdk.*#enabled keys");
        Configuration p = Configuration.getConfiguration("profile");
        check("profile".equals(p.getName()), "cfg profile name");
        check("Oracle".equals(p.getProvider()), "cfg profile provider Oracle");
        check(!p.getSettings().isEmpty(), "cfg profile settings non-empty");
        check(!d.getSettings().equals(p.getSettings()), "cfg default and profile differ in settings");
        List<Configuration> all = Configuration.getConfigurations();
        check(all.size() >= 2, "cfg getConfigurations at least 2");
        Set<String> names = new HashSet<String>();
        for (Configuration cfg : all) {
            names.add(cfg.getName());
            check(cfg.getProvider() != null, "cfg provider non-null: " + cfg.getName());
        }
        check(names.contains("default") && names.contains("profile"), "cfg configurations include default+profile");
    }

    static void testDeepCreateFromContent() throws Exception {
        // from Reader
        Configuration mc = Configuration.create(new StringReader(MINIMAL_JFC));
        check(mc.getName() == null, "cfg reader-created has null name");
        check("ML".equals(mc.getLabel()), "cfg reader label roundtrip");
        check("MP".equals(mc.getProvider()), "cfg reader provider roundtrip");
        check("MD".equals(mc.getDescription()), "cfg reader description roundtrip");
        check("true".equals(mc.getSettings().get("test.Configuration.Ev#enabled")), "cfg reader enabled roundtrip");
        check("3 ms".equals(mc.getSettings().get("test.Configuration.Ev#threshold")), "cfg reader threshold roundtrip");
        check(mc.getContents() != null, "cfg reader contents non-null");
        // from Path (same content, written to a temp file)
        Path tmp = Files.createTempFile("cfg", ".jfc");
        try {
            Files.write(tmp, MINIMAL_JFC.getBytes("UTF-8"));
            Configuration pc = Configuration.create(tmp);
            check("ML".equals(pc.getLabel()), "cfg path label roundtrip");
            check("true".equals(pc.getSettings().get("test.Configuration.Ev#enabled")), "cfg path enabled roundtrip");
            check("3 ms".equals(pc.getSettings().get("test.Configuration.Ev#threshold")), "cfg path threshold roundtrip");
            check(!pc.getSettings().isEmpty(), "cfg path settings non-empty");
        } finally { Files.deleteIfExists(tmp); }
        // a Configuration drives a Recording
        try (jdk.jfr.Recording r = new jdk.jfr.Recording(mc)) {
            check(!r.getSettings().isEmpty(), "cfg config feeds Recording settings");
        }
    }

    static void testDeepNegatives() {
        try { Configuration.create(new StringReader("this is not xml <<>>")); check(false, "cfg bad content should ParseException"); }
        catch (ParseException e) { check(true, "cfg bad content ParseException"); }
        catch (IOException e) { check(false, "cfg bad content: unexpected IOException " + e); }
        try { Configuration.create((java.io.Reader) null); check(false, "cfg null reader should NPE"); }
        catch (NullPointerException e) { check(true, "cfg null reader NPE"); }
        catch (Exception e) { check(false, "cfg null reader: unexpected " + e); }
        try { Configuration.getConfiguration("bogus"); check(false, "cfg unknown name should fail"); }
        catch (java.nio.file.NoSuchFileException e) {
            check(String.valueOf(e.getMessage()).contains("Could not locate configuration"), "cfg unknown name NoSuchFileException msg");
        }
        catch (IOException e) { check(false, "cfg unknown name: unexpected IOException " + e); }
        catch (ParseException e) { check(false, "cfg unknown name: unexpected ParseException " + e); }
        // version diff: on JDK-8 getConfiguration(null) degrades to a lookup miss (NoSuchFileException);
        // on JDK-27+ it throws NullPointerException directly
        try {
            Configuration.getConfiguration(null);
            check(false, "cfg null name should fail on JDK-8");
        }
        catch (java.nio.file.NoSuchFileException e) {
            check(String.valueOf(e.getMessage()).contains("null"), "cfg null name treated as missing name on JDK-8");
        }
        catch (Exception e) { check(false, "cfg null name: unexpected " + e); }
    }
}
