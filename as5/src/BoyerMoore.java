import java.util.Arrays;

public class BoyerMoore {
    public int search(String text, String pattern) {
        int i = pattern.length();
        int j = pattern.length();
        int[] goodSuffix = goodSuffix(pattern, text);
        int[] badChars = badCharacter(pattern, text);

        while (i < text.length()) {
            while (pattern.charAt(j - 1) == text.charAt(i - 1)) {
                i--;
                j--;

                if (j == 0) {
                    return i;
                }
            }

            int b = badChars[i];
            int g = goodSuffix[i];
            int jump = Math.min(b, g);
            i += jump;
            j = jump;
            if (j == 0) {
                return i;
            }
            //System.out.println(b + " : " + g);
        }

        return -1;
    }

    /**
     * good suffix rule
     */
    private int[] goodSuffix(String pattern, String text) {
        int i = text.length() - 1;
        int j = pattern.length() - 1;
        int[] table = new int[text.length()];
        Arrays.fill(table, pattern.length());

        while (i >= 0) {
            if (j < 0) {
                break;
            } else if (pattern.charAt(j) != text.charAt(i)) {
                if (i == text.length() - 1) {
                    i--;
                    continue;
                }

                // decide how much to skip by
                int suffix = suffix(pattern, text.charAt(i + 1), j);

                // reset j
                if (suffix < 0) {
                    j = pattern.length();
                } else {
                    j = suffix;
                    i++;
                    System.out.println(pattern.charAt(j) + " : " + text.charAt(i));
                }
            }

            i--;
            j--;
            table[i] = j;
        }

        return table;
    }

    /**
     * returns a positive integer if the good suffix rule can be applied to the pattern, otherwise it will return -1
     */
    private int suffix(String pattern, char lastMatchedChar, int j) {
        for (int i = j - 1; i >= 0; i--) {
            if (pattern.charAt(i) == lastMatchedChar) {
                return i;
            }
        }

        return -1;
    }

    /**
     * bad character rule.
     */
    private int[] badCharacter(String pattern, String text) {
        int i = text.length() - 1;
        int j = pattern.length() - 1;
        int[] table = new int[text.length()];
        // fill the skip table with the length of the pattern initially
        Arrays.fill(table, pattern.length());

        while (i >= 0) {
            if (j == 0) {
                break;
            } else if (pattern.charAt(j) != text.charAt(i)) {
                // decide how much to skip by
                int containsChar = containsChar(pattern, text.charAt(i));
                table[i] = containsChar > -1 ? containsChar : pattern.length();
                // reset j
                j = pattern.length() - 1;
            }

            i--;
            j--;
        }

        return table;
    }

    /**
     * checks if the pattern contains a character.
     */
    private int containsChar(String pattern, char c) {
        for (int i = pattern.length() - 1; i >= 0; i--) {
            if (pattern.charAt(i) == c) {
                return i;
            }
        }

        return -1;
    }
}
