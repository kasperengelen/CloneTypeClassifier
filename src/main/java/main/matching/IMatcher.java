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
