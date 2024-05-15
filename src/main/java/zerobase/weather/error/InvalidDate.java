package zerobase.weather.error;

public class InvalidDate extends RuntimeException { // invalid : 유효하지않은
    private static final String MESSAGE = "너무 과거 혹은 미래의 날짜입니당";
    public InvalidDate() {
        super(MESSAGE);
    }
}
