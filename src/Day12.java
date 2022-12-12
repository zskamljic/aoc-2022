import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Day12 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input12.txt"));
        var elevations = input.stream()
            .map(line -> line.chars().toArray())
            .toArray(int[][]::new);

        var locations = findStartAndEnd(elevations);
        var start = locations.getKey();
        var end = locations.getValue();

        part1(elevations, start, end);
        part2(elevations, end);
    }

    private static void part1(int[][] elevations, Point start, Point end) {
        var distance = dijkstra(elevations, start, end);
        System.out.println(distance);
    }

    private static void part2(int[][] elevations, Point end) {
        var starts = new ArrayList<Point>();

        for (int i = 0; i < elevations.length; i++) {
            for (int j = 0; j < elevations[i].length; j++) {
                if (elevations[i][j] == 'a') starts.add(new Point(j, i));
            }
        }
        var bestDistance = starts.parallelStream()
            .mapToInt(start -> dijkstra(elevations, start, end))
            .min()
            .orElseThrow();
        System.out.println(bestDistance);
    }

    private static int dijkstra(int[][] elevations, Point start, Point end) {
        var distances = new int[elevations.length][elevations[0].length];
        for (int[] distance : distances) {
            Arrays.fill(distance, Integer.MAX_VALUE);
        }
        distances[start.y()][start.x()] = 0;

        var queue = new PriorityQueue<Point>(Comparator.comparingInt(point -> distances[point.y()][point.x()]));
        queue.add(start);
        while (!queue.isEmpty()) {
            var point = queue.poll();
            var neighbours = point.neighbours(elevations);
            for (var neighbour : neighbours) {
                var distance = 1 + distances[point.y()][point.x()];
                if (distance >= distances[neighbour.y()][neighbour.x()]) continue;

                distances[neighbour.y()][neighbour.x()] = distance;
                queue.add(neighbour);
            }
        }
        return distances[end.y()][end.x()];
    }

    private static Map.Entry<Point, Point> findStartAndEnd(int[][] elevations) {
        Point start = null;
        Point end = null;
        for (int i = 0; i < elevations.length; i++) {
            for (int j = 0; j < elevations[i].length; j++) {
                if (elevations[i][j] == 'S') {
                    start = new Point(j, i);
                    elevations[i][j] = 'a';
                } else if (elevations[i][j] == 'E') {
                    end = new Point(j, i);
                    elevations[i][j] = 'z';
                }
                if (start != null && end != null) return Map.entry(start, end);
            }
        }
        throw new IllegalStateException("Start or end was null: " + start + ", " + end);
    }

    record Point(int x, int y) {
        public List<Point> neighbours(int[][] elevations) {
            var neighbours = new ArrayList<Point>();
            if (x > 0) {
                neighbours.add(new Point(x - 1, y));
            }
            if (y > 0) {
                neighbours.add(new Point(x, y - 1));
            }
            if (x < elevations[0].length - 1) {
                neighbours.add(new Point(x + 1, y));
            }
            if (y < elevations.length - 1) {
                neighbours.add(new Point(x, y + 1));
            }
            neighbours.removeIf(point -> elevations[point.y()][point.x()] > elevations[y][x] + 1);
            return neighbours;
        }
    }
}
