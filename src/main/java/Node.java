/*
 * Solution written with the help of Claude Code (Anthropic's CLI coding agent),
 * model Claude Opus 5.5 (claude-opus-5-5).
 */

import java.util.*;

/**
 * Tree node in first-child / next-sibling pointer representation.
 */
public class Node {

   private String name;
   private Node firstChild;
   private Node nextSibling;

   Node (String n, Node d, Node r) {
      name = n;
      firstChild = d;
      nextSibling = r;
   }

   /** Kind of the previously read token while parsing. */
   private enum Token { START, LEFT, NAME, COMMA, RIGHT }

   /**
    * Builds a tree from its right parenthetic (postfix) representation,
    * e.g. "(B1,C)A". Whitespace is allowed only around brackets and commas,
    * never inside a node name.
    *
    * @param s right parenthetic representation of the tree
    * @return root node of the tree
    * @throws RuntimeException if s is not a valid right parenthetic representation
    */
   public static Node parsePostfix (String s) {
      if (s == null)
         throw new RuntimeException ("Cannot build a tree: input string is null");
      if (s.trim().isEmpty())
         throw new RuntimeException (error (s, "input contains no nodes", -1));

      // frames.peek() holds the nodes collected inside the innermost open bracket;
      // the bottom frame holds the top-level node(s).
      Deque<List<Node>> frames = new ArrayDeque<> ();
      frames.push (new ArrayList<> ());
      List<Node> closedChildren = null; // children waiting for their parent's name
      Token prev = Token.START;
      int i = 0;
      int n = s.length();

      while (i < n) {
         char c = s.charAt (i);
         if (Character.isWhitespace (c)) {
            i++;
            continue;
         }
         switch (c) {
            case '(':
               if (prev == Token.NAME)
                  throw new RuntimeException (error (s,
                     "'(' directly after a node name (missing comma or misplaced subtree)", i));
               if (prev == Token.RIGHT)
                  throw new RuntimeException (error (s,
                     "'(' directly after ')' (subtree has no root name)", i));
               frames.push (new ArrayList<> ());
               prev = Token.LEFT;
               i++;
               break;
            case ')':
               if (frames.size() == 1)
                  throw new RuntimeException (error (s,
                     "')' without matching '('", i));
               if (prev == Token.LEFT)
                  throw new RuntimeException (error (s,
                     "empty subtree list \"()\"", i));
               if (prev == Token.COMMA)
                  throw new RuntimeException (error (s,
                     "missing subtree between ',' and ')'", i));
               if (prev == Token.RIGHT)
                  throw new RuntimeException (error (s,
                     "subtree has no root name (double brackets)", i));
               closedChildren = frames.pop();
               prev = Token.RIGHT;
               i++;
               break;
            case ',':
               if (frames.size() == 1)
                  throw new RuntimeException (error (s,
                     "',' outside of brackets (a tree must have exactly one root)", i));
               if (prev == Token.LEFT)
                  throw new RuntimeException (error (s,
                     "missing subtree between '(' and ','", i));
               if (prev == Token.COMMA)
                  throw new RuntimeException (error (s,
                     "missing subtree between two commas", i));
               if (prev == Token.RIGHT)
                  throw new RuntimeException (error (s,
                     "subtree before ',' has no root name", i));
               prev = Token.COMMA;
               i++;
               break;
            default:
               int start = i;
               while (i < n && !isDelimiter (s.charAt (i))) i++;
               String name = s.substring (start, i);
               if (prev == Token.NAME)
                  throw new RuntimeException (error (s,
                     "node name contains whitespace or two names are not separated by a comma: \""
                     + name + "\"", start));
               if (frames.size() == 1 && !frames.peek().isEmpty())
                  throw new RuntimeException (error (s,
                     "more than one root node", start));
               Node node = new Node (name, linkSiblings (closedChildren), null);
               closedChildren = null;
               frames.peek().add (node);
               prev = Token.NAME;
         }
      }

      if (frames.size() > 1)
         throw new RuntimeException (error (s,
            (frames.size() - 1) + " unclosed '(' bracket(s)", -1));
      if (prev == Token.RIGHT)
         throw new RuntimeException (error (s,
            "root of the tree has no name after the last ')'", -1));
      List<Node> top = frames.peek();
      if (top.size() != 1)
         throw new RuntimeException (error (s,
            "tree must have exactly one root, found " + top.size(), -1));
      return top.get (0);
   }

   /** Characters that terminate a node name. */
   private static boolean isDelimiter (char c) {
      return c == '(' || c == ')' || c == ',' || Character.isWhitespace (c);
   }

   /** Links the nodes into a sibling chain and returns the first one (or null). */
   private static Node linkSiblings (List<Node> nodes) {
      if (nodes == null || nodes.isEmpty()) return null;
      for (int k = 0; k < nodes.size() - 1; k++)
         nodes.get (k).nextSibling = nodes.get (k + 1);
      return nodes.get (0);
   }

   /** Builds an error message containing the whole input string. */
   private static String error (String s, String reason, int position) {
      StringBuilder b = new StringBuilder ();
      b.append ("Invalid tree string \"").append (s).append ("\": ").append (reason);
      if (position >= 0)
         b.append (" (at position ").append (position).append (')');
      return b.toString();
   }

   /**
    * Left parenthetic representation of the tree rooted at this node,
    * e.g. "A(B1,C)".
    *
    * @return the representation without spaces
    */
   public String leftParentheticRepresentation() {
      StringBuilder b = new StringBuilder ();
      appendLeft (this, b);
      return b.toString();
   }

   /** Appends the left parenthetic representation of the subtree into b. */
   private static void appendLeft (Node node, StringBuilder b) {
      b.append (node.name);
      if (node.firstChild != null) {
         b.append ('(');
         for (Node child = node.firstChild; child != null; child = child.nextSibling) {
            appendLeft (child, b);
            if (child.nextSibling != null) b.append (',');
         }
         b.append (')');
      }
   }

   public static void main (String[] param) {
      String[] valid = {
         "(B1,C)A",
         "A",
         "(((2,1)-,4)*,(69,3)/)+",
         "((C,(E)D)B,F)A",
         "( B1 , C ) A",
      };
      for (String s : valid) {
         Node t = Node.parsePostfix (s);
         String v = t.leftParentheticRepresentation();
         System.out.println (s + " ==> " + v); // (B1,C)A ==> A(B1,C)
      }
      System.out.println (new Node ("X", null, null).leftParentheticRepresentation());

      String[] invalid = {
         "", "\t", "A B", "(B,,C)A", "()A", "( , ) ", "A,B", "((C,D))A",
         "(,B)A", "(B)A,(D)C", ")A(", "((A)B(C)D)E", "((A),(C)D)E",
         "((A)(C)D)E", "((A)B,(C)D)", "(A", "A)", "(B,)A",
      };
      for (String s : invalid) {
         try {
            Node.parsePostfix (s);
            System.out.println ("NOT DETECTED: " + s);
         } catch (RuntimeException e) {
            System.out.println (e.getMessage());
         }
      }
   }
}
