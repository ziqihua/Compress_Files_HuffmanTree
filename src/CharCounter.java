import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class CharCounter implements ICharCounter, IHuffConstants {
    private Map<Integer, Integer> charTable; // each entry: <the letter, the letter's frequency>

    public CharCounter() {
        charTable = new HashMap<>();
        // Explicitly add the pseudo-EOF with a count of 1
    }

    @Override
    public int getCount(int ch) {
        if (ch < 0 || ch >= ALPH_SIZE) {
            throw new IllegalArgumentException("Character out of valid range: " + ch);
        }
        return charTable.getOrDefault(ch, 0);
    }

    @Override
    public int countAll(InputStream stream) throws IOException {
        clear();
        int totalChars = 0;
        int readChar;
        while ((readChar = stream.read()) != -1) {
            add(readChar);
            totalChars++;
        }

        return totalChars;
    }

    @Override
    public void add(int i) {
        if (i < 0 || i > ALPH_SIZE) {
            throw new IllegalArgumentException("Illegal character to be added");
        }
        if (!charTable.containsKey(i)) {
            charTable.put(i, 1);
        } else {
            charTable.put(i, charTable.get(i) + 1);
        }
    }

    @Override
    public void set(int i, int value) {
        if (i < 0 || i > ALPH_SIZE) {
            throw new IllegalArgumentException("Illegal character to be set");
        }
        charTable.put(i, value);

    }

    @Override
    public void clear() {
        charTable.clear();
    }

    @Override
    public Map<Integer, Integer> getTable() {
        return new HashMap<>(charTable);
    }
}
