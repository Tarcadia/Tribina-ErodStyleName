package net.tarcadia.tribina.erod.stylename.util.type;

public record Pair<X, Y>(X x, Y y) implements Cloneable{

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public Pair<X, Y> clone() { return new Pair<>(this.x(), this.y()); }

}
