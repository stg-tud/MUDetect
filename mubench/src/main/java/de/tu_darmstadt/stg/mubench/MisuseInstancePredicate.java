package de.tu_darmstadt.stg.mubench;

import edu.iastate.cs.egroum.aug.EGroumGraph;
import edu.iastate.cs.egroum.aug.UsageExamplePredicate;
import edu.iastate.cs.egroum.utils.JavaASTUtil;

import org.eclipse.jdt.core.dom.*;

import java.util.*;
import java.util.stream.Collectors;

public class MisuseInstancePredicate implements UsageExamplePredicate {
	private final Collection<Misuse> misuses;
	private final Map<CompilationUnit, Collection<Misuse>> mapping = new HashMap<>();

	public static MisuseInstancePredicate examplesOf(Collection<Misuse> misuses) {
		return new MisuseInstancePredicate(misuses);
	}

    private MisuseInstancePredicate(Collection<Misuse> misuses) {
		this.misuses = misuses;
	}

	@Override
	public boolean matches(String sourceFilePath, CompilationUnit cu) {
        List<Misuse> misuses = this.misuses.stream()
                .filter(misuse -> misuse.isIn(sourceFilePath))
                .collect(Collectors.toList());
        if (!misuses.isEmpty()) {
            mapping.put(cu, misuses);
            return true;
        } else {
            return false;
        }
	}

	@Override
	public boolean matches(MethodDeclaration methodDeclaration) {
        String signature = buildSignature(methodDeclaration);
        Collection<Misuse> misuses = mapping.get(methodDeclaration.getRoot());
        return misuses.stream().anyMatch(misuse -> misuse.getMethodSignature().equals(signature));
	}

	@Override
	public boolean matches(EGroumGraph graph) {
    	return true;
    }

	private static String buildSignature(MethodDeclaration method) {
		StringBuilder sb = new StringBuilder(method.getName().getIdentifier()).append("(");
		for (int i = 0; i < method.parameters().size(); i++) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) method.parameters().get(i);
			if (i > 0)
				sb.append(", ");
            String simpleType = JavaASTUtil.getSimpleType(svd.getType());
            // remove qualifier from inner types
            simpleType = simpleType.substring(simpleType.lastIndexOf('.') + 1);
            sb.append(simpleType);
		}
		sb.append(")");
		return sb.toString();
	}
}
