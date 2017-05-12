package de.tu_darmstadt.stg.mubench;

import egroum.AUGConfiguration;

class DefaultAUGConfiguration extends AUGConfiguration {
    {
        collapseIsomorphicSubgraphs = true;
        collapseTemporaryDataNodes = false;
        collapseTemporaryDataNodesIncomingToControlNodes = true;
        encodeUnaryOperators = false;
        encodeConditionalOperators = false;
        removeImplementationCode = 2;
        groum = false;
        minStatements = 0;
    }
}
