import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day16 {
    private static final int MAX_TICK = 30;

    public static void main(String[] args) throws IOException {
        var input = Arrays.asList("""
            Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
            Valve BB has flow rate=13; tunnels lead to valves CC, AA
            Valve CC has flow rate=2; tunnels lead to valves DD, BB
            Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
            Valve EE has flow rate=3; tunnels lead to valves FF, DD
            Valve FF has flow rate=0; tunnels lead to valves EE, GG
            Valve GG has flow rate=0; tunnels lead to valves FF, HH
            Valve HH has flow rate=22; tunnel leads to valve GG
            Valve II has flow rate=0; tunnels lead to valves AA, JJ
            Valve JJ has flow rate=21; tunnel leads to valve II
            """.split("\n"));
        var input2 = Files.readAllLines(Path.of("input16.txt"));
        input = input2;

        var valves = input.stream()
            .map(Valve::parse)
            .collect(Collectors.toMap(Valve::name, Function.identity()));

        part1(valves);
    }

    private static void part1(Map<String, Valve> valves) {
        var nonEmpty = valves.values()
            .stream()
            .filter(v -> v.rate != 0)
            .map(Valve::name)
            .toList();
        var distances = findDistances(valves);

        var pressure = valves.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().rate));
        var result = recurse(distances, pressure, 0, 0, 0, "AA", nonEmpty);
        System.out.println(result);
    }

    static int recurse(Map<String, Integer> distances, Map<String, Integer> pressures, int pressure, int pressurePerTick, int currentTick, String location, List<String> paths) {
        var max = pressure + (MAX_TICK - currentTick) * pressurePerTick;

        for (var path : paths) {
            var moveAndOpen = distances.get(location + path) + 1;
            if (currentTick + moveAndOpen >= MAX_TICK) continue;

            var tick = currentTick + moveAndOpen;
            var newPressure = pressure + moveAndOpen * pressurePerTick;
            var newPressurePerTick = pressurePerTick + pressures.get(path);
            var candidate = recurse(distances, pressures, newPressure, newPressurePerTick, tick, path, pathsWithoutSome(paths, path));
            if (candidate > max) {
                max = candidate;
            }
        }
        return max;
    }

    private static List<String> pathsWithoutSome(List<String> paths, String path) {
        var result = new ArrayList<>(paths);
        result.remove(path);
        return result;
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
