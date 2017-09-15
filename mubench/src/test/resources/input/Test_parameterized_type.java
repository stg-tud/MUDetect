package input;

class Test_parameterized_type {
	
	@Override
	public void processThis(Node externs, Node root) {
		NodeTraversal.traverse(compiler, Preconditions.checkNotNull(this), this);
	}
	@Override
	public void processNode(Node externs, Node root) {
		NodeTraversal.traverse(compiler, Preconditions.checkNotNull(root), this);
	}

	class Preconditions {
		@CanIgnoreReturnValue
		public static <T> T checkNotNull(T reference) {
			if (reference == null) {
				throw new NullPointerException();
			}
			return reference;
		}
	}
}