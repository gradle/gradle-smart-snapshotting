package org.gradle.snapshotting.rules;

import org.gradle.snapshotting.contexts.Context;
import org.gradle.snapshotting.files.Fileish;

public class RuleMatcher {
    private final Iterable<? extends Rule<?, ?>> rules;

    public RuleMatcher(Iterable<? extends Rule<?, ?>> rules) {
        this.rules = rules;
    }

    public <F extends Fileish, C extends Context> Rule<? super F, ? super C> match(F file, C context) {
        Rule<?, ?> matchedRule = null;
        for (Rule<?, ?> rule : rules) {
            if (rule.getContextType().isAssignableFrom(context.getType())
                && rule.getFileType().isAssignableFrom(file.getClass())
                && (rule.getPathMatcher() == null || rule.getPathMatcher().matcher(file.getPath()).matches())) {
                matchedRule = rule;
                break;
            }
        }
        if (matchedRule == null) {
            throw new IllegalStateException(String.format("Cannot find matching rule for %s in context %s", file, context));
        }
        //noinspection unchecked
        return (Rule<F, C>) matchedRule;
    }
}
