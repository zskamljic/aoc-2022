import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Day13 {
    public static void main(String[] args) throws IOException {
        var input = Files.readString(Path.of("input13.txt"));

        var pairs = Arrays.stream(input.split("\n\n"))
            .map(pair -> pair.split("\n"))
            .map(pair -> new ItemList[]{parseList(pair[0]), parseList(pair[1])})
            .toList();
        part1(pairs);
        part2(pairs);
    }

    private static void part1(List<ItemList[]> pairs) {
        var score = 0;
        for (int i = 0; i < pairs.size(); i++) {
            var pair = pairs.get(i);
            if (pair[0].compareTo(pair[1]) < 0) {
                score += i + 1;
            }
        }
        System.out.println(score);
    }

    private static void part2(List<ItemList[]> pairs) {
        var allPairs = pairs.stream()
            .flatMap(Arrays::stream)
            .collect(Collectors.toCollection(ArrayList::new));
        var divider1 = new ItemList(List.of(new ItemList(List.of(new Literal(2)))));
        var divider2 = new ItemList(List.of(new ItemList(List.of(new Literal(6)))));
        allPairs.add(divider1);
        allPairs.add(divider2);

        allPairs.sort(Item::compareTo);
        var divider1Index = allPairs.indexOf(divider1) + 1;
        var divider2Index = allPairs.indexOf(divider2) + 1;
        System.out.println(divider1Index * divider2Index);
    }

    static ItemList parseList(String line) {
        var characters = line.toCharArray();
        var numberStart = -1;
        var itemStack = new ArrayDeque<List<Item>>();
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < characters.length; i++) {
            if (characters[i] == '[') {
                itemStack.push(items);
                items = new ArrayList<>();
            } else if (characters[i] == ']') {
                if (numberStart != -1) {
                    items.add(new Literal(Integer.parseInt(line, numberStart, i, 10)));
                    numberStart = -1;
                }
                var result = new ItemList(items);
                items = itemStack.pop();
                items.add(result);
            } else if (characters[i] == ',') {
                if (numberStart != -1) {
                    items.add(new Literal(Integer.parseInt(line, numberStart, i, 10)));
                    numberStart = -1;
                }
            } else if (numberStart == -1) {
                numberStart = i;
            }
        }
        return (ItemList) items.get(0);
    }

    sealed interface Item extends Comparable<Item> {
        default int compareTo(Item other) {
            if (this instanceof Literal left && other instanceof Literal right) {
                return Integer.compare(left.value, right.value);
            } else if (this instanceof ItemList left && other instanceof ItemList right) {
                for (int i = 0; i < left.value.size() && i < right.value.size(); i++) {
                    var result = left.value.get(i).compareTo(right.value.get(i));
                    if (result != 0) return result;
                }
                return Integer.compare(left.value.size(), right.value.size());
            } else if (this instanceof Literal left && other instanceof ItemList right) {
                return new ItemList(List.of(left)).compareTo(right);
            } else if (this instanceof ItemList left && other instanceof Literal right) {
                return left.compareTo(new ItemList(List.of(right)));
            }
            throw new IllegalStateException("Left: " + this.getClass() + ", right: " + other.getClass());
        }
    }

    record Literal(int value) implements Item {
    }

    record ItemList(List<Item> value) implements Item {
    }
}
