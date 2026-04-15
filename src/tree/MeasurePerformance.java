package tree;

import interfaces.Entry;
import interfaces.Position;

import java.util.ArrayList;
import java.util.List;

import static tree.SortingAlgorithms.printArray;

public class MeasurePerformance  {
    public MeasurePerformance() {
    }

    public Iterable<Position<Entry<Integer, Integer>>> inorder(AVLTreeMap<Integer, Integer> tree) {
        List<Position<Entry<Integer, Integer>>> snapshot = new ArrayList<>();
        if (tree.size() != 0) {
            inorderSubtree(tree.root(), snapshot, tree); // fill the snapshot recursively
        }
        return snapshot;
    }

    private void inorderSubtree(Position<Entry<Integer, Integer>> p, List<Position<Entry<Integer, Integer>>> snapshot, AVLTreeMap<Integer, Integer> tree) {
        if (p.getElement() == null) {
            return ;
        }
        inorderSubtree(tree.left(p), snapshot, tree);
        snapshot.add(p);
        inorderSubtree(tree.right(p), snapshot, tree);
    }

    public static void main(String[] args) {
        AVLTreeMap<Integer, Integer> avl = new AVLTreeMap<>();

        Integer[] arr = new Integer[]{5, 3, 10, 2, 4, 7, 11, 1, 6, 9, 12, 8};

        for (Integer i : arr) {
            if (i != null) {
                avl.put(i, i);
            }
        }

        System.out.println(avl.toBinaryTreeString());

        // inorder traversal
        MeasurePerformance m = new MeasurePerformance();
        Iterable<Position<Entry<Integer, Integer>>> snapshot = m.inorder(avl);
        for (Position<Entry<Integer, Integer>> element : snapshot) {
            System.out.println(element);
        }

        // to do bubble sort (or any other algo - inside of SortingAlgorithms)
        SortingAlgorithms sa = new SortingAlgorithms();
        sa.bubble_sort(arr);
        printArray(arr);

    }

}
