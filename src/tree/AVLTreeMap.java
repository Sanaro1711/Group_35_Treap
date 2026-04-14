package tree;

import interfaces.Entry;
import interfaces.Position;

import java.util.Comparator;

/**
 * An implementation of a sorted map using an AVL tree.
 */

public class  AVLTreeMap<K, V> extends TreeMap<K, V> {

    /**
     * Constructs an empty map using the natural ordering of keys.
     */
    public AVLTreeMap() {
        super();
    }

    /**
     * Constructs an empty map using the given comparator to order keys.
     *
     * @param comp comparator defining the order of keys in the map
     */
    public AVLTreeMap(Comparator<K> comp) {
        super(comp);
    }

    /**
     * Returns the height of the given tree position.
     * External nodes (null entry) have height 0; internal nodes use cached height in {@code aux}.
     */
    protected int height(Position<Entry<K, V>> p) {
        if (p == null || p.getElement() == null) {
            return 0;
        }
        return tree.getAux(p);
    }

    /**
     * Recomputes the height of the given position based on its children's heights.
     * Call after children's {@code aux} values are up to date (e.g. while walking up from a leaf).
     */
    protected void recomputeHeight(Position<Entry<K, V>> p) {
        if (p == null) {
            return;
        }
        if (p.getElement() == null) {
            tree.setAux(p, 0);
        } else {
            tree.setAux(p, 1 + Math.max(height(left(p)), height(right(p))));
        }
    }

    /**
     * Returns whether a position has balance factor between -1 and 1 inclusive.
     */
    protected boolean isBalanced(Position<Entry<K, V>> p) {
        if (p == null || p.getElement() == null) {
            return true;
        }
        int diff = height(left(p)) - height(right(p));
        return diff >= -1 && diff <= 1;
    }

    /**
     * Returns a child of p with height no smaller than that of the other child.
     */
    protected Position<Entry<K, V>> tallerChild(Position<Entry<K, V>> p) {
        if (p == null || p.getElement() == null) {
            return null;
        }
        Position<Entry<K, V>> l = left(p);
        Position<Entry<K, V>> r = right(p);
        if (height(l) > height(r)) {
            return l;
        }
        if (height(r) > height(l)) {
            return r;
        }
        return l;
    }

    /**
     * Walks to the nearest ancestor that holds a map entry (internal node).
     */
    private Position<Entry<K, V>> ascendToInternal(Position<Entry<K, V>> p) {
        while (p != null && p.getElement() == null) {
            p = parent(p);
        }
        return p;
    }

    /**
     * Utility used to rebalance after an insert or removal operation. This
     * traverses the path upward from p to the root, performing a trinode
     * restructuring whenever a node is out of AVL balance.
     */
    protected void rebalance(Position<Entry<K, V>> p) {
        p = ascendToInternal(p);
        while (p != null) {
            recomputeHeight(left(p));
            recomputeHeight(right(p));
            if (!isBalanced(p)) {
                p = restructure(tallerChild(tallerChild(p)));
                recomputeHeight(left(p));
                recomputeHeight(right(p));
            }
            recomputeHeight(p);
            p = ascendToInternal(parent(p));
        }
    }

    /**
     * Overrides the TreeMap rebalancing hook that is called after an insertion.
     */
    @Override
    protected void rebalanceInsert(Position<Entry<K, V>> p) {
        rebalance(p);
    }

    /**
     * Overrides the TreeMap rebalancing hook that is called after a deletion.
     */
    @Override
    protected void rebalanceDelete(Position<Entry<K, V>> p) {
        rebalance(p);
    }

    /**
     * Ensure that current tree structure is valid AVL (for debug use only).
     */
    private boolean sanityCheck() {
        for (Position<Entry<K, V>> p : tree.positions()) {
            if (isInternal(p)) {
                if (p.getElement() == null)
                    System.out.println("VIOLATION: Internal node has null entry");
                else if (height(p) != 1 + Math.max(height(left(p)), height(right(p)))) {
                    System.out.println("VIOLATION: AVL unbalanced node with key " + p.getElement().getKey());
                    dump();
                    return false;
                }
            }
        }
        return true;
    }

    public String toBinaryTreeString() {
        BinaryTreePrinter<Entry<K, V>> btp = new BinaryTreePrinter<>(this.tree);
        return btp.print();
    }

    public static void main(String[] args) {
        AVLTreeMap<Integer, Integer> avl = new AVLTreeMap<>();

        Integer[] arr = new Integer[]{5, 3, 10, 2, 4, 7, 11, 1, 6, 9, 12, 8};

        for (Integer i : arr) {
            if (i != null) {
                avl.put(i, i);
            }
            System.out.println("root " + avl.root());
        }
        System.out.println(avl.toBinaryTreeString());

        avl.remove(5);
        System.out.println(avl.toBinaryTreeString());

    }
}
