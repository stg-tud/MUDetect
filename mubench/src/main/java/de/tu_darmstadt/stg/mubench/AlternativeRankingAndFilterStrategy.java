package de.tu_darmstadt.stg.mubench;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.tu_darmstadt.stg.mudetect.AlternativePatternInstancePredicate;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import edu.iastate.cs.mudetect.mining.Model;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static de.tu_darmstadt.stg.mudetect.AlternativeViolationPredicate.firstAlternativeViolation;

public class AlternativeRankingAndFilterStrategy implements BiFunction<Overlaps, Model, List<Violation>> {
    private final Collection<ViolationWeightFunction> strategies;
    private final AlternativePatternInstancePredicate alternativePatternInstancePredicate;

    public AlternativeRankingAndFilterStrategy() {
        strategies = new ArrayList<>();
        strategies.add(new RarenessWeightFunction());
        strategies.add(new DefectIndicatorWeightFunction());
        strategies.add(new ConfidenceWeightFunction());

        Collection<Set<ViolationWeightFunction>> permutations = Sets.powerSet(ImmutableSet.of(
                new OverlapWithoutEdgesToMissingNodesWeightFunction(new ConstantNodeWeightFunction()),
                new PatternSupportWeightFunction(),
                new PatternViolationsWeightFunction(),
                new ViolationSupportWeightFunction(),
                new PatternUniquenessWeightFunction()
        ));
        for (Set<ViolationWeightFunction> permutation : permutations) {
            if (permutation.isEmpty()) {
                continue;
            }
            strategies.add(new ProductWeightFunction(permutation.toArray(new ViolationWeightFunction[0])));
        }
        alternativePatternInstancePredicate = new AlternativePatternInstancePredicate();
    }

    @Override
    public List<Violation> apply(Overlaps overlaps, Model model) {
        SortedMap<String, List<Violation>> alternativeRankings = buildAlternativeRankings(overlaps, model);

        List<Violation> violations = new ArrayList<>();
        for (Overlap violationOverlap : overlaps.getViolations()) {
            String rankInfo = getRankInfo(alternativeRankings, violationOverlap);
            if (rankInfo != null) {
                violations.add(new Violation(violationOverlap, 1.0, rankInfo));
            }
        }
        return violations;
    }

    private SortedMap<String, List<Violation>> buildAlternativeRankings(Overlaps overlaps, Model model) {
        SortedMap<String, List<Violation>> alternativelyRankedViolations = new TreeMap<>();
        for (ViolationWeightFunction strategy : strategies) {
            List<Violation> rankedViolations = new WeightRankingStrategy(strategy).rankViolations(overlaps, model).stream()
                    .filter(violation -> isNotAlternativePatternInstance(violation, overlaps))
                    .filter(firstAlternativeViolation())
                    .collect(Collectors.toList());

            alternativelyRankedViolations.put(strategy.getId(), rankedViolations);
        }
        return alternativelyRankedViolations;
    }

    private boolean isNotAlternativePatternInstance(Violation violation, Overlaps overlaps) {
        Overlap overlap = violation.getOverlap();
        Set<Overlap> instancesInViolationTarget = overlaps.getInstancesInSameTarget(overlap);
        return !alternativePatternInstancePredicate.test(overlap, instancesInViolationTarget);
    }

    private String getRankInfo(SortedMap<String, List<Violation>> alternativeRankings, Overlap violationOverlap) {
        StringBuilder rankInfo =
                new StringBuilder("<table style=\"display:block;height:400px;width:500px;overflow:scroll;border: 1px solid black;\">")
                .append("<thead><tr><th>Strategy</th><th>Rank</th><th>Desc</th></tr></thead><tbody>");
        boolean possiblyNotFiltered = false;
        for (String strategyId : alternativeRankings.keySet()) {
            List<Violation> alternativeRanking = alternativeRankings.get(strategyId);
            possiblyNotFiltered |= appendRankInfo(rankInfo, strategyId, alternativeRanking, violationOverlap);
        }
        rankInfo.append("</tbody></table>");
        return possiblyNotFiltered ? rankInfo.toString() : null;
    }

    private boolean appendRankInfo(StringBuilder rankInfo, String strategyId, List<Violation> alternativeRanking, Overlap violationOverlap) {
        int rank = 0;
        Violation violation = null;
        for (; rank < alternativeRanking.size(); rank++) {
            if (alternativeRanking.get(rank).getOverlap() == violationOverlap) {
                violation = alternativeRanking.get(rank);
                break;
            }
        }

        return appendRankInfo(rankInfo, strategyId, rank, violation);
    }

    private boolean appendRankInfo(StringBuilder rankInfo, String strategyId, int rank, Violation violation) {
        rankInfo.append("<tr><td>").append(strategyId);
        if (violation != null) {
            rankInfo.append("</td><td>").append(rank)
                    .append("</td><td>").append(violation.getConfidenceString());
        } else {
            rankInfo.append("</td><td>").append("-")
                    .append("</td><td>").append("filtered");
        }
        rankInfo.append("</td></tr>");
        return violation != null;
    }
}
