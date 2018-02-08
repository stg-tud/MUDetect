package de.tu_darmstadt.stg.mudetect.typehierarchy;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import edu.iastate.cs.egroum.utils.FileIO;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetSrcTypeHierarchy extends TypeHierarchy {
    @SuppressWarnings("unchecked")
    public static TypeHierarchy build(String sourcePath, String[] classPath) {
        List<String> paths = getAbsoluteFilePaths(sourcePath);
        TypeHierarchyCollectingVisitor visitor = new TypeHierarchyCollectingVisitor();
        FileASTRequestor r = new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit cu) {
                cu.accept(visitor);
            }
        };
        @SuppressWarnings("rawtypes")
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setCompilerOptions(options);
        parser.setEnvironment(classPath, new String[0], new String[0], true);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.createASTs(paths.toArray(new String[0]), null, new String[0], r, null);
        return visitor.getHierarchy();
    }

    private TargetSrcTypeHierarchy() {}

    private static List<String> getAbsoluteFilePaths(String sourcePath) {
        Stream<File> files = FileIO.getPaths(new File(sourcePath)).stream();
        return files.map(File::getAbsolutePath).collect(Collectors.toList());
    }

    private static class TypeHierarchyCollectingVisitor extends ASTVisitor {
        private final TypeHierarchy hierarchy;

        TypeHierarchyCollectingVisitor() {
            this.hierarchy = new TargetSrcTypeHierarchy();
        }

        TypeHierarchy getHierarchy() {
            return hierarchy;
        }

        @Override
        public boolean visit(TypeDeclaration node) {
            addType(node.resolveBinding());
            return true;
        }

        @Override
        public boolean visit(SimpleType node) {
            addType(node.resolveBinding());
            return true;
        }

        @Override
        public boolean visit(MethodInvocation node) {
            IMethodBinding methodBinding = node.resolveMethodBinding();
            if (methodBinding != null) {
                addType(methodBinding.getReturnType());
            }
            return true;
        }

        private void addSuperclass(ITypeBinding typeBinding, String typeName) {
            ITypeBinding superclass = typeBinding.getSuperclass();
            while (superclass != null) {
                String supertypeName = getRawSimpleName(superclass);
                hierarchy.addSupertype(typeName, supertypeName);
                addInterfaces(superclass, typeName);
                superclass = superclass.getSuperclass();
            }
        }

        private void addInterfaces(ITypeBinding typeBinding, String typeName) {
            for (ITypeBinding i : typeBinding.getInterfaces()) {
                String interfaceName = getRawSimpleName(i);
                hierarchy.addSupertype(typeName, interfaceName);
                addInterfaces(i, typeName);
            }
        }

        private void addType(ITypeBinding typeBinding) {
            if (typeBinding != null) {
                String typeName = getRawSimpleName(typeBinding);
                addSuperclass(typeBinding, typeName);
                addInterfaces(typeBinding, typeName);
            }
        }

        private String getRawSimpleName(ITypeBinding typeBinding) {
            return typeBinding.getErasure().getName();
        }
    }
}
