package apipkg;

public class Api {
    public int pub = 1;
    protected int prot = 2;
    int pack = 3;
    private int priv = 4;

    public int getPriv() { return priv; }
    public int getPack() { return pack; }
    public int getProt() { return prot; }
}
