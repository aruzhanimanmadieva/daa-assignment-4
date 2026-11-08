# Assignment 4 — Dynamic Programming and String Processing

## 1. Environment and Commands

### Environment

- Language: Java
- Java version: 25.0.1
- Operating system: Windows 11
- CPU: 13th Gen Intel(R) Core(TM) i5-13420H
- RAM: 15.65 GB
- Architecture: amd64
- Available processors: 12
- Random benchmark seed: 42

### Build, Run and Test Commands

The following commands were used from the repository root:

```bash
python tools/run.py --setup
python tools/run.py --check
python tools/run.py --input tests/student/input.jsonl
python tools/run.py --input tests/student/edge_cases.jsonl
```

The setup command checked compilation and startup.

The public check passed all 44 public tests and the repeated mixed-batch state-reset test.

The student test files were also executed separately.

The benchmark was run using the Java Benchmark class. Compilation was excluded from the measured time. Two warm-up runs and five measured runs were used for each case.

The final commit URL must be recorded in Moodle after pushing the completed project.

---

## 2. Algorithm Explanations

### 2.1 Longest Common Subsequence (LCS)

**State and invariant**

I use a two-dimensional DP table `dp`.

`dp[i][j]` stores the length of the longest common subsequence of the first `i` characters of string A and the first `j` characters of string B.

The indices `i` and `j` represent prefix lengths.

**Transition**

If the current characters are equal:

`dp[i][j] = dp[i-1][j-1] + 1`

Otherwise:

`dp[i][j] = max(dp[i-1][j], dp[i][j-1])`

The base cases are `dp[0][j] = 0` and `dp[i][0] = 0`.

**Processing order**

The table is filled from left to right and from top to bottom. Every required previous cell is already calculated.

**Correctness**

Every common subsequence either uses both current characters or does not use at least one of them. The transition checks these possibilities and keeps the best length.

**Reconstruction and ties**

The algorithm starts at `dp[n][m]` and moves backwards.

When characters are equal, they are added to the answer. Otherwise, it moves to the cell with the larger value. If both values are equal, it moves upwards.

This produces one valid LCS. Other optimal answers may also exist.

**Complexity**

- Time: O(n × m)
- Memory: O(n × m)
- The output sequence has length at most min(n, m).

**Boundary test**

Empty strings must return an empty subsequence with length zero. This checks the DP base cases and reconstruction boundaries.

---

### 2.2 Ordered Adjacent-Block Merging

**State and invariant**

I use interval DP.

`dp[i][j]` stores the minimum total cost of merging the interval from index `i` to index `j` into one block.

The prefix sum array allows the sum of an interval to be calculated efficiently.

**Transition**

For each possible split position `k`:

`dp[i][j] = min(dp[i][k] + dp[k+1][j] + sum(i,j))`

The split table stores the position that gives the minimum cost.

For intervals with zero or one element, the cost is zero.

**Processing order**

Intervals are processed by increasing length. Therefore, both smaller intervals needed for a transition are already calculated.

**Correctness**

The last merge of an interval must combine two adjacent blocks. These blocks correspond to two smaller intervals separated by some split position. Checking every possible split finds the minimum possible cost.

**Reconstruction and ties**

The split table is used recursively. The left interval is reconstructed first, then the right interval, and finally the merge operation for the current interval is added.

If several splits have the same minimum cost, the implementation keeps one of them.

**Complexity**

- Time: O(n³)
- Memory: O(n²)
- Prefix sums use `long` to support large sums.

**Boundary test**

An empty array and an array with one element must have zero cost and no merge operations. This checks the base cases.

---

### 2.3 Maximum-Weight Independent Set on a Tree

**State and invariant**

For each vertex `v`, I calculate two values:

- `take[v]`: the maximum weight when vertex `v` is selected.
- `skip[v]`: the maximum weight when vertex `v` is not selected.

**Transition**

When selecting a vertex, none of its children can be selected:

`take[v] = weight[v] + sum(skip[child])`

When not selecting a vertex, each child can be selected or skipped:

`skip[v] = sum(max(take[child], skip[child]))`

The algorithm uses a parent array and an order list to process the tree.

**Processing order**

The tree is traversed iteratively. Then the vertices are processed in reverse traversal order. This means children are processed before their parents.

**Correctness**

For every vertex, there are only two possibilities: select it or do not select it. The transitions consider all valid choices while respecting the rule that two connected vertices cannot both be selected.

**Reconstruction and ties**

The algorithm reconstructs the selected vertices using the stored DP values.

A vertex is selected when `take[v] > skip[v]`. If both values are equal, it is skipped.

This returns one optimal independent set.

**Complexity**

- Time: O(V + E)
- Memory: O(V + E)
- For a tree, E = V - 1.

