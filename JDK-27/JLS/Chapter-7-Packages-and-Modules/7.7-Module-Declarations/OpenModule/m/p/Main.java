package p;

import java.lang.module.ModuleDescriptor;

public class Main {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        ModuleDescriptor md = Main.class.getModule().getDescriptor();
        check(md.isOpen(), "open module isOpen");

        // 7.7: an open module grants run-time reflective access to all its
        // packages, even those not exported; q is not exported but is opened.
        Class<?> c = Class.forName("q.Hidden");
        check("q.Hidden".equals(c.getName()), "non-exported package reflectively accessible");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
