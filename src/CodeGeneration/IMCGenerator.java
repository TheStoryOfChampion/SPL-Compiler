package CodeGeneration;

import Node.Scope;
import Node.SymbolType;
import Node.TreeNode;
import com.sun.source.tree.Tree;

public class IMCGenerator {
    private TreeNode treeRoot;
    static int LineCounter = 10;
    public static String BasicCode = "";
    public IMCGenerator(TreeNode treeRoot)
    {
        this.treeRoot = treeRoot;
    }

    public TreeNode start() throws Exception
    {
        if(treeRoot != null){
            if(treeRoot.getValue().equals(SymbolType.PROG.name()) && !treeRoot.isTerminal() )
            {
                Scope scope = new Scope( );
                treeRoot.setScope(scope);
                for (TreeNode child: treeRoot.getChildren())
                {
                    crawlDown(child, 1, "PROCDEF");
                }
                return treeRoot;
            }
        }
        throw new Exception("root node is not SPL");

    }

    private void crawlDown(TreeNode node, int currentScope, String currentProc){
        if(node == null){
            return;
        }


        if(node.isTerminal()){
            if(node.getValue().equals("PROCDEF")){
                if(node.getChildren().size() ==2){
                    crawlDown(node.getChildren().get(0), currentScope, currentProc);
                    BasicCode+= String.valueOf(LineCounter) + " END\n";
                    LineCounter+=10;
                    crawlDown(node.getChildren().get(1), currentScope, currentProc);
                }
                else{
                    crawlDown(node.getChildren().get(0), currentScope, currentProc);
                    BasicCode += String.valueOf(LineCounter) + " END\n";
                    LineCounter+=10;

                }
                return;
            }
            else if(node.getValue().equals("INOUT")){

                TreeNode theNode = node.getChildren().get(1); // NUMVAR

                theNode = theNode.getChildren().get(1);// DIGITS

                String digits = concatenateDigits(theNode);


                BasicCode += LineCounter + " INPUT \"\"; " + " n" + digits + "\n ";
                LineCounter += 10;
                // LineCounter INPUT “”; var_name

            }
            else if(node.getValue().equals("OUTPUT")){
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
            }
            else if(node.getValue().equals("ASSGN")){
                TransAssgn(node);
            }

            else if(node.getValue().equals("BRANCH")){

                TransBra(node, currentScope, currentProc);

                return;
            }
            else if(node.getValue().equals("LOOP")){

                TransLP(node, currentScope, currentProc);
                return;
            }

            for(TreeNode child: node.getChildren()){
                crawlDown(child, currentScope, currentProc);
            }

        }else{
            if(node.getValue().equals("h")){
                BasicCode += LineCounter + " STOP" +"\n ";
                LineCounter += 10;

            }
            crawlDown(null, currentScope, currentProc);
        }
    }

    private void TransLP(TreeNode node, int currentScope, String currentProc) {
        String boolexpr = TransBoolExpr(node.getChildren().get(2));

        int enL = LineCounter;


        BasicCode += String.valueOf(LineCounter) + " " + "IF " + boolexpr + " Then GOTO other"+    "\n";
        LineCounter += 10;
        BasicCode += String.valueOf(LineCounter) + " " + "GOTO exit" +   "\n";
        LineCounter += 10;
        int otherLineCounter = LineCounter;
        crawlDown(node.getChildren().get(5), currentScope, currentProc);
        BasicCode+= String.valueOf(LineCounter) + " " + "GOTO "  +enL + "\n";
        LineCounter += 10;
        int exitLineCounter = LineCounter;


        BasicCode = BasicCode.replace("other" , String.valueOf(otherLineCounter));
        BasicCode = BasicCode.replace("exit" , String.valueOf(exitLineCounter));



    }


