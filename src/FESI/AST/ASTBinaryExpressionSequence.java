/* Generated By:JJTree: Do not edit this line. ASTBinaryExpressionSequence.java */

package FESI.AST;

import FESI.Parser.*;

public class ASTBinaryExpressionSequence extends SimpleNode {
  public ASTBinaryExpressionSequence(int id) {
    super(id);
  }

  public ASTBinaryExpressionSequence(EcmaScript p, int id) {
    super(p, id);
  }

  public static Node jjtCreate(int id) {
      return new ASTBinaryExpressionSequence(id);
  }

  public static Node jjtCreate(EcmaScript p, int id) {
      return new ASTBinaryExpressionSequence(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}