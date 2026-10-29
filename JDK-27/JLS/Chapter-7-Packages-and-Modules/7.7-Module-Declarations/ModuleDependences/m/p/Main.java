package p;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        // The type java.sql.Connection is usable only because the module
        // declaration requires java.sql. Verifying its module confirms that the
        // dependence was resolved.
        if (!Connection.class.getName().equals("java.sql.Connection"))
            throw new AssertionError("Connection type");
        if (!Connection.class.getModule().getName().equals("java.sql"))
            throw new AssertionError("Connection module");

        System.out.println("PASS");
    }
}
