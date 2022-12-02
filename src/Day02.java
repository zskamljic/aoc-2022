import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Day02 {
    public static void main(String[] args) throws IOException {
        var plays = Files.readAllLines(Path.of("input02.txt"))
            .stream()
            .map(Play::parse)
            .toList();

        part1(plays);
        part2(plays);
    }

    private static void part1(List<Play> plays) {
        var sum = plays.stream()
            .mapToInt(Play::part1)
            .sum();

        System.out.println(sum);
    }

    private static void part2(List<Play> plays) {
        var sum = plays.stream()
            .mapToInt(Play::part2)
            .sum();

        System.out.println(sum);
    }

    enum Selection {
        ROCK,
        PAPER,
        SCISSORS;

        static Selection fromLetter(String letter) {
            return switch (letter) {
                case "A", "X" -> ROCK;
                case "B", "Y" -> PAPER;
                case "C", "Z" -> SCISSORS;
                default -> throw new IllegalArgumentException("Unable to parse letter " + letter);
            };
        }

        int score() {
            return switch (this) {
                case ROCK -> 1;
                case PAPER -> 2;
                case SCISSORS -> 3;
            };
        }

        Selection getForOutcome(Selection other, Outcome outcome) {
            return switch (outcome) {
                case DRAW -> other;
                case WIN -> switch (other) {
                    case ROCK -> PAPER;
                    case PAPER -> SCISSORS;
                    case SCISSORS -> ROCK;
                };
                case LOSE -> switch (other) {
                    case ROCK -> SCISSORS;
                    case PAPER -> ROCK;
                    case SCISSORS -> PAPER;
                };
            };
        }

        boolean winsAgainst(Selection other) {
            return this == ROCK && other == SCISSORS ||
                this == PAPER && other == ROCK ||
                this == SCISSORS && other == PAPER;
        }

        int scoreAgainst(Selection other) {
            if (this == other) return 3 + score();
            if (winsAgainst(other)) return 6 + score();

            return score();
        }
    }

    enum Outcome {
        LOSE, DRAW, WIN;

        static Outcome fromString(String string) {
            return switch (string) {
                case "X" -> LOSE;
                case "Y" -> DRAW;
                case "Z" -> WIN;
                default -> throw new IllegalArgumentException("Unable to parse letter " + string);
            };
        }
    }

    record Play(Selection opponent, String selection) {
        static Play parse(String play) {
            var parts = play.split(" ");
            return new Play(Selection.fromLetter(parts[0]), parts[1]);
        }

        int part1() {
            return Selection.fromLetter(selection).scoreAgainst(opponent);
        }

        int part2() {
            var selection = opponent.getForOutcome(opponent, Outcome.fromString(selection()));
            return selection.scoreAgainst(opponent);
        }
    }
}