    private void TransBra(TreeNode node, int currentScope, String currentProc) {

        if(node.getChildren().size() == 9){
            String boolexpr = TransBoolExpr(node.getChildren().get(2));
            // int  = branchCount;


            BasicCode+= String.valueOf(LineCounter) + " " + "IF " + boolexpr + " Then GOTO thenbranch" + "\n";
            LineCounter += 10;
            crawlDown(node.getChildren().get(8), currentScope, currentProc);
            BasicCode+= String.valueOf(LineCounter) + " " + "GOTO exit" +   "\n";
            LineCounter += 10;
            int thenLineCounter = LineCounter;
            crawlDown(node.getChildren().get(6), currentScope, currentProc);
            int exitLineCounter = LineCounter;


            BasicCode = BasicCode.replace("thenbranch" , String.valueOf(thenLineCounter));
            BasicCode = BasicCode.replace("exit"  , String.valueOf(exitLineCounter));



        }else{
            String boolexpr = TransBoolExpr(node.getChildren().get(2));


            BasicCode+= String.valueOf(LineCounter) + " " + "IF " + boolexpr + " Then GOTO thenbranch"  + "\n";
            LineCounter += 10;
            BasicCode+= String.valueOf(LineCounter) + " " + "GOTO exit"   + "\n";
            LineCounter += 10;
            int thenLineCounter = LineCounter;
            crawlDown(node.getChildren().get(6), currentScope, currentProc);
            int exitLineCounter = LineCounter;
            System.out.println("thenLineCounter: "+thenLineCounter);

            BasicCode = BasicCode.replace("thenbranch" , String.valueOf(thenLineCounter));
            BasicCode = BasicCode.replace("exit"  , String.valueOf(exitLineCounter));


        }

    }

    private void TransAssgn(TreeNode node) {
        String lefthandside="";
        String righthandside    = "";
        TreeNode theNode = node.getChildren().get(0); // NUMVAR

        theNode = theNode.getChildren().get(1);// DIGITS

        String digits = concatenateDigits(theNode);

        if(node.getChildren().get(3).equals("NUMEXPR")){
            lefthandside = " n" + digits;
            righthandside = TransNumExpr(node.getChildren().get(3));
            BasicCode += LineCounter  +" LET "+ lefthandside + " = " + righthandside + "\n ";
        }
        else if(node.getChildren().get(3).equals("BOOLEXPR")){
            lefthandside = " b" + digits;
            righthandside = TransBoolExpr(node.getChildren().get(3));
            BasicCode += LineCounter  +" LET "+ lefthandside + " = " + righthandside + "\n ";

        }else if(node.getChildren().get(3).equals("STRI")){
            lefthandside = " s" + digits;
            lefthandside+="$";
            righthandside = node.getChildren().get(3).getChildren().get(0).getValue();
            BasicCode += LineCounter  +" LET "+ lefthandside + " = " + righthandside + "\n ";

        }




    }
    private String TransBoolExpr(TreeNode node) {
        if(node.getChildren().get(0).equals("LOGIC")){



            return TransLogic(node.getChildren().get(0));
        }
        else{


            return TransCmpr(node.getChildren().get(0));
        }
    }



    private String TransCmpr(TreeNode node) {
        if(node.getChildren().get(0).equals("E")){
            return TransNumExpr(node.getChildren().get(2)) + " = " + TransNumExpr(node.getChildren().get(4));
        }
        else if(node.getChildren().get(0).equals("<")){
            return TransNumExpr(node.getChildren().get(2)) + " < " + TransNumExpr(node.getChildren().get(4));
        }
        else if(node.getChildren().get(0).equals(">")){
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
        else if(node.getChildren().get(0).equals("^")){
            TransAnd(node);
            return "P";
        }
        else if(node.getChildren().get(0).equals("v")){
            TransOr(node);
            return "P";


        }
        else if(node.getChildren().get(0).equals("!")){
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

        if(node.getChildren().get(0).equals("NUMVAR")){



            TreeNode theNode = node.getChildren().get(0).getChildren().get(1);// DIGITS

            String digits = concatenateDigits(theNode);

            return "n" + digits;

        }else if(node.getChildren().get(0).equals("DECNUM")){
            if(node.getChildren().get(0).getChildren().get(0).equals("0")){
                return "0.00";
            }
            else if(node.getChildren().get(0).getChildren().get(0).equals("POS")){
                String pos=CollectPos(node.getChildren().get(0).getChildren().get(0));
                return pos;

            }else if(node.getChildren().get(0).getChildren().get(0).equals("-")){

                String pos2=CollectPos(node.getChildren().get(0).getChildren().get(1));
                return "-"+pos2;

            }

        }else if(node.getChildren().get(0).equals("a")){
            return TransNumExpr(node.getChildren().get(2)) + "+" + TransNumExpr(node.getChildren().get(4));

        }else if(node.getChildren().get(0).equals("m")){
            return TransNumExpr(node.getChildren().get(2)) + "*" + TransNumExpr(node.getChildren().get(4));

        }else if(node.getChildren().get(0).equals("d")){
            return TransNumExpr(node.getChildren().get(2)) + "/" + TransNumExpr(node.getChildren().get(4));

        }
        return "";
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
