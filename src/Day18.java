import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class Day18 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input18.txt"));
        var voxels = input.stream()
            .map(Point3d::parse)
            .collect(Collectors.toSet());

        part1(voxels);
        part2(voxels);
    }

    private static void part1(Set<Point3d> voxels) {
        var visibleSides = 0;
        for (var voxel : voxels) {
            visibleSides += voxel.findVisibleSides(voxels);
        }
        System.out.println(visibleSides);
    }

    private static void part2(Set<Point3d> voxels) {
        var minX = min(voxels, Point3d::x) - 1;
        var minY = min(voxels, Point3d::y) - 1;
        var minZ = min(voxels, Point3d::z) - 1;
        var maxX = max(voxels, Point3d::x) + 1;
        var maxY = max(voxels, Point3d::y) + 1;
        var maxZ = max(voxels, Point3d::z) + 1;

        var water = new HashSet<Point3d>();
        var queue = new ArrayDeque<Point3d>();
        queue.add(new Point3d(minX, minY, minZ));
        while (!queue.isEmpty()) {
            var candidate = queue.poll();
            if (candidate.x < minX || candidate.x > maxX ||
                candidate.y < minY || candidate.y > maxY ||
                candidate.z < minZ || candidate.z > maxZ) {
                continue;
            }

            var neighbours = candidate.sideNeighbours();
            for (var neighbour : neighbours) {
                if (!voxels.contains(neighbour) && !water.contains(neighbour)) {
                    queue.add(neighbour);
                    water.add(neighbour);
                }
            }
        }

        var exposed = voxels.stream()
            .mapToLong(v -> v.sideNeighbours()
                .stream()
                .filter(water::contains)
                .count())
            .sum();
        System.out.println(exposed);
    }

    private static int min(Collection<Point3d> voxels, ToIntFunction<Point3d> getter) {
        return voxels.stream().mapToInt(getter).min().orElseThrow();
    }

    private static int max(Collection<Point3d> voxels, ToIntFunction<Point3d> getter) {
        return voxels.stream().mapToInt(getter).max().orElseThrow();
    }

    record Point3d(int x, int y, int z) {
        public static Point3d parse(String line) {
            var parts = line.split(",");
            return new Point3d(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }

        public List<Point3d> sideNeighbours() {
            return List.of(
                new Point3d(x - 1, y, z),
                new Point3d(x + 1, y, z),
                new Point3d(x, y - 1, z),
                new Point3d(x, y + 1, z),
                new Point3d(x, y, z - 1),
                new Point3d(x, y, z + 1)
            );
        }

        public int findVisibleSides(Set<Point3d> voxels) {
            var neighbours = sideNeighbours();
            int visible = neighbours.size();
            for (var neighbour : neighbours) {
                if (voxels.contains(neighbour)) visible--;
            }
            return visible;
        }
    }
}
