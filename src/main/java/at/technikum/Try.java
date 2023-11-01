package at.technikum;

public class Try<T> {

	private T value;
	private Exception exception;

	// /////////////////////////////////////////////////////////////////////////
	// Init
	// /////////////////////////////////////////////////////////////////////////

	private Try(T value, Exception exception) {
		this.value = value;
		this.exception = exception;
	}

	public static <T> Try<T> success(T value) {
		return new Try<>(value, null);
	}

	public static <T> Try<T> failure(Exception exception) {
		return new Try<>(null, exception);
	}

	// /////////////////////////////////////////////////////////////////////////
	// Methods
	// /////////////////////////////////////////////////////////////////////////

	public boolean isSuccess() {
		return value != null;
	}

	public boolean isFailure() {
		return exception != null;
	}

	public T get() {
		return value;
	}

	public Exception getException() {
		return exception;
	}
}
