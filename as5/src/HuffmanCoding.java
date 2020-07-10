import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * A new instance of HuffmanCoding is created for every run. The constructor is
 * passed the full text to be encoded or decoded, so this is a good place to
 * construct the tree. You should store this tree in a field and then use it in
 * the encode and decode methods.
 */
public class HuffmanCoding {
    PriorityQueue<Node> queue = new PriorityQueue<>();
    HashMap<String, String> dictionary = new HashMap<>();

    /**
     * This would be a good place to compute and store the tree.
     */
    public HuffmanCoding(String text) {
        computeTree(text);
    }

    private void computeTree(String text) {
        long start = System.currentTimeMillis();
        HashMap<String, Node> symbols = new HashMap<>();

        for (int i = 0; i < text.length(); i++) {
            String ch = "" + text.charAt(i);
            if (symbols.get(ch) == null) {
                symbols.put(ch, new Node(ch));
            } else {
                Node n = symbols.get(ch);
                n.frequency = n.frequency + 1;
                symbols.put(ch, n);
            }
        }

        queue.addAll(symbols.values());
        buildTree();
        long end = System.currentTimeMillis();
        System.out.println("built tree in: " + (end - start) + "ms");
    }

    private void buildTree() {
        while (queue.size() > 1) {
            Node n1 = queue.poll();
            Node n2 = queue.poll();
            Node parent = new Node();
            parent.frequency = n1.frequency + n2.frequency;
            parent.leftChild = n1;
            parent.rightChild = n2;
            queue.add(parent);
        }
    }

    /**
     * Take an input string, text, and encode it with the stored tree. Should
     * return the encoded text as a binary string, that is, a string containing
     * only 1 and 0.
     */
    public String encode(String text) {
        long start = System.currentTimeMillis();
        StringBuilder encoding = new StringBuilder();
        char s = 0;

        for (int i = 0; i < text.length(); i++) {
            if (dictionary.get("" + text.charAt(i)) == null) {
                encoding.append(getBinaryCode(queue.peek(), "" + text.charAt(i), ""));
            } else {
                encoding.append(dictionary.get("" + text.charAt(i)));
            }
            s = text.charAt(i);
        }

        long end = System.currentTimeMillis();
        System.out.println("encoded text in: " + (end - start) + "ms");

        return encoding.toString();
    }

    private String getBinaryCode(Node current, String symbol, String code) {
        if (current.leftChild == null && current.rightChild == null) {
            if (current.symbol.equals(symbol)) {
                dictionary.put(symbol, code);
                return code;
            } else {
                return "";
            }
        }

        String left = getBinaryCode(current.leftChild, symbol, code + "0");
        if (!left.equals("")) {
            return left;
        }

        String right = getBinaryCode(current.rightChild, symbol, code + "1");
        if (!right.equals("")) {
            return right;
        }

        return "";
    }

    /**
     * Take encoded input as a binary string, decode it using the stored tree,
     * and return the decoded text as a text string.
     */
    public String decode(String encoded) {
        long start = System.currentTimeMillis();
        StringBuilder decoded = new StringBuilder();
        Node current = queue.peek();
        Scanner scan = new Scanner(encoded);
        scan.useDelimiter("");

        while (scan.hasNext()) {
            String currentChar = scan.next();
            if (currentChar.equals("0")) {
                current = current.leftChild;
            } else {
                current = current.rightChild;
            }

            if (current.symbol != null) {
                decoded.append(current.symbol);
                current = queue.peek();
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("decoded text in: " + (end - start) + "ms");

        return decoded.toString();
    }

    /**
     * The getInformation method is here for your convenience, you don't need to
     * fill it in if you don't wan to. It is called on every run and its return
     * value is displayed on-screen. You could use this, for example, to print
     * out the encoding tree.
     */
    public String getInformation() {
        return "";
    }

    /**
     * prints the tree
     */
    void getTree(Node n) {
        if (n.leftChild == null && n.rightChild == null) {
            System.out.println("symbol: " + n.symbol + ", frequency: " + n.frequency);
            return;
        }

        getTree(n.leftChild);
        getTree(n.rightChild);
    }

    private class Node implements Comparable<Node> {
        String symbol;
        int frequency = 1;
        Node leftChild;
        Node rightChild;

        public Node() {
        }

        public Node(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public int compareTo(Node o) {
            return this.frequency - o.frequency;
        }
    }
}
