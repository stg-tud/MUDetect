package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import edu.iastate.cs.mudetect.mining.Model;

public class RankingTestUtils {
    static ViolationWeightFunction w(Function<Overlap, Overlaps, Model, Double> weight) {
        return new ViolationWeightFunction() {
            @Override
            public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
                return weight.apply(violation, overlaps, model);
            }

            @Override
            public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getId() {
                return ":test-weight-function:";
            }
        };
    }

    @FunctionalInterface
    interface Function<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
