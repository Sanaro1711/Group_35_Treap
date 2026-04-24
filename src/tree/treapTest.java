package tree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TreapTest {
    @Test
    void testPut() {
        treap<Integer, String> t = new treap<>();
        t.put(1, "one");
        t.put(2, "two");
        t.put(3, "three");
        t.put(4, "four");


        assertEquals(4, t.size()); // ensures elements have been drawn
        System.out.println(t.toBinaryTreeString()); // santiy test too!!
    }
    @Test
    void testLargePut() {
        treap<Integer, String> t = new treap<>();

        int n = 20;

        for (int i = 1; i <= n; i++) {
            t.put(i, "v" + i);
        }

        assertEquals(n, t.size());
        System.out.println(t.toBinaryTreeString()); // can also see that it looks pretty balanced!
    }
    @Test
    void testSize() {
        treap<Integer, String> t = new treap<>();
        assertEquals(0, t.size());

        t.put(1, "one");
        t.put(2, "two");

        assertEquals(2, t.size());
    }

    @Test
    void testGetPut() {
        treap<Integer, String> t = new treap<>();

        t.put(10, "ten");
        t.put(20, "twenty");

        assertEquals("ten", t.get(10));
        assertEquals("twenty", t.get(20));
        assertNull(t.get(99));
    }

    @Test
    void testRemove() {
        treap<Integer, String> t = new treap<>();

        t.put(5, "five");
        t.put(3, "three");
        t.put(8, "eight");

        assertEquals("three", t.remove(3));
        assertNull(t.get(3));

        assertEquals(2, t.size());
    }

    @Test
    void testRootExists() {
        treap<Integer, String> t = new treap<>();

        t.put(50, "50");
        t.put(30, "30");
        t.put(70, "70");

        assertNotNull(t.root());
        assertNotNull(t.root().getElement());
    }

    @Test
    void testInOrderSortProperty() {
        treap<Integer, String> t = new treap<>();

        int[] values = {5, 1, 9, 3, 7};

        for (int v : values) {
            t.put(v, String.valueOf(v));
        }

        int prev = Integer.MIN_VALUE;

        for (var e : t.entrySet()) {
            int key = e.getKey();
            assertTrue(key >= prev);
            prev = key;
        }
    }
}