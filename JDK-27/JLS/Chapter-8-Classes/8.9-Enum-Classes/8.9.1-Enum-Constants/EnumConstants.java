/*
 * @test
 * @summary JLS 8.9.1: an enum constant defines an instance of the enum class and
 *          provides an implicit public static final field of the enum type.
 * @run main EnumConstants
 */

public class EnumConstants {
    enum Season { WINTER, SPRING, SUMMER, FALL }

    public static void main(String[] args) {
        Season[] all = Season.values();
        if (all.length != 4) throw new AssertionError("length: " + all.length);
        if (all[0] != Season.WINTER || all[1] != Season.SPRING
            || all[2] != Season.SUMMER || all[3] != Season.FALL)
            throw new AssertionError("declaration order");
        if (Season.valueOf("WINTER") != Season.WINTER) throw new AssertionError("valueOf");
        if (!"WINTER".equals(Season.WINTER.name())) throw new AssertionError("name");
        if (Season.WINTER.ordinal() != 0 || Season.FALL.ordinal() != 3)
            throw new AssertionError("ordinal");
        System.out.println("PASS");
    }
}
