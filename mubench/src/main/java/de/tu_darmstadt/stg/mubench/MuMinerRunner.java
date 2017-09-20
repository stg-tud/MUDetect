package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.*;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.mining.Anomaly;
import de.tu_darmstadt.stg.mudetect.mining.Fragment;
import de.tu_darmstadt.stg.mudetect.mining.Miner;
import de.tu_darmstadt.stg.mudetect.src2aug.AUGBuilder;

import java.io.FileNotFoundException;
import java.util.*;

public class MuMinerRunner {
    public static void main(String[] args) throws Exception {
        new MuBenchRunner().withMineAndDetectStrategy(new MuMinerStrategy()).run(args);
    }

    private static class MuMinerStrategy implements DetectionStrategy {
        @Override
        public DetectorOutput detectViolations(DetectorArgs args) throws Exception {
            Collection<APIUsageExample> augs = loadTargetData(args);
            ArrayList<Anomaly> anomalies = detectAnomalies(augs);
            List<DetectorFinding> findings = toDetectorFindings(anomalies);
            return createOutput().withFindings(findings);
        }

        private Collection<APIUsageExample> loadTargetData(DetectorArgs args) throws FileNotFoundException {
            return new AUGBuilder(new DefaultAUGConfiguration())
                    .build(args.getTargetPath().srcPath, args.getDependencyClassPath());
        }

        private ArrayList<Anomaly> detectAnomalies(Collection<APIUsageExample> augs) {
            Miner miner = new Miner("-project-name-", new DefaultMiningConfiguration());
            miner.mine(new ArrayList<>(augs));
            return miner.anomalies;
        }

        private List<DetectorFinding> toDetectorFindings(ArrayList<Anomaly> anomalies1) {
            Queue<Anomaly> anomalies = new PriorityQueue<>((a1, a2) -> -Double.compare(a1.getScore(), a2.getScore()));
            anomalies.addAll(anomalies1);
            List<DetectorFinding> findings = new ArrayList<>();
            while (!anomalies.isEmpty()) {
                Anomaly anomaly = anomalies.poll();
                for (Fragment fragment : anomaly.getInstances()) {
                    APIUsageExample target = fragment.getGraph();
                    Location location = target.getLocation();
                    DetectorFinding finding = new DetectorFinding(location.getFilePath(), location.getMethodSignature());
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
