package mining;

import egroum.EGroumGraph;
import utils.JavaASTUtil;

import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.Set;

public class MethodSignatureExamplePredicate extends UsageExamplePredicate {
    private final Set<String> signatures;

	public static MethodSignatureExamplePredicate usageExamplesOf(String[] signatures) {
		return new MethodSignatureExamplePredicate(signatures);
	}

    private MethodSignatureExamplePredicate(String[] signatures) {
        this.signatures = new HashSet<>();
        for (String s : signatures)
        	this.signatures.add(s);
    }

	@Override
	protected boolean matchesAnyExample() {
        return signatures.isEmpty();
    }

    @Override
	public boolean matches(EGroumGraph graph) {
    	return true;
    }

    @Override
	public boolean matches(ASTNode node) {
    	return true;
    }

	public boolean matches(String path, String type, MethodDeclaration method) {
		return signatures.contains(getVersionId(path) + "/" + type + "/" + buildSignature(method));
	}

	private String getVersionId(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String buildSignature(MethodDeclaration method) {
		StringBuilder sb = new StringBuilder();
		sb.append(method.getName().getIdentifier() + "(");
		for (int i = 0; i < method.parameters().size(); i++) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) method.parameters().get(i);
			if (i > 0)
				sb.append(", ");
			sb.append(JavaASTUtil.getSimpleType(svd.getType()));
		}
		sb.append(")");
		return sb.toString();
	}
}
