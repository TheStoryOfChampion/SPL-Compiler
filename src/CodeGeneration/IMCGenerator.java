package CodeGeneration;

import Node.Node;
import Node.Scope;
import Node.SymbolType;
import Node.TreeNode;
import com.sun.source.tree.Tree;

import java.sql.SQLInvalidAuthorizationSpecException;

public class IMCGenerator {
    private TreeNode treeRoot;
    static int LineCounter = 10;
    public static String BasicCode = "";
    boolean inBranch = false;
    boolean inLoop = false;
    int branchCount = 0;
    int loopCount = 0;
    int scopeNum = 0;
    String logicExpr = "";
    int currentProcID = 0;
    Scope scoping = null;
    public IMCGenerator(TreeNode treeRoot)
    {
        this.treeRoot = treeRoot;
    }

    public String start() throws Exception
    {
        if(treeRoot != null){
            if(treeRoot.getValue().equals(SymbolType.PROG.name()) && !treeRoot.isTerminal() )
            {
                Scope scope = new Scope( );
                treeRoot.setScope(scope);
                for (TreeNode child: treeRoot.getChildren())
                {
                    crawlDown(child, 1, "PROG");
                }
                return BasicCode;
            }
        }
        throw new Exception("root node is not SPL");

    }

    private void crawlDown(TreeNode node, int currentScope, String currentProc){
        if(node == null){
            return;
        }


        if(!node.isTerminal()){
            if(node.getValue().equals("PROG")){
                if(node.getChildren().size() ==2){
                    crawlDown(node.getChildren().get(0), currentScope, currentProc);
                    BasicCode+= String.valueOf(LineCounter) + "\n";
                    LineCounter+=10;
                    crawlDown(node.getChildren().get(1), currentScope, currentProc);
                }
                else{
                    crawlDown(node.getChildren().get(0), currentScope, currentProc);
                    BasicCode += String.valueOf(LineCounter) + "\n";
                    LineCounter+=10;

                }
                return;
            }
            else if(node.getValue().equals("INOUT")){
                TreeNode theNode = node.getChildren().get(1); // NUMVAR
                theNode = theNode.getChildren().get(1); // DIGITS
                if(node.getChildren().get(0).equals("input")){
//                    TransInput(node, currentProc);
                    theNode = node.getChildren().get(0); // NUMVAR
                    theNode = theNode.getChildren().get(1);// DIGITS
                    String digits = formatName(theNode, currentScope);
                    BasicCode += LineCounter + " INPUT \"\"; " + " n" + digits + "\n ";
                    LineCounter += 10;
                }
                else if(node.getChildren().get(0).equals("print")){ // TEXT OR VALUE
//                    TransOutput(node, currentScope);
                    theNode = node.getChildren().get(1); // NUMVAR
                    theNode = theNode.getChildren().get(1); // DIGITS
                    if (theNode.getValue().equals("text")) {
                        theNode = theNode.getChildren().get(0); // STRINGV
                        String digits = formatName(theNode, currentScope);
                        TreeNode theNode2 = theNode.getChildren().get(1);
                        BasicCode += LineCounter + "  PRINT ; " + " s" + digits + "$\n ";
                        LineCounter += 10;
                    } else if (theNode.getValue().equals("VALUE")) {
                        theNode = theNode.getChildren().get(1); // NUMVAR
                        String digits = concatenateDigits(theNode);
                        theNode = theNode.getChildren().get(1);// DIGITS

                        BasicCode += LineCounter + "  PRINT ; " + " n" + digits + "\n ";
                        LineCounter += 10;

                    }
                }



                // LineCounter INPUT “”; var_name

            }
            /*else if(node.getValue().equals("OUTPUT")){
                TreeNode theNode = node.getChildren().get(0); // TEXT OR VALUE
                if (theNode.getValue().equals("TEXT")) {
                    theNode = theNode.getChildren().get(1); // STRINGV

                    TreeNode theNode2 = theNode.getChildren().get(1); // Digits
                    String digits = concatenateDigits(theNode2);
                    BasicCode += LineCounter + "  PRINT ; " + " s" + digits + "$\n ";
                    LineCounter += 10;


                } else if (theNode.getValue().equals("VALUE")) {
                    theNode = theNode.getChildren().get(1); // NUMVAR

                    theNode = theNode.getChildren().get(1);// DIGITS

                    String digits = concatenateDigits(theNode);
                    BasicCode += LineCounter + "  PRINT ; " + " n" + digits + "\n ";
                    LineCounter += 10;

                }
            }*/
            else if(node.getValue().equals("ASSGN")){
                TransAssgn(node);
            }

            else if(node.getValue().equals("BRANCH")){

                TransBra(node, currentScope, currentProc);
                branchCount++;
                return;
            }
            else if(node.getValue().equals("LOOP")){

                TransLP(node, currentScope, currentProc);
                loopCount++;
                return;
            }

            for(TreeNode child: node.getChildren()){
                crawlDown(child, currentScope, currentProc);
            }

        } else if(node.getValue().equals("CALLP")){
            String procName = node.getChildren().get(1).getChildren().get(0).getValue();
            BasicCode += String.valueOf(LineCounter)+ " GOSUB " + procName + "\n";
            LineCounter += 10;

        } else if(node.getValue().equals("dummy")){
            if(inLoop){
                inLoop = false;
                BasicCode += String.valueOf(LineCounter) + " REM LOOP ENDS HERE\n";
                LineCounter++;
                return;
            }
            else if (inBranch){
                inBranch = false;
                BasicCode += String.valueOf(LineCounter) + " REM END OF BRANCH";
                LineCounter++;
                return;
            }
            else {
                BasicCode += LineCounter + " RETURN" +"\n ";
                LineCounter += 10;
            }
        }
        else if (node.getValue().equals("PROCDEFS")){
            if (!node.getChildren().isEmpty()){
                String proc = node.getChildren().get(1).getChildren().get(0).getValue();
                for (int c = 0 ; c < BasicCode.length() ; c++){
                    if (BasicCode.charAt(c) == 'G'){
                        String gSub = "";
                        for (int i = c ; i < BasicCode.length() ; i++){
                            if (BasicCode.charAt(i) == ' ')
                                break;
                            gSub += BasicCode.charAt(i);
                        }

                        if (gSub.equals("GOSUB")){
                            BasicCode = BasicCode.replace(gSub + " " + proc, gSub + " " + String.valueOf(LineCounter));
                            break;
                        }
                    }
                }
                crawlDown(node.getChildren().get(3), currentScope, proc);
            }
        }
        else{

            crawlDown(null, currentScope, currentProc);
        }
    }

