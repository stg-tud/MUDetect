package de.tu_darmstadt.stg.mubench;

import egroum.AUGConfiguration;

class DefaultAUGConfiguration extends AUGConfiguration {
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
