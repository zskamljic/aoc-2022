import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

public class Day17 {
    private static final int[][] SHAPES = {
        {2, 3, 4, 5},
        {3, 9, 10, 11, 17},
        {2, 3, 4, 11, 18},
        {2, 9, 16, 23},
        {2, 3, 9, 10}
    };

    public static void main(String[] args) throws IOException {
        var input = Files.readString(Path.of("input17.txt")).trim();

        var commands = input.toCharArray();
        System.out.println(run(commands, 2022));
        System.out.println(run(commands, 1_000_000_000_000L));
    }

    private static long run(char[] commands, long count) {
        var currentShape = 0;
        var currentCommand = 0;
        var highest = 0;
        var blocked = new HashSet<Integer>();
        IntStream.range(0, 7).forEach(blocked::add);

        var outcomes = new ArrayList<Integer>();
        var visited = new HashMap<Integer, List<Integer>>();

        while (count-- > 0) {
            int finalHighest = highest;
            var shape = Arrays.stream(SHAPES[currentShape]).map(v -> v + (finalHighest + 4) * 7).toArray();

            var isMoving = true;
            while (isMoving) {
                moveCommand(commands[currentCommand] == '<', blocked, shape);
                currentCommand++;
                currentCommand %= commands.length;
                if (!moveDown(shape, blocked)) continue;

                Arrays.stream(shape).forEach(blocked::add);
                var high = shape[shape.length - 1] / 7;
                var increase = Math.max(0, high - highest);
                outcomes.add(increase);
                highest += increase;
                var state = currentCommand * SHAPES.length + currentShape;

                var pastVisits = visited.computeIfAbsent(state, i -> new ArrayList<>());
                pastVisits.add(outcomes.size() - 1);
                var cycleLength = findPattern(pastVisits, outcomes);
                if (cycleLength > 0) {
                    return computeHeight(count, highest, outcomes, cycleLength);
                }
                isMoving = false;
            }
            currentShape++;
            currentShape %= SHAPES.length;
        }
        return highest;
    }

    private static long computeHeight(long count, int highest, ArrayList<Integer> outcomes, int cycleLength) {
        var fullIterations = count / cycleLength;
        var partialIteration = count % cycleLength;
        return highest + fullIterations * outcomes.subList(outcomes.size() - cycleLength, outcomes.size())
            .stream()
            .mapToLong(x -> x)
            .sum() +
            outcomes.subList(outcomes.size() - cycleLength, (int) (outcomes.size() - cycleLength + partialIteration))
                .stream()
                .mapToLong(x -> x)
                .sum();
    }

    private static void moveCommand(boolean left, HashSet<Integer> blocked, int[] shape) {
        if (left) {
            if (all(shape, v -> v % 7 != 0) && none(shape, v -> blocked.contains(v - 1))) {
                for (int x = 0; x < shape.length; x++) {
                    shape[x]--;
                }
            }
        } else if (all(shape, v -> v % 7 != 6) && none(shape, v -> blocked.contains(v + 1))) {
            for (int x = 0; x < shape.length; x++) {
                shape[x]++;
            }
        }
    }

    private static boolean moveDown(int[] shape, HashSet<Integer> blocked) {
        var canMove = none(shape, i -> blocked.contains(i - 7));
        if (canMove) {
            for (int i = 0; i < shape.length; i++) {
                shape[i] -= 7;
            }
        }
        return !canMove;
    }

    private static boolean all(int[] array, IntPredicate condition) {
        return Arrays.stream(array).allMatch(condition);
    }

    private static boolean none(int[] array, IntPredicate condition) {
        return Arrays.stream(array).noneMatch(condition);
    }

    private static int findPattern(List<Integer> pastVisits, List<Integer> outcomes) {
        var lastIndex = pastVisits.get(pastVisits.size() - 1);
        for (int i = 0; i < pastVisits.size() - 1; i++) {
            var testIndex = pastVisits.get(i);
            var cycleLength = lastIndex - testIndex;
            if (testIndex + 1 < cycleLength) continue;
            var j = 0;
            while (j < cycleLength) {
                if (!Objects.equals(outcomes.get(lastIndex - j), outcomes.get(testIndex - j))) break;
                j++;
            }
            if (j == cycleLength) return cycleLength;
        }
        return 0;
    }
}