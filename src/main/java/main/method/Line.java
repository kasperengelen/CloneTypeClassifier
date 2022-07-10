/**
 *   Copyright (C) 2020  Kasper Engelen
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.

 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.

 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
