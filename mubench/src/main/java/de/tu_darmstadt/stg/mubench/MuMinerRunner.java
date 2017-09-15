package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.*;
import egroum.AUGBuilder;
import egroum.AUGConfiguration;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class MuMinerRunner {
    public static void main(String[] args) throws Exception {
        new MuBenchRunner().withMineAndDetectStrategy(new MuMinerStrategy()).run(args);
    }

    private static class MuMinerStrategy implements DetectionStrategy {
        @Override
        public DetectorOutput detectViolations(DetectorArgs args) throws Exception {
            ArrayList<EGroumGraph> groums = loadTargetData(args);
            ArrayList<Anomaly> anomalies = detectAnomalies(groums);
            List<DetectorFinding> findings = toDetectorFindings(anomalies);
            return createOutput().withFindings(findings);
        }

        private ArrayList<EGroumGraph> loadTargetData(DetectorArgs args) throws FileNotFoundException {
            return new EGroumBuilder(new DefaultAUGConfiguration())
                    .buildBatch(args.getTargetPath().srcPath, args.getDependencyClassPath());
        }

        private ArrayList<Anomaly> detectAnomalies(ArrayList<EGroumGraph> groums) {
            mining.Miner miner = new mining.Miner("-project-name-", new DefaultMiningConfiguration());
            miner.mine(groums);
            return miner.anomalies;
        }

        private List<DetectorFinding> toDetectorFindings(ArrayList<Anomaly> anomalies1) {
            Queue<Anomaly> anomalies = new PriorityQueue<>((a1, a2) -> -Double.compare(a1.getScore(), a2.getScore()));
            anomalies.addAll(anomalies1);
            List<DetectorFinding> findings = new ArrayList<>();
            while (!anomalies.isEmpty()) {
                Anomaly anomaly = anomalies.poll();
                for (Fragment fragment : anomaly.getInstances()) {
                    EGroumGraph target = fragment.getGraph();
                    DetectorFinding finding = new DetectorFinding(target.getFilePath(), AUGBuilder.getMethodSignature(target));
                    finding.put("score", anomaly.getScore());
                    finding.put("pattern", anomaly.getPattern().getDotGraph().getGraph());
                    finding.put("violation", fragment.getDotGraph().getGraph());
                    findings.add(finding);
                }
            }
            return findings;
        }
    }
}
