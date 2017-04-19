package input;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

class Test_anonymous_class {
    private Set<Node> findStorageLocationReferences(Node root) {
        final Set<Node> references = Sets.newHashSet();
   
        NodeTraversal.traverse(compiler, root, new AbstractShallowCallback() {
          @Override
          public void visit(NodeTraversal t, Node n, Node parent) {
            if (NodeUtil.isGet(n)
                || (NodeUtil.isName(n) && !NodeUtil.isFunction(parent))) {
              references.add(n);
            } 
          }       
        });
        
        return references;
      }

	void m1() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Object();
			}
		});
	}

	public static void pattern(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame("Main Window");
				// add components
				f.pack();
				f.setVisible(true);
			}
		});
	}

	public static void misuse(String[] args) {
		JFrame f = new JFrame("Main Window");
		// add components
		f.pack();
		f.setVisible(true);
	}

	public void m() {
		// create a lock file
		final File lockFile = new File("");
		new Object() {
			public boolean obtain() throws IOException {
				if (DISABLE_LOCKS) {
					return true;
				}
				return lockFile.createNewFile();
			}
		};
	}
}