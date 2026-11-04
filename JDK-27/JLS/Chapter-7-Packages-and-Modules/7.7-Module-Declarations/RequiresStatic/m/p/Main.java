package p;

import java.lang.module.ModuleDescriptor;
import java.sql.Connection;

public class Main {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // requires static is mandatory at compile time: java.sql.Connection is
        // usable here even though the dependence is optional at run time.
        check("java.sql.Connection".equals(Connection.class.getName()),
              "type from requires static dependency usable at compile time");

        ModuleDescriptor md = Main.class.getModule().getDescriptor();
        boolean hasStatic = md.requires().stream()
                .anyMatch(r -> r.name().equals("java.sql")
                        && r.modifiers().contains(ModuleDescriptor.Requires.Modifier.STATIC));
        check(hasStatic, "requires static java.sql recorded in descriptor");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
