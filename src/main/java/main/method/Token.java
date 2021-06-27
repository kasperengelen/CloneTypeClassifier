package main.method;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import main.clone.EnumCloneType;

/**
 * A lexical token. This is used for both tokens and tree nodes.
 */
public class Token
{
    private final String m_contents;
    private final EnumTokenCategory m_category;

    /**
     * Construct a token from the specified Javaparser token.
     */
    public Token(JavaToken token)
    {
        this(token.getText(), EnumTokenCategory.fromJavaToken(token));
    }

    /**
     * Construct a token from the specified Javaparser AST leaf node.
     */
    public Token(Node ast_leaf_node)
    {
        this(ast_leaf_node.toString(), EnumTokenCategory.fromASTNode(ast_leaf_node));
    }

    /**
     * Constructor.
     *
     * @param contents The textual contents of the token.
     * @param cat The syntactic category that the token belongs to.
     */
    public Token(String contents, EnumTokenCategory cat)
    {
        m_contents = contents;
        m_category = cat;
    }

    /**
     * The textual contents of the token.
     */
    public String getContents()
    {
        return m_contents;
    }

    /**
     * The syntactic category that the token belongs to.
     */
    public EnumTokenCategory getCategory()
    {
        return m_category;
    }

    /**
     * Enum that lists the different categories that a token can belong to. This is so that when comparing tokens, the type
     * of the token can be taken into account.
     */
    public enum EnumTokenCategory
    {
        LITERAL,
        IDENTIFIER,
        OTHER;

        /**
         * Determine the category of the specified Javaparser token.
         */
        public static EnumTokenCategory fromJavaToken(JavaToken java_token)
        {
            switch(java_token.getCategory()) {
                case IDENTIFIER:
                    return IDENTIFIER;
                case LITERAL:
                    return LITERAL;
                default:
                    return OTHER;
            }
        }

        /**
         * Determine the category of the specified Javaparser AST leaf node.
         */
        public static EnumTokenCategory fromASTNode(Node ast_leaf_node)
        {
            if(ast_leaf_node instanceof SimpleName) {
                return IDENTIFIER;
            } else if(ast_leaf_node instanceof LiteralExpr) {
                return LITERAL;
            }

            return OTHER;
        }

        /**
         * Determine if two tokens with the specified categories form a parameterized match. This is true if they are both
         * identifiers or literals of the same type.
         */
        public static boolean isParameterizedMatch(EnumTokenCategory category_1, EnumTokenCategory category_2)
        {
            // only literals and identifiers can be parameterised
            if(category_1 == OTHER || category_2 == OTHER) {
                return false;
            }

            return category_1 == category_2;
        }
    }

    /**
     * Compare the two tokens.
     *
     * @param A The first token.
     * @param B The second token.
     * @param ignore_case If true, then contents of the tokens will be compared without paying attention to case.
     *
     * @return TYPE_1 if the contents are equal, TYPE_2 if they are both identifiers or literals of the same type, null otherwise.
     */
    public static EnumCloneType compareTokens(Token A, Token B, boolean ignore_case) {
        if(A.getContents().equals(B.getContents()) // check exact
                || (ignore_case && A.getContents().equalsIgnoreCase(B.getContents()))) // check without case
        {
            return EnumCloneType.TYPE_1;
        } else if (Token.EnumTokenCategory.isParameterizedMatch(A.getCategory(), B.getCategory())) {
            return EnumCloneType.TYPE_2;
        }

        return null;
    }
}
