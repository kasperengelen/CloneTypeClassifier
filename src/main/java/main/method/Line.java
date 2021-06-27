package main.method;

import java.util.List;

/**
 * Class that contains information about a single line of code. The line does not contain any whitespace, newlines, or comments.
 */
public class Line
{
    private final String m_lineContent;
    private final List<Token> m_tokens;

    /**
     * Constructor.
     *
     * @param line_content The exact content of the line.
     * @param tokens The tokens that line is made up of.
     */
    public Line(String line_content, List<Token> tokens)
    {
        m_lineContent = line_content;
        m_tokens = tokens;
    }

    /**
     * Retrieve the contents of the line.
     */
    public String getLineContent()
    {
        return m_lineContent;
    }

    /**
     * Retrieve the tokens of the line.
     */
    public List<Token> getTokens()
    {
        return m_tokens;
    }
}
