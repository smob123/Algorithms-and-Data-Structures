import java.time.Duration;
import java.time.Instant;

/**
 * A new KMP instance is created for every substring search performed. Both the
 * pattern and the text are passed to the constructor and the search method. You
 * could, for example, use the constructor to create the match table and the
 * search method to perform the search itself.
 */
public class KMP {
    int[] jumpTable;

    public KMP(String pattern, String text) {
        initTable(pattern);
    }

    private void initTable(String pattern) {
        jumpTable = new int[pattern.length()];
        jumpTable[0] = -1;
        if(pattern.length() == 1) {
            return;
        }

        jumpTable[1] = 0;
        int prefix = 0;
        int pos = 2;

        while (pos < pattern.length()) {
            if (pattern.charAt(pos - 1) == pattern.charAt(prefix)) {
                jumpTable[pos] = prefix + 1;
                pos++;
                prefix++;
            } else if (prefix > 0) {
                prefix = jumpTable[prefix];
            } else {
                jumpTable[pos] = 0;
                pos++;
            }
        }
    }

    public int bruteForceSearch(String pattern, String text) {
        Instant startTime = Instant.now();
        int txtLen = text.length();
        int patLen = pattern.length();

        for (int i = 0; i < txtLen - patLen; i++) {
            boolean found = true;

            for (int j = 0; j < patLen; j++) {
                if (text.charAt(j + i) != pattern.charAt(j)) {
                    found = false;
                    break;
                }
            }

            if (found) {
                Instant endTime = Instant.now();
                System.out.println("brute force execution time: " + Duration.between(startTime, endTime).toMillis() + "ms");
                return i;
            }
        }

        Instant endTime = Instant.now();
        System.out.println("brute force execution time: " + Duration.between(startTime, endTime).toMillis() + "ms");
        return -1;
    }

    /**
     * Perform KMP substring search on the given text with the given pattern.
     * <p>
     * This should return the starting index of the first substring match if it
     * exists, or -1 if it doesn't.
     */
    public int search(String pattern, String text) {
        Instant startTime = Instant.now();
        int currentMatch = 0;
        int pos = 0;

        while ((pos + currentMatch) < text.length()) {
            if (pattern.charAt(pos) == text.charAt(currentMatch + pos)) {
                pos++;
                if (pos == pattern.length()) {
                    Instant endTime = Instant.now();
                    System.out.println("KMP execution time: " + Duration.between(startTime, endTime).toMillis() + "ms");
                    return currentMatch;
                }
            } else if (jumpTable[pos] == -1) {
                currentMatch = currentMatch + pos + 1;
                pos = 0;
            } else {
                currentMatch = currentMatch + pos - jumpTable[pos];
                pos = jumpTable[pos];
            }
        }

        Instant endTime = Instant.now();
        System.out.println("KMP execution time: " + Duration.between(startTime, endTime).toMillis() + "ms");
        return -1;
    }
}
