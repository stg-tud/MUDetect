package mining;

import egroum.EGroumGraph;
import org.eclipse.jdt.core.dom.*;

public interface UsageExamplePredicate {
    public static UsageExamplePredicate allUsageExamples() {
    	return new UsageExamplePredicate() {
            @Override
            public boolean matches(String sourceFilePath, CompilationUnit cu) {
                return true;
            }

            @Override
            public boolean matches(MethodDeclaration methodDeclaration) {
                return true;
            }

            @Override
            public boolean matches(EGroumGraph graph) {
                return true;
            }

        };
    }

    boolean matches(String sourceFilePath, CompilationUnit cu);
    boolean matches(MethodDeclaration methodDeclaration);

    boolean matches(EGroumGraph graph);
}
