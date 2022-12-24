import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class Day24 {
    private static final int[][] MOVEMENTS = {
        {1, 0},
        {0, 1},
        {-1, 0},
        {0, -1},
        {0, 0}
    };

    public static void main(String[] args) throws IOException {
        var input = Arrays.asList("""
            #.######
            #>>.<^<#
            #.<..<<#
            #>v.><>#
            #<^v^^>#
            ######.#""".split("\n"));
        var input2 = Files.readAllLines(Path.of("input24.txt"));
        input = input2;

        var entrance = new Point(input.get(0).indexOf('.'), 0);
        var exit = new Point(input.get(input.size() - 1).indexOf('.'), input.size() - 1);

        var width = input.get(0).length();
        var height = input.size();

        // Don't count walls in size
        var period = (width - 2) * (height - 2) / gcd(width - 2, height - 2);

        // States
        var states = new ArrayList<Set<Blizzard>>();
        for (int i = 0; i < period; i++) {
            states.add(new HashSet<>());
        }

        makeStates(input, width, height, period, states);

        var part1 = solve(states, 0, width, height, period, entrance, exit);
        System.out.println(part1);
        var back = solve(states, part1, width, height, period, exit, entrance);
        var backAgain = solve(states, part1 + back, width, height, period, entrance, exit);
        System.out.println(part1 + back + backAgain);
    }

    private static void makeStates(List<String> input, int width, int height, int period, ArrayList<Set<Blizzard>> states) {
        for (int y = 0; y < input.size(); y++) {
            for (int x = 0; x < input.get(y).length(); x++) {
                switch (input.get(y).charAt(x)) {
                    case '>' -> states.get(0).add(new Blizzard(x, y, 0));
                    case 'v' -> states.get(0).add(new Blizzard(x, y, 1));
                    case '<' -> states.get(0).add(new Blizzard(x, y, 2));
                    case '^' -> states.get(0).add(new Blizzard(x, y, 3));
                }
            }

            for (int time = 0; time < period - 1; time++) {
                for (var blizzard : states.get(time)) {
                    var movement = MOVEMENTS[blizzard.rotation];
                    var newX = (blizzard.x + movement[0]) % (width - 2);
                    var newY = (blizzard.y + movement[1]) % (height - 2);
                    if (newX == 0) newX = width - 2;
                    if (newY == 0) newY = height - 2;
                    states.get(time + 1).add(new Blizzard(newX, newY, blizzard.rotation));
                }
            }
        }
    }

    private static int solve(List<Set<Blizzard>> states, int startTime, int width, int height, int period, Point start, Point end) {
        var distances = new int[period][height][width];
        for (var p : distances) {
            for (var line : p) {
                Arrays.fill(line, Integer.MAX_VALUE);
            }
        }
        distances[startTime % period][start.y][start.x] = 0;

        var queue = new PriorityQueue<>(Comparator.comparing(State::distance));
        queue.add(new State(0, startTime % period, start.x, start.y));

        while (!queue.isEmpty()) {
            var state = queue.poll();
            if (distances[state.time][state.y][state.x] != state.distance) continue; // We got a better solution

            for (var option : MOVEMENTS) {
                var newX = state.x + option[0] % width;
                var newY = state.y + option[1] % height;
                if (isInvalid(newX, newY, start, end, width, height)) continue; // Wall
                if (states.get((state.time + 1) % period).stream().anyMatch(b -> b.x == newX && b.y == newY)) {
                    continue; // Blizzard in next step
                }
                if (distances[(state.time + 1) % period][newY][newX] > state.distance + 1) {
                    distances[(state.time + 1) % period][newY][newX] = state.distance + 1;
                    var newState = new State(state.distance + 1, (state.time + 1) % period, newX, newY);
                    queue.add(newState);
                }
            }
        }
        var result = Integer.MAX_VALUE;
        for (int time = 0; time < period; time++) {
            result = Math.min(result, distances[time][end.y][end.x]);
        }
        return result;
    }

    private static boolean isInvalid(int newX, int newY, Point start, Point end, int width, int height) {
        if (newY == start.y && newX == start.x || newY == end.y && newX == end.x) return false;
        if (newX <= 0 || newX >= width - 1) return true;
        return newY <= 0 || newY >= height - 1;
    }

    private static int gcd(int a, int b) {
        while (b != 0) {
            var tmp = a;
            a = b;
            b = tmp % b;
        }
        return a;
    }

    record State(int distance, int time, int x, int y) {
    }

    record Point(int x, int y) {
    }

    record Blizzard(int x, int y, int rotation) {
    }
}
