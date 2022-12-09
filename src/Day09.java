import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Day09 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input09.txt"));
        var movements = input.stream()
            .mapMulti(Day09::parseMovement)
            .toList();

        part1(movements);
        part2(movements);
    }

    private static void part1(List<Movement> movements) {
        var rope = Rope.withSize(2);
        var visited = new HashSet<Position>();
        for (var movement : movements) {
            rope.apply(movement);
            rope.addTail(visited);
        }
        System.out.println(visited.size());
    }

    private static void part2(List<Movement> movements) {
        var rope = Rope.withSize(10);
        var visited = new HashSet<Position>();
        for (var movement : movements) {
            rope.apply(movement);
            rope.addTail(visited);
        }
        System.out.println(visited.size());
    }

    private static void parseMovement(String line, Consumer<Movement> consumer) {
        var parts = line.split(" ");
        var count = Integer.parseInt(parts[1]);

        var movement = switch (parts[0]) {
            case "U" -> Movement.UP;
            case "D" -> Movement.DOWN;
            case "L" -> Movement.LEFT;
            case "R" -> Movement.RIGHT;
            default -> throw new IllegalArgumentException("Unknown direction: " + parts[0]);
        };
        for (int i = 0; i < count; i++) {
            consumer.accept(movement);
        }
    }

    record Rope(List<Position> positions) {
        static Rope withSize(int length) {
            var positions = new ArrayList<Position>();
            for (int i = 0; i < length; i++) {
                positions.add(new Position());
            }
            return new Rope(positions);
        }

        public void apply(Movement movement) {
            positions.set(0, positions.get(0).add(movement));

            for (int i = 1; i < positions.size(); i++) {
                var position = positions.get(i);
                var previous = positions.get(i - 1);
                if (position.shouldFollow(previous)) {
                    positions.set(i, position.follow(previous));
                }
            }
        }

        public void addTail(Set<Position> visited) {
            visited.add(positions.get(positions.size() - 1));
        }

        @Override
        public String toString() {
            var minX = positions.stream()
                .mapToInt(Position::x)
                .min()
                .orElse(0);
            var maxX = positions.stream()
                .mapToInt(Position::x)
                .max()
                .orElse(0);
            var minY = positions.stream()
                .mapToInt(Position::y)
                .min()
                .orElse(0);
            var maxY = positions.stream()
                .mapToInt(Position::y)
                .max()
                .orElse(0);

            var builder = new StringBuilder();
            for (int y = Math.min(minY, 0); y <= maxY; y++) {
                for (int x = Math.min(minX, 0); x <= maxX; x++) {
                    var didPrint = false;
                    for (int i = 0; i < positions.size(); i++) {
                        var item = positions.get(i);
                        if (item.x == x && item.y == y) {
                            if (i == 0) {
                                builder.append('H');
                            } else if (i == positions.size() - 1) {
                                builder.append('T');
                            } else {
                                builder.append(i);
                            }
                            didPrint = true;
                            break;
                        }
                    }
                    if (!didPrint) {
                        builder.append('.');
                    }
                }
                builder.append('\n');
            }
            return builder.toString();
        }
    }

    enum Movement {
        UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0);

        final int x;
        final int y;

        Movement(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    record Position(int x, int y) {
        Position() {
            this(0, 0);
        }

        public Position add(Movement movement) {
            return new Position(x + movement.x, y + movement.y);
        }

        public boolean shouldFollow(Position previous) {
            return Math.max(Math.abs(x - previous.x), Math.abs(y - previous.y)) > 1;
        }

        public Position follow(Position previous) {
            int dx = (int) Math.signum(previous.x - x);
            int dy = (int) Math.signum(previous.y - y);

            return new Position(x + dx, y + dy);
        }
    }
}
