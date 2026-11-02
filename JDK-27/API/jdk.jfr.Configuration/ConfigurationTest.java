/*
 * @test
 * @summary jdk.jfr.Configuration conformance: built-in configurations and settings.
 * @run main ConfigurationTest
 */

import jdk.jfr.*;
import java.util.List;

public class ConfigurationTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        List<Configuration> cs = Configuration.getConfigurations();
        check(cs != null && cs.size() > 0, "has configurations");
        Configuration c = Configuration.getConfiguration("default");
        check(c != null, "default configuration");
        check("default".equals(c.getName()), "name");
        check(c.getSettings() != null && !c.getSettings().isEmpty(), "settings");
        check(c.getContents() != null, "contents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
