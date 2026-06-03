package build_optimization;

import java.util.*;

public class MonorepoBuildEngine {

    // Three-Color Marking Schema Constants
    static final int WHITE = 0;
    static final int GRAY = 1;
    static final int BLACK = 2;

    public static class TopoResult {
        public List<String> order; // Populated if graph is a valid DAG
        public List<String> cycle; // Populated if a circular deadlock is identified

        public TopoResult(List<String> order, List<String> cycle) {
            this.order = order;
            this.cycle = cycle;
        }
    }

    /**
     * Executes a DFS-based topological sort over the monorepo build graph.
     * @param adj The adjacency list mapping components to their dependencies
     * @return TopoResult containing either a valid compilation order or cycle details
     */
    public static TopoResult topoSortWithCycleDetection(Map<String, List<String>> adj) {
        Map<String, Integer> color = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        List<String> order = new ArrayList<>();
        List<String> cycle = new ArrayList<>();

        // Initialize all nodes as unvisited (WHITE)
        for (String node : adj.keySet()) {
            color.put(node, WHITE);
        }

        // Process vertices alphabetically on the outer loop
        List<String> sortedVertices = new ArrayList<>(adj.keySet());
        Collections.sort(sortedVertices);

        for (String vertex : sortedVertices) {
            if (color.get(vertex) == WHITE) {
                if (dfs(vertex, adj, color, parent, order, cycle)) {
                    return new TopoResult(null, cycle); // Cycle found, return early
                }
            }
        }

        // Standard DFS finishes in reverse topological order, so we flip it
        Collections.reverse(order);
        return new TopoResult(order, Collections.emptyList());
    }

    /**
     * Core recursive DFS utility with three-color graph marking mechanics.
     */
    private static boolean dfs(String u, Map<String, List<String>> adj,
                               Map<String, Integer> color, Map<String, String> parent,
                               List<String> order, List<String> cycle) {

        color.put(u, GRAY); // Mark node as active on the current recursion stack

        List<String> neighbors = adj.getOrDefault(u, Collections.emptyList());
        for (String v : neighbors) {
            Integer nextColor = color.get(v);

            if (nextColor == WHITE) {
                parent.put(v, u);
                if (dfs(v, adj, color, parent, order, cycle)) {
                    return true;
                }
            } else if (nextColor == GRAY) {
                // Back-edge identified (u -> v). Reconstruct the cycle.
                String curr = u;
                cycle.add(v); // Close the cycle visually with the target node first
                cycle.add(curr);

                while (!curr.equals(v)) {
                    curr = parent.get(curr);
                    if (curr == null) break;
                    cycle.add(curr);
                }

                // Reverse the path to restore its true chronological order
                Collections.reverse(cycle);
                return true;
            }
        }

        color.put(u, BLACK); // Mark node as fully processed
        order.add(u);        // Record to topological completion list
        return false;
    }

    public static void main(String[] args) {
        // Build the Razorpay case study graph configuration
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("auth", Arrays.asList("ledger"));
        graph.put("payments", Arrays.asList("auth"));
        graph.put("kyc", Arrays.asList("auth"));
        graph.put("ledger", Arrays.asList("fraud"));
        graph.put("fraud", Arrays.asList("ledger")); // Circular link: fraud -> ledger
        graph.put("admin-ui", Arrays.asList("payments", "kyc"));
        graph.put("customer-ui", Arrays.asList("payments"));
        graph.put("gateway", Arrays.asList("admin-ui", "customer-ui"));
        graph.put("notify", Arrays.asList("gateway"));

        System.out.println("=== RAZORPAY CI MONOREPO BUILD ENGINE ===");
        TopoResult result = topoSortWithCycleDetection(graph);

        if (result.cycle != null && !result.cycle.isEmpty()) {
            System.out.println("\nBUILD FAILURE: Circular dependency loop identified!");
            System.out.println("Offending Cycle Path: " + result.cycle);
        } else {
            System.out.println("\nBUILD SUCCESS: Safe Compilation Path Established.");
            System.out.println("Execution Order: " + result.order);
        }
    }
}
