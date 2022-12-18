import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.disjoint;

public class Day16 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input16.txt"));

        var valves = input.stream()
            .map(Valve::parse)
            .collect(Collectors.toMap(Valve::name, Function.identity()));
        var distances = findDistances(valves);

        part1(distances, valves);
        part2(distances, valves);
    }

    private static void part1(Map<String, Integer> distances, Map<String, Valve> valves) {
        var result = new Walker(distances, valves).recurse(Set.of(), valves.get("AA"), 30, 0, new HashMap<>())
            .values()
            .stream()
            .mapToInt(i -> i)
            .max()
            .orElseThrow();
        System.out.println(result);
    }

    private static void part2(Map<String, Integer> distances, Map<String, Valve> valves) {
        var result = new Walker(distances, valves).recurse(Set.of(), valves.get("AA"), 26, 0, new HashMap<>());

        var best = result.entrySet()
            .stream()
            .flatMapToInt(e ->
                result.entrySet().stream()
                    .filter(e2 -> disjoint(e.getKey(), e2.getKey()))
                    .mapToInt(e2 -> e.getValue() + e2.getValue())
            )
            .max()
            .orElseThrow();
        System.out.println(best);
    }

    static class Walker {
        private final Map<String, Integer> distances;
        private final List<Valve> openable;

        public Walker(Map<String, Integer> distances, Map<String, Valve> valves) {
            this.distances = distances;
            openable = valves.values()
                .stream()
                .filter(v -> v.rate != 0)
                .toList();
        }

        Map<Set<Valve>, Integer> recurse(Set<Valve> open, Valve current, int timeRemaining, int totalFlow, Map<Set<Valve>, Integer> bestSeen) {
            bestSeen.merge(open, totalFlow, Math::max);
            for (var candidate : openable) {
                int timeAfter = timeRemaining - distances.get(current.name + candidate.name) - 1;
                if (open.contains(candidate) || timeAfter <= 0) continue;

                recurse(join(candidate, open), candidate, timeAfter, timeAfter * candidate.rate + totalFlow, bestSeen);
            }
            return bestSeen;
        }

        private Set<Valve> join(Valve element, Set<Valve> s) {
            var ss = new HashSet<>(s);
            ss.add(element);
            return ss;
        }
    }

    private static Map<String, Integer> findDistances(Map<String, Valve> valves) {
        var indices = new HashMap<String, Integer>();
        for (var entry : valves.entrySet()) {
            indices.put(entry.getKey(), indices.size());
        }
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
