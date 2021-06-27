package main.matching;

/**
 * Exception for when something goes wrong in {@link IMatcher} or {@link IMethodMatching}.
 */
public class MatchingException extends Exception
{
    /**
     * Constructor based on another exception.
     */
    public MatchingException(Throwable cause) {
        super(cause);
    }
}
