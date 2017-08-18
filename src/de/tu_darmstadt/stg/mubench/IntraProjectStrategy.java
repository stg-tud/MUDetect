package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mudetect.*;
import de.tu_darmstadt.stg.mudetect.model.AUG;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.mining.AUGMiner;
import de.tu_darmstadt.stg.mudetect.mining.DefaultAUGMiner;
import de.tu_darmstadt.stg.mudetect.mining.MinPatternActionsModel;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.AlternativeViolationPredicate.firstAlternativeViolation;

class IntraProjectStrategy extends MuDetectStrategy {
    @Override
    Collection<EGroumGraph> loadTrainingExamples(DetectorArgs args) throws IOException {
        return new EGroumBuilder(new DefaultAUGConfiguration())
                .buildBatch(args.getTargetPath().srcPath, args.getDependencyClassPath());
    }

    @Override
    AUGMiner createMiner() {
        return new DefaultAUGMiner(new DefaultMiningConfiguration());
    }

    @Override
    MuDetect createDetector(Model model) {
        return new MuDetect(
                new MinPatternActionsModel(model, 2),
                new AlternativeMappingsOverlapsFinder(new DefaultOverlapFinderConfig(new DefaultMiningConfiguration())),
                new FirstDecisionViolationPredicate(
                        new OptionalDefPrefixViolationPredicate(),
                        new MissingElementViolationPredicate()),
                new WeightRankingStrategy(new ProductWeightFunction(
                        new PatternViolationsWeightFunction(),
                        new ViolationSupportWeightFunction(),
                        new PatternSupportWeightFunction()
                )));
    }

    @Override
    protected List<Violation> findViolations(MuDetect detector, Collection<AUG> targets) {
        return super.findViolations(detector, targets).stream()
                .filter(firstAlternativeViolation()).collect(Collectors.toList());
    }
}