The iterative traversal avoids recursive tree traversal.

**Boundary test**

An empty tree must return weight zero and an empty selected-vertex list. This checks empty input handling.

---

### 2.4 Naive String Search

**State and invariant**

The algorithm checks every possible starting position in the text.

The index `i` represents the current starting position, and `j` represents the character being compared inside the pattern.

**Transition**

Characters are compared one by one. If all pattern characters match, the current position is added to the result.

The next starting position is always checked, so overlapping matches are supported.

An empty pattern matches all boundaries from zero to the text length.

**Correctness**

Every possible starting position is checked. A position is added only when all pattern characters match.

Therefore, all matches are found.

**Complexity**

- Worst-case time: O(n × m)
- Memory: O(1) auxiliary memory, excluding the output list.
- Output size can be O(n).

**Boundary test**

When the pattern is longer than the text, the result must be empty. This checks the search range.

---

### 2.5 Knuth-Morris-Pratt (KMP)

**State and invariant**

KMP uses the prefix function `pi`.

`pi[i]` stores the length of the longest proper prefix of the pattern that is also a suffix ending at index `i`.

The variable `matched` stores the number of pattern characters currently matched.

**Transition**

When characters match, `matched` increases.

When they do not match, the algorithm uses the prefix function to find the next possible matched prefix without starting the search again.

After a full match, it sets:

`matched = pi[matched - 1]`

This allows overlapping matches.

**Processing order**

The prefix function is calculated first. Then the text is processed from left to right.

All prefix information is ready before the search starts.

**Correctness**

The prefix function stores the longest valid prefix that can still match after a mismatch. Therefore, KMP does not skip a possible match.

After a full match, using the previous prefix value keeps possible overlapping matches.

**Boundary test**

The text `aaaaa` and pattern `aaa` must return positions 0, 1 and 2. This checks overlapping matches.

An empty pattern returns all text boundaries.

**Complexity**

- Time: O(n + m)
- Memory: O(m), excluding the output list.
- Output size can be O(n).

---

### 2.6 Exact Rabin-Karp Search

**State and invariant**

Rabin-Karp uses a rolling hash.

The implementation uses:

- Base: 31
- Modulus: 101

The algorithm maintains the hash of the current text window.

**Transition**

The first window hash is calculated. When the window moves, the old leading character is removed, the hash is updated, and the new character is added.

If the pattern hash and window hash are equal, the algorithm compares the actual characters.

This verification is important because two different strings can have the same hash.

**Correctness**

A hash match is only a candidate. The algorithm adds a position only after checking every character.

Therefore, hash collisions cannot create false matches.

**Complexity**

- Expected time is often close to O(n + m) when there are few hash collisions.
- Worst-case time: O(n × m), because many windows may have matching hashes and require character verification.
- Auxiliary memory: O(1), excluding the output list.
-The rolling hash is updated in O(1) time for each window. If there are c hash-matching candidate windows, character verification takes O(c × m) time in total. Therefore, the worst-case time is O(n × m). The auxiliary space is O(1), excluding the output list.
The output size can be O(n).

**Collision test**

A real collision was checked:

- Pattern: `aa`
- Different string: `di`
- Both strings have hash 74 using base 31 and modulus 101.

The test uses:

Text: `diaa`

Pattern: `aa`

Expected positions: `[2]`

The first window has the same hash but different characters. The algorithm must reject it after character verification. The second window is a real match.

This test checks that the implementation does not report false matches.

---

### 2.7 Suffix Array and Kasai LCP

#### Suffix Array

**State and invariant**

The suffix array stores all starting positions of suffixes in lexicographical order.

The initial ranks represent individual characters.

**Transition**

The implementation uses prefix doubling.

At every step, suffixes are sorted using two rank values:

- The rank of the first part.
- The rank of the second part.

The compared prefix length doubles after each iteration.

New ranks are assigned after sorting. The process continues until all suffixes have different ranks or the required length is covered.

**Processing order**

Each iteration uses ranks calculated in the previous iteration. Therefore, the ranks are ready before the next sorting step.

The implementation uses a boxed `Integer[]` suffix array and `Arrays.sort` with a comparator.

**Correctness**

At each doubling step, suffixes are ordered by longer prefixes. When the process finishes, the order represents the lexicographical order of complete suffixes.

**Complexity**

- Time: O(n log² n) with comparator-based sorting at each doubling step.
- Memory: O(n) for the arrays and rank information, excluding sorting implementation overhead.

#### Kasai LCP

**State and invariant**

The LCP array stores the longest common prefix between neighboring suffixes in suffix-array order.

The value at each position represents the common prefix length with the previous suffix in the sorted order.

**Transition**

Kasai's algorithm uses the inverse suffix-array position to find the previous suffix.

