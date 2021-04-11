package indair.basic;

public class Crypto {

    private String key;
    private char[] key_chars;

    private final String default_key = "Indoor";
    private final int MAX_CHAR = 126;
    private final int MIN_CHAR = 32;


    public Crypto() {
        key = default_key;
        key_chars = key.toCharArray();
    }

    public void setKey(String _key) {
        key = _key;
        key_chars = key.toCharArray();
    }

    public String encode(String message) {

        StringBuilder result = new StringBuilder();
        char chars[] = message.toCharArray();
        int counter = 0;
        int size = key.length();
        int new_char;

        for (char ch : chars) {
            new_char = (int)ch + (int)key_chars[counter++ % size];

            if (new_char > MAX_CHAR) {
                new_char = MIN_CHAR + new_char - MAX_CHAR;
                if (new_char > MAX_CHAR) {
                    new_char = MIN_CHAR + new_char - MAX_CHAR;
                }
            }
            result.append((char)new_char);
        }

        return result.toString();
    }

    public String decode(String message) {

        StringBuilder result = new StringBuilder();
        char chars[] = message.toCharArray();
        int counter = 0;
        int size = key.length();
        int new_char;

        for (char ch : chars) {
            new_char = (int)ch - (int)key_chars[counter++ % size];

            if (new_char < 0) {
                new_char = MAX_CHAR - (Math.abs(new_char) + MIN_CHAR);
                if (new_char < 0) new_char = MAX_CHAR - (Math.abs(new_char) + MIN_CHAR);
                else if (new_char < MIN_CHAR)  new_char = MAX_CHAR - (MIN_CHAR - new_char);
            }
            else if (new_char < MIN_CHAR)  new_char = MAX_CHAR - (MIN_CHAR - new_char);

            result.append((char)new_char);
        }

        return result.toString();
    }
}
