import static org.junit.Assert.*;
import org.junit.Test;
// import java.util.*;

/** Testklass.
 * @author Jaanus
 */
public class NodeTest {

   @Test (timeout=1000)
   public void testParsePostfix() { 
      String s = "(B1,C,D)A";
      Node t = Node.parsePostfix (s);
      String r = t.leftParentheticRepresentation();
      assertEquals ("Tree: " + s, "A(B1,C,D)", r);
      s = "(((2,1)-,4)*,(69,3)/)+";
      t = Node.parsePostfix (s);
      r = t.leftParentheticRepresentation();
      assertEquals ("Tree: " + s, "+(*(-(2,1),4),/(69,3))", r);
   }

   @Test (timeout=1000)
   public void testParsePostfixAndLeftParentheticRepresentation1() {
      String s = "((((E)D)C)B)A";
      Node t = Node.parsePostfix (s);
      String r = t.leftParentheticRepresentation();
      assertEquals ("Tree: " + s, "A(B(C(D(E))))", r);
      s = "((C,(E)D)B,F)A";
      t = Node.parsePostfix (s);
      r = t.leftParentheticRepresentation();
      assertEquals ("Tree: " + s, "A(B(C,D(E)),F)", r);
   }

   @Test (timeout=1000)
   public void testParsePostfixAndLeftParentheticRepresentation2() {
      String s = "(((512,1)-,4)*,(-6,3)/)+";
      Node t = Node.parsePostfix (s);
      String r = t.leftParentheticRepresentation();
      assertEquals ("Tree: " + s, "+(*(-(512,1),4),/(-6,3))", r);
      s = "(B,C,D,E,F)A";
      t = Node.parsePostfix (s);
      r = t.leftParentheticRepresentation();
      assertEquals ("Tree: " + s, "A(B,C,D,E,F)", r);
      s = "((1,(2)3,4)5)6";
      t = Node.parsePostfix (s);
      r = t.leftParentheticRepresentation();
       assertEquals ("Tree: " + s, "6(5(1,3(2),4))", r);
   }

   @Test (timeout=1000)
   public void testSingleRoot() {
      String s = "ABC";
      Node t = Node.parsePostfix (s);
      String r = t.leftParentheticRepresentation();
      assertEquals ("Tree: " + s, "ABC", r);
      s = ".Y.";
      t = new Node (s, null, null);
      r = t.leftParentheticRepresentation();
      assertEquals ("Single node" + s, s, r);
   } 

   @Test (timeout=1000)
   public void testMore() {
      String s = "((A)B,(C)D)E";
      Node t = Node.parsePostfix (s);
      String r = t.leftParentheticRepresentation();
      assertEquals ("Tree: " + s, "E(B(A),D(C))", r);
   }

   @Test (expected=RuntimeException.class)
   public void testSpaceInNodeName() {
      Node root = Node.parsePostfix ("A B");
   }

   @Test (expected=RuntimeException.class)
   public void testTwoCommas() {
      Node t = Node.parsePostfix ("(B,,C)A");
   }

   @Test (expected=RuntimeException.class)
   public void testEmptySubtree() {
      Node root = Node.parsePostfix ("()A");
   }

   @Test (expected=RuntimeException.class)
   public void testInputWithBracketsAndComma() {
      Node t = Node.parsePostfix ("( , ) ");
   }

   @Test (expected=RuntimeException.class)
   public void testInputWithoutBrackets() {
      Node t = Node.parsePostfix ("A,B");
   }

   @Test (expected=RuntimeException.class)
   public void testInputWithDoubleBrackets() {
      Node t = Node.parsePostfix ("((C,D))A");
   }

   @Test (expected=RuntimeException.class)
   public void testComma1() {
      Node root = Node.parsePostfix ("(,B)A");
   }

   @Test (expected=RuntimeException.class)
   public void testComma2() {
      Node root = Node.parsePostfix ("(B)A,(D)C");
   }

   @Test (expected=RuntimeException.class)
   public void testComma3() {
      Node root = Node.parsePostfix ("(B,C)A,D");
   }

   @Test (expected=RuntimeException.class)
   public void testTab1() {
      Node root = Node.parsePostfix ("\t");
   }

