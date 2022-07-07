package edu.xjtu.zipper.xjtupayment.data;

public class TokenHolder {
    private final String content;
    private final ErrorType errorType;

    public TokenHolder(String content) {
        this.content = content;
        this.errorType = ErrorType.ok;
    }

    public TokenHolder(ErrorType errorType) {
        content = null;
        this.errorType = errorType;
    }

    public TokenHolder(String content, ErrorType errorType) {
        this.content = content;
        this.errorType = errorType;
    }

    public String getContent() {
        return content;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
    public enum ErrorType {
        ok,failed,expired
    }
}
