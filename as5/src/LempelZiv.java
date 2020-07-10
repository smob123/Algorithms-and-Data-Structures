import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * A new instance of LempelZiv is created for every run.
 */
public class LempelZiv {

    /**
     * Take uncompressed input as a text string, compress it, and return it as a
     * text string.
     */
    public String compress(String input) {
        StringBuilder compressed = new StringBuilder();
        int cursor = 0;
        int window = 500;

        while (cursor < input.length()) {
            int length = 0;
            int previousMatch = 0;
            while (true) {
                char c = input.charAt(cursor + length);
                String sequence = getSequence(input, cursor, cursor + length);
                int startIndex = cursor < window ? 0 : cursor - window;
                int sequenceIndex = findSequence(input, sequence, startIndex, cursor);
                boolean match = sequenceIndex > -1;

                if (match && (cursor + 1 + length) < input.length()) {
                    previousMatch = sequenceIndex;
                    length++;
                } else {
                    compressed.append("[").append(previousMatch).append(",").append(length).append(",")
                            .append(c).append("]");

                    cursor = cursor + length + 1;
                    break;
                }
            }
        }

        return compressed.toString();
    }

    private int findSequence(String text, String sequence, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex - sequence.length(); i++) {
            String substr = text.substring(i, i + sequence.length());
            if (substr.equals(sequence)) {
                return i;
            }
        }
        return -1;
    }

    private String getSequence(String text, int startIndex, int endIndex) {
        return text.substring(startIndex, endIndex + 1);
    }

    /**
     * Take compressed input as a text string, decompress it, and return it as a
     * text string.
     */
    public String decompress(String compressed) {
        StringBuilder decompressed = new StringBuilder();
        Scanner scan = new Scanner(compressed);
        scan.useDelimiter("");

        while (scan.hasNext()) {
            scan.next("\\[");
            int previousMatch = parseNum(scan);
            scan.next(",");
            int length = parseNum(scan);
            scan.next(",");
            char nextChar = scan.next().charAt(0);
            scan.next("]");
            if (previousMatch == 0 && length == 0) {
                decompressed.append(nextChar);
            } else {
                String previousString = decompressed.substring(previousMatch, previousMatch + length);
                decompressed.append(previousString).append(nextChar);
            }
        }

        return decompressed.toString();
    }

    private int parseNum(Scanner scan) {
        StringBuilder numSting = new StringBuilder();
        Pattern NUMPAT = Pattern.compile("\\d");

        while (scan.hasNext(NUMPAT)) {
            numSting.append(scan.next());
        }

        return Integer.parseInt(numSting.toString());
    }

    /**
     * The getInformation method is here for your convenience, you don't need to
     * fill it in if you don't want to. It is called on every run and its return
     * value is displayed on-screen. You can use this to print out any relevant
     * information from your compression.
     */
    public String getInformation() {
        return "";
    }
}
