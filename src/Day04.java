import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Day04 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input04.txt"))
            .stream()
            .map(Pair::parse)
            .toList();

        part1(input);
        part2(input);
    }

    private static void part1(List<Pair> input) {
        var fullyContained = input.stream()
            .filter(Pair::hasFullyContained)
            .count();
        System.out.println(fullyContained);
    }

    private static void part2(List<Pair> input) {
        var overlapped = input.stream()
            .filter(Pair::hasOverlap)
            .count();
        System.out.println(overlapped);
    }

    record Pair(Range first, Range second) {
        static Pair parse(String string) {
            var parts = string.split(",");
            return new Pair(Range.parse(parts[0]), Range.parse(parts[1]));
        }

        public boolean hasFullyContained() {
            return first.fullyContains(second) || second.fullyContains(first);
        }

        public boolean hasOverlap() {
            return first.overlaps(second);
        }
    }

    record Range(int min, int max) {

        public static Range parse(String string) {
            var ranges = string.split("-");
            return new Range(Integer.parseInt(ranges[0]), Integer.parseInt(ranges[1]));
        }

        public boolean fullyContains(Range second) {
            return second.min >= min && second.max <= max;
        }

        public boolean overlaps(Range second) {
            return min <= second.max && second.min <= max;
        }
    }
}
