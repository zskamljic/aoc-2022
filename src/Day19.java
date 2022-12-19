import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Day19 {
    private static final Pattern BLUEPRINT_REGEX = Pattern.compile("Blueprint (\\d+): Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore. Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian.");

    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input19.txt"));

        var blueprints = input.stream()
            .map(Blueprint::parse)
            .toList();

        part1(blueprints);
        part2(blueprints);
    }

    private static void part1(List<Blueprint> blueprints) {
        var score = blueprints.stream()
            .mapToLong(b -> b.simulate(24) * b.id)
            .sum();
        System.out.println(score);
    }

    private static void part2(List<Blueprint> blueprints) {
        var score = blueprints.stream()
            .limit(3)
            .mapToLong(b -> b.simulate(32))
            .reduce((a, b) -> a * b)
            .orElseThrow();
        System.out.println(score);
    }

    record Blueprint(int id, int[] ore, int[] clay, int[] obsidian, int[] geode) {
        static Blueprint parse(String line) {
            var matcher = BLUEPRINT_REGEX.matcher(line);
            if (!matcher.matches()) throw new IllegalArgumentException("Parsing failed");

            return new Blueprint(
                Integer.parseInt(matcher.group(1)),
                new int[]{Integer.parseInt(matcher.group(2)), 0, 0, 0}, // Ore
                new int[]{Integer.parseInt(matcher.group(3)), 0, 0, 0}, // Clay
                new int[]{Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)), 0, 0}, // Obsidian
                new int[]{Integer.parseInt(matcher.group(6)), 0, Integer.parseInt(matcher.group(7)), 0} // Geode
            );
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Blueprint bp && id == bp.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return "%d, ore: %s, clay: %s, obsidian: %s, geode: %s".formatted(id, Arrays.toString(ore), Arrays.toString(clay), Arrays.toString(obsidian), Arrays.toString(geode));
        }

        public long simulate(int time) {
            var simulation = new Simulation(this);
            return simulation.run(time);
        }
    }

    static class Simulation {
        private final Blueprint blueprint;
        private int maxGeodes;

        public Simulation(Blueprint blueprint) {
            this.blueprint = blueprint;
        }

        public long run(int time) {
            var maxRobots = new int[4];
            Arrays.fill(maxRobots, Integer.MAX_VALUE);
            for (int i = 0; i < 3; i++) {
                maxRobots[i] = IntStream.of(blueprint.ore[i], blueprint.clay[i], blueprint.obsidian[i], blueprint.geode[i]).max().orElseThrow();
            }

            recurse(new State(), time, maxRobots);
            return maxGeodes;
        }

        private void recurse(State state, int maxTime, int[] maxRobots) {
            var hasRecursed = false;
            for (int currentResource = 0; currentResource < 4; currentResource++) {
                if (state.robots[currentResource] == maxRobots[currentResource]) continue;

                var recipe = selectRecipe(currentResource);
                var waitTime = getWaitTime(state, maxTime, recipe);
                var timeAfter = state.time + waitTime + 1;
                if (timeAfter >= maxTime) continue;

                var newOres = new int[4];
                var newRobots = new int[4];
                updateState(state, recipe, waitTime, currentResource, newOres, newRobots);

                var remaining = maxTime - timeAfter;
                if (((remaining - 1) * remaining) / 2 + newOres[3] + remaining * newRobots[3] < maxGeodes) continue;

                hasRecursed = true;
                recurse(new State(newOres, newRobots, timeAfter), maxTime, maxRobots);
            }
            if (!hasRecursed) {
                maxGeodes = Math.max(
                    maxGeodes, state.ores[3] + state.robots[3] * (maxTime - state.time)
                );
            }
        }

        private void updateState(State state, int[] recipe, int waitTime, int currentResource, int[] newOres, int[] newRobots) {
            for (int ore = 0; ore < 4; ore++) {
                newOres[ore] = state.ores[ore] + state.robots[ore] * (waitTime + 1) - recipe[ore];
                newRobots[ore] = state.robots[ore] + (ore == currentResource ? 1 : 0);
            }
        }

        private static int getWaitTime(State state, int maxTime, int[] recipe) {
            return IntStream.range(0, 3)
                .filter(oreType -> recipe[oreType] != 0)
                .mapMulti((oreType, consumer) -> {
                    if (recipe[oreType] <= state.ores[oreType]) {
                        consumer.accept(0);
                    } else if (state.robots[oreType] == 0) {
                        consumer.accept(maxTime + 1);
                    } else {
                        var output = (recipe[oreType] - state.ores[oreType] + state.robots[oreType] - 1) / state.robots[oreType];
                        consumer.accept(output);
                    }
                })
                .max()
                .orElseThrow();
        }

        private int[] selectRecipe(int i) {
            return switch (i) {
                case 0 -> blueprint.ore;
                case 1 -> blueprint.clay;
                case 2 -> blueprint.obsidian;
                case 3 -> blueprint.geode;
                default -> throw new IllegalArgumentException();
            };
        }
    }

    record State(int[] ores, int[] robots, int time) { // NOSONAR
        State() {
            this(new int[4], new int[]{1, 0, 0, 0}, 0);
        }
    }
}
