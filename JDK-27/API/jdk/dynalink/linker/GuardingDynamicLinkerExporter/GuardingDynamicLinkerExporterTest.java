/*
 * @test
 * @summary jdk.dynalink.linker.GuardingDynamicLinkerExporter conformance: white-box structure + black-box behavior.
 * @run main GuardingDynamicLinkerExporterTest
 */

import jdk.dynalink.*;
import jdk.dynalink.linker.*;
import jdk.dynalink.linker.support.*;
import jdk.dynalink.beans.*;
import jdk.dynalink.support.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class GuardingDynamicLinkerExporterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static final class Exporter extends GuardingDynamicLinkerExporter {
        public java.util.List<GuardingDynamicLinker> get() { return java.util.Collections.emptyList(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.linker.GuardingDynamicLinkerExporter");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "get"), "get");

    }

    static void testBlackBox() throws Exception {
        check(GuardingDynamicLinkerExporter.AUTOLOAD_PERMISSION_NAME != null, "AUTOLOAD_PERMISSION_NAME");
        check("dynalink.exportLinkersAutomatically".equals(GuardingDynamicLinkerExporter.AUTOLOAD_PERMISSION_NAME), "AUTOLOAD_PERMISSION_NAME value");
        Exporter e = new Exporter();
        check(e instanceof GuardingDynamicLinkerExporter, "subclass instanceof");
        java.util.List<GuardingDynamicLinker> links = e.get();
        check(links != null && links.isEmpty(), "get returns empty list");
    }


    static class Concrete extends GuardingDynamicLinkerExporter {
        public List<GuardingDynamicLinker> get() { return List.of(); }
    }

    static void testDeepContract() {
        check(Modifier.isAbstract(GuardingDynamicLinkerExporter.class.getModifiers()), "abstract class");
        check(Modifier.isProtected(GuardingDynamicLinkerExporter.class.getDeclaredConstructors()[0].getModifiers()),
              "protected constructor");
        check(java.util.function.Supplier.class.isAssignableFrom(GuardingDynamicLinkerExporter.class),
              "implements Supplier<List<GuardingDynamicLinker>>");
        check(GuardingDynamicLinkerExporter.AUTOLOAD_PERMISSION_NAME.equals("dynalink.exportLinkersAutomatically"),
              "autoload permission name");
        boolean hasGet = false;
        for (Method m : GuardingDynamicLinkerExporter.class.getMethods())
            if (m.getName().equals("get") && m.getParameterCount() == 0) hasGet = true;
        check(hasGet, "public get() with no arguments");
        check(new Concrete().get().isEmpty(), "subclass returns its list");
        // ServiceLoader discovery contract: the class is the advertised service type
        check(java.util.ServiceLoader.load(GuardingDynamicLinkerExporter.class).getClass() != null, "service loadable");
        // a JDK-default runtime ships no exporters (jdk.dynalink has none in its module descriptor provides)
        boolean any = false;
        for (GuardingDynamicLinkerExporter e : java.util.ServiceLoader.load(GuardingDynamicLinkerExporter.class,
                GuardingDynamicLinkerExporter.class.getClassLoader())) any = true;
        check(!any, "no exporters registered in a stock runtime");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testDeepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
