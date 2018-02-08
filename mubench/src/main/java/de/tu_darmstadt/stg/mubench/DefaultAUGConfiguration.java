package de.tu_darmstadt.stg.mubench;


import edu.iastate.cs.egroum.aug.AUGConfiguration;

public class DefaultAUGConfiguration extends AUGConfiguration {
    {
        minStatements = 0;
        groum = false;

        collapseIsomorphicSubgraphs = true;

        collapseTemporaryDataNodes = false;
        collapseTemporaryDataNodesIncomingToControlNodes = true;

        encodeUnaryOperators = false;
        encodeConditionalOperators = false;

        buildTransitiveDataEdges = false;

        removeImplementationCode = 2;
    }
}
