/*
 * @test
 * @summary java.awt.image.WritableRenderedImage conformance: interface reflection.
 * @run main WritableRenderedImageTest
 */

public class WritableRenderedImageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.image.WritableRenderedImage");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.RenderedImage"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addTileObserver"), "method addTileObserver");
        check(hasMethod(c, "removeTileObserver"), "method removeTileObserver");
        check(hasMethod(c, "getWritableTile"), "method getWritableTile");
        check(hasMethod(c, "releaseWritableTile"), "method releaseWritableTile");
        check(hasMethod(c, "isTileWritable"), "method isTileWritable");
        check(hasMethod(c, "getWritableTileIndices"), "method getWritableTileIndices");
        check(hasMethod(c, "hasTileWriters"), "method hasTileWriters");
        check(hasMethod(c, "setData"), "method setData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
