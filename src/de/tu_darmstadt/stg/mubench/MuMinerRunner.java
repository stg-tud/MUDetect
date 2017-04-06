package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.*;
import egroum.AUGBuilder;
import egroum.AUGConfiguration;
import egroum.EGroumBuilder;
import egroum.EGroumGraph;
import mining.*;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class MuMinerRunner extends MuBenchRunner {

    public static void main(String[] args) throws Exception {
        new MuMinerRunner().run(args);
    }

    @Override
    protected void detectOnly(DetectorArgs args, DetectorOutput detectorOutput) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void mineAndDetect(DetectorArgs args, DetectorOutput output) throws Exception {
        EGroumBuilder builder = new EGroumBuilder(new AUGConfiguration());
        ArrayList<EGroumGraph> groums = builder.buildBatch(args.getTargetPath().srcPath, null);
        Miner miner = new Miner("-project-name-", new Configuration() {{ minPatternSupport = 10; }});
        miner.mine(groums);
        Queue<Anomaly> anomalies = new PriorityQueue<>((a1, a2) -> -Double.compare(a1.getScore(), a2.getScore()));
        anomalies.addAll(miner.anomalies);
        while (!anomalies.isEmpty()) {
            Anomaly anomaly = anomalies.poll();
            for (Fragment fragment : anomaly.getInstances()) {
                EGroumGraph target = fragment.getGraph();
                DetectorFinding finding = output.add(target.getFilePath(), AUGBuilder.getMethodSignature(target));
                finding.put("score", Double.toString(anomaly.getScore()));
                finding.put("pattern", anomaly.getPattern().getDotGraph().getGraph());
                finding.put("violation", fragment.getDotGraph().getGraph());
            }
        }
    }
}
