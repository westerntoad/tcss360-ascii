import java.util.Random;

@SuppressWarnings("unchecked")
public class MyCuckooTable<K, V> {
    private final int DEFAULT_TABLE_SIZE = 512;

    private final int DEFAULT_LOOPS = 27;

    private final SipHash sh = new SipHash();

    private final Random rand = new Random();

    private Node<K, V>[] t0;

    private Node<K, V>[] t1;

    private int maxLoop;

    private int tableSize;

    private int mySize;
    private int hash1;
    private int hash2;


    public MyCuckooTable(int n) {
        tableSize = DEFAULT_TABLE_SIZE;
        maxLoop = DEFAULT_LOOPS;
        t0 = (Node<K, V>[]) new Node[tableSize];
        t1 = (Node<K, V>[]) new Node[tableSize];
        mySize = 0;
        rand.setSeed(rand.nextInt(tableSize));
        hash1 = rand.nextInt(tableSize);
        rand.setSeed(rand.nextInt(tableSize));
        hash2 = rand.nextInt(tableSize);
    }

    public V put(K searchKey, V newValue) {
        if (this.containsKey(searchKey)) {
            return null;
        }
        Node<K, V> currentNode = new Node<>(searchKey, newValue);
        Node<K, V> tempNode;
        int index;
        int counter = 0;
        while (counter < maxLoop) {
            index = hash(currentNode.key, 1);
            if (slotsIn(currentNode, index, t0)) return newValue;
            tempNode = t0[index];
            t0[index] = currentNode;
            index = hash(tempNode.key, 2);
            if (slotsIn(tempNode, index, t1)) return newValue;
            currentNode = t1[index];
            t1[index] = tempNode;
            counter++;

        }
        rehash();
        put(currentNode.key, currentNode.value);

        return newValue;
    }


    public void rehash() {
        tableSize *= 2;
        maxLoop = 3 * (int) (Math.log(tableSize) / Math.log(2));
        Node<K, V>[][] oldTable = new Node[][]{t0, t1};
        t0 = (Node<K, V>[]) new Node[tableSize];
        t1 = (Node<K, V>[]) new Node[tableSize];
        rand.setSeed(rand.nextInt(tableSize));
        hash1 = rand.nextInt(tableSize);
        rand.setSeed(rand.nextInt(tableSize));
        hash2 = rand.nextInt(tableSize);
        for (Node<K, V>[] table : oldTable) {
            for (Node<K, V> node : table) {
                if (node != null) {
                    put(node.key, node.value);
                    }
                }
            }

    }

    private boolean slotsIn(Node<K, V> node, int index, Node<K, V>[] table) {
        if (table[index] != null) {
            return false;
        }
        table[index] = node;
        mySize++;
        return true;

    }

    public V get(K searchKey) {
        int index = hash(searchKey, 1);
        if (t0[index] != null) {
            if (t0[index].key.equals(searchKey)) return t0[index].value;
        }
        index = hash(searchKey, 2);
        if (t1[index] != null) {
            if (t1[index].key.equals(searchKey)) return t1[index].value;
        }
        return null;
    }

    public boolean containsKey(K searchKey) {
        int index = hash(searchKey, 1);
        if (t0[index] != null) {
            return t0[index].key.equals(searchKey);
        }
        index = hash(searchKey, 2);
        if (t1[index] != null) {
            return t1[index].key.equals(searchKey);
        }
        return false;
    }

    private int hash(K key, int fno) {
        return (int) ((fno == 1) ? Math.abs(sh.hash(String.valueOf(hash1) + key) % (tableSize - 1))
                        : Math.abs(sh.hash(String.valueOf(hash2) + key) % (tableSize - 1)));
    }


    private static class Node<K, V> {
        K key;
        V value;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

}
