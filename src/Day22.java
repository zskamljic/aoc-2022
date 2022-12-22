import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Day22 {
    private static final int RIGHT = 0;
    private static final int DOWN = 1;
    private static final int LEFT = 2;
    private static final int UP = 3;

    public static void main(String[] args) throws IOException {
        var input = Files.readString(Path.of("input22.txt"));
        var parts = input.split("\n\n");
        var fieldLines = parts[0].split("\n");
        var commands = Command.parse(parts[1].trim());

        Tile start = null;
        var width = Arrays.stream(fieldLines).mapToInt(String::length).max().orElseThrow();
        var tiles = new Tile[fieldLines.length][width];
        for (int y = 0; y < fieldLines.length; y++) {
            Tile first = null;
            Tile previous = null;

            var chars = fieldLines[y].toCharArray();
            for (int x = 0; x < chars.length; x++) {
                if (chars[x] == ' ') continue;

                var current = new Tile(x, y, chars[x] == '#');
                if (start == null) start = current;

                if (first == null) first = current;
                if (previous != null) {
                    previous.neighbors[RIGHT] = current;
                    current.neighbors[LEFT] = previous;
                }
                previous = current;
                tiles[y][x] = current;
            }
            first.neighbors[LEFT] = previous;
            previous.neighbors[RIGHT] = first;
        }
        Arrays.stream(tiles)
            .flatMap(Arrays::stream)
            .filter(Objects::nonNull)
            .forEach(tile -> {
                int row = tile.y == 0 ? tiles.length - 1 : tile.y - 1;
                while (tiles[row][tile.x] == null) {
                    if (row == 0) row = tiles.length - 1;
                    else row--;
                }

                var top = tiles[row][tile.x];
                top.neighbors[DOWN] = tile;
                tile.neighbors[UP] = top;
            });

        part1(start, commands);
        part2(start, commands, tiles);
    }

    private static void part1(Tile start, List<Command> commands) {
        var current = start;
        int facing = RIGHT;
        for (var command : commands) {
            if (command instanceof Walk walk) {
                for (int i = 0; i < walk.steps; i++) {
                    if (current.neighbors[facing].wall) break;

                    var adjust = current.facing[facing];
                    current = current.neighbors[facing];
                    facing = wrap(facing + adjust);
                }
            } else if (command instanceof Direction direction) {
                if (direction.left) {
                    facing = wrap(facing - 1);
                } else {
                    facing = wrap(facing + 1);
                }
            }
        }
        System.out.println(1000 * (current.y + 1) + 4 * (current.x + 1) + facing);
    }

    private static int wrap(int number) {
        return ((number % 4) + 4) % 4;
    }

    private static void part2(Tile start, List<Command> commands, Tile[][] tiles) {
        var a = tiles.length / 4;
        for (int i = 0; i < a; i++) {
            stitch(tiles, a, i);
        }
        part1(start, commands);
    }

    /**
     * Layout:
     * _12
     * _3
     * 54
     * 6
     */
    private static void stitch(Tile[][] tiles, int faceSize, int i) {
        var top1 = tiles[0][faceSize + i];
        var left6 = tiles[3 * faceSize + i][0];
        top1.neighbors[UP] = left6;
        top1.facing[UP] = 1;
        left6.neighbors[LEFT] = top1;
        left6.facing[LEFT] = 3;

        var top2 = tiles[0][faceSize * 2 + i];
        var bottom6 = tiles[faceSize * 4 - 1][i];
        top2.neighbors[UP] = bottom6;
        bottom6.neighbors[DOWN] = top2;

        var right2 = tiles[i][faceSize * 3 - 1];
        var right4 = tiles[faceSize * 3 - i - 1][faceSize * 2 - 1];
        right2.neighbors[RIGHT] = right4;
        right2.facing[RIGHT] = 2;
        right4.neighbors[RIGHT] = right2;
        right4.facing[RIGHT] = 2;

        var bottom2 = tiles[faceSize - 1][faceSize * 2 + i];
        var right3 = tiles[faceSize + i][faceSize * 2 - 1];
        bottom2.neighbors[DOWN] = right3;
        bottom2.facing[DOWN] = 1;
        right3.neighbors[RIGHT] = bottom2;
        right3.facing[RIGHT] = 3;

        var bottom4 = tiles[faceSize * 3 - 1][faceSize + i];
        var right6 = tiles[3 * faceSize + i][faceSize - 1];
        bottom4.neighbors[DOWN] = right6;
        bottom4.facing[DOWN] = 1;
        right6.neighbors[RIGHT] = bottom4;
        right6.facing[RIGHT] = 3;

        var left5 = tiles[faceSize * 2 + i][0];
        var left1 = tiles[faceSize - i - 1][faceSize];
        left5.neighbors[LEFT] = left1;
        left5.facing[LEFT] = 2;
        left1.neighbors[LEFT] = left5;
        left1.facing[LEFT] = 2;

        var left3 = tiles[faceSize + i][faceSize];
        var top5 = tiles[2 * faceSize][i];
        left3.neighbors[LEFT] = top5;
        left3.facing[LEFT] = 3;
        top5.neighbors[UP] = left3;
        top5.facing[UP] = 1;
    }

    sealed interface Command {
        static List<Command> parse(String line) {
            var commands = new ArrayList<Command>();
            int steps = 0;
            for (var c : line.toCharArray()) {
                if (Character.isDigit(c)) {
                    steps *= 10;
                    steps += c - '0';
                } else {
                    if (steps != 0) {
                        commands.add(new Walk(steps));
                        steps = 0;
                    }
                    commands.add(new Direction(c == 'L'));
                }
            }
            if (steps != 0) {
                commands.add(new Walk(steps));
            }
            return commands;
        }
    }

    record Walk(int steps) implements Command {
    }

    record Direction(boolean left) implements Command {
    }

    record Tile(int x, int y, Tile[] neighbors, int[] facing, boolean wall) {
        public Tile(int x, int y, boolean wall) {
            this(x, y, new Tile[4], new int[4], wall);
        }
    }
}
