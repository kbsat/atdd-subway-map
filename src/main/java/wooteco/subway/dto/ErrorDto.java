package wooteco.subway.dto;

public class ErrorDto {

    private String message;

    private ErrorDto() {
    }

    public ErrorDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
