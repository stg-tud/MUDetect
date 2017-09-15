package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;

public class MuDetectRunner {
    public static void main(String[] args) throws Exception {
        new MuBenchRunner()
                .withDetectOnlyStrategy(new ProvidedPatternsStrategy())
                .withMineAndDetectStrategy(new IntraProjectStrategy())
                .run(args);
    }
}
