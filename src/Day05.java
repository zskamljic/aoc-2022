import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class Day05 {
    public static void main(String[] args) throws IOException {
        var input = Files.readString(Path.of("input05.txt"));

        var parts = input.split("\n\n");
        var stacks = parts[0];
        var instructions = parseInstructions(parts[1]);

        part1(stacks, instructions);
        part2(stacks, instructions);
    }

    private static void part1(String stacks, List<Instruction> instructions) {
        var state = State.parse(stacks);
        for (var instruction : instructions) {
            instruction.applySingle(state.stacks);
        }
        System.out.println(state.tops());
    }

    private static void part2(String stacks, List<Instruction> instructions) {
        var state = State.parse(stacks);
        for (var instruction : instructions) {
            instruction.applyStack(state.stacks);
        }
        System.out.println(state.tops());
    }

    private static List<Instruction> parseInstructions(String part) {
        return Arrays.stream(part.split("\n"))
            .map(Instruction::parse)
            .toList();
    }

    record State(List<Deque<Character>> stacks) {
        static State parse(String stacks) {
            var lines = stacks.split("\n");
            var count = lines[0].length() / 4 + 1;

            var columns = new ArrayList<Deque<Character>>();
            for (int i = 0; i < count; i++) {
                columns.add(new ArrayDeque<>());
            }

            for (int i = 0; i < lines.length - 1; i++) {
                for (int j = 0; j < count; j++) {
                    var crate = lines[i].charAt(1 + j * 4);
                    if (crate != ' ') {
                        columns.get(j).addLast(crate);
                    }
                }
            }

            return new State(columns);
        }

        public String tops() {
            var builder = new StringBuilder();
            for (var stack : stacks) {
                builder.append(stack.peek());
            }
            return builder.toString();
        }
    }

    record Instruction(int count, int from, int to) {
        static Instruction parse(String line) {
            var parts = line.split("\s");
            return new Instruction(Integer.parseInt(parts[1]), Integer.parseInt(parts[3]), Integer.parseInt(parts[5]));
        }

        public void applySingle(List<Deque<Character>> stacks) {
            for (int i = 0; i < count; i++) {
                var item = stacks.get(from - 1).pop();
                stacks.get(to - 1).push(item);
            }
        }

        public void applyStack(List<Deque<Character>> stacks) {
            var stack = new ArrayList<Character>();
            for (int i = 0; i < count; i++) {
                stack.add(0, stacks.get(from - 1).pop());
            }

            while (!stack.isEmpty()) {
                stacks.get(to - 1).push(stack.remove(0));
            }
        }
    }
}
