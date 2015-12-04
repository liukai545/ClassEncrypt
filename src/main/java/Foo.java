// Foo.java

public class Foo {
    public Foo() {
        String str = "sss/ggg/k$lkjf";
        System.out.println(str.replaceAll("\\/|\\$", "."));

        System.out.println("foo");
    }

    public static void main(String[] args) {
        new Foo();
    }
}