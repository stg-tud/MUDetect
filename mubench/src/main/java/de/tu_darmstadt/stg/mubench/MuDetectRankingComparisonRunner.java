package de.tu_darmstadt.stg.mubench;

import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;

public class MuDetectRankingComparisonRunner {
    public static void main(String[] args) throws Exception {
        new MuBenchRunner()
                .withMineAndDetectStrategy(new AlternativeRankingIntraProjectStrategy())
                .run(args);
    }
}
