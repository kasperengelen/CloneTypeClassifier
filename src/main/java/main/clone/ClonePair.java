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

package main.clone;

import main.method.Method;

/**
 * Class that represents a pair of methods that have previously been marked as clones.
 */
public class ClonePair
{
    private final Method m_method1;
    private final Method m_method2;
    private final EnumCloneType m_manualClass;

    /**
     * Constructor.
     *
     * @param method_1 One method.
     * @param method_2 The other method.
     * @param manual_class The type that the clones has been manually been classified as.
     */
    public ClonePair(Method method_1, Method method_2, EnumCloneType manual_class)
    {
        m_method1 = method_1;
        m_method2 = method_2;
        m_manualClass = manual_class;
    }

    /**
     * Retrieve the first method of the clone pair.
     */
    public Method getMethod1()
    {
        return m_method1;
    }

    /**
     * Retrieve the second method of the clone pair.
     */
    public Method getMethod2()
    {
        return m_method2;
    }

    /**
     * Retrieve the clone type that has been manually assigned to the clone.
     */
    public EnumCloneType getManualClassification()
    {
        return m_manualClass;
    }
}
