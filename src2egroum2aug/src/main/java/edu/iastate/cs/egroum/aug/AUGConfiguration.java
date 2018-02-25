package edu.iastate.cs.egroum.aug;

public class AUGConfiguration {
    /**
     * Collapse isomporphic subgraphs within AUGs. Not Implemented.
     */
    public boolean collapseIsomorphicSubgraphs = true;

    /**
     * Whether to remove data nodes with only one usage (i.e., outgoing edge). Removing them makes AUGs smaller, keeping
     * them potentially increases the similarity between different AUGs.
     *
     * https://bitbucket.org/nguyen_hoan/grouminer/issues/20
     */
    public boolean collapseTemporaryDataNodes = false;

    /**
     * Whether to remove data nodes between conditions and their controlled statements.
     *
     * https://bitbucket.org/nguyen_hoan/grouminer/issues/29
     */
    public boolean collapseTemporaryDataNodesIncomingToControlNodes = true;

    /**
     * Whether to encode unary operators (!, +, and -) in AUGs. Since we don’t encode whether a controlled action is
     * executed along the if or the else path, <code>if (!(l.isEmpty())) l.get();</code> and
     * <code>if (!(l.isEmpty())) {} else { l.get(); }</code> are represented by the same AUG. Similarly, both
     * <code>if (l.isEmpty()) l.get();</code> and <code>if (l.isEmpty()) {} else { l.get(); }</code> are represented by
     * the same AUG. If we don't encode the ! operator, all four snippets are represented by the same AUG, which seems
     * reasonable.
     *
     * TODO Do we have an example for the sign operators (+ and -)?
     */
    public boolean encodeUnaryOperators = false;

    /**
     * Whether to encode conditional operators (&& and ||) in AUGs. The conditional operators are only used to combine
     * atomic conditions. Since we only encode which conditions control which statements, but not whether the condition
     * needs to be true or false in order for the statement to be executed, it is irrelevant how conditions are
     * combined.
     */
    public boolean encodeConditionalOperators = false;

    /**
     * Whether to encode conditional operators (&& and ||) in AUGs. The conditional operators are only used to combine
     * atomic conditions. We do not aim to find problems in arithmetic logic and they only introduce variance.
     */
    public boolean encodeArithmeticOperators = false;

    /**
     * Whether to encode bitwise operators in AUGs. The conditional operators are only used to combine
     * atomic conditions. We do not aim to find problems in bitwise logic and they only introduce variance.
     */
    public boolean encodeBitwiseOperators = false;
    
    /**
     * The level to go for removing the members, methods and fields, of this object
     * 0 - No removal
     * 1 - Remove immediate members
     * 2 - Remove immediate members and their members
     */
    public int removeImplementationCode = 0;
    
    /**
     * The level of encoding constants
     * 0 - use types
     * 1 - use names
     * 2 - use values
     */
    public int encodeConstants = 1;
    
    /**
     * 
     */
    public boolean groum = false;
    
    /**
     * Threshold for the minimum number of statements in a method to build AUG
     */
    public int minStatements = 0;
    
    /**
     * 
     */
    public boolean keepQualifierEdges = true;
    
    /**
     * 
     */
    public boolean removeIndependentControlEdges = true;
    
    /**
     * 
     */
	public boolean removeTransitiveDefinitionEdgesFromMethodCalls = true;
	
	/**
	 * Predicate that matches usage examples of interest.
	 */
	public UsageExamplePredicate usageExamplePredicate = UsageExamplePredicate.allUsageExamples();
	
	/**
	 * 
	 */
	public boolean buildTransitiveDataEdges = false;

	public boolean encodeQualifiers = false;
}
