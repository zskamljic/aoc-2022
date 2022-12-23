import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day23 {
    private static final ProposedMove MOVE_NORTH = (neighbours, proposed, elf) -> {
        if (northEmpty(neighbours)) {
            proposed.put(elf, new Point(elf.x, elf.y - 1));
            return true;
        }
        return false;
    };
    private static final ProposedMove MOVE_SOUTH = (neighbours, proposed, elf) -> {
        if (southEmpty(neighbours)) {
            proposed.put(elf, new Point(elf.x, elf.y + 1));
            return true;
        }
        return false;
    };
    private static final ProposedMove MOVE_WEST = (neighbours, proposed, elf) -> {
        if (westEmpty(neighbours)) {
            proposed.put(elf, new Point(elf.x - 1, elf.y));
            return true;
        }
        return false;
    };
    private static final ProposedMove MOVE_EAST = (neighbours, proposed, elf) -> {
        if (eastEmpty(neighbours)) {
            proposed.put(elf, new Point(elf.x + 1, elf.y));
            return true;
        }
        return false;
    };
    private static final List<ProposedMove> PROPOSITIONS = List.of(MOVE_NORTH, MOVE_SOUTH, MOVE_WEST, MOVE_EAST);

    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input23.txt"));

        var elves = new HashSet<Point>();
        for (int y = 0; y < input.size(); y++) {
            var chars = input.get(y).toCharArray();
            for (int x = 0; x < chars.length; x++) {
                if (chars[x] == '#') elves.add(new Point(x, y));
            }
        }

        part1(elves);
        part2(elves);
    }

    private static void part1(Set<Point> elves) {
        for (int i = 0; i < 10; i++) {
            elves = doStep(elves, i);
        }
        var minX = Integer.MAX_VALUE;
        var maxX = Integer.MIN_VALUE;
        var minY = Integer.MAX_VALUE;
        var maxY = Integer.MIN_VALUE;
        for (var elf : elves) {
            if (elf.x < minX) minX = elf.x;
            if (elf.x > maxX) maxX = elf.x;
            if (elf.y < minY) minY = elf.y;
            if (elf.y > maxY) maxY = elf.y;
        }
        var width = maxX - minX + 1;
        var height = maxY - minY + 1;
        var area = width * height;
        System.out.println(area - elves.size());
    }

    private static void part2(Set<Point> elves) {
        var steps = 0;
        while (true) {
            var newElves = doStep(elves, steps);
            if (newElves.equals(elves)) {
                System.out.println(steps + 1);
                return;
            }
            elves = newElves;
            steps++;
        }
    }

    private static HashSet<Point> doStep(Set<Point> elves, int currentStep) {
        // First half
        var proposedPositions = new HashMap<Point, Point>();
        var newPositions = new HashSet<Point>();
        for (var elf : elves) {
            var neighbours = new boolean[8];
            var coordinates = neighbours(elf.x, elf.y);
            for (int j = 0; j < neighbours.length; j++) {
                var neighbour = coordinates[j];
                neighbours[j] = elves.contains(neighbour);
            }
            if (allEmpty(neighbours)) {
                newPositions.add(elf);
                continue;
            }
            if (cannotMove(currentStep, proposedPositions, elf, neighbours)) {
                newPositions.add(elf);
            }
        }

        // Second half
        var uniquePositions = proposedPositions.values()
            .stream()
            .collect(Collectors.toMap(Function.identity(), e -> 1, Math::addExact));
        for (var elfPair : proposedPositions.entrySet()) {
            var current = elfPair.getKey();
            var value = elfPair.getValue();
            if (uniquePositions.get(value) > 1 || newPositions.contains(value)) {
                newPositions.add(current);
            } else {
                newPositions.add(value);
            }
        }
        if (elves.size() != newPositions.size()) {
            throw new IllegalStateException("Sizes: " + elves.size() + ", " + newPositions.size());
        }

        return newPositions;
    }

    private static boolean cannotMove(int i, Map<Point, Point> proposedPositions, Point elf, boolean[] neighbours) {
        for (int j = 0; j < PROPOSITIONS.size(); j++) {
            if (PROPOSITIONS.get((j + i) % PROPOSITIONS.size()).propose(neighbours, proposedPositions, elf)) {
                return false;
            }
        }
        return true;
    }

    private static boolean northEmpty(boolean[] neighbours) {
        return !(neighbours[0] || neighbours[1] || neighbours[2]);
    }

    private static boolean southEmpty(boolean[] neighbours) {
        return !(neighbours[5] || neighbours[6] || neighbours[7]);
    }

    private static boolean westEmpty(boolean[] neighbours) {
        return !(neighbours[0] || neighbours[3] || neighbours[5]);
    }

    private static boolean eastEmpty(boolean[] neighbours) {
        return !(neighbours[2] || neighbours[4] || neighbours[7]);
    }

    private static boolean allEmpty(boolean[] neighbours) {
        for (var neighbour : neighbours) {
            if (neighbour) return false;
        }
        return true;
    }

    static Point[] neighbours(int x, int y) {
        return new Point[]{
            new Point(x - 1, y - 1), new Point(x, y - 1), new Point(x + 1, y - 1),
            new Point(x - 1, y), new Point(x + 1, y),
            new Point(x - 1, y + 1), new Point(x, y + 1), new Point(x + 1, y + 1)
        };
    }

    record Point(int x, int y) {
    }

    @FunctionalInterface
    interface ProposedMove {
        boolean propose(boolean[] neighbours, Map<Point, Point> proposedPositions, Point current);
    }
}
