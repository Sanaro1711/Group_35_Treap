package tree;

import interfaces.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Gatherer;

// import static jdk.javadoc.internal.doclets.toolkit.util.DocPath.parent;
//import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Concrete implementation of a binary tree using a node-based, linked
 * structure.
 */

public class LinkedBinaryTree<E> extends AbstractBinaryTree<E> {

    static java.util.Random rnd = new java.util.Random();
    /**
     * The root of the binary tree
     */
    protected Node<E> root = null; // root of the tree

    // LinkedBinaryTree instance variables
    /**
     * The number of nodes in the binary tree
     */
    protected int size = 0; // number of nodes in the tree

    /**
     * Constructs an empty binary tree.
     */
    public LinkedBinaryTree() {
    } // constructs an empty binary tree

    // constructor

    public static LinkedBinaryTree<Integer> makeRandom(int n) {
        LinkedBinaryTree<Integer> bt = new LinkedBinaryTree<>();
        bt.root = randomTree(null, 1, n);
        return bt;
    }

    // nonpublic utility

    public static <T extends Integer> Node<T> randomTree(Node<T> parent, Integer first, Integer last) {
        if (first > last) return null;
        else {
            Integer treeSize = last - first + 1;
            Integer leftCount = rnd.nextInt(treeSize);
            Integer rightCount = treeSize - leftCount - 1;
            Node<T> root = new Node<T>((T) ((Integer) (first + leftCount)), parent, null, null);
            root.setLeft(randomTree(root, first, first + leftCount - 1));
            root.setRight(randomTree(root, first + leftCount + 1, last));
            return root;
        }
    }

    // accessor methods (not already implemented in AbstractBinaryTree)

