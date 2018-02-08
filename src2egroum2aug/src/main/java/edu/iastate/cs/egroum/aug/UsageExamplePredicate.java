package edu.iastate.cs.egroum.aug;

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

    /**
     * This is only here, because we need a check during the src2aug transformation, which uses EGroum as an
     * intermediate state.
     */
    @Deprecated
    boolean matches(EGroumGraph graph);
}
