package p;

import java.lang.module.ModuleDescriptor;

public class Main {
    public static void main(String[] args) {
        Module me = Main.class.getModule();
        ModuleDescriptor md = me.getDescriptor();
        if (md == null) throw new AssertionError("unnamed module");

        // 7.7: the module has the declared name.
        if (!md.name().equals("m")) throw new AssertionError("name: " + md.name());

        // 7.7.1: every module implicitly requires java.base.
        boolean hasJavaBase = md.requires().stream()
                .anyMatch(r -> r.name().equals("java.base")
                               && r.modifiers().contains(ModuleDescriptor.Requires.Modifier.MANDATED));
        if (!hasJavaBase) throw new AssertionError("implicit java.base require");

        // 7.7.2: exports p makes the package available to other modules.
        boolean exportsP = md.exports().stream()
                .anyMatch(e -> e.source().equals("p") && !e.isQualified());
        if (!exportsP) throw new AssertionError("exports p");

        System.out.println("PASS");
    }
}
