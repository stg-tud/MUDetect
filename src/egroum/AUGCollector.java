package egroum;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AUGCollector {
    private final AUGConfiguration config;
    private final Collection<EGroumGraph> augs = new ArrayList<>();

    public AUGCollector(AUGConfiguration config) {
        this.config = config;
    }

    public void collectFrom(Path path, String[] dependencies) {
        augs.addAll(new EGroumBuilder(config).buildBatch(convertPath(path), dependencies));
    }

    private String convertPath(Path path) {
        return path.toAbsolutePath().toString();
    }

    public Collection<EGroumGraph> getAUGs() {
        return augs;
    }
}
