import java.util.*;
import java.util.regex.Pattern;

/**
 * Ngrams predictive probabilities for text
 */
public class Ngrams {
    /**
     * The constructor would be a good place to compute and store the Ngrams probabilities.
     * Take uncompressed input as a text string, and store a List of Maps. The n-th such
     * Map has keys that are prefixes of length n. Each value is itself a Map, from
     * characters to floats (this is the probability of the char, given the prefix).
     */
    HashMap<Integer, Map<String, Map<Character, Float>>> ngram;  /* nb. suggestion only - you don't have to use
                                                     this particular data structure */
    HashMap<Integer, Map<String, Map<Character, Integer>>> counts;

    public Ngrams(String input) {
        ngram = new HashMap<>();
        counts = new HashMap<>();
        for (int i = 0; i <= 5; i++) {
            ngram.put(i, new HashMap<>());
            counts.put(i, new HashMap<>());
        }
        counts.get(0).put("", new HashMap<>());

        buildTables(input);
    }

    private void buildTables(String input) {
        System.out.println("building tables...");

        for (int i = 0; i < input.length(); i++) {
            Map<String, Map<Character, Integer>> zeroTable = counts.get(0);
            Map<Character, Integer> zeroSequenceTable = zeroTable.get("");
            char character = input.charAt(i);
            int zeroCount = 0;
            if (zeroSequenceTable.get(character) != null) {
                zeroCount = zeroSequenceTable.get(character);
            }
            zeroSequenceTable.put(character, ++zeroCount);
            zeroTable.put("", zeroSequenceTable);
            counts.put(0, zeroTable);

            for (int j = 1; j <= 5; j++) {
                if (i - j < 0) {
                    break;
                }

                String prefix = input.substring(i - j, i);
                Map<String, Map<Character, Integer>> table = counts.get(j);
                Map<Character, Integer> sequenceTable = table.get(prefix);
                int count = 0;

                if (sequenceTable != null) {
                    if (sequenceTable.get(character) != null) {
                        count = sequenceTable.get(character);
                    }
                } else {
                    sequenceTable = new HashMap<>();
                }

                count++;
                sequenceTable.put(character, count);
                table.put(prefix, sequenceTable);
                counts.put(j, table);
            }
        }

        calculateFrequencies();
    }

    private void calculateFrequencies() {
        System.out.println("calculating frequencies...");
        for (int key : counts.keySet()) {
            Map<String, Map<Character, Integer>> table = counts.get(key);
            for (String sequence : table.keySet()) {
                Map<Character, Integer> sequenceCounts = table.get(sequence);
                int sum = sequenceCounts.values().stream().mapToInt(Integer::intValue).sum();
                Map<String, Map<Character, Float>> probSequenceTable = ngram.get(key);
                Map<Character, Float> probTable = new HashMap<>();
                for (char c : sequenceCounts.keySet()) {
                    int count = sequenceCounts.get(c);
                    probTable.put(c, ((float) count / (float) sum));
                    probSequenceTable.put(sequence, probTable);
                }
                ngram.put(key, probSequenceTable);
            }
        }
    }

    /**
     * Take a string, and look up the probability of each character in it, under the Ngrams model.
     * Returns a List of Floats (which are the probabilities).
     */
    public List<Float> findCharProbs(String mystring) {
        ArrayList<Float> probs = new ArrayList<>();

        for (int i = 0; i < mystring.length(); i++) {
            float highestProb = (float) Double.POSITIVE_INFINITY;
            int n = 5;

            while (highestProb == (float) Double.POSITIVE_INFINITY) {
                if (n < 0) {
                    highestProb = 0f;
                    break;
                }

                Map<String, Map<Character, Float>> sequenceTable = ngram.get(n);
                if (i - n < 0) {
                    n--;
                    continue;
                }

                String sequence = mystring.substring(i - n, i);
                if (sequenceTable.get(sequence) == null) {
                    n--;
                    continue;
                }

                Map<Character, Float> charProb = sequenceTable.get(sequence);
                if (charProb.get(mystring.charAt(i)) == null) {
                    n--;
                    continue;
                }

                highestProb = charProb.get(mystring.charAt(i));
            }

            probs.add(highestProb);
        }

        return probs;
    }

    /**
     * Take a list of probabilites (floats), and return the sum of the logs (base 2) in the list.
     */
    public float calcTotalLogProb(List<Float> charProbs) {
        float sum = 0f;

        for (float f : charProbs) {
            sum += Math.round(Math.log(f) / Math.log(2));
        }

        return sum;
    }
}
