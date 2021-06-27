package main.matching;

import main.clone.EnumCloneType;
import main.method.Method;

import java.io.IOException;

/**
 * Interface for producing a matching between two methods.
 */
@FunctionalInterface
public interface IMatcher
{
    /**
     * Match the two specified methods to produce an {@link IMethodMatching}.
     *
     * @param method1 The first method.
     * @param method2 The second method.
     */
    IMethodMatching match(Method method1, Method method2) throws MatchingException;
}
