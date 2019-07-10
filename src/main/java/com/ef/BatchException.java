package com.ef;

class BatchException extends Exception{

    private final long startLine;
    private final long endLine;


    public BatchException(long startLine, long endLine, Throwable cause) {
        super(cause);
        this.startLine = startLine;
        this.endLine = endLine;
    }

    @Override
    public String getMessage() {
        return "Batch(" + startLine + "-" + endLine + "): Failed with exception: " + getCause().getMessage();
    }
}
