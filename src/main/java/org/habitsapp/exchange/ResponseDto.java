package org.habitsapp.exchange;

public class ResponseDto {
    private String message;

    public ResponseDto() {}

    public ResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
