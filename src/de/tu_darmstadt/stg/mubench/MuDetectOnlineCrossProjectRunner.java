package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;

import static de.tu_darmstadt.stg.mubench.CrossProjectStrategy.Mode.ONLINE;

public class MuDetectOnlineCrossProjectRunner {
    public static void main(String[] args) throws Exception {
        new MuBenchRunner()
                .withDetectOnlyStrategy(new ProvidedPatternsStrategy())
                .withMineAndDetectStrategy(new CrossProjectStrategy(ONLINE))
                .run(args);
    }
}
