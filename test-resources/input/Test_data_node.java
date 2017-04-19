package input;

class Test_data_node extends BufferedIndexOutput {

	private Set<Node> findStorageLocationReferences(Node root) {
		final Set<Node> references = Sets.newHashSet();

		NodeTraversal.traverse(compiler, root, new AbstractShallowCallback() {
			@Override
			public void visit(NodeTraversal t, Node n, Node parent) {
				if (NodeUtil.isGet(n) || (NodeUtil.isName(n) && !NodeUtil.isFunction(parent))) {
					references.add(n);
				} 
			}       
		});

		return references;
	}

	void apply() {
		if (NodeUtil.isFunctionDeclaration(node)) {
			traverseFunction(node, scope);
		} else {
			for (Node child = node.getFirstChild();
					child != null; child = child.getNext()) {
				traverseNode(child, node, scope);
			}
		}
	}


	void pattern(InputStream in) throws IOException {
		byte[] spoolBuffer = new byte[0x2000];
		int read;
		try {
			while ((read = in.read(spoolBuffer)) > 0) {
				// do something...
			}
		} finally {
			in.close();
		}
	}

	int v1, v2, v3, v4;
	void m() {
		v1 = m1();
		v2 = m2(v1);
		v3 = m3(v1);
		m4(v2, v3);
		v1 = m1();
		v2 = m2(v1);
		v3 = m3(v1);
		m4(v2, v3);
		m4(v2, v3);
	}
}