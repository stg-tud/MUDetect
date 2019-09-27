<img align="right" width="320" height="320" alt="MUDetect Logo" src="./meta/logo.png?raw=true" />

# MUDetect : An API-Misuse Detector

This is the codebase of the API-misuse detector MUDetect. Please feel free to contact [Sven Amann](http://www.stg.tu-darmstadt.de/staff/sven_amann), if you have any questions.

## Contributors

* [Sven Amann](http://www.stg.tu-darmstadt.de/staff/sven_amann) (Project Lead)
* [Hoan Anh Nguyen](https://sites.google.com/site/nguyenanhhoan)

## Publications

* Amann, S.; Nguyen, H.; Nadi, S.; Nguyen, T.; and Mezini, M. [Investigating Next-Steps in Static API-Misuse Detection](http://sven-amann.de/publications/mudetect-msr19.pdf). In Proceedings of the 16th International Conference on Mining Software Repositories (MSR '19) , 2019. 

## Usage

### Through MUBench

We run the detector in our experiments through [the benchmarking pipeline MUBench](https://github.com/stg-tud/MUBench).
The respective detector runners are [MuDetectRunner](./mubench/src/main/java/de/tu_darmstadt/stg/mubench/MuDetectRunner.java) and [MuDetectCrossProjectRunner](./mubench/src/main/java/de/tu_darmstadt/stg/mubench/MuDetectCrossProjectRunner.java).

### Standalone

To run the detector directly, you may invoke it with one of the following commands, depending on whether you want to provide correct usage examples for pattern mining or whether the detector should mine patterns from the target project itself:

    $> java de.tu_darmstadt.stg.mubench.[X]Runner detector_mode "1" \
          pattern_src_path "/path/to/correct/usages/src" pattern_classpath "" \
          target_src_path "/path/to/target/project/src" target_classpath "" \
          dep_classpath "target:dependency:classpath" \
          target "findings-output.yml" run_info "run-info-output.yml"

    $> java de.tu_darmstadt.stg.mubench.[X]Runner detector_mode "0" \
          target_src_path "/path/to/target/project/src" target_classpath "" \
          dep_classpath "target:dependency:classpath" \
          target "findings-output.yml" run_info "run-info-output.yml"

### From Code

For examples on how to instantiate and invoke MUDetect from the code, please refer to
[our configuration for the intra-project setting](./mubench/src/main/java/de/tu_darmstadt/stg/mubench/IntraProjectStrategy.java) (MUDetect) and
[our configuration for the cross-project setting](./mubench/src/main/java/de/tu_darmstadt/stg/mubench/CrossProjectStrategy.java
) (MUDetectXP).

To use our code in your project, you can import all parts of MUDetect as Maven dependencies via our own repository.
Simply configure the following repository either in your project's `pom.xml` or in your local `settings.xml`:

    <repository>
        <id>stg-mubench</id>
        <name>MuBench</name>
        <url>http://www.st.informatik.tu-darmstadt.de/artifacts/mubench/mvn/</url>
    </repository>

Subsequently, you can depend on our code in your project.
To use the entire detector (AUG model, mining algorithm, and detection algorithm), declare the following dependency:

    <dependency>
        <groupId>de.tu-darmstadt.stg.mudetect</groupId>
        <artifactId>mubench</artifactId>
        <version>0.0.3-SNAPSHOT</version>
    </dependency>

To use only our model of API-usage graphs (AUGs), add the following dependency:

    <dependency>
        <groupId>de.tu-darmstadt.stg.mudetect</groupId>
        <artifactId>augs</artifactId>
        <version>0.0.3-SNAPSHOT</version>
    </dependency>

## License

All software provided in this repository is subject to the [Mozilla Public License Version 2.0](LICENSE.txt).

The project artwork is subject to the [Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)](https://creativecommons.org/licenses/by-sa/4.0/).
