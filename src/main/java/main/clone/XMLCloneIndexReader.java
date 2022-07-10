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
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that reads an XML file that contains a list of clone pairs. It will output a list of clone pairs. The clone pairs will contain methods of the specified type.
 * Note that this class will simply read the index. It will not perform any verification, and it will not load the referenced source code files.
 */
public class XMLCloneIndexReader
{
    /**
     * Parse the specified XML file into a list of clones. All the paths specified in the XML file
     * will be relative to "source_file_root".
     *
     * @param xml_file The index file that contains a list of clones.
     * @param source_file_root Directory that contains all the paths specified in the XML file.
     */
    public static List<ClonePair> readIndex(File xml_file, File source_file_root) throws IOException
    {
        try {
            List<ClonePair> retval = new ArrayList<>();

            // read XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml_file);

            // iterate over clones specified in XML file
            NodeList xml_node_pairs = doc.getElementsByTagName("clone");
            // foreach <clone> in <clones>
            for (int i = 0; i < xml_node_pairs.getLength(); i++) {
                Node clone_node = xml_node_pairs.item(i);
                // only elements are relevant
                if (clone_node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element clone_elem = (Element) clone_node;

                // retrieve method information from XML
                // first method of clone pair
                NamedNodeMap attrs0 = clone_elem.getElementsByTagName("source").item(0).getAttributes();
                Method method0 = M_parseMethodXML(attrs0, source_file_root);

                // second method of clone pair
                NamedNodeMap attrs1 = clone_elem.getElementsByTagName("source").item(1).getAttributes();
                Method method1 = M_parseMethodXML(attrs1, source_file_root);

                // retrieve manual classification from XML, convert classification to enum
                String type_name = clone_elem.getAttribute("type");
                EnumCloneType manual_classification = EnumCloneType.fromNameInXMLFile(type_name);

                // construct clone pair and add to output list
                retval.add(new ClonePair(method0, method1, manual_classification));
            }

            return retval;
        } catch (SAXException | ParserConfigurationException | IllegalArgumentException e) {
            // encapsulate in IOException since this is all I/O
            throw new IOException(e);
        }
    }

    /**
     * Parse a method from the specified XML attribute map. The map has to contain attributes "source", "startline", "endline".
     *
     * @param method_attrs The XML attribute map that contains information about the method.
     * @param source_file_root The directory that contains the path that is specified in the attribute map under "source".
     */
    private static Method M_parseMethodXML(NamedNodeMap method_attrs, File source_file_root)
    {
        // construct path to file, this does NOT yet verify the existence of the file.
        File path = new File(source_file_root, method_attrs.getNamedItem("file").getTextContent());

        int begin = Integer.parseInt(method_attrs.getNamedItem("startline").getTextContent());
        int end = Integer.parseInt(method_attrs.getNamedItem("endline").getTextContent());

        return new Method(path, begin, end);
    }
}
