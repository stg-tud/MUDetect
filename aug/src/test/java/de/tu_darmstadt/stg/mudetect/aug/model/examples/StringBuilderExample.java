package de.tu_darmstadt.stg.mudetect.aug.model.examples;

import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.aug.model.dot.DisplayAUGDotExporter;

import static de.tu_darmstadt.stg.mudetect.aug.builder.APIUsageExampleBuilder.buildAUG;

public class StringBuilderExample {
    public String code() {
        StringBuilder sb = new StringBuilder();
        sb.append("foo").append("bar");
        return sb.toString();
    }

    public void aug() {
        APIUsageExample aug = buildAUG(new Location("aug",
                "src/test/java/de/tu_darmstadt/stg/mudetect/aug/model/examples/StringBuilderExample.java",
                "code()"))
                .withConstructorCall("createSB", "java.lang.StringBuilder", 11)
                .withVariable("sb", "java.lang.StringBuilder", "sb")
                .withDefinitionEdge("createDB", "sb")
                .withLiteral("str1", "java.lang.String", "\"foo\"")
                .withMethodCall("append1", "java.lang.AbstractStringBuilder", "append()", 12)
                .withParameterEdge("str1", "append1")
                .withReceiverEdge("sb", "append1")
                .withLiteral("str2", "java.lang.String", "\"bar\"")
                .withMethodCall("append2", "java.lang.AbstractStringBuilder", "append()", 12)
                .withOrderEdge("append1", "append2")
                .withParameterEdge("str2", "append2")
                .withReceiverEdge("sb", "append2")
                .withMethodCall("toStr", "java.lang.AbstractStringBuilder", "toString()", 13)
                .withOrderEdge("append2", "toStr")
                .withReceiverEdge("sb", "toStr")
                .withAnonymousObject("result", "java.lang.String")
                .withDefinitionEdge("toStr", "result")
                .withReturn("ret", 13)
                .withParameterEdge("result", "ret")
                .build();

        System.out.println(new DisplayAUGDotExporter().toDotGraph(aug));
    }
}
