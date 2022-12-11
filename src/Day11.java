import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day11 {
    public static void main(String[] args) throws IOException {
        var input = Files.readString(Path.of("input11.txt"));
        var monkeys = Arrays.stream(input.split("\n\n"))
            .map(Monkey::parse)
            .toList();
        var lcm = monkeys.stream()
            .mapToLong(Monkey::modulo)
            .reduce((a, b) -> a * b)
            .orElseThrow() / gcd(monkeys);

        part1(monkeys, lcm);
        part2(monkeys, lcm);
    }

    private static long gcd(List<Monkey> monkeys) {
        var numbers = monkeys.stream()
            .mapToLong(Monkey::modulo)
            .toArray();
        var result = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            result = gcd(result, numbers[i]);
        }
        return result;
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            var tmp = a;
            a = b;
            b = tmp % b;
        }
        return a;
    }

    private static void part1(List<Monkey> monkeys, long lcm) {
        monkeys = monkeys.stream()
            .map(Monkey::copy)
            .toList();

        var inspections = new int[monkeys.size()];
        for (int round = 0; round < 20; round++) {
            for (int monkey = 0; monkey < monkeys.size(); monkey++) {
                inspections[monkey] += monkeys.get(monkey).inspectItems(monkeys, true, lcm);
            }
        }
        var monkeyBusiness = IntStream.of(inspections)
            .sorted()
            .skip(monkeys.size() - 2L)
            .reduce((a, b) -> a * b)
            .orElseThrow();
        System.out.println(monkeyBusiness);
    }

    private static void part2(List<Monkey> monkeys, long lcm) {
        var inspections = new int[monkeys.size()];
        for (int round = 0; round < 10_000; round++) {
            for (int monkey = 0; monkey < monkeys.size(); monkey++) {
                inspections[monkey] += monkeys.get(monkey).inspectItems(monkeys, false, lcm);
            }
        }
        var monkeyBusiness = IntStream.of(inspections)
            .mapToLong(i -> i)
            .sorted()
            .skip(monkeys.size() - 2L)
            .reduce((a, b) -> a * b)
            .orElseThrow();
        System.out.println(monkeyBusiness);
    }

    record Monkey(List<Long> items, LongUnaryOperator operation, long modulo, int trueTarget,
                  int falseTarget) {

        public static final String UNKNOWN_OPERATOR = "Unknown operator ";

        public static Monkey parse(String input) {
            var lines = input.split("\n");
            var startingItems = Arrays.stream(lines[1].substring("  Starting items: ".length()).split(", "))
                .map(Long::parseLong)
                .collect(Collectors.toCollection(ArrayList::new));
            var operation = parseOperation(lines[2]);
            var modulo = parseModulo(lines[3]);
            var trueTarget = parseTarget(lines[4]);
            var falseTarget = parseTarget(lines[5]);
            return new Monkey(startingItems, operation, modulo, trueTarget, falseTarget);
        }

        private static LongUnaryOperator parseOperation(String line) {
            var operation = line.substring("  Operation: new = ".length());
            var operands = operation.split(" ");
            if ("old".equals(operands[0]) && !"old".equals(operands[2])) {
                var constant = Integer.parseInt(operands[2]);
                return switch (operands[1]) {
                    case "+" -> i -> i + constant;
                    case "*" -> i -> i * constant;
                    default -> throw new IllegalArgumentException(UNKNOWN_OPERATOR + operands[1]);
                };
            } else if ("old".equals(operands[0])) {
                return switch (operands[1]) {
                    case "+" -> i -> i + i;
                    case "*" -> i -> i * i;
                    default -> throw new IllegalArgumentException(UNKNOWN_OPERATOR + operands[1]);
                };
            } else if ("old".equals(operands[2])) {
                var constant = Integer.parseInt(operands[0]);
                return switch (operands[1]) {
                    case "+" -> i -> i + constant;
                    case "*" -> i -> i * constant;
                    default -> throw new IllegalArgumentException(UNKNOWN_OPERATOR + operands[1]);
                };
            } else {
                var constant1 = Integer.parseInt(operands[0]);
                var constant2 = Integer.parseInt(operands[2]);
                return switch (operands[1]) {
                    case "+" -> i -> constant1 + constant2;
                    case "*" -> i -> constant1 * constant2;
                    default -> throw new IllegalArgumentException(UNKNOWN_OPERATOR + operands[1]);
                };
            }
        }

        private static long parseModulo(String line) {
            return Long.parseLong(line.substring("  Test: divisible by ".length()));
        }

        private static int parseTarget(String line) {
            return Integer.parseInt(line.substring(line.lastIndexOf(' ') + 1));
        }

        public int inspectItems(List<Monkey> monkeys, boolean relief, long lcm) {
            var inspections = items.size();
            for (var iterator = items.iterator(); iterator.hasNext(); iterator.remove()) {
                var item = operation.applyAsLong(iterator.next());
                if (relief) {
                    item /= 3;
                }
                if (item % modulo == 0) {
                    monkeys.get(trueTarget).items.add(item % lcm);
                } else {
                    monkeys.get(falseTarget).items.add(item % lcm);
                }
            }
            return inspections;
        }

        public Monkey copy() {
            return new Monkey(new ArrayList<>(items), operation, modulo, trueTarget, falseTarget);
        }
    }
}
