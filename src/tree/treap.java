package tree;

import interfaces.Entry;
import interfaces.Position;

import java.util.Comparator;
import java.util.Random;

/**
 * A Treap (tree + heap): BST ordered by key, heap ordered by random priority.
 *
 * Uses the underlying {@link TreeMap} search-tree structure and stores a node's
 * priority in the inherited {@code aux} field (see {@link TreeMap.BalanceableBinaryTree}).
 *
 * Heap property used here: parent priority <= child priority (min-heap on priority).
 */
public class treap<K, V> extends TreeMap<K, V> {

    private final Random rnd = new Random();

    public treap() {
        super();
    }

    public treap(Comparator<K> comp) {
        super(comp);
    }

    public treap(long randomSeed) {
        super();
        rnd.setSeed(randomSeed);
    }

    public String toBinaryTreeString() {
        BinaryTreePrinter<Entry<K, V>> btp = new BinaryTreePrinter<>(this.tree,
            p -> {
                if (p.getElement() == null) {
                    return "\u29B0";
                }
                K key = p.getElement().getKey();
                int pr = tree.getAux(p);
                return key + "(" + pr + ")";
            }
        );
        return btp.print();
    }

    // ----- priority helpers -----

    private int priority(Position<Entry<K, V>> p) {
        // external nodes (null entry) never participate in heap comparisons
        if (p == null || p.getElement() == null) {
            return Integer.MAX_VALUE;
        }
        return tree.getAux(p);
    }

    private void assignPriorityIfNeeded(Position<Entry<K, V>> p) {
        // For a brand new internal node, aux will be 0 by default; that's a valid random
        // value but leads to non-random structure. We assign a fresh random priority
        // at insertion time.
        if (p != null && p.getElement() != null) {
            tree.setAux(p, rnd.nextInt());
        }
    }

    // ----- search (TreeMap.treeSearch is private) -----

    private Position<Entry<K, V>> treeSearch(Position<Entry<K, V>> p, K key) {
        if (p == null || p.getElement() == null) {
            return p;
        }
        int c = compare(key, p.getElement());
        if (c == 0) {
            return p;
        } else if (c < 0) {
            return treeSearch(left(p), key);
        } else {
            return treeSearch(right(p), key);
        }
    }

    // ----- rebalancing hooks -----

    @Override
    protected void rebalanceInsert(Position<Entry<K, V>> p) {
        assignPriorityIfNeeded(p);

        // bubble up while heap property is violated
        while (!isRoot(p)) {
            Position<Entry<K, V>> par = parent(p);
            if (priority(p) < priority(par)) {
                rotate(p);
            } else {
                break;
            }
        }
    }

    @Override
    protected void rebalanceDelete(Position<Entry<K, V>> p) {
        // Treap deletion is handled explicitly in remove(key) by rotating the target down.
        // Nothing to do here.
    }

    // ----- removal -----

    @Override
    public V remove(K key) throws IllegalArgumentException {
        checkKey(key);
        Position<Entry<K, V>> p = treeSearch(root(), key);
        if (p == null || p.getElement() == null) {
            return null;
        }

        // Rotate the target node down until it becomes a (BST) leaf (both children external).
        // This guarantees that splicing it out cannot violate the heap-order with its parent.
        while (left(p).getElement() != null || right(p).getElement() != null) {
            Position<Entry<K, V>> l = left(p);
            Position<Entry<K, V>> r = right(p);

            boolean lInternal = l.getElement() != null;
            boolean rInternal = r.getElement() != null;

            if (lInternal && rInternal) {
                // rotate up the child with smaller priority (min-heap)
                if (priority(l) <= priority(r)) {
                    rotate(l);
                } else {
                    rotate(r);
                }
            } else if (lInternal) {
                rotate(l);
            } else if (rInternal) {
                rotate(r);
            } else {
                break; // both external
            }
        }

        // Delegate the actual splice (and node-count maintenance) to TreeMap's remove,
        // which runs inside the correct package to access the underlying tree internals.
        return super.remove(key);
    }

    public static void main(String[] args) {
        treap<Integer, String> map = new treap<>(42L);

        System.out.println("--- after puts ---");
        int[] insertOrder = {50, 30, 70, 20, 40, 60, 80, 10, 35, 65};
        for (int k : insertOrder) {
            map.put(k, "v" + k);
            System.out.println("put(" + k + ", \"v" + k + "\")");
            System.out.println(map.toBinaryTreeString());
        }

        System.out.println("--- gets ---");
        System.out.println("get(40) = " + map.get(40));
        System.out.println("get(99) = " + map.get(99));

        System.out.println("--- after removes ---");
        int[] removeKeys = {20, 50, 30};
        for (int k : removeKeys) {
            System.out.println("remove(" + k + ") -> " + map.remove(k));
            System.out.println(map.toBinaryTreeString());
        }

        System.out.println("final size = " + map.size());
    }
}
