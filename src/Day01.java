import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Day01 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input01.txt"));

        var maximums = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        var current = 0;

        for (var line : input) {
            if (line.isBlank()) {
                insertIfNeeded(maximums, current);
                current = 0;
                continue;
            }

            var calories = Integer.parseInt(line);
            current += calories;
        }
        insertIfNeeded(maximums, current);

        // Part 1 maximum
        System.out.println(maximums[0]);
        // Part 2 sum top three
        System.out.println(maximums[0] + maximums[1] + maximums[2]);
    }

    private static void insertIfNeeded(int[] maximums, int current) {
        for (int i = 0; i < maximums.length; i++) {
            if (current > maximums[i]) {
                var old = maximums[i];
                maximums[i] = current;
                current = old;
            }
        }
    }
}
