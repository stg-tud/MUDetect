package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.dataflow.QualifierEdge;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

import java.util.Optional;
import java.util.Set;

public class MissingQualifierNoViolationPredicate implements ViolationPredicate {
    @Override
    public Optional<Boolean> apply(Overlap overlap) {
        Set<Edge> missingEdges = overlap.getMissingEdges();
        if (missingEdges.size() == 1) {
            Edge missingEdge = missingEdges.iterator().next();
            if (missingEdge instanceof QualifierEdge) {
                return Optional.of(false);
            }
        }
        return Optional.empty();
    }
}