    public static void main(String [] args) {
        LinkedBinaryTree<String> bt = new LinkedBinaryTree<>();
        String[] arr = { "A", "B", "C", "D", "E", null, "F", null, null, "G", "H", null, null, null, null };
        // ArrayList<String> list = new ArrayList<>(Arrays.asList(arr));

        Integer [] inorder= {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        Integer [] preorder= {18, 2, 1, 14, 13, 12, 4, 3, 9, 6, 5, 8, 7, 10,
                11, 15, 16, 17, 28, 23, 19, 22, 20, 21, 24, 27, 26, 25, 29, 30};
        bt.construct(inorder, preorder);
        System.out.println(bt.toBinaryTreeString());

        Integer [] inorder2 = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        Integer [] preorder2 = {5, 1, 0, 4, 2, 3, 7, 6, 8};
        LinkedBinaryTree<Integer> bt2 = new LinkedBinaryTree<>();
        bt2.construct(inorder2, preorder2);

        System.out.println(bt2.toBinaryTreeString());

        System.out.println(bt2.rootToLeafPaths().toString());

        System.out.println(bt2.diameter(bt2.root, 0));

        int x = bt.averageHeight();
        System.out.println("Average height: " +  x);

        Node<String> p = bt.root.getLeft().getLeft();
        int d = bt.depth(p);
        System.out.println(d);
        System.out.println("\n");

        String[] arr2 = { "A", "B", "C", "D", "E","F", "G", "H"};
        ArrayList<String> list2 = new ArrayList<>(Arrays.asList(arr2));

        bt.createLevelOrder(list2);
        System.out.println(bt.toBinaryTreeString());

        Iterable<Position<String>> new_tree = bt.preorder();
        for (Position<String> element : new_tree) {
            System.out.println(element);
        }

        System.out.println("printing leaf nodes in left to right orde");
        bt.printLeafNodes(bt.root);


    }

    public int averageHeight() {
        int sum = 0;
        int total = 0;
        // generate the random binary trees
        for (int i = 50; i < 5000; i++) {
            for (int j = 0; j < 2; j++) { // generate 100 trees of size n
                total+=1;
                LinkedBinaryTree<Integer> bt = makeRandom(i);
                sum += height_recursive((Position<E>)bt.root);
            }
        }
        // System.out.println("Average height: " + sum / total);
        return sum / total;
    }
    public int getDepth(Node<E> p) {
        if (p == null) {
            return 0;
        }

        else {
            int l_depth = 1 + getDepth(p.left);
            int r_depth = 1 + getDepth(p.right);
            return Math.max(l_depth, r_depth);
        }
    }

    public void printLeafNodes(Node<E> p) {
        if (p == null) {
            return;
        }
        if (p.left == null && p.right == null) {
            System.out.println(p.getElement());
        }
        printLeafNodes(p.left);
        printLeafNodes(p.right);


    }
    public int diameter(Node<E> p, int max_d) {
        // the diameter is just the max depth on the left and max depth on the right
        // the depth is from node to lowest point
        if (p == null) {
            return max_d;
        }
        else {
            int curr_d = getDepth(p.left) + getDepth(p.right) + 1; // + 1 accounts for current node too
            max_d = Math.max(max_d, curr_d);
            int l_d = diameter(p.left, max_d);
            int r_d = diameter(p.right, max_d);

            return Math.max(max_d, Math.max(l_d, r_d));
        }
    }
    public void construct(Integer[] inorder, Integer[] preorder) {
        // index of root in in-order
        size = 0;
        root = constructHelper(inorder, preorder, 0, preorder.length, root);
    }

    public Node<E> constructHelper(Integer[] inorder, Integer[] preorder, int inStart, int inEnd, Node<E> p) {
        if (inStart > inEnd || size > preorder.length - 1) {
            return null;
        }
        // index of current element in preorder
        int idx = -1;
        for (int i = inStart; i <= inEnd; i++) {
            if (inorder[i].equals(preorder[size])) {
                idx = i;
                break;
            }
        }

        Node<E> new_node;
        new_node = createNode((E) preorder[size++], p, null, null);

        new_node.left = constructHelper(inorder, preorder, inStart, idx-1, new_node);
        new_node.right = constructHelper(inorder, preorder, idx+1, inEnd, new_node);
        return new_node;
    }

    public ArrayList<ArrayList<E>> rootToLeafPaths() {
        ArrayList<ArrayList<E>> snapshot = new ArrayList<>();
        rootToLeafPathHelper(snapshot, new ArrayList<E>(), root);
        return snapshot;
    }


    public void rootToLeafPathHelper(ArrayList<ArrayList<E>> snapshot, ArrayList<E> temp, Node<E> p) {
        if (p == null) {
            return;
        }
        temp.add(p.getElement());

        if (p.left == null && p.right == null ) {
            snapshot.add(new ArrayList<E>(temp));
        }
        else {
            rootToLeafPathHelper(snapshot, temp, p.left);
            rootToLeafPathHelper(snapshot, temp, p.right);
        }

        temp.removeLast();
        return;
    }

    /**
     * Factory function to create a new node storing element e.
     */
    protected Node<E> createNode(E e, Node<E> parent, Node<E> left, Node<E> right) {
        return new Node<E>(e, parent, left, right);
    }

    /**
     * Verifies that a Position belongs to the appropriate class, and is not one
     * that has been previously removed. Note that our current implementation does
     * not actually verify that the position belongs to this particular list
     * instance.
     *
     * @param p a Position (that should belong to this tree)
     * @return the underlying Node instance for the position
     * @throws IllegalArgumentException if an invalid position is detected
     */
    protected Node<E> validate(Position<E> p) throws IllegalArgumentException {
        if (!(p instanceof Node)) throw new IllegalArgumentException("Not valid position type");
        Node<E> node = (Node<E>) p; // safe cast
        if (node.getParent() == node) // our convention for defunct node
            throw new IllegalArgumentException("p is no longer in the tree");
        return node;
    }

    /**
     * Returns the number of nodes in the tree.
     *
     * @return number of nodes in the tree
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns the root Position of the tree (or null if tree is empty).
     *
     * @return root Position of the tree (or null if tree is empty)
     */
    @Override
    public Position<E> root() {
        return root;
    }

    // update methods supported by this class

    /**
     * Returns the Position of p's parent (or null if p is root).
     *
     * @param p A valid Position within the tree
     * @return Position of p's parent (or null if p is root)
     * @throws IllegalArgumentException if p is not a valid Position for this tree.
     */
    @Override
    public Position<E> parent(Position<E> p) throws IllegalArgumentException {
        return ((Node<E>) p).getParent();
    }

    /**
     * Returns the Position of p's left child (or null if no child exists).
     *
     * @param p A valid Position within the tree
     * @return the Position of the left child (or null if no child exists)
     * @throws IllegalArgumentException if p is not a valid Position for this tree
     */
    @Override
    public Position<E> left(Position<E> p) throws IllegalArgumentException {
        return ((Node<E>) p).getLeft();
    }

    /**
     * Returns the Position of p's right child (or null if no child exists).
     *
     * @param p A valid Position within the tree
     * @return the Position of the right child (or null if no child exists)
     * @throws IllegalArgumentException if p is not a valid Position for this tree
     */
    @Override
    public Position<E> right(Position<E> p) throws IllegalArgumentException {
        return ((Node<E>) p).getRight();
    }

    /**
     * Places element e at the root of an empty tree and returns its new Position.
     *
     * @param e the new element
     * @return the Position of the new element
     * @throws IllegalStateException if the tree is not empty
     */
    public Position<E> addRoot(E e) throws IllegalStateException {
        if (root != null) {
            throw new IllegalStateException("Root already exists");
        }

        root = createNode(e, null, null, null);
        size = 1;
        return root;
    }

    public void insert(E e) {
        addRecursive(root, e);
    }

    // recursively add Nodes to binary tree in proper position
    private Node<E> addRecursive(Node<E> p, E e) {
        if ((Integer) e < (Integer) p.getElement()) {
            if (p.getLeft() == null) {
                Node<E> child = createNode(e, p, null, null);
                p.setLeft(child);
                size++;
                return child;
            }
            else {
                return addRecursive(p.getLeft(), e);
            }
        }
        else {
            if (p.getRight() == null) {
                Node<E> child = createNode(e, p, null, null);
                p.setRight(child);
                size++;
                return child;
            } else {
                return addRecursive(p.getRight(), e);
            }
        }
    }

    /**
     * Creates a new left child of Position p storing element e and returns its
     * Position.
     *
     * @param p the Position to the left of which the new element is inserted
     * @param e the new element
     * @return the Position of the new element
     * @throws IllegalArgumentException if p is not a valid Position for this tree
     * @throws IllegalArgumentException if p already has a left child
     */
    public Position<E> addLeft(Position<E> p, E e) throws IllegalArgumentException {
        Node<E> parent = validate(p);
        if (parent.getLeft() != null) {
            throw new IllegalStateException("Left child already exists");
        }
        Node<E> child = createNode(e, parent, null, null);
        parent.setLeft(child);
        size++;

        return child;
    }

    /**
     * Creates a new right child of Position p storing element e and returns its
     * Position.
     *
     * @param p the Position to the right of which the new element is inserted
     * @param e the new element
     * @return the Position of the new element
     * @throws IllegalArgumentException if p is not a valid Position for this tree.
     * @throws IllegalArgumentException if p already has a right child
     */
    public Position<E> addRight(Position<E> p, E e) throws IllegalArgumentException {
        Node<E> parent = validate(p);

        if (parent.getRight() != null) {
            throw new IllegalStateException("Right child already exists");
        }

        Node<E> child = createNode(e, parent, null, null);
        parent.setRight(child);
        size++;
        return child;
    }

    /**
     * Replaces the element at Position p with element e and returns the replaced
     * element.
     *
     * @param p the relevant Position
     * @param e the new element
     * @return the replaced element
     * @throws IllegalArgumentException if p is not a valid Position for this tree.
     */
    public E set(Position<E> p, E e) throws IllegalArgumentException {
        Node<E> node = validate(p);
        E old_val = node.getElement();
        node.setElement(e);
        return old_val;
    }

    /**
     * Attaches trees t1 and t2, respectively, as the left and right subtree of the
     * leaf Position p. As a side effect, t1 and t2 are set to empty trees.
     *
     * @param p  a leaf of the tree
     * @param t1 an independent tree whose structure becomes the left child of p
     * @param t2 an independent tree whose structure becomes the right child of p
     * @throws IllegalArgumentException if p is not a valid Position for this tree
     * @throws IllegalArgumentException if p is not a leaf
     */
    public void attach(Position<E> p, LinkedBinaryTree<E> t1, LinkedBinaryTree<E> t2) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("P must be a node");
        }

