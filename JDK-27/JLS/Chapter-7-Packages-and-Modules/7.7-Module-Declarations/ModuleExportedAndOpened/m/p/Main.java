package p;

import java.lang.reflect.Field;

public class Main {
    public static void main(String[] args) throws Exception {
        // exports p: a public member of an exported package is reflectively
        // accessible without setAccessible.
        Class<?> exported = Class.forName("p.Exported");
        int pub = exported.getField("pub").getInt(null);
        if (pub != 7) throw new AssertionError("exported public member");

        // opens q: deep reflection on a private member is permitted because q is
        // opened. Without the opens directive this would throw
        // java.lang.reflect.InaccessibleObjectException.
        Class<?> opened = Class.forName("q.Opened");
        Object o = opened.getDeclaredConstructor().newInstance();
        Field secret = opened.getDeclaredField("secret");
        secret.setAccessible(true);
        int v = secret.getInt(o);
        if (v != 42) throw new AssertionError("opened private member");

        System.out.println("PASS");
    }
}
