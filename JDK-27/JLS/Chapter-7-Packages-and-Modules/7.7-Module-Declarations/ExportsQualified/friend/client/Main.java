package client;

import pub.Api;

public class Main {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // pub is exported by provider only to the friend module, which is the
        // current module, so pub.Api is accessible here.
        Api a = new Api();
        check(a.id == 5, "qualified export grants access to the friend module");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
