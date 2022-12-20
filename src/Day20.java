import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.IntToLongFunction;

public class Day20 {
    private static final long ENCRYPTION_KEY = 811_589_153;

    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input20.txt"));

        var numbers = input.stream()
            .mapToLong(Long::parseLong)
            .toArray();

        part1(numbers);
        part2(numbers);
    }

    private static void part1(long[] numbers) {
        var data = new Node[numbers.length];
        var zero = makeNodes(numbers, data, i -> numbers[i]);

        linkNodes(numbers, data);

        mix(numbers, data);

        findAndPrintAnswer(data, zero);
    }

    private static void part2(long[] numbers) {
        var data = new Node[numbers.length];
        var zero = makeNodes(numbers, data, i -> numbers[i] * ENCRYPTION_KEY);

        linkNodes(numbers, data);

        for (int i = 0; i < 10; i++) {
            mix(numbers, data);
        }

        findAndPrintAnswer(data, zero);
    }

    private static int makeNodes(long[] numbers, Node[] data, IntToLongFunction valueMapper) {
        int zero = -1;
        for (int i = 0; i < numbers.length; i++) {
            data[i] = new Node(valueMapper.applyAsLong(i));
            if (numbers[i] == 0) zero = i;
        }
        return zero;
    }

    private static void linkNodes(long[] numbers, Node[] data) {
        for (int i = 0; i < numbers.length; i++) {
            data[i].next = (i + 1) % numbers.length;
            if (i != 0) {
                data[i].previous = i - 1;
            } else {
                data[i].previous = numbers.length - 1;
            }
        }
    }

    private static void mix(long[] numbers, Node[] data) {
        for (int i = 0; i < data.length; i++) {
            var current = data[i];
            var shift = wrap(current.value, numbers.length - 1);
            if (shift == 0) continue;

            // Remove
            data[current.previous].next = current.next;
            data[current.next].previous = current.previous;

            // Find destination
            var target = current.next;
            for (int j = 1; j < shift; j++) {
                target = data[target].next;
            }

            // Insert
            var next = data[target].next;
            var previous = target;

            data[next].previous = i;
            data[previous].next = i;

            data[i].next = next;
            data[i].previous = previous;
        }
    }

    private static void findAndPrintAnswer(Node[] data, int zero) {
        var current = zero;
        var answer = 0L;
        for (int i = 1; i <= 3000; i++) {
            current = data[current].next;
            if (i % 1000 == 0) {
                answer += data[current].value;
            }
        }
        System.out.println(answer);
    }

    private static long wrap(long number, int n) {
        return ((number % n) + n) % n;
    }

    static class Node {
        final long value;
        int next;
        int previous;

        Node(long value) {
            this.value = value;
        }
    }
}
