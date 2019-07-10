package com.ef;

import java.util.ArrayList;
import java.util.List;

/**
 * @author natc <nathanielcamomot@gmail.com>
 */
class BatchResult {
    private final long startLine;
    private final long endLine;
    private final boolean success;
    private final String message;
    private final int[] results;

    public BatchResult(long startLine, long endLine, int[] results, int successCode) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.results = results;

        List<String> failedIndices = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            if (results[i] != successCode) {
                failedIndices.add(Integer.toString(i));
            }
        }

        if (failedIndices.isEmpty()) {
            success = true;
            message = "Batch(" + startLine + "-" + endLine + "): Successfully inserted " + results.length;
        } else {
            success = false;
            message = "Batch(" + startLine + "-" + endLine + "): Failed on index: " + String.join(",", failedIndices);
        }
    }

    public long getStartLine() {
        return startLine;
    }

    public long getEndLine() {
        return endLine;
    }

    public boolean isSuccess() {
        return success;
    }

    public int[] getResults() {
        return results;
    }

    public String getMessage() {
        return message;
    }
}
