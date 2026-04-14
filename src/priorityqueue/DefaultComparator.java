package priorityqueue;

import java.util.Comparator;

/**
 * Comparator based on the compareTo method of a Comparable element type.
 */
public class DefaultComparator<E> implements Comparator<E> {

    @SuppressWarnings("unchecked")
    @Override
    public int compare(E a, E b) throws ClassCastException {
        return ((Comparable<E>) a).compareTo(b);
    }
}