   @Test (expected=RuntimeException.class)
   public void testWeirdBrackets() {
      Node root = Node.parsePostfix (")A(");
   }

   @Test (expected=RuntimeException.class)
   public void test17() {
      Node root = Node.parsePostfix ("((A)B(C)D)E");
   }

   @Test (expected=RuntimeException.class)
   public void test18() {
      Node root = Node.parsePostfix ("((A),(C)D)E");
   }

   @Test (expected=RuntimeException.class)
   public void test19() {
      Node root = Node.parsePostfix ("((A)(C)D)E");
   }

   @Test (expected=RuntimeException.class)
   public void test20() {
      Node root = Node.parsePostfix ("((A)B,(C)D)");
   }

   /** Removes all whitespace (spaces, tabs, newlines) for layout-independent comparison. */
   private static String noSpace (String s) {
      return s.replaceAll ("\\s+", "");
   }

   @Test (timeout=1000)
   public void testToXmlExample() {
      String s = "((C)B,(E,F)D,G)A";
      String expected =
         "<L1> A\n" +
         "    <L2> B\n" +
         "        <L3> C </L3>\n" +
         "    </L2>\n" +
         "    <L2> D\n" +
         "        <L3> E </L3>\n" +
         "        <L3> F </L3>\n" +
         "    </L2>\n" +
         "    <L2> G </L2>\n" +
         "</L1>\n";
      String r = Node.parsePostfix (s).toXML();
      assertEquals ("Tree: " + s, noSpace (expected), noSpace (r));
   }

   @Test (timeout=1000)
   public void testToXmlSingleNode() {
      String s = "ABC";
      String r = Node.parsePostfix (s).toXML();
      assertEquals ("Tree: " + s, "<L1>ABC</L1>", noSpace (r));
      r = new Node (".Y.", null, null).toXML();
      assertEquals ("Single node .Y.", "<L1>.Y.</L1>", noSpace (r));
   }

   @Test (timeout=1000)
   public void testToXmlChain() {
      String s = "((((E)D)C)B)A";
      String r = Node.parsePostfix (s).toXML();
      assertEquals ("Tree: " + s,
         "<L1>A<L2>B<L3>C<L4>D<L5>E</L5></L4></L3></L2></L1>", noSpace (r));
   }

   @Test (timeout=1000)
   public void testToXmlWide() {
      String s = "(B,C,D,E,F)A";
      String r = Node.parsePostfix (s).toXML();
      assertEquals ("Tree: " + s,
         "<L1>A<L2>B</L2><L2>C</L2><L2>D</L2><L2>E</L2><L2>F</L2></L1>",
         noSpace (r));
   }

   @Test (timeout=1000)
   public void testToXmlExpression() {
      String s = "(((512,1)-,4)*,(-6,3)/)+";
      String r = Node.parsePostfix (s).toXML();
      assertEquals ("Tree: " + s,
         "<L1>+<L2>*<L3>-<L4>512</L4><L4>1</L4></L3><L3>4</L3></L2>"
         + "<L2>/<L3>-6</L3><L3>3</L3></L2></L1>", noSpace (r));
   }

   @Test (timeout=1000)
   public void testToXmlMixed() {
      String s = "((C,(E)D)B,F)A";
      String r = Node.parsePostfix (s).toXML();
      assertEquals ("Tree: " + s,
         "<L1>A<L2>B<L3>C</L3><L3>D<L4>E</L4></L3></L2><L2>F</L2></L1>",
         noSpace (r));
   }

   @Test (timeout=1000)
   public void testToXmlExactLayout() {
      String s = "(B)A";
      String r = Node.parsePostfix (s).toXML();
      assertEquals ("Tree: " + s, "<L1> A\n    <L2> B </L2>\n</L1>\n", r);
   }

   @Test (timeout=1000)
   public void testToXmlDoesNotChangeTree() {
      String s = "((1,(2)3,4)5)6";
      Node t = Node.parsePostfix (s);
      t.toXML();
      assertEquals ("Tree: " + s, "6(5(1,3(2),4))", t.leftParentheticRepresentation());
   }

}

