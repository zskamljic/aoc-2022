import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Day15 {
    private static int OBSERVED_Y = 2000000;
    private static int MAX_X = 4000000;
    private static int MAX_Y = 4000000;
//    private static int MAX_X = 20;
//    private static int MAX_Y = 20;


    public static void main(String[] args) throws IOException {
        var input = Arrays.asList("""
            Sensor at x=2, y=18: closest beacon is at x=-2, y=15
            Sensor at x=9, y=16: closest beacon is at x=10, y=16
            Sensor at x=13, y=2: closest beacon is at x=15, y=3
            Sensor at x=12, y=14: closest beacon is at x=10, y=16
            Sensor at x=10, y=20: closest beacon is at x=10, y=16
            Sensor at x=14, y=17: closest beacon is at x=10, y=16
            Sensor at x=8, y=7: closest beacon is at x=2, y=10
            Sensor at x=2, y=0: closest beacon is at x=2, y=10
            Sensor at x=0, y=11: closest beacon is at x=2, y=10
            Sensor at x=20, y=14: closest beacon is at x=25, y=17
            Sensor at x=17, y=20: closest beacon is at x=21, y=22
            Sensor at x=16, y=7: closest beacon is at x=15, y=3
            Sensor at x=14, y=3: closest beacon is at x=15, y=3
            Sensor at x=20, y=1: closest beacon is at x=15, y=3
            """.split("\n"));
        var input2 = Files.readAllLines(Path.of("input15.txt"));
        input = input2;

        var sensors = input.stream()
            .map(Day15::parseSensor)
            .toList();
        part1(sensors);
        part2(sensors);
    }

    private static void part1(List<Sensor> sensors) {
        var illegals = new HashSet<Integer>();
        var beacons = sensors.stream()
            .map(Sensor::beacon)
            .filter(s -> s.y == OBSERVED_Y)
            .map(Point::x)
            .collect(Collectors.toSet());
        for (var sensor : sensors) {
            findIllegal(sensor, illegals, OBSERVED_Y);
        }
        illegals.removeAll(beacons);
        System.out.println(illegals.size());
    }

    private static void part2(List<Sensor> sensors) {
        var beacon = sensors.stream()
            .flatMap(s -> s.outerBound().stream())
            .filter(c -> sensors.stream().noneMatch(s -> s.isInRange(c.x, c.y)))
            .findFirst();

        if (beacon.isPresent()) {
            var target = beacon.get();
            System.out.println(target.x * 4_000_000L + target.y);
            return;
        }
        // Borders
        for (int i = 0; i <= MAX_X; i++) {
            for (int j = 0; j <= MAX_Y; j++) {
                int finalI = i;
                if (sensors.stream().noneMatch(s -> s.isInRange(finalI, 0))) {
                    System.out.println(i * 4_000_000L);
                    return;
                } else if (sensors.stream().noneMatch(s -> s.isInRange(finalI, MAX_Y))) {
                    System.out.println(i * 4_000_000L + MAX_Y);
                    return;
                } else {
                    int finalJ = j;
                    if (sensors.stream().noneMatch(s -> s.isInRange(0, finalJ))) {
                        System.out.println(j);
                        return;
                    } else if (sensors.stream().noneMatch(s -> s.isInRange(MAX_X, finalJ))) {
                        System.out.println(MAX_X * 4_000_000L + j);
                        return;
                    }
                }
            }
        }
    }

    private static void findIllegal(Sensor sensor, Set<Integer> illegal, int y) {
        var manhattan = sensor.range() - Math.abs(sensor.position.y - y);
        for (int i = sensor.position.x - manhattan; i <= sensor.position.x + manhattan; i++) {
            illegal.add(i);
        }
    }

    private static Sensor parseSensor(String line) {
        line = line.substring("Sensor at x=".length());
        var coordinates = line.split(": closest beacon is at x=");
        return new Sensor(Point.parse(coordinates[0]), Point.parse(coordinates[1]));
    }

    record Sensor(Point position, Point beacon, int range) {
        public Sensor(Point position, Point beacon) {
            this(position, beacon, manhattan(position.x, position.y, beacon.x, beacon.y));
        }

        public boolean isInRange(int x, int y) {
            return manhattan(position.x, position.y, x, y) <= range;
        }

        static int manhattan(int x1, int y1, int x2, int y2) {
            return Math.abs(x1 - x2) + Math.abs(y1 - y2);
        }

        public Set<Point> outerBound() {
            var border = new HashSet<Point>();
            for (int i = 0; i < range + 1; i++) {
                border.add(new Point(position.x - range - 1 + i, position.y - i)); // Left
                border.add(new Point(position.x + i, position.y - range - 1 + i)); // Top
                border.add(new Point(position.x + range + 1 - i, position.y + i)); // Right
                border.add(new Point(position.x - i, position.y + range + 1 - i)); // Bottom
            }
            border.removeIf(p -> p.x < 0 || p.y < 0 || p.x > MAX_X || p.y > MAX_Y);
            return border;
        }
    }

    record Point(int x, int y) {
        public static Point parse(String coordinate) {
            var parts = coordinate.split(", y=");
            return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
    }
}
