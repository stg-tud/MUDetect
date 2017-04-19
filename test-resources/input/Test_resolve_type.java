package input;

import com.google.common.base.Joiner;

class Test_resolve_type {

    private void processStringNode(NodeTraversal t, Node n) {
        String[] parts = n.getString().split("-");
        if (symbolMap != null) {
          n.setString(Joiner.on("-").join(parts));
        }
    }
}