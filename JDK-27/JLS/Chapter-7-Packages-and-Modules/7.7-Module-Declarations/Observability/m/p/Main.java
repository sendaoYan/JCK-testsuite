package p;

import java.lang.module.ResolvedModule;

public class Main {
    public static void main(String[] args) {
        ModuleLayer boot = ModuleLayer.boot();

        // 7.7.6: java.sql is observable, hence resolvable into the boot layer.
        if (boot.findModule("java.sql").isEmpty())
            throw new AssertionError("java.sql not observable");

        boolean resolved = boot.configuration().modules().stream()
                .map(ResolvedModule::name)
                .anyMatch("java.sql"::equals);
        if (!resolved) throw new AssertionError("java.sql not in resolved configuration");

        System.out.println("PASS");
    }
}
