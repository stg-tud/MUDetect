package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.EmptyOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.EverythingViolationPredicate;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import edu.iastate.cs.mudetect.mining.AUGMiner;
import edu.iastate.cs.mudetect.mining.DefaultAUGMiner;
import edu.iastate.cs.mudetect.mining.MinPatternActionsModel;
import edu.iastate.cs.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import edu.iastate.cs.egroum.aug.AUGBuilder;

import java.io.FileNotFoundException;
import java.util.Collection;

public class ProvidedPatternsStrategy extends MuDetectStrategy {
    @Override
    protected Collection<APIUsageExample> loadTrainingExamples(DetectorArgs args, DetectorOutput.Builder output) throws FileNotFoundException {
        return new AUGBuilder(new DefaultAUGConfiguration())
                .build(args.getPatternSrcPath(), args.getDependencyClassPath());
    }

    @Override
    protected AUGMiner createMiner() {
        DefaultMiningConfiguration config = new DefaultMiningConfiguration() {{
            minPatternSupport = 1; // create a pattern from each provided example
        }};
        return new DefaultAUGMiner(config);
    }

    @Override
    protected MuDetect createDetector(Model model) {
        return new MuDetect(
                new MinPatternActionsModel(model, 2),
                new EmptyOverlapsFinder(
                        new AlternativeMappingsOverlapsFinder(
                                new DefaultOverlapFinderConfig(new DefaultMiningConfiguration()))),
                new EverythingViolationPredicate(),
                new WeightRankingStrategy(
                        new OverlapWithoutEdgesToMissingNodesWeightFunction(
                                new ConstantNodeWeightFunction()
                        ))::rankViolations);
    }

}
