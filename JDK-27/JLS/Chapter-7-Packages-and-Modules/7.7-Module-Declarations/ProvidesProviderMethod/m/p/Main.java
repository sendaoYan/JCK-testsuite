package p;

import java.util.ServiceLoader;

public class Main {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // 7.7.4: a service provider may declare a public static provider method
        // with no formal parameters; ServiceLoader discovers it.
        boolean found = false;
        for (S s : ServiceLoader.load(S.class)) {
            if ("P".equals(s.name())) found = true;
        }
        check(found, "provider method service provider discoverable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
