package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import edu.iastate.cs.mudetect.mining.MinPatternActionsModel;
import edu.iastate.cs.mudetect.mining.Model;

public class AlternativeRankingIntraProjectStrategy extends IntraProjectStrategy {
    @Override
    protected MuDetect createDetector(Model model) {
        return new MuDetect(
                new MinPatternActionsModel(model, 2),
                new AlternativeMappingsOverlapsFinder(new DefaultOverlapFinderConfig(new DefaultMiningConfiguration())),
                new FirstDecisionViolationPredicate(
                        new MissingDefPrefixNoViolationPredicate(),
                        new OnlyDefPrefixNoViolationPredicate(),
                        new MissingCatchNoViolationPredicate(),
                        new MissingAssignmentNoViolationPredicate(),
                        new MissingElementViolationPredicate()),
                new AlternativeRankingAndFilterStrategy());
    }
}