    private void TransLP(TreeNode node, int currentScope, String currentProc) {
        String boolexpr = TransBoolExpr(node.getChildren().get(3));
        int loops = loopCount;
        int enL = LineCounter;
        int temp = loops;
        inLoop = true;
        crawlDown(node.getChildren().get(2), currentScope, currentProc);
        BasicCode += String.valueOf(LineCounter) + " " + "IF " + boolexpr + " THEN GOTO exit" + loops+    "\n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " " + "GOTO repeat" + temp +   "\n";
        LineCounter += 10;
//        crawlDown(node.getChildren().get(5), currentScope, currentProc);
        int exitLineCounter = LineCounter;


        BasicCode = BasicCode.replace("repeat" , String.valueOf(enL));
        BasicCode = BasicCode.replace("exit" , String.valueOf(exitLineCounter));



    }


    private void TransBra(TreeNode node, int currentScope, String currentProc) {
        String boolexpr = TransBoolExpr(node.getChildren().get(2));
         int branches = branchCount;


        BasicCode+= String.valueOf(LineCounter) + " " + "IF " + boolexpr + " THEN GOTO Branch" + branches + "\n";
        LineCounter += 10;
        crawlDown(node.getChildren().get(8), currentScope, currentProc);
        BasicCode+= String.valueOf(LineCounter) + " " + "GOTO exit" +   "\n";
        LineCounter += 10;
        int thenLineCounter = LineCounter;
        crawlDown(node.getChildren().get(4), currentScope, currentProc);
        int exitLineCounter = LineCounter;


        BasicCode = BasicCode.replace("branch" , String.valueOf(thenLineCounter));
        BasicCode = BasicCode.replace("exit"  , String.valueOf(exitLineCounter));

    }

