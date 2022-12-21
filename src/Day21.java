import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongUnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day21 {
    private static final Pattern EXPRESSION = Pattern.compile("(\\w+) ([+\\-*/]) (\\w+)");

    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input21.txt"));

        var literals = new HashMap<String, Long>();
        var expressions = new ArrayList<Expression>();
        for (var line : input) {
            var nameAndOperation = line.split(": ");
            if (!nameAndOperation[1].contains(" ")) {
                literals.put(nameAndOperation[0], Long.parseLong(nameAndOperation[1]));
            } else {
                var matcher = EXPRESSION.matcher(nameAndOperation[1]);
                if (!matcher.matches()) throw new IllegalArgumentException(nameAndOperation[1]);

                var left = matcher.group(1);
                var operator = matcher.group(2);
                var right = matcher.group(3);
                expressions.add(new Expression(nameAndOperation[0], left, right, operator));
            }
        }

        // For part 2
        var pathToHuman = new ArrayList<Expression>();
        var byName = expressions.stream()
            .collect(Collectors.toMap(Expression::name, Function.identity()));

        // For part 2
        var needle = "humn";
        while (true) {
            var finalNeedle = needle;
            var found = expressions.stream()
                .filter(e -> e.left.equals(finalNeedle) || e.right.equals(finalNeedle))
                .findFirst();
            if (found.isEmpty()) break;

            var next = found.get();
            pathToHuman.add(0, next);
            needle = next.name;
        }

        // Simplify
        var queue = new ArrayDeque<>(expressions);
        while (!queue.isEmpty()) {
            performOperation(literals, queue);
        }

        part1(literals);
        part2(literals, byName, pathToHuman);
    }

    private static void part1(Map<String, Long> literals) {
        System.out.println(literals.get("root"));
    }

    private static void part2(Map<String, Long> literals, Map<String, Expression> byName, ArrayList<Expression> pathToHuman) {
        var root = byName.get("root");

        literals.remove("humn");
        pathToHuman.stream()
            .map(e -> e.name)
            .forEach(literals::remove);
        pathToHuman.remove(root);

        long targetValue;
        if (literals.containsKey(root.left)) {
            targetValue = literals.get(root.left);
        } else {
            targetValue = literals.get(root.right);
        }

        var operations = pathToHuman.stream()
            .map(e -> e.reverse(literals))
            .toList();
        for (var operation : operations) {
            targetValue = operation.applyAsLong(targetValue);
        }
        System.out.println(targetValue);
    }

    private static void performOperation(Map<String, Long> literals, ArrayDeque<Expression> queue) {
        var item = queue.poll();
        if (!literals.containsKey(item.left) || !literals.containsKey(item.right)) {
            queue.add(item);
            return;
        }

        var left = literals.get(item.left);
        var right = literals.get(item.right);

        var value = switch (item.operation) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            case "/" -> left / right;
            default -> throw new IllegalArgumentException("Invalid operator: " + item.operation);
        };
        literals.put(item.name, value);
    }

    record Expression(String name, String left, String right, String operation) {
        public LongUnaryOperator reverse(Map<String, Long> literals) {
            // equation in form a = b <op> c
            if (literals.containsKey(left)) {
                var b = literals.get(left);
                return switch (operation) {
                    case "+" -> a -> a - b; // a = b + c -> c = a - b
                    case "-" -> a -> b - a; // a = b - c -> c = b - a
                    case "*" -> a -> a / b; // a = b * c -> c = a / b
                    case "/" -> a -> b / a; // a = b / c -> c = b / a
                    default -> throw new IllegalArgumentException("Invalid operator: " + operation);

                };
            } else if (literals.containsKey(right)) {
                var c = literals.get(right);
                return switch (operation) {
                    case "+" -> a -> a - c; // a = b + c -> b = a - c
                    case "-" -> a -> a + c; // a = b - c -> b = a + c
                    case "*" -> a -> a / c; // a = b * c -> b = a / c
                    case "/" -> a -> a * c; // a = b / c -> b = a * c
                    default -> throw new IllegalArgumentException("Invalid operator: " + operation);
                };
            } else {
                throw new IllegalStateException("Value not found: " + name);
            }
        }
    }
}
