public class Animal {
    public String name() { return "animal"; }
}

class Dog extends Animal implements Speaker {
    @Override public String name() { return "dog"; }
    @Override public String speak() { return "woof"; }
}

interface Speaker { String speak(); }
