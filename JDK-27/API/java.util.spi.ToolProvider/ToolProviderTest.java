/*
 * @test
 * @summary java.util.spi.ToolProvider conformance: interface name/description/run/findFirst.
 * @run main ToolProviderTest
 */
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.spi.ToolProvider;

public class ToolProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ToolProvider.class.isInterface(), "is interface");
        ToolProvider tp = new ToolProvider() {
            public String name() { return "probe-tool"; }
            public int run(PrintWriter out, PrintWriter err, String... args) { out.println("hi"); return 0; }
        };
        check("probe-tool".equals(tp.name()), "name");
        check(tp.description().isEmpty(), "description empty");
        StringWriter sw = new StringWriter();
        int rc = tp.run(new PrintWriter(sw), new PrintWriter(sw), "a");
        check(rc == 0 && sw.toString().contains("hi"), "run(PrintWriter)");
        check(ToolProvider.findFirst("definitely-not-a-tool").isEmpty(), "findFirst empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
