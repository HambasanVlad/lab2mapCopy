package model.adt;

import exception.MyException;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque; // Use this for thread safety

public class MyStack<T> implements MyIStack<T> {
    // ConcurrentLinkedDeque is thread-safe and its iterator will not throw ConcurrentModificationException
    private Deque<T> stack;

    public MyStack() {
        this.stack = new ConcurrentLinkedDeque<>();
    }

    @Override
    public T pop() throws MyException {
        if (stack.isEmpty()) {
            throw new MyException("Stack is empty. Cannot pop.");
        }
        return stack.pop();
    }

    @Override
    public void push(T v) {
        stack.push(v);
    }

    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public T peek() throws MyException {
        if (stack.isEmpty()) {
            throw new MyException("Stack is empty. Cannot peek.");
        }
        return stack.peek();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Safe iteration with ConcurrentLinkedDeque
        for (T elem : stack) {
            sb.append(elem.toString()).append("\n");
        }
        return sb.toString();
    }
}