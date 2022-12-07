import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Day07 {
    public static void main(String[] args) throws IOException {
        var input = Files.readAllLines(Path.of("input07.txt"));

        var cli = new Cli();
        for (var line : input) {
            cli.handle(line);
        }
        part1(cli);
        part2(cli);
    }

    private static void part1(Cli cli) {
        var smallDirectories = cli.directories()
            .stream()
            .filter(directory -> directory.size() < 100_000)
            .mapToInt(Node::size)
            .sum();
        System.out.println(smallDirectories);
    }

    private static void part2(Cli cli) {
        var totalUsed = cli.root.size();
        var unused = 70_000_000 - totalUsed;
        var requiredSpace = 30_000_000;
        var minimalDeletionSize = requiredSpace - unused;

        var smallestValidDirectory = cli.directories()
            .stream()
            .filter(directory -> directory.size() > minimalDeletionSize)
            .min(Comparator.comparing(Directory::size))
            .orElseThrow();
        System.out.println(smallestValidDirectory.size());
    }

    static class Cli {
        private boolean expectingOutput = false;
        private Deque<Directory> path = new ArrayDeque<>();
        private Directory root = new Directory("/");
        private Directory current = root;

        public void handle(String line) {
            if (line.startsWith("$")) {
                expectingOutput = false;
            }
            if (expectingOutput) {
                handleOutput(line);
            } else {
                handleCommand(line);
            }
        }

        private void handleOutput(String line) {
            var fileInfo = line.split(" ");
            if ("dir".equals(fileInfo[0])) {
                current.nodes.put(fileInfo[1], new Directory(fileInfo[1]));
            } else {
                current.nodes.put(fileInfo[1], new File(fileInfo[1], Integer.parseInt(fileInfo[0])));
            }
        }

        private void handleCommand(String line) {
            var command = line.substring(2).split(" "); // Strip "$ "
            if (command[0].equals("cd")) {
                if (command[1].equals("..")) {
                    current = path.pop();
                } else if (command[1].equals("/")) {
                    path.clear();
                    current = root;
                } else {
                    path.push(current);
                    current = current.getDirectory(command[1]);
                }
            } else if (command[0].equals("ls")) {
                expectingOutput = true;
            } else {
                throw new IllegalArgumentException("Unknown command " + line);
            }
        }

        public List<Directory> directories() {
            var directories = new ArrayList<Directory>();
            var queue = new ArrayDeque<Directory>();
            queue.push(root);
            while (!queue.isEmpty()) {
                var directory = queue.poll();
                directory.nodes()
                    .values()
                    .stream()
                    .filter(Directory.class::isInstance)
                    .map(Directory.class::cast)
                    .forEach(queue::push);
                directories.add(directory);
            }
            return directories;
        }
    }

    sealed interface Node {
        int size();
    }

    record Directory(String name, Map<String, Node> nodes, AtomicInteger directorySize) implements Node {
        Directory(String name) {
            this(name, new HashMap<>(), new AtomicInteger());
        }

        public Directory getDirectory(String name) {
            return (Directory) nodes.get(name);
        }

        @Override
        public int size() {
            if (directorySize.get() == 0) {
                var size = nodes.values()
                    .stream()
                    .mapToInt(Node::size)
                    .sum();
                directorySize.set(size);
            }

            return directorySize.get();
        }
    }

    record File(String name, int size) implements Node {
    }
}
