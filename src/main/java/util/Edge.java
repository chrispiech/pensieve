package util;

public class Edge<T> {
	T source;
	T target;
	double weight;

	public Edge(T a, T b, double weight) {
		source = a;
		target = b;
		this.weight = weight;
	}

	public T getSource() {
		return source;
	}

	public T getTarget() {
		return target;
	}
	
	public double getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return source + " -> " + target + " weight = " + weight;
	}
	
	@Override
	public int hashCode() {
		return source.hashCode() + 31 * target.hashCode();
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge<?> other = (Edge<?>) o;
        return other.source.equals(source) && other.target.equals(target);
    }
}
