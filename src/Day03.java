import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Day03 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input03.txt"));

        part1(input);
        part2(input);
    }

    private static void part1(List<String> input) {
        var sum = input.parallelStream()
            .mapToInt(Day03::findRepeated)
            .sum();
        System.out.println(sum);
    }

    private static void part2(List<String> input) {
        var sum = 0;
        for (int i = 0; i < input.size(); i += 3) {
            sum += findBadge(input.get(i), input.get(i + 1), input.get(i + 2));
        }
        System.out.println(sum);
    }

    private static int findRepeated(String s) {
        var left = new boolean[52];
        var right = new boolean[52];

        var chars = s.toCharArray();
        for (int i = 0; i < chars.length / 2; i++) {
            var leftPriority = toPriority(chars[i]);
            var rightPriority = toPriority(chars[i + chars.length / 2]);
            left[leftPriority] = true;
            right[rightPriority] = true;

            if (left[leftPriority] && right[leftPriority]) return leftPriority + 1;
            if (left[rightPriority] && right[rightPriority]) return rightPriority + 1;
        }

        return 0;
    }

    private static int findBadge(String s1, String s2, String s3) {
        var found = new boolean[3][52];

        var chars = new char[3][];
        chars[0] = s1.toCharArray();
        chars[1] = s2.toCharArray();
        chars[2] = s3.toCharArray();
        var maxLen = Math.max(chars[0].length, Math.max(chars[1].length, chars[2].length));
        for (int i = 0; i < maxLen; i++) {
            for (int j = 0; j < chars.length; j++) {
                if (chars[j].length <= i) continue;

                var priority = toPriority(chars[j][i]);
                found[j][priority] = true;
                if (found[0][priority] && found[1][priority] && found[2][priority]) {
                    return priority + 1;
                }
            }
        }
        return 0;
    }

    private static int toPriority(int value) {
        if (value <= 'Z') {
            return value - 65 + 27 - 1;
        } else {
            return value - 97 + 1 - 1;
        }
    }
}
