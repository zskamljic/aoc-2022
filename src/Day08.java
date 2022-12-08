import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Day08 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input08.txt"));

        var grid = input.stream()
            .map(line -> line.chars().map(i -> i - '0').toArray())
            .toArray(int[][]::new);

        part1(grid);
        part2(grid);
    }

    private static void part1(int[][] grid) {
        var visible = grid.length * 2; // vertical lines
        visible += (grid[0].length - 2) * 2; // horizontal lines, ignoring corners (present in previous)

        for (int row = 1; row < grid.length - 1; row++) {
            for (int column = 1; column < grid[row].length - 1; column++) {
                if (isVisible(grid, row, column)) {
                    visible++;
                }
            }
        }
        System.out.println(visible);
    }

    private static void part2(int[][] grid) {
        var bestScore = 0;
        for (int row = 0; row < grid.length; row++) {
            for (int column = 0; column < grid[row].length; column++) {
                var scenicScore = scenicScore(grid, row, column);
                if (scenicScore > bestScore) {
                    bestScore = scenicScore;
                }
            }
        }
        System.out.println(bestScore);
    }

    private static boolean isVisible(int[][] grid, int row, int column) {
        var visibleSides = 4;
        for (int y = row - 1; y >= 0; y--) {
            if (grid[y][column] >= grid[row][column]) {
                visibleSides--;
                break;
            }
        }
        for (int y = row + 1; y < grid.length; y++) {
            if (grid[y][column] >= grid[row][column]) {
                visibleSides--;
                break;
            }
        }
        for (int x = column - 1; x >= 0; x--) {
            if (grid[row][x] >= grid[row][column]) {
                visibleSides--;
                break;
            }
        }
        for (int x = column + 1; x < grid[0].length; x++) {
            if (grid[row][x] >= grid[row][column]) {
                visibleSides--;
                break;
            }
        }
        return visibleSides > 0;
    }

    private static int scenicScore(int[][] grid, int row, int column) {
        var up = 0;
        for (int y = row - 1; y >= 0; y--) {
            up++;
            if (grid[y][column] >= grid[row][column]) {
                break;
            }
        }
        var down = 0;
        for (int y = row + 1; y < grid.length; y++) {
            down++;
            if (grid[y][column] >= grid[row][column]) {
                break;
            }
        }
        var left = 0;
        for (int x = column - 1; x >= 0; x--) {
            left++;
            if (grid[row][x] >= grid[row][column]) {
                break;
            }
        }
        var right = 0;
        for (int x = column + 1; x < grid[0].length; x++) {
            right++;
            if (grid[row][x] >= grid[row][column]) {
                break;
            }
        }
        return up * left * down * right;
    }
}
