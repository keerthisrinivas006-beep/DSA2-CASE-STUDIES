package banking_analytics;

import java.util.*;

public class HDFCNetBankingEngine {

    private final int n;
    private final long[] bit; // 1-indexed internal Fenwick array storage

    /**
     * Initializes a Fenwick Tree structure for a specific calendar range tracking.
     * @param rangeLimit Total tracking slots (e.g., 365 days)
     */
    public HDFCNetBankingEngine(int rangeLimit) {
        this.n = rangeLimit;
        this.bit = new long[rangeLimit + 1]; // 0 index remains unused
    }

    /**
     * Updates a specific day's transaction value.
     * Time Complexity: O(log n)
     * @param dayIndex 1-based index representing the day of the year
     * @param delta The transaction value adjustment (positive or negative)
     */
    public void pointUpdate(int dayIndex, long delta) {
        int i = dayIndex;
        while (i <= n) {
            bit[i] += delta;
            i += (i & -i); // Navigate to the next higher operational ancestor node
        }
    }

    /**
     * Computes the accumulated sum from Day 1 up to Day 'i'.
     * Time Complexity: O(log n)
     * @param dayIndex 1-based index target boundary
     * @return Accumulated sum of expenditures
     */
    public long queryPrefix(int dayIndex) {
        long sum = 0;
        int i = dayIndex;
        while (i > 0) {
            sum += bit[i];
            i -= (i & -i); // Corrected LSB removal step: strips the lowest set bit
        }
        return sum;
    }

    /**
     * Computes the net expenditure across an arbitrary statement window.
     * @param leftDay Beginning day of the statement interval (1-based)
     * @param rightDay Ending day of the statement interval (1-based)
     * @return Total financial transactions within the specified window
     */
    public long queryRange(int leftDay, int rightDay) {
        if (leftDay > rightDay || leftDay < 1 || rightDay > n) {
            throw new IllegalArgumentException("Invalid date range interval specified.");
        }
        return queryPrefix(rightDay) - queryPrefix(leftDay - 1);
    }

    public static void main(String[] args) {
        // Mock data from the case study: first 15 days of January
        long[] initialSpend = {
            1200, 800, 0, 2400, 1500, 600, 0, 0, 3500, 0, 1100, 950, 700, 0, 0
        };

        HDFCNetBankingEngine ledger = new HDFCNetBankingEngine(365);

        // Populate the Fenwick Tree using point updates
        for (int d = 0; d < initialSpend.length; d++) {
            ledger.pointUpdate(d + 1, initialSpend[d]);
        }

        System.out.println("=== HDFC NETBANKING TRANSACTION CORE ENGINE ===");
        
        // Scenario validation query: Total spend over Jan 5 to Jan 12
        int startDay = 5;
        int endDay = 12;
        long totalStatementSpend = ledger.queryRange(startDay, endDay);

        System.out.println("\nQuery Analysis Input Windows:");
        System.out.println("  Prefix Sum up to Day " + endDay + ": ₹" + ledger.queryPrefix(endDay));
        System.out.println("  Prefix Sum up to Day " + (startDay - 1) + ": ₹" + ledger.queryPrefix(startDay - 1));
        System.out.println("\nFinal Output Results:");
        System.out.println("  Calculated Total Statement Spend (Jan " + startDay + " - Jan " + endDay + "): ₹" + totalStatementSpend);
    }
}