        Node<E> node_p = (Node<E>) p;
        Node<E> root_t1 = (Node<E>) t1.root();
        Node<E> root_t2 = (Node<E>) t2.root();

        if ((Integer) root_t1.getElement() < (Integer) node_p.getElement()) {
            node_p.setLeft(root_t1);
        }
        if ((Integer) root_t2.getElement() > (Integer) node_p.getElement()) {
            node_p.setRight(root_t2);
        }
    }

    /**
     * Removes the node at Position p and replaces it with its child, if any.
     *
     * @param p the relevant Position
     * @return element that was removed
     * @throws IllegalArgumentException if p is not a valid Position for this tree.
     * @throws IllegalArgumentException if p has two children.
     */
    public E remove(Position<E> p) throws IllegalArgumentException {
        int numOfChildren = numChildren(p);
        Node<E> node = (Node<E>) p;
        Node<E> parent = node.getParent();
        Node<E> child;

        if (node.getRight() != null) {
            child = node.getRight();
        }
        else {
            child = node.getLeft();
        }

        E old_node = p.getElement();
        if (parent != null) {
            if (parent.getLeft() == node){
                parent.setLeft(child);
            }
            else {
                parent.setRight(child);
            }
        }
        else { // parent is root
            root = child;
        }

        if (child != null) {
            child.setParent(parent);
        }

        size--;
        return old_node;
    }

    public String toString() {
        return positions().toString();
    }

    public void createLevelOrder(ArrayList<E> l) {
        root = createLevelOrderHelper(l, root, 0);
    }

    private Node<E> createLevelOrderHelper(java.util.ArrayList<E> l, Node<E> p, int i) {
        if (i < l.size()) {
            Node<E> n = createNode(l.get(i), p, null, null);
            n.left = createLevelOrderHelper(l, n, 2*i + 1);
            n.right = createLevelOrderHelper(l, n, 2*i + 2);
            size++;
            return n;
        }
        return null;
    }


    public String toBinaryTreeString() {
        BinaryTreePrinter<E> btp = new BinaryTreePrinter<>(this);
        return btp.print();
    }

    /**
     * Nested static class for a binary tree node.
     */
    public static class Node<E> implements Position<E> {
        private E element;
        private Node<E> left, right, parent;

        public Node(E e, Node<E> p, Node<E> l, Node<E> r) {
            element = e;
            left = l;
            right = r;
            parent = p;
        }

        // accessor
        public E getElement() {
            return element;
        }

        // modifiers
        public void setElement(E e) {
            element = e;
        }

        public Node<E> getLeft() {
            return left;
        }

        public void setLeft(Node<E> n) {
            left = n;
        }

        public Node<E> getRight() {
            return right;
        }

        public void setRight(Node<E> n) {
            right = n;
        }

        public Node<E> getParent() {
            return parent;
        }

        public void setParent(Node<E> n) {
            parent = n;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (element == null) {
                sb.append("\u29B0");
            } else {
                sb.append(element);
            }
            return sb.toString();
        }
    }
}
