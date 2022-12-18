import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Day14 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input14.txt"));

        var filled = new HashSet<Point>();
        var maxY = 0;
        for (var line : input) {
            var points = line.split(" -> ");
            Point previous = null;
            for (var point : points) {
                var coordinates = point.split(",");
                var x = Integer.parseInt(coordinates[0]);
                var y = Integer.parseInt(coordinates[1]);
                if (y > maxY) {
                    maxY = y;
                }

                if (previous != null) {
                    for (int i = Math.min(x, previous.x()); i <= Math.max(x, previous.x()); i++) {
                        for (int j = Math.min(previous.y(), y); j <= Math.max(y, previous.y()); j++) {
                            filled.add(new Point(i, j));
                        }
                    }
                }
                previous = new Point(x, y);
            }
        }

        System.out.println(fallSand(new HashSet<>(filled), maxY, false));
        System.out.println(fallSand(filled, maxY + 2, true));
    }

    private static int fallSand(Set<Point> filled, int maxY, boolean hasMaxFloor) {
        var sandCount = 0;
        while (!filled.contains(new Point(500, 0))) {
            int sandX = 500;
            int sandY = 0;

            while (
                canFill(filled, sandX, sandY + 1, hasMaxFloor, maxY) ||
                    canFill(filled, sandX - 1, sandY + 1, hasMaxFloor, maxY) ||
                    canFill(filled, sandX + 1, sandY + 1, hasMaxFloor, maxY)
            ) {
                if (sandY > maxY) return sandCount;

                if (canFill(filled, sandX, sandY + 1, hasMaxFloor, maxY)) {
                    sandY++;
                } else if (canFill(filled, sandX - 1, sandY + 1, hasMaxFloor, maxY)) {
                    sandX--;
                    sandY++;
                } else if (canFill(filled, sandX + 1, sandY + 1, hasMaxFloor, maxY)) {
                    sandX++;
                    sandY++;
                }
            }
            filled.add(new Point(sandX, sandY));
            sandCount++;

        }
        return sandCount;
    }

    static boolean canFill(Set<Point> filled, int x, int y, boolean hasFloor, int floor) {
        if (hasFloor && y == floor) {
            return false;
        }
        return !filled.contains(new Point(x, y));
    }

    record Point(int x, int y) {
    }
}
