package p;

public class P implements S {
    public static P provider() {
        return new P();
    }

    public String name() {
        return "P";
    }
}
