import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Day06 {
    public static void main(String[] args) throws IOException {
        var input = Files.readString(Path.of("input06.txt"));

        var characters = input.toCharArray();
        solveCommon(characters, 4); // Part 1
        solveCommon(characters, 14); // Part 2
    }

    private static void solveCommon(char[] characters, int size) {
        var buffer = new SizedBuffer(size);
        int markerIndex;
        for (markerIndex = 0; markerIndex < characters.length && !buffer.isMarker(); markerIndex++) {
            buffer.add(characters[markerIndex]);
        }
        System.out.println(markerIndex);
    }

    static class SizedBuffer {
        private final char[] buffer;
        private final int size;
        private int index = 0;

        SizedBuffer(int size) {
            buffer = new char[size];
            this.size = size;
        }

        public void add(char character) {
            buffer[index] = character;
            index++;
            index %= size;
        }

        public boolean isMarker() {
            for (int i = 0; i < buffer.length - 1; i++) {
                if (buffer[i] == 0) return false;

                for (int j = i + 1; j < buffer.length; j++) {
                    if (buffer[j] == 0 || buffer[i] == buffer[j]) return false;
                }
            }
            return true;
        }
    }
}
