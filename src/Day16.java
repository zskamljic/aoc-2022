import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day16 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input16.txt"));

        var valves = input.stream()
            .map(Valve::parse)
            .collect(Collectors.toMap(Valve::name, Function.identity()));
        var indices = new HashMap<String, Integer>();
        for (var entry : valves.entrySet()) {
            indices.put(entry.getKey(), indices.size());
        }

        var distances = findDistances(valves, indices);

        part1(distances, indices, valves);
        part2(distances, indices, valves);
    }

    private static void part1(Map<String, Integer> distances, Map<String, Integer> indices, Map<String, Valve> valves) {
        var result = new Walker(distances, valves, indices).recurse(new BitSet(indices.size()), valves.get("AA"), 30, 0, new HashMap<>())
            .values()
            .stream()
            .mapToInt(i -> i)
            .max()
            .orElseThrow();
        System.out.println(result);
    }

    private static void part2(Map<String, Integer> distances, Map<String, Integer> indices, Map<String, Valve> valves) {
        var result = new Walker(distances, valves, indices).recurse(new BitSet(indices.size()), valves.get("AA"), 26, 0, new HashMap<>());

        var best = result.entrySet()
            .parallelStream()
            .flatMapToInt(e ->
                result.entrySet().stream()
                    .filter(e2 -> !e.getKey().intersects(e2.getKey()))
                    .mapToInt(e2 -> e.getValue() + e2.getValue())
            )
            .max()
            .orElseThrow();
        System.out.println(best);
    }

    static class Walker {
        private final Map<String, Integer> distances;
        private final int[] openable;
        private final Valve[] valves;

        public Walker(Map<String, Integer> distances, Map<String, Valve> valves, Map<String, Integer> indices) {
            this.distances = distances;
            openable = valves.values()
                .stream()
                .filter(v -> v.rate != 0)
                .mapToInt(v -> indices.get(v.name))
                .toArray();
            this.valves = new Valve[indices.size()];
            valves.values()
                .forEach(v -> this.valves[indices.get(v.name)] = v);
        }

        Map<BitSet, Integer> recurse(BitSet open, Valve current, int timeRemaining, int totalFlow, Map<BitSet, Integer> bestSeen) {
            bestSeen.merge(open, totalFlow, Math::max);
            for (var candidate : openable) {
                int timeAfter = timeRemaining - distances.get(current.name + valves[candidate].name) - 1;
                if (open.get(candidate) || timeAfter <= 0) continue;

                var next = open.get(0, open.size());
                next.set(candidate, true);
                recurse(next, valves[candidate], timeAfter, timeAfter * valves[candidate].rate + totalFlow, bestSeen);
            }
            return bestSeen;
        }
    }

    private static Map<String, Integer> findDistances(Map<String, Valve> valves, Map<String, Integer> indices) {
        var reached = valves.entrySet()
            .stream()
            .collect(Collectors.toMap(e -> indices.get(e.getKey()), e -> e.getValue().leadsTo.stream().map(indices::get).toList()));

        var distances = new HashMap<String, Integer>();
        for (var start : indices.entrySet()) {
            for (var end : indices.entrySet()) {
                var startIndex = indices.get(start.getKey());
                var endIndex = indices.get(end.getKey());
                distances.put(start.getKey() + end.getKey(), dijkstra(reached, startIndex, endIndex));
            }
        }
        return distances;
    }

    private static int dijkstra(Map<Integer, List<Integer>> reached, int start, int end) {
        var distances = new int[reached.size()];
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[start] = 0;

        var queue = new PriorityQueue<Integer>(Comparator.comparingInt(i -> distances[i]));
        queue.add(start);
        while (!queue.isEmpty()) {
            var item = queue.poll();
            reached.get(item).forEach(candidate -> {
                var distance = distances[item] + 1;
                if (distance < distances[candidate]) {
                    distances[candidate] = distance;
                    queue.add(candidate);
                }
            });
        }
        return distances[end];
    }

    record Valve(String name, int rate, List<String> leadsTo) {
        public static Valve parse(String line) {
            var name = line.substring("Valve ".length(), "Valve ".length() + 2);
            var rate = Integer.parseInt(line.substring("Valve AA has flow rate=".length(), line.indexOf(";")));
            var valves = Arrays.asList(line.split("valves? ")[1].split(", "));
            return new Valve(name, rate, valves);
        }
    }
}