It compares characters while they are equal. The common prefix length is stored, and the carried length is reduced by one before moving to the next text position.

**Correctness**

The previous LCP value helps avoid repeating comparisons. The algorithm calculates the exact common prefix for every neighboring suffix pair.

The maximum LCP value gives the length of the longest repeated substring.

**Complexity**

- Time: O(n)
- Memory: O(n)

**Boundary tests**

An empty string must return empty suffix and LCP arrays. A one-character string must return one suffix and an LCP value of zero.

These tests check array sizes and boundary indexing.

---

### 2.8 Aho-Corasick

**State and invariant**

Aho-Corasick uses a trie with failure links.

Each state represents a prefix of one or more patterns.

The implementation uses a transition table with 95 possible characters.

**Transition**

First, all patterns are inserted into the trie.

Then BFS calculates failure links. Missing transitions are completed using the failure transitions.

During text processing, the current state follows the transition table.

The algorithm counts visits to states. After processing a text, the counts are propagated in reverse BFS order to failure-link states.

**Processing order**

BFS processes states in increasing depth. A failure link always points to a state at a smaller depth.

Reverse BFS order ensures that a state's visits are propagated after its deeper descendants have been processed.

**Correctness**

When a pattern ends at a state, the corresponding terminal state receives the number of visits to that state and its descendants through failure links.

This counts patterns that are suffixes of longer matched patterns.
Failure links allow the algorithm to count patterns that are suffixes of longer matched patterns. Reverse BFS propagation adds each state's visits to its failure state, so suffix patterns receive their correct counts. Duplicate patterns are handled separately through their stored terminal states.

**Duplicates and reset**

Each original pattern stores its terminal state. Duplicate patterns can have the same terminal state, but each original pattern receives its own output count.

The visit array is reset for each text, so counts from one text do not affect another text.

**Complexity**

Let P be the total length of all patterns, T the total length of the texts, and S the number of trie states.

- Building the trie: O(P).
- Building failure links and transitions: O(S × 95).
- Processing texts: O(T).
- Propagating counts: O(S).
- Memory: O(S × 95), plus pattern and visit arrays.

**Boundary test**

An empty pattern dictionary must return an empty count list. Duplicate patterns and suffix patterns are also tested.

---

## 3. Own Tests

The following student test files were used.

| Command | Input or fixture | Expected result | Actual result | What it checks |
|---|---|---|---|---|
| `python tools/run.py --input tests/student/input.jsonl` | `input.jsonl` | Stored in `expected.jsonl` | Stored in `actual.jsonl` | Eight basic custom cases |
| `python tools/run.py --input tests/student/edge_cases.jsonl` | `edge_cases.jsonl` | Stored in `edge_expected.jsonl` | Stored in `edge_actual.jsonl` | Boundary cases and special inputs |
| `python tools/run.py --input tests/student/tree_stress.jsonl` | `tree_stress.jsonl` | Weight 50000, 50000 selected vertices | Verified | Large tree and iterative traversal |
| `python tools/run.py --input tests/student/lcs_stress.jsonl` | `lcs_stress.jsonl` | LCS length 500 | Verified | Larger DP input and reconstruction |
| `python tools/run.py --input tests/student/merge_stress.jsonl` | `merge_stress.jsonl` | Cost 1544, 199 operations | Verified | Merge reconstruction and total cost |
| `python tools/run.py --input tests/student/search_stress.jsonl` | `search_stress.jsonl` | Naive 2901, KMP 199001, RK 9901 matches | Verified | Large search inputs and overlapping matches |
| `python tools/run.py --input tests/student/suffix_stress.jsonl` | `suffix_stress.jsonl` | 50000 suffixes and 50000 LCP entries | Verified | Large suffix array and LCP |
| `python tools/run.py --input tests/student/aho_stress.jsonl` | `aho_stress.jsonl` | 100 pattern counts; first and last are 1 | Verified | Duplicate patterns and large trie |

The edge tests also include empty inputs, one-element inputs, negative and zero values, large numbers, overlapping matches, repeated characters, duplicate patterns, and multiple texts.

The Rabin-Karp collision test uses `di` and `aa`, which have the same hash but different characters. The expected result for text `diaa` and pattern `aa` is `[2]`.

The edge test output was compared with the expected output using `diff -u`. No differences were reported.

Tree stress: maximum weight = 50000; selected vertices = 50000.
LCS stress: LCS length = 500.
Merge stress: cost = 1544; operations = 199.
Search stress: matches = 2901 / 199001 / 9901.
Suffix stress: 50000 suffixes and 50000 LCP values.
Aho stress: 100 pattern counts; first and last counts = 1.
---

## 4. Search Algorithm Benchmark

### 4.1 Benchmark Setup

