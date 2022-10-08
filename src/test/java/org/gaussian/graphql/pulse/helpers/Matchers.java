package org.gaussian.graphql.pulse.helpers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.Map;

public class Matchers {

    public static Matcher<List<Map<String, Object>>> hasPulseCounter(String type, String field, int value) {
        return new TypeSafeMatcher<>() {
            @Override
            public boolean matchesSafely(List<Map<String, Object>> items) {
                return items.stream()
                        .anyMatch(item -> String.valueOf(item.get("type")).equals(type) &&
                                String.valueOf(item.get("field")).equals(field) &&
                                item.get("value").equals(value)
                        );
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has items with type")
                        .appendValue(type)
                        .appendText("with field")
                        .appendValue(field)
                        .appendText("with value")
                        .appendValue(value);
            }
        };
    }

}