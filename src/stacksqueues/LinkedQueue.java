package stacksqueues;

import interfaces.Queue;

/**
 * Queue implementation using a singly linked list of nodes.
 */
public class LinkedQueue<E> implements Queue<E> {

    private static class Node<E> {
        private final E element;
        private Node<E> next;

        Node(E e, Node<E> n) {
            element = e;
            next = n;
        }
    }

    private Node<E> head;
    private Node<E> tail;
    private int size;

    public LinkedQueue() {
        head = null;
        tail = null;
        size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void enqueue(E e) {
        Node<E> newest = new Node<>(e, null);
        if (isEmpty()) {
            head = newest;
        } else {
            tail.next = newest;
        }
        tail = newest;
        size++;
    }

    @Override
    public E first() {
        if (isEmpty()) {
            return null;
        }
        return head.element;
    }

    @Override
    public E dequeue() {
        if (isEmpty()) {
            return null;
        }
        E answer = head.element;
        head = head.next;
        size--;
        if (isEmpty()) {
            tail = null;
        }
        return answer;
    }
}
