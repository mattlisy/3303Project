import java.util.*;

public class UniqueStack<E> {
    private Deque<E> stack;
    private Set<E> set;

    public UniqueStack() {
        stack = new ArrayDeque<>();
        set = new HashSet<>();
    }

    public void push(E element) {
        if (!set.contains(element)) {
            stack.push(element);
            set.add(element);
        }
    }
    public E pop() {
        E element = stack.pop();
        set.remove(element);
        return element;
    }

    public E peek() {
        return stack.peek();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }
     @Override
    public String toString() {
        return stack.toString();
    }
}

