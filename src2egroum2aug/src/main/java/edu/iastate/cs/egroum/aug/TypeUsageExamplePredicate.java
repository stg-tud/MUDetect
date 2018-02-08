package edu.iastate.cs.egroum.aug;

import org.eclipse.jdt.core.dom.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TypeUsageExamplePredicate implements UsageExamplePredicate {
    private final Set<String> fullyQualifiedTypeNames;
    private final Set<String> simpleTypeNames;

    public static TypeUsageExamplePredicate usageExamplesOf(String... fullyQualifiedTypeNames) {
        return new TypeUsageExamplePredicate(fullyQualifiedTypeNames);
    }

    protected TypeUsageExamplePredicate(String... fullyQualifiedTypeNames) {
        this.fullyQualifiedTypeNames = new HashSet<>(Arrays.asList(fullyQualifiedTypeNames));
        this.simpleTypeNames = new HashSet<>();
        for (String fullyQualifiedTypeName : fullyQualifiedTypeNames) {
            simpleTypeNames.add(fullyQualifiedTypeName.substring(fullyQualifiedTypeName.lastIndexOf('.') + 1));
        }
    }

    @Override
    public boolean matches(String sourceFilePath, CompilationUnit cu) {
        return matches(cu);
    }

    @Override
    public boolean matches(MethodDeclaration methodDeclaration) {
        return matches((ASTNode) methodDeclaration);
    }

    private boolean containing;
    private boolean matches(ASTNode node) {
        if (matchesAnyExample()) return true;

        containing = false;
        node.accept(new ASTVisitor(false) {
            @Override
            public boolean visit(MethodInvocation node) {
                return !isDeclaredByApiClass(node.resolveMethodBinding()) && super.visit(node);
            }

            @Override
            public boolean visit(ConstructorInvocation node) {
                return !isDeclaredByApiClass(node.resolveConstructorBinding()) && super.visit(node);
            }

            @Override
            public boolean visit(ClassInstanceCreation node) {
                return !isDeclaredByApiClass(node.resolveConstructorBinding()) && super.visit(node);
            }

            private boolean isDeclaredByApiClass(IMethodBinding mb) {
                if (mb != null) {
                    String name = mb.getDeclaringClass().getTypeDeclaration().getQualifiedName();
                    if (fullyQualifiedTypeNames.contains(name)) {
                        containing = true;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean preVisit2(ASTNode node) {
                return !containing && super.preVisit2(node);
            }
        });
        return containing;
    }

    @Override
	public boolean matches(EGroumGraph graph) {
        return matchesAnyExample() || !Collections.disjoint(graph.getAPIs(), simpleTypeNames);
    }

    private boolean matchesAnyExample() {
        return fullyQualifiedTypeNames.isEmpty();
    }
}
