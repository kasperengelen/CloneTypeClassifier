package main.method;

import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a method. This contains the exact location of the method, as well as utilities to retrieve multiple representations of the method.
 */
public class Method
{
    private final File m_path;
    private final int m_beginLine;
    private final int m_endLine;

    /**
     * Constructor.
     *
     * @param path The path of the source file that contains the method.
     * @param begin_line The number of the first line of the method (1-indexed).
     * @param end_line The number of the last line of the method (1-indexed).
     */
    public Method(File path, int begin_line, int end_line) {
        m_path = path;
        m_beginLine = begin_line;
        m_endLine = end_line;
    }

    /**
     * The path of the source file that contains the method.
     */
    public File getPath()
    {
        return m_path;
    }

    /**
     * The number of the first line of the method (1-indexed).
     */
    public int getBegin()
    {
        return m_beginLine;
    }

    /**
     * The number of the last line of the method (1-indexed).
     */
    public int getEnd()
    {
        return m_endLine;
    }

    /**
     * Retrieve the raw source code text of the method. This will simply read the source file and return all lines between begin
     * and end.
     */
    public String getText() throws IOException
    {
        StringBuilder file_contents = new StringBuilder();

        // read all lines in the file
        int i = 1;
        for (String line : Files.readAllLines(m_path.toPath())) {
            if(i < m_beginLine) {
                i++;
                continue;
            }

            // append to output
            file_contents.append(line + "\n");

            if(i >= m_endLine) {
                break;
            }

            i++;
        }

        return file_contents.toString();
    }

    /**
     * Retrieve the body of the method. This will not include the method signature.
     */
    public Node getMethodBody(boolean remove_signature) throws IOException
    {
        BodyDeclaration<?> method_decl = StaticJavaParser.parseBodyDeclaration(this.getText());

        if(remove_signature) {
            if(method_decl.isMethodDeclaration()) {
                return method_decl.asMethodDeclaration().getBody().get();
            } else if(method_decl.isConstructorDeclaration()) {
                return method_decl.asConstructorDeclaration().getBody();
            } else {
                throw new IllegalStateException("Code does not represent a method or a constructor body.");
            }
        } else {
            return method_decl;
        }
    }

    /**
     * Retrieve a line-by-line representation of the source. All whitespace and comments have been removed, and the code has been pretty-printed to enforce a uniform representation.
     */
    public List<Line> getLines() throws IOException
    {
        // set pretty printer to remove comments and whitespace
        PrettyPrinterConfiguration config = new PrettyPrinterConfiguration();
        config.setPrintComments(false);
        config.setPrintJavadoc(false);
        config.setIndentSize(0);

        // retrieve pretty-printed source
        String pretty_printed = this.getMethodBody(true).toString(config);

        // split source into lines
        List<String> line_list = Arrays.asList(pretty_printed.split("\n"));

        // parse pretty-printed source so we can tokenize it using Javaparser
        BodyDeclaration normalized_method = StaticJavaParser.parseBodyDeclaration(pretty_printed);

        // we iterate over tokens and group them line-by-line
        List<List<Token>> tokenized_source = new ArrayList<>();
        tokenized_source.add(new ArrayList<>());
        for (JavaToken token : normalized_method.getTokenRange().get()) {
            // skip irrelevant tokens
            if(token.getCategory() == JavaToken.Category.WHITESPACE_NO_EOL || token.getCategory() == JavaToken.Category.COMMENT) {
                continue;
            }

            if(token.getCategory() == JavaToken.Category.EOL) {
                tokenized_source.add(new ArrayList<>());
                continue;
            }

            // create new Token based on the JavaToken instance
            tokenized_source.get(tokenized_source.size() - 1).add(new Token(token));
        }

        List<Line> retval = new ArrayList<>();

        // merge lines and their respective token list
        for(int i = 0; i < line_list.size(); i++) {
            retval.add(new Line(line_list.get(i), tokenized_source.get(i)));
        }

        return retval;
    }

    /**
     * Retrieve a tokenized representation of the method source code. This will be a tokenized representation of the return value of {@link Method#getText()}.
     */
    public List<Token> getTokens() throws IOException
    {
        List<Token> retval = new ArrayList<>();

        for (JavaToken token : this.getMethodBody(true).getTokenRange().get()) {
            if(token.getCategory() == JavaToken.Category.WHITESPACE_NO_EOL
                    || token.getCategory() == JavaToken.Category.COMMENT
                    || token.getCategory() == JavaToken.Category.EOL
                    //|| token.getCategory() == JavaToken.Category.SEPARATOR
            )
            {
                continue;
            }

            retval.add(new Token(token));
        }

        return retval;
    }

    /**
     * Traverse the tree. Each leaf node is converted to a {@link Token}.
     *
     * @param preorder True if the tree leaves will be traversed in preorder. If false, the postorder traversal will be used.
     */
    public List<Token> getLeafTraversal(boolean preorder) throws IOException
    {
        List<Token> retval = new ArrayList<>();

        Iterator<Node> iterator;

        if(preorder) {
            iterator = new Node.PreOrderIterator(this.getMethodBody(true));
        } else {
            iterator = new Node.PostOrderIterator(this.getMethodBody(true));
        }

        while(iterator.hasNext()) {
            Node n = iterator.next();
            if(n.getChildNodes().size() == 0) {
                retval.add(new Token(n));
            }
        }

        return retval;
    }

    @Override
    public String toString() {
        return String.format("%s:%d:%d", m_path.toString(), m_beginLine, m_endLine);
    }
}
