import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Day25 {
    public static void main(String[] args) throws IOException {
        var input = Arrays.asList("""
            1=-0-2
            12111
            2=0=
            21
            2=01
            111
            20012
            112
            1=-1=
            1-12
            12
            1=
            122""".split("\n"));
        var input2 = Files.readAllLines(Path.of("input25.txt"));
        input = input2;

        var sum = input.stream()
            .mapToLong(Day25::decodeSnafu)
            .sum();

        System.out.println(sum);
        System.out.println(encodeToSnafu(sum));
    }

    private static long decodeSnafu(String snafu) {
        var result = 0L;
        for (int i = 0; i < snafu.length(); i++) {
            var digitValue = Math.pow(5L, snafu.length() - 1 - i);
            result += digitValue * switch (snafu.charAt(i)) {
                case '0' -> 0;
                case '1' -> 1;
                case '2' -> 2;
                case '-' -> -1;
                case '=' -> -2;
                default -> throw new IllegalArgumentException("Not valid snafu");
            };
        }
        return result;
    }

    private static String encodeToSnafu(long dec) {
        var builder = new StringBuilder();
        while (dec > 0) {
            int remainder = (int) (dec % 5);
            switch (remainder) {
                case 0 -> builder.append('0');
                case 1 -> builder.append('1');
                case 2 -> builder.append('2');
                case 3 -> {
                    builder.append('=');
                    dec += 2;
                }
                case 4 -> {
                    builder.append('-');
                    dec++;
                }
                default -> throw new IllegalStateException("Mod cannot be more than 4");
            }
            dec /= 5;
        }
        return builder.reverse().toString();
    }
}
