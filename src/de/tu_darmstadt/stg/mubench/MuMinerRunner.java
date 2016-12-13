package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.CodePath;
import de.tu_darmstadt.stg.mubench.cli.DetectorFinding;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import egroum.AUGBuilder;
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
    protected void detectOnly(CodePath codePath, CodePath codePath1, DetectorOutput detectorOutput) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void mineAndDetect(CodePath trainAndTargetPath, DetectorOutput output) throws Exception {
        ArrayList<EGroumGraph> groums = new EGroumBuilder().buildBatch(trainAndTargetPath.srcPath, null);
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
