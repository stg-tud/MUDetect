package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import edu.iastate.cs.mudetect.mining.AUGMiner;
import edu.iastate.cs.mudetect.mining.DefaultAUGMiner;
import edu.iastate.cs.mudetect.mining.MinPatternActionsModel;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import edu.iastate.cs.egroum.aug.AUGBuilder;

import java.io.IOException;
import java.util.Collection;

public class IntraProjectStrategy extends MuDetectStrategy {
    @Override
    protected Collection<APIUsageExample> loadTrainingExamples(DetectorArgs args, DetectorOutput.Builder output) throws IOException {
        return new AUGBuilder(new DefaultAUGConfiguration())
                .build(args.getTargetSrcPaths(), args.getDependencyClassPath());
    }

    @Override
    protected AUGMiner createMiner() {
        return new DefaultAUGMiner(new DefaultMiningConfiguration());
    }

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
                new DefaultFilterAndRankingStrategy(new WeightRankingStrategy(
                        new ProductWeightFunction(
                                new OverlapWithoutEdgesToMissingNodesWeightFunction(new ConstantNodeWeightFunction()),
                                new PatternSupportWeightFunction(),
                                new ViolationSupportWeightFunction()
                        ))));
    }
}
