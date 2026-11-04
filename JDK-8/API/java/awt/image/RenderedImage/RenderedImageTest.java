/*
 * @test
 * @summary java.awt.image.RenderedImage conformance: interface reflection.
 * @run main RenderedImageTest
 */

public class RenderedImageTest {
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
        Class<?> c = Class.forName("java.awt.image.RenderedImage");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getSources"), "method getSources");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getPropertyNames"), "method getPropertyNames");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "getSampleModel"), "method getSampleModel");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getMinX"), "method getMinX");
        check(hasMethod(c, "getMinY"), "method getMinY");
        check(hasMethod(c, "getNumXTiles"), "method getNumXTiles");
        check(hasMethod(c, "getNumYTiles"), "method getNumYTiles");
        check(hasMethod(c, "getMinTileX"), "method getMinTileX");
        check(hasMethod(c, "getMinTileY"), "method getMinTileY");
        check(hasMethod(c, "getTileWidth"), "method getTileWidth");
        check(hasMethod(c, "getTileHeight"), "method getTileHeight");
        check(hasMethod(c, "getTileGridXOffset"), "method getTileGridXOffset");
        check(hasMethod(c, "getTileGridYOffset"), "method getTileGridYOffset");
        check(hasMethod(c, "getTile"), "method getTile");
        check(hasMethod(c, "getData"), "method getData");
        check(hasMethod(c, "copyData"), "method copyData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