    private void TransAssgn(TreeNode node) {
        String lefthandside="";
        String righthandside    = "";
        TreeNode theNode = node.getChildren().get(0).getChildren().get(0); // NUMVAR

        theNode = theNode.getChildren().get(1);// DIGITS

        String digits = concatenateDigits(theNode);

        lefthandside = node.getChildren().get(0).getChildren().get(0).getValue();

        if(node.getChildren().get(3).equals("NAME")){
//            lefthandside = " n" + digits;
            righthandside = TransNumExpr(node.getChildren().get(3));
            BasicCode += LineCounter  +" LET "+ lefthandside + " = " + righthandside + "\n ";
            LineCounter++;
        }
        else if(node.getChildren().get(3).equals("CONST")){
//            lefthandside = " b" + digits;
            righthandside = TransBoolExpr(node.getChildren().get(3));
            BasicCode += LineCounter  +" LET "+ lefthandside + " = " + righthandside + "\n ";
            LineCounter++;

        }else{
            if(node.getChildren().get(0).getValue().equals("add") || node.getChildren().get(0).getValue().equals("mult")){
                righthandside = TransNumExpr(node);
            }
            else if(node.getChildren().get(0).getValue().equals("eq") || node.getChildren().get(0).getValue().equals("or") || node.getChildren().get(0).getValue().equals("and") || node.getChildren().get(0).getValue().equals("not")){
                righthandside = TransBoolExpr(node);
            }
            BasicCode += LineCounter  +" LET "+ lefthandside + " = " + righthandside + "\n ";
            LineCounter++;

        }

        if (lefthandside.equals("text")){
            righthandside = "\"" + righthandside + "\"";
            BasicCode += LineCounter + " LET " + lefthandside + " = "+ righthandside + "\n";
            LineCounter++;
        }

    }
    private String TransBoolExpr(TreeNode node) {
        if(node.getChildren().get(0).getValue().equals("eq")){
            return TransCmpr(node);
        }
        else if(node.getChildren().get(0).getValue().equals("larger")){
            return TransCmpr(node);
        }
        else if (node.getChildren().get(0).getValue().equals("or")){
            TransOr(node);
            return "P";
        }
        else if (node.getChildren().get(0).getValue().equals("and")){
            TransAnd(node);
            return "P";
        }
        else if (node.getChildren().get(0).getValue().equals("not")){
            TransNot(node);
            return "P";
        }
        else if(node.getChildren().get(0).equals("CONST")){
            TreeNode cons = node.getChildren().get(0).getChildren().get(0).getChildren().get(0);
            String val = cons.getValue();
            if (node.getChildren().get(0).getValue().equals("TRUTH"))
                val = TransLogic(node.getChildren().get(0));
            return val;
        }
        else if (node.getChildren().get(0).getValue().equals("NAME")){
            TreeNode tmp = node.getChildren().get(0).getChildren().get(0);
            String name = formatName(tmp, currentProcID);

            return name;
        } else {
            return "";
        }
    }



    private String TransCmpr(TreeNode node) {
        if(node.getChildren().get(0).equals("eq")){
            return TransNumExpr(node.getChildren().get(2)) + " = " + TransNumExpr(node.getChildren().get(4));
        }
        else if(node.getChildren().get(0).equals("larger")){
            return TransNumExpr(node.getChildren().get(2)) + " > " + TransNumExpr(node.getChildren().get(4));
        }
        else{
            return "";
        }

    }

    private String TransLogic(TreeNode node) {
        if(node.getChildren().get(0).equals("BOOLVAR")){
            TreeNode theNode = node.getChildren().get(0).getChildren().get(1);// DIGITS

            String digits = concatenateDigits(theNode);
            String boolv = "b" + digits;
            return boolv;

        }
        else if(node.getChildren().get(0).equals("T")){
            return "1";
        }
        else if(node.getChildren().get(0).equals("F")){
            return "0";
        }
        else if(node.getChildren().get(0).equals("and")){
            TransAnd(node);
            return "P";
        }
        else if(node.getChildren().get(0).equals("or")){
            TransOr(node);
            return "P";


        }
        else if(node.getChildren().get(0).equals("not")){
            TransNot(node);
            return "P";


        }
        else{
            return "";

        }

    }

    private void TransNot(TreeNode node) {
        String boolexpr1 = TransBoolExpr(node.getChildren().get(2));


        BasicCode += String.valueOf(LineCounter) + " IF " + boolexpr1 + " THEN GOTO failed \n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " LET P = 1 \n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " GOTO exit \n";
        LineCounter += 10;
        int failedLineNumber = LineCounter;
        BasicCode += String.valueOf(LineCounter) + " LET P = 0 \n";
        LineCounter += 10;
        int exitLineNumber = LineCounter;


        BasicCode = BasicCode.replace("failed", String.valueOf(failedLineNumber));
        BasicCode = BasicCode.replace("exit", String.valueOf(exitLineNumber));

    }

