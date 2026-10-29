package p;

import java.lang.module.ModuleDescriptor;

public class Main {
    public static void main(String[] args) {
        ModuleDescriptor md = Main.class.getModule().getDescriptor();
        boolean usesService = md.uses().contains("api.Service");
        if (!usesService) throw new AssertionError("uses api.Service");
        System.out.println("PASS");
    }
}
