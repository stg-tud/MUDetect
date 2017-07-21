package mining;

import org.eclipse.jdt.core.dom.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UsageExamplePredicate {
    private final Set<String> fullyQualifiedTypeNames;

    public static UsageExamplePredicate allUsageExamples() {
        return new UsageExamplePredicate(new String[0]);
    }

    public static UsageExamplePredicate usageExamplesOf(String... fullyQualifiedTypeNames) {
        return new UsageExamplePredicate(fullyQualifiedTypeNames);
    }

    private UsageExamplePredicate(String[] fullyQualifiedTypeNames) {
        this.fullyQualifiedTypeNames = new HashSet<>(Arrays.asList(fullyQualifiedTypeNames));
    }

    private boolean matchesAnyExample() {
        return fullyQualifiedTypeNames.isEmpty();
    }

    private boolean containing;
    public boolean matches(ASTNode node) {
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
}
