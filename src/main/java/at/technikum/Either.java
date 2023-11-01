package at.technikum;

public class Either<L, R> {

	private L left;
	private R right;

	// /////////////////////////////////////////////////////////////////////////
	// Init
	// /////////////////////////////////////////////////////////////////////////

	private Either(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public static <L, R> Either<L, R> left(L left) {
		return new Either<>(left, null);
	}

	public static <L, R> Either<L, R> right(R right) {
		return new Either<>(null, right);
	}

	// /////////////////////////////////////////////////////////////////////////
	// Methods
	// /////////////////////////////////////////////////////////////////////////

	public boolean isLeft() {
		return left != null;
	}

	public boolean isRight() {
		return right != null;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}
}