The benchmark compares Naive, KMP and Rabin-Karp using the same inputs.

The benchmark uses:

- Random seed: 42
- Text sizes: 500, 1500 and 3000
- Random pattern length: 16
- Repetitive pattern length: 100
- Warm-up runs: 2
- Measured runs: 5
- Time unit: milliseconds
- Algorithms: Naive, KMP and Rabin-Karp

-Random text was generated using seed 42.
Random pattern length was 16 and it was copied from the middle of the text.
Repetitive text and pattern contained only a.
The same generated input was passed to all three algorithms.
Correctness was checked before measuring execution time.
The random pattern was taken from the middle of the generated random text. This creates one expected match.

The repetitive text and pattern use the character `a`. This creates many overlapping matches.

Correctness was checked by comparing the algorithm outputs before timing.

Compilation and input construction were excluded from the measured time. The measured scope includes algorithm execution and result creation.

### 4.2 Random Inputs

| n | m | Algorithm | Run 1 | Run 2 | Run 3 | Run 4 | Run 5 | Median |
|---:|---:|---|---:|---:|---:|---:|---:|---:|
| 500 | 16 | Naive | 0.056 | 0.054 | 0.056 | 0.057 | 0.056 | 0.056 |
| 500 | 16 | KMP | 0.095 | 0.037 | 0.034 | 0.032 | 0.032 | 0.034 |
| 500 | 16 | RK | 0.170 | 0.872 | 0.161 | 0.250 | 0.128 | 0.170 |
| 1500 | 16 | Naive | 0.028 | 0.028 | 0.028 | 0.028 | 0.029 | 0.028 |
| 1500 | 16 | KMP | 0.065 | 0.051 | 0.051 | 0.050 | 0.050 | 0.051 |
| 1500 | 16 | RK | 0.126 | 0.137 | 0.196 | 0.096 | 0.149 | 0.137 |
| 3000 | 16 | Naive | 0.108 | 0.046 | 0.045 | 0.063 | 0.046 | 0.046 |
| 3000 | 16 | KMP | 0.419 | 0.265 | 0.160 | 0.141 | 0.139 | 0.160 |
| 3000 | 16 | RK | 0.250 | 0.236 | 0.273 | 0.269 | 0.240 | 0.250 |

Each random case produced one match.

### 4.3 Repetitive Inputs

| n | m | Algorithm | Run 1 | Run 2 | Run 3 | Run 4 | Run 5 | Median |
|---:|---:|---|---:|---:|---:|---:|---:|---:|
| 500 | 100 | Naive | 1.456 | 1.300 | 1.222 | 1.090 | 1.173 | 1.222 |
| 500 | 100 | KMP | 0.419 | 0.153 | 0.124 | 0.163 | 0.120 | 0.153 |
| 500 | 100 | RK | 1.553 | 1.384 | 1.279 | 0.701 | 1.087 | 1.279 |
| 1500 | 100 | Naive | 2.478 | 2.441 | 2.479 | 1.974 | 1.926 | 2.441 |
| 1500 | 100 | KMP | 0.155 | 0.180 | 0.162 | 0.195 | 0.176 | 0.176 |
| 1500 | 100 | RK | 0.654 | 0.410 | 0.416 | 0.427 | 0.391 | 0.416 |
| 3000 | 100 | Naive | 1.075 | 0.700 | 0.612 | 0.731 | 0.730 | 0.730 |
| 3000 | 100 | KMP | 0.187 | 0.125 | 0.125 | 0.137 | 0.126 | 0.126 |
| 3000 | 100 | RK | 0.873 | 0.687 | 0.754 | 0.811 | 0.731 | 0.754 |

The numbers of matches were:

- n = 500: 401 matches
- n = 1500: 1401 matches
- n = 3000: 2901 matches

### 4.4 Observations

KMP performed well on repetitive inputs because it avoids repeating comparisons after a mismatch.

Naive search performs more character comparisons when many windows have long matching prefixes.

Rabin-Karp uses a small modulus of 101. This makes hash collisions more likely than with a larger modulus. Character verification keeps the result correct, but collisions can increase running time.

The results are measurements from one computer and one Java runtime. Some times are not strictly increasing when the input becomes larger. JVM warm-up, garbage collection, operating-system activity and measurement noise can affect short runs.

Therefore, these measurements do not prove asymptotic complexity. The theoretical complexity analysis is given in the algorithm sections.

The collision test confirms that Rabin-Karp checks actual characters after a hash match.

---

## 5. Sources and AI Assistance


The assignment specification, repository documentation and course materials were used as the main sources.

AI assistance was used for algorithm explanations, debugging and code review.

I implemented the algorithms and performed the tests myself. I also checked the test results, benchmark outputs and final implementation.