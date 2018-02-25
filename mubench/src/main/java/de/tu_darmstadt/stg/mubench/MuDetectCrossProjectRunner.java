package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;

public class MuDetectCrossProjectRunner {
    public static void main(String[] args) throws Exception {
        new MuBenchRunner()
                .withMineAndDetectStrategy(new CrossProjectStrategy())
                .run(args);
    }
}
