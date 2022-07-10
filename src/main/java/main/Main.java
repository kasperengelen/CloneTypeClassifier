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

package main;

import java.io.File;
import java.util.*;

import main.clone.ClonePair;
import main.clone.XMLCloneIndexReader;
import main.matching.*;

/**
 * Main class.
 */
public class Main
{
    /**
     * Output the specified string.
     *
     * @param format The string.
     * @param args Parameters.
     */
    public static void log(String format, Object... args)
    {
        System.out.printf("[LOG] " + format + "\n", args);
    }

    /**
     * Main function.
     *
     * @param args <matcher_type> <xml_path> <source_directory>
     */
    public static void main(String[] args)
    {
        System.out.println("Running application...");
        try {
            // args: <matcher_type> <xml_path> <source_directory>
            if(args.length != 3) {
                Main.log("Error: invalid arguments. First argument must be matcher type (line, token, tree_preorder, tree_postorder), second argument must be XML file path, third argument must be source files directory.");
                return;
            }

            // retrieve arguments
            String matcher_type = args[0];
            File xml_path = new File(args[1]);
            File source_root = new File(args[2]);

            // check if files exist
            if(!xml_path.isFile() || !source_root.isDirectory()) {
                Main.log("Error: xml path must be a file, source files directory must be a directory.");
            }

            // create matcher
            IMatcher matcher;
            switch (matcher_type) {
                case "line":
                    matcher = (method1, method2) -> new LineMatching(method1, method2, SequenceComparisonAlgos::computeLCS, -1, -1, false, true);
                    break;
                case "token":
                    matcher = ((method1, method2) -> new TokenMatching(method1, method2, SequenceComparisonAlgos::computeLCS, -1, -1));
                    break;
                case "tree_preorder":
                    matcher = ((method1, method2) -> new TraversalTreeMatching(method1, method2, SequenceComparisonAlgos::computeLCS, -1, -1, true));
                    break;
                case "tree_postorder":
                    matcher = ((method1, method2) -> new TraversalTreeMatching(method1, method2, SequenceComparisonAlgos::computeLCS, -1, -1, false));
                    break;
                default:
                    Main.log("Invalid matcher type: '%s'", matcher_type);
                    return;
            }

            // read clones
            List<ClonePair> dataset = XMLCloneIndexReader.readIndex(xml_path, source_root);

            // create and run application
            Application app = new Application(
                    "Clone Viewer",
                    dataset,
                    matcher
            );
            app.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
