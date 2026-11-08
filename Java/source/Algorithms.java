import java.util.*;

public final class Algorithms {
    private Algorithms() {}
    // Part 1: Longest Common Subsequence
    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");
        int n = a.length();
        int m = b.length();

        int[][] dp = new int[n + 1][m + 1];

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        StringBuilder reversed = new StringBuilder();
        int i = n;
        int j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                reversed.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("length", dp[n][m]);
        result.put("sequence", reversed.reverse().toString());
        return result;
    }

     // Part 2: Minimum-cost adjacent merges
    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> raw = (List<?>) input.get("sizes");
        int n = raw.size();

        Map<String, Object> result = new LinkedHashMap<>();
        if (n <= 1) {
            result.put("cost", 0L);
            result.put("plan", new ArrayList<List<Integer>>());
            return result;
        }

        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + ((Number) raw.get(i)).longValue();
        }

        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];

        for (int length = 2; length <= n; length++) {
            for (int left = 0; left + length <= n; left++) {
                int right = left + length - 1;
                long intervalSum = prefix[right + 1] - prefix[left];
                long best = Long.MAX_VALUE;
                int bestSplit = left;

                for (int k = left; k < right; k++) {
                    long candidate = dp[left][k] + dp[k + 1][right] + intervalSum;
                    if (candidate < best) {
                        best = candidate;
                        bestSplit = k;
                    }
                }
                dp[left][right] = best;
                split[left][right] = bestSplit;
            }
        }

        List<List<Integer>> plan = new ArrayList<>();
        appendMergePlan(0, n - 1, split, plan);

        result.put("cost", dp[0][n - 1]);
        result.put("plan", plan);
        return result;
    }

    private static void appendMergePlan(int left, int right, int[][] split,
                                        List<List<Integer>> plan) {
        if (left >= right) return;
        int k = split[left][right];
        appendMergePlan(left, k, split, plan);
        appendMergePlan(k + 1, right, split, plan);
        plan.add(Arrays.asList(left, k, right));
    }

    // Part 3: Maximum-weight independent set on a tree
    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();
        List<?> rawWeights = (List<?>) input.get("weights");

        long[] weights = new long[n];
        for (int i = 0; i < n; i++) {
            weights[i] = ((Number) rawWeights.get(i)).longValue();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0) {
            result.put("weight", 0L);
            result.put("selected", new ArrayList<Integer>());
            return result;
        }

        List<?> edges = (List<?>) input.get("edges");
        @SuppressWarnings("unchecked")
        List<Integer>[] graph = new ArrayList[n];
        for (int i = 0; i < n; i++) graph[i] = new ArrayList<>();

        for (Object edgeObject : edges) {
            List<?> edge = (List<?>) edgeObject;
            int u = ((Number) edge.get(0)).intValue();
            int v = ((Number) edge.get(1)).intValue();
            graph[u].add(v);
            graph[v].add(u);
        }

        int[] parent = new int[n];
        Arrays.fill(parent, -2);
        int[] order = new int[n];
        int size = 0;

        int[] stack = new int[n];
        int top = 0;
        stack[top++] = 0;
        parent[0] = -1;

        while (top > 0) {
            int v = stack[--top];
            order[size++] = v;
            for (int next : graph[v]) {
                if (next != parent[v]) {
                    parent[next] = v;
                    stack[top++] = next;
                }
            }
        }

        long[] take = new long[n];
        long[] skip = new long[n];

        for (int index = size - 1; index >= 0; index--) {
            int v = order[index];
            take[v] = weights[v];
            skip[v] = 0L;

            for (int child : graph[v]) {
                if (parent[child] == v) {
                    take[v] += skip[child];
                    skip[v] += Math.max(take[child], skip[child]);
                }
            }
        }

        List<Integer> selected = new ArrayList<>();
        int[] nodeStack = new int[n];
        boolean[] parentTakenStack = new boolean[n];
        top = 0;
        nodeStack[top] = 0;
        parentTakenStack[top] = false;
        top++;

        while (top > 0) {
            top--;
            int v = nodeStack[top];
            boolean parentTaken = parentTakenStack[top];

            boolean choose = !parentTaken && take[v] > skip[v];
            if (choose) selected.add(v);

            for (int child : graph[v]) {
                if (parent[child] == v) {
                    nodeStack[top] = child;
                    parentTakenStack[top] = choose;
                    top++;
                }
            }
        }

        result.put("weight", Math.max(take[0], skip[0]));
        result.put("selected", selected);
        return result;
    }

    private static Map<String, Object> positionsResult(List<Integer> positions) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("positions", positions);
        return result;
    }

    private static List<Integer> emptyPatternPositions(int textLength) {
        List<Integer> positions = new ArrayList<>(textLength + 1);
        for (int i = 0; i <= textLength; i++) positions.add(i);
        return positions;
    }

     // Part 4a: Naive exact search
    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();

        if (m == 0) return positionsResult(emptyPatternPositions(n));

        List<Integer> positions = new ArrayList<>();
        if (m > n) return positionsResult(positions);

        for (int start = 0; start <= n - m; start++) {
            int j = 0;
            while (j < m && text.charAt(start + j) == pattern.charAt(j)) {
                j++;
            }
            if (j == m) positions.add(start);
        }
        return positionsResult(positions);
    }

    // Part 4b: Knuth-Morris-Pratt
    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();

        if (m == 0) return positionsResult(emptyPatternPositions(n));

        List<Integer> positions = new ArrayList<>();
        if (m > n) return positionsResult(positions);

        int[] pi = new int[m];
        for (int i = 1; i < m; i++) {
            int j = pi[i - 1];
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = pi[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) j++;
            pi[i] = j;
        }

        int matched = 0;
        for (int i = 0; i < n; i++) {
            while (matched > 0 && text.charAt(i) != pattern.charAt(matched)) {
                matched = pi[matched - 1];
            }
            if (text.charAt(i) == pattern.charAt(matched)) matched++;

            if (matched == m) {
                positions.add(i - m + 1);
                matched = pi[matched - 1];
            }
        }
        return positionsResult(positions);
    }

    // Part 4c: Rabin-Karp
    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();

        if (m == 0) return positionsResult(emptyPatternPositions(n));

        List<Integer> positions = new ArrayList<>();
        if (m > n) return positionsResult(positions);

        final long base = 31L;
        final long mod = 101L;

        long highestPower = 1L;
        for (int i = 1; i < m; i++) {
            highestPower = (highestPower * base) % mod;
        }

        long patternHash = 0L;
        long windowHash = 0L;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % mod;
            windowHash = (windowHash * base + text.charAt(i)) % mod;
        }

        for (int start = 0; start <= n - m; start++) {
            if (patternHash == windowHash) {
                boolean equal = true;
                for (int j = 0; j < m; j++) {
                    if (text.charAt(start + j) != pattern.charAt(j)) {
                        equal = false;
                        break;
                    }
                }
                if (equal) positions.add(start);
            }

            if (start < n - m) {
                long leading = (text.charAt(start) * highestPower) % mod;
                windowHash = (windowHash - leading) % mod;
                if (windowHash < 0) windowHash += mod;
                windowHash = (windowHash * base + text.charAt(start + m)) % mod;
            }
        }
        return positionsResult(positions);
    }

    // Part 5: Suffix array by prefix doubling + Kasai LCP
    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        int n = text.length();

        Map<String, Object> result = new LinkedHashMap<>();
        if (n == 0) {
            result.put("sa", new ArrayList<Integer>());
            result.put("lcp", new ArrayList<Integer>());
            result.put("repeated", "");
            return result;
        }

        int[] sa = new int[n];
        int[] rank = new int[n];
        int[] nextRank = new int[n];

        for (int i = 0; i < n; i++) {
            sa[i] = i;
            rank[i] = text.charAt(i);
        }

        Integer[] boxedSa = new Integer[n];
        for (int i = 0; i < n; i++) boxedSa[i] = i;

        for (int width = 1; width < n; width <<= 1) {
            final int currentWidth = width;
            final int[] currentRank = rank;

            Arrays.sort(boxedSa, (x, y) -> {
                if (currentRank[x] != currentRank[y]) {
                    return Integer.compare(currentRank[x], currentRank[y]);
                }
                int rx = x + currentWidth < n ? currentRank[x + currentWidth] : -1;
                int ry = y + currentWidth < n ? currentRank[y + currentWidth] : -1;
                return Integer.compare(rx, ry);
            });

            for (int i = 0; i < n; i++) sa[i] = boxedSa[i];

            nextRank[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                int previous = sa[i - 1];
                int current = sa[i];

                int previousSecond = previous + width < n ? rank[previous + width] : -1;
                int currentSecond = current + width < n ? rank[current + width] : -1;

                boolean different = rank[previous] != rank[current]
                        || previousSecond != currentSecond;
                nextRank[current] = nextRank[previous] + (different ? 1 : 0);
            }

            int[] temp = rank;
            rank = nextRank;
            nextRank = temp;

            if (rank[sa[n - 1]] == n - 1) break;
            if (width > n / 2) break;
        }

        int[] inverse = new int[n];
        for (int i = 0; i < n; i++) inverse[sa[i]] = i;

        int[] lcpArray = new int[n];
        int common = 0;
        for (int i = 0; i < n; i++) {
            int order = inverse[i];
            if (order == 0) {
                common = 0;
                continue;
            }

            int previousSuffix = sa[order - 1];
            while (i + common < n && previousSuffix + common < n
                    && text.charAt(i + common) == text.charAt(previousSuffix + common)) {
                common++;
            }
            lcpArray[order] = common;
            if (common > 0) common--;
        }

        List<Integer> saList = new ArrayList<>(n);
        List<Integer> lcpList = new ArrayList<>(n);
        int bestLength = 0;
        int bestStart = 0;

        for (int i = 0; i < n; i++) {
            saList.add(sa[i]);
            lcpList.add(lcpArray[i]);
            if (lcpArray[i] > bestLength) {
                bestLength = lcpArray[i];
                bestStart = sa[i];
            }
        }

        result.put("sa", saList);
        result.put("lcp", lcpList);
        result.put("repeated", text.substring(bestStart, bestStart + bestLength));
        return result;
    }

    // Part 6: Aho-Corasick with reverse failure-link visit propagation
    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> rawPatterns = (List<?>) input.get("patterns");
        List<?> rawTexts = (List<?>) input.get("texts");

        int patternCount = rawPatterns.size();
        int textCount = rawTexts.size();

        Map<String, Object> result = new LinkedHashMap<>();
        List<List<Long>> allCounts = new ArrayList<>(textCount);

        if (patternCount == 0) {
            for (int t = 0; t < textCount; t++) {
                allCounts.add(new ArrayList<Long>());
            }
            result.put("counts", allCounts);
            return result;
        }

        final int alphabet = 95;
        int totalLength = 0;
        for (Object p : rawPatterns) totalLength += ((String) p).length();
        int maxStates = totalLength + 1;

        int[][] next = new int[maxStates][alphabet];
        int[] fail = new int[maxStates];
        int[] bfsOrder = new int[maxStates];
        int states = 1;

        int[] terminalState = new int[patternCount];

        for (int p = 0; p < patternCount; p++) {
            String pattern = (String) rawPatterns.get(p);
            int state = 0;
            for (int i = 0; i < pattern.length(); i++) {
                int c = pattern.charAt(i) - 32;
                if (next[state][c] == 0) {
                    next[state][c] = states++;
                }
                state = next[state][c];
            }
            terminalState[p] = state;
        }

        int head = 0;
        int tail = 0;
        bfsOrder[tail++] = 0;

        while (head < tail) {
            int state = bfsOrder[head++];

            for (int c = 0; c < alphabet; c++) {
                int child = next[state][c];
                if (child != 0) {
                    fail[child] = (state == 0) ? 0 : next[fail[state]][c];
                    bfsOrder[tail++] = child;
                } else if (state != 0) {
                    next[state][c] = next[fail[state]][c];
                }
            }
        }

        for (Object textObject : rawTexts) {
            String text = (String) textObject;
            long[] visits = new long[states];
            int state = 0;

            for (int i = 0; i < text.length(); i++) {
                int c = text.charAt(i) - 32;
                state = next[state][c];
                visits[state]++;
            }

            for (int i = tail - 1; i > 0; i--) {
                int v = bfsOrder[i];
                visits[fail[v]] += visits[v];
            }

            List<Long> row = new ArrayList<>(patternCount);
            for (int p = 0; p < patternCount; p++) {
                row.add(visits[terminalState[p]]);
            }
            allCounts.add(row);
        }

        result.put("counts", allCounts);
        return result;
    }
}