    private void TransOr(TreeNode node) {

        String blexpr1 = TransBoolExpr(node.getChildren().get(2));
        String blexpr2 = TransBoolExpr(node.getChildren().get(4));


        BasicCode += String.valueOf(LineCounter) + " IF " + blexpr1 + " THEN GOTO success \n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " IF " + blexpr2 + " THEN GOTO success \n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " GOTO failed \n";
        LineCounter += 10;
        int successLineCounter = LineCounter;
        BasicCode += String.valueOf(LineCounter) + " LET P = 1 \n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " GOTO exit \n";
        LineCounter += 10;
        int failedLineCounter = LineCounter;
        BasicCode += String.valueOf(LineCounter) + " LET P = 0 \n";
        LineCounter += 10;
        int exitLineCounter = LineCounter;


        BasicCode = BasicCode.replace("success", String.valueOf(successLineCounter));
        BasicCode = BasicCode.replace("failed", String.valueOf(failedLineCounter));
        BasicCode = BasicCode.replace("exit", String.valueOf(exitLineCounter));



    }

    private void TransAnd(TreeNode node) {
        String blexpr1 = TransBoolExpr(node.getChildren().get(2));
        String blexpr2 = TransBoolExpr(node.getChildren().get(4));


        BasicCode += String.valueOf(LineCounter) + " IF " + blexpr1 + " THEN GOTO otherCond \n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " GOTO failed \n";
        LineCounter += 10;
        int otherCondLineCounter = LineCounter;
        BasicCode += String.valueOf(LineCounter) + " IF " + blexpr2 + " THEN GOTO success \n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " GOTO failed \n";
        LineCounter += 10;
        int successLineCounter = LineCounter;
        BasicCode += String.valueOf(LineCounter) + " LET P = 1 \n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " GOTO exit \n";
        LineCounter += 10;
        int failedLineCounter = LineCounter;
        BasicCode += String.valueOf(LineCounter) + " LET P = 0 \n";
        LineCounter += 10;
        int exitLineCounter = LineCounter;


        BasicCode = BasicCode.replace("otherCond", String.valueOf(otherCondLineCounter));
        BasicCode = BasicCode.replace("success", String.valueOf(successLineCounter));
        BasicCode = BasicCode.replace("failed", String.valueOf(failedLineCounter));
        BasicCode = BasicCode.replace("exit", String.valueOf(exitLineCounter));




    }

    String TransNumExpr(TreeNode node){

        if(node.getChildren().get(0).equals("NAME")){
            TreeNode nm  = node.getChildren().get(0).getChildren().get(0);
            String var = nm.getValue();
            var = formatName(nm, currentProcID);
            return var;

        }else if(node.getChildren().get(0).equals("CONST")){
            TreeNode cons = node.getChildren().get(0).getChildren().get(0).getChildren().get(0);
            String val = cons.getValue();
            return  val;

        }else if(node.getChildren().get(0).equals("add")){
            return TransNumExpr(node.getChildren().get(2)) + "+" + TransNumExpr(node.getChildren().get(4));

        }else if(node.getChildren().get(0).equals("mult")){
            return TransNumExpr(node.getChildren().get(2)) + "*" + TransNumExpr(node.getChildren().get(4));

        }
        return "";
    }

    private String formatName(TreeNode var, int currentSCop){
        String type = "";
        String name = var.getValue();

        if (var.getParent().getChildren().get(0).getValue().equals("num") || var.getParent().getChildren().get(0).getValue().equals("bool") ){
            name = name.toUpperCase();
            name = name.charAt(0) + String.valueOf(name.charAt(2));
        }

        if (var.getParent().getChildren().get(0).getValue().equals("text") || var.getParent().getChildren().get(0).getValue().equals("proc")){
            name = name.toUpperCase();
            name = name.charAt(0) + "$";
        }

        return name;
    }

    private String CollectPos(TreeNode n) {
        StringBuilder sb = new StringBuilder();

        // Base case: node is a D node, append its name to the string
        if (n.equals("D")) {
            sb.append(n.getChildren().get(0).getValue());
        }
        else if(n.equals("INT")){
            sb.append(n.getChildren().get(0).getValue());
        }
        else if(n.getValue().equals(".")){
            sb.append(".");
        }


        // Recursive case: node has getChildren(), traverse them and append their names to
        // the string
        else if (n.getChildren().size() > 0) {
            for (TreeNode child : n.getChildren()) {
                sb.append(CollectPos(child));
            }
        }

        // Return the concatenated string
        return sb.toString();
    }

    private static String concatenateDigits(TreeNode n) {
        StringBuilder sb = new StringBuilder();

        // Base case: node is a D node, append its name to the string
        if (n.getValue().equals("D")) {
            sb.append(n.getChildren().get(0).getValue());
        }

        // Recursive case: node has children, traverse them and append their names to
        // the string
        else if (n.getChildren().size() > 0) {
            for (TreeNode child : n.getChildren()) {
                sb.append(concatenateDigits(child));
            }
        }

        // Return the concatenated string
        return sb.toString();
    }
}
