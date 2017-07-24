package mining;

import egroum.EGroumGraph;
import org.eclipse.jdt.core.dom.*;

public abstract class UsageExamplePredicate {

    public static UsageExamplePredicate allUsageExamples() {
    	return new TypeUsageExamplePredicate(new String[0]);
    }

    protected abstract boolean matchesAnyExample();

    public abstract boolean matches(EGroumGraph graph);

    protected boolean containing;
    public abstract boolean matches(ASTNode node);
}
