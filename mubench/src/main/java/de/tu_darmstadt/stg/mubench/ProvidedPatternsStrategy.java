package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mudetect.aug.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.EmptyOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.EverythingViolationPredicate;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import de.tu_darmstadt.stg.mudetect.mining.AUGMiner;
import de.tu_darmstadt.stg.mudetect.mining.DefaultAUGMiner;
import de.tu_darmstadt.stg.mudetect.mining.MinPatternActionsModel;
import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.ranking.NoRankingStrategy;
import egroum.AUGBuilder;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;

import java.io.FileNotFoundException;
import java.util.Collection;

class ProvidedPatternsStrategy extends MuDetectStrategy {
    @Override
    Collection<APIUsageExample> loadTrainingExamples(DetectorArgs args, DetectorOutput.Builder output) throws FileNotFoundException {
        return new AUGBuilder(new DefaultAUGConfiguration())
                .build(args.getPatternPath().srcPath, args.getDependencyClassPath());
    }

    @Override
    AUGMiner createMiner() {
        DefaultMiningConfiguration config = new DefaultMiningConfiguration() {{
            minPatternSupport = 1; // create a pattern from each provided example
        }};
        return new DefaultAUGMiner(config);
    }

    @Override
    MuDetect createDetector(Model model) {
        return new MuDetect(
                new MinPatternActionsModel(model, 2),
                new EmptyOverlapsFinder(
                        new AlternativeMappingsOverlapsFinder(
                                new DefaultOverlapFinderConfig(new DefaultMiningConfiguration()))),
                new EverythingViolationPredicate(),
                new NoRankingStrategy());
    }
}
