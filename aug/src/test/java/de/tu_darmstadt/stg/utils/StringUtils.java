package de.tu_darmstadt.stg.utils;

public class StringUtils {
    /**
     * Splits camel-cased words into a sequence of words divided by space. Converts capitals to lower case.
     * E.g., <code>splitCamelCase("fooBarBaz").equals("foo bar baz")</code>.
     */
    public static String splitCamelCase(String camelCaseString) {
        return camelCaseString.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " ").toLowerCase();
    }
}
