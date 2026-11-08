import java.util.*;

public class Benchmark {

    private static final Random RANDOM = new Random(42);
    private static final int WARM_UP = 2;
    private static final int RUNS = 5;

    private static Map<String, Object> createInput(
            String text, String pattern) {

        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("pattern", pattern);

        return data;
    }

    private static String randomString(int length) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            result.append((char) ('a' + RANDOM.nextInt(26)));
        }

        return result.toString();
    }

    private static Map<String, Object> execute(
            String algorithm, Map<String, Object> data) {

        switch (algorithm) {
            case "Naive":
                return Algorithms.naive(data);

            case "KMP":
                return Algorithms.kmp(data);

            default:
                return Algorithms.rk(data);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> getPositions(
            String algorithm, Map<String, Object> data) {

        return (List<Integer>) execute(algorithm, data).get("positions");
    }

    private static long measure(
            String algorithm, Map<String, Object> data) {

        long start = System.nanoTime();

        execute(algorithm, data);

        return System.nanoTime() - start;
    }

    private static long median(long[] values) {
        long[] copy = values.clone();
        Arrays.sort(copy);

        return copy[copy.length / 2];
    }

    private static void benchmark(
            String type,
            int n,
            int m,
            String text,
            String pattern) {

        String[] algorithms = {
                "Naive",
                "KMP",
                "Rabin-Karp"
        };

        Map<String, Object> data = createInput(text, pattern);

        // Verify correctness before timing.
        List<Integer> expected = getPositions("Naive", data);

        for (String algorithm : algorithms) {
            List<Integer> actual = getPositions(algorithm, data);

            if (!expected.equals(actual)) {
                throw new RuntimeException(
                        "Incorrect results from " + algorithm);
            }
        }

        System.out.println("\nDataset: " + type);
        System.out.println("n = " + n + ", m = " + m);
        System.out.println("Correctness: PASS");
        System.out.println("Matches: " + expected.size());

        for (String algorithm : algorithms) {
            for (int i = 0; i < WARM_UP; i++) {
                measure(algorithm, data);
            }
        }

        for (String algorithm : algorithms) {

            long[] times = new long[RUNS];

            for (int i = 0; i < RUNS; i++) {
                times[i] = measure(algorithm, data);
            }

            System.out.print(algorithm + " | Runs (ms): ");

            for (long time : times) {
                System.out.printf(
                        Locale.US, "%.3f ", time / 1_000_000.0);
            }

            System.out.printf(
                    Locale.US,
                    "| Median: %.3f ms%n",
                    median(times) / 1_000_000.0
            );
        }
    }

    public static void main(String[] args) {

        System.out.println("Java version: "
                + System.getProperty("java.version"));

        System.out.println("OS: "
                + System.getProperty("os.name"));

        System.out.println("Architecture: "
                + System.getProperty("os.arch"));

        System.out.println("Available processors: "
                + Runtime.getRuntime().availableProcessors());

        System.out.println("Random seed: 42");
        System.out.println("Warm-up runs: " + WARM_UP);
        System.out.println("Measured runs: " + RUNS);

        int[] sizes = {500, 1500, 3000};

        for (int n : sizes) {

            String randomText = randomString(n);
            String randomPattern =
                    randomText.substring(n / 2, n / 2 + 16);

            benchmark(
                    "Random",
                    n,
                    randomPattern.length(),
                    randomText,
                    randomPattern
            );

            String repetitiveText = "a".repeat(n);
            String repetitivePattern = "a".repeat(100);

            benchmark(
                    "Repetitive",
                    n,
                    repetitivePattern.length(),
                    repetitiveText,
                    repetitivePattern
            );
        }
    }
}