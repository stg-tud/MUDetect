package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;

public class MuDetectCrossProjectRunner {
    public static void main(String[] args) throws Exception {
        new MuBenchRunner()
                .withDetectOnlyStrategy(new ProvidedPatternsStrategy())
                .withMineAndDetectStrategy(new CrossProjectStrategy(CrossProjectStrategy.Mode.OFFLINE))
                .run(args);
    }
}
