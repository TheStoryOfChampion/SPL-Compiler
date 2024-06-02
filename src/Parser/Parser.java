package Parser;
import Lexer.LinkedList;
import Node.Node;
import Node.TreeNode;
import Node.NodeType;
import Node.SymbolType;
import java.util.ArrayList;
import java.util.Currency;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Parser {
    final String UDN = "-NAME";
    final String SS = "-STRNG";
    final String NUM = "-DIGIT";
    private Node currentNode;
    private String treeString = "";
    private TreeNode treeRoot = null;
    public Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public Parser(LinkedList oList)
    {
        oList.add(new Node("$", NodeType.EOC)); //TODO this is so that i know i have reached the end of the linked list successfully
        currentNode = oList.getHead();
    }

    public TreeNode start(boolean clean, boolean prune) throws Exception
    {
        if (currentNode != null)
        {
            treeRoot = parse();
            //TODO These two if statement were my attempt at pruning a tree
            // can't say if it is correct or not.
            if (clean)
                removeGrouping(treeRoot);
            if (prune)
                pruneSubTree(treeRoot);
            return treeRoot;
        }
        else
            throw new Exception("Invalid input list");
    }

    private TreeNode match(String input) throws Exception
    {
        if( (input.equals(UDN) && currentNode.getType() == NodeType.UserDefinedName) ||
                (input.equals(SS) && currentNode.getType() == NodeType.ShortString) ||
                (input.equals(NUM) && currentNode.getType() == NodeType.Number) ||
                input.equals(currentNode.getValue()))
        {
            TreeNode temp = new TreeNode(currentNode);
            currentNode = currentNode.next();
            return temp;
        }
        else
        {
            switch (input)
            {
                case UDN:
                    input = "a NAME token";
                    break;
                case SS:
                    input = "a STRING Token";
                    break;
                case NUM:
                    input = "a INTEG Token";
                    break;
            }
            throw new Exception("Invalid token "+currentNode.matchError() +", Expected "+input);
        }
    }

    private TreeNode parse() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(currentNode != null)
        {
            TreeNode temp = parseSPL();
            match("$");
            return temp;
            /*if(isFirst(SymbolType.DECL, currentNode))    // check if currentNode (current token) is within the starting set of SPL (use enum to avoid spelling errors)
            {

            }else if (currentNode.getValue().equals("$")) // an empty file will only have '$' added in the cParser constructor. Add this only if aan empty file is valid
            {
                match("$");
                return null;
            }
            else
                throw new Exception("[Parse Error] PROG has no action for "+currentNode);*/
        }
        else
            throw new Exception("[Parse] Error at first node is null");
    }

    private TreeNode parseSPL() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(isFirst(SymbolType.DECL, currentNode)){
            children = addChild(parseVarDecl(), children, SymbolType.DECL);
            if (isFirst(SymbolType.CODE, currentNode)){
                children = addChild(parseCode(), children, SymbolType.CODE);
            }
            if(isFirst(SymbolType.PROCDEFS, currentNode)){
                children = addChild(parseProcDefs(), children,SymbolType.PROCDEFS);
            }
//            return new TreeNode(new Node(SymbolType.PROG.name(), null), children);
        }
        else if (isFirst(SymbolType.CODE, currentNode)){
            children = addChild(parseCode(), children, SymbolType.CODE);
            if(isFirst(SymbolType.PROCDEFS, currentNode)){
                children = addChild(parseProcDefs(), children,SymbolType.PROCDEFS);
            }
//            return new TreeNode(new Node(SymbolType.PROG.name(), null), children);
        }
        else if(isFirst(SymbolType.PROCDEFS, currentNode)){
            children = addChild(parseProcDefs(), children, SymbolType.PROCDEFS);
//            return new TreeNode(new Node(SymbolType.PROG.name(), null), children);
        } else
            return null;
        return new TreeNode(new Node(SymbolType.PROG.name(), null), children);

/*
    if (currentNode.getValue().equals("def"))
        {
            children.add(match("def"));
            children.add(match("{"));
            if (isFirst(SymbolType.BODY, currentNode))
                children = addChild(parseAlg(), children, SymbolType.COMMANDS);
            children.add(match("dummy"));
            children.add(match(";"));
            if(isFirst(SymbolType.DECL, currentNode))
                children = addChild(parseVarDecl(), children, SymbolType.DECL);
            children.add(match("}"));
            return new TreeNode(new Node(SymbolType.PROG.name(), null), children);

        }*/

    }

    private TreeNode parseVarDecl() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(isFirst(SymbolType.D, currentNode))
        {
            children = addChild(parseDec(), children, SymbolType.D);
            children.add(match(","));

            if (isFirst(SymbolType.DECL, currentNode))
                children = addChild(parseVarDecl(), children, SymbolType.DECL);

            return new TreeNode(new Node(SymbolType.DECL.name(), null), children);
        }
        else
            throw new Exception("Parse Error] DECL has no action for "+currentNode);
    }

    private TreeNode parseDec() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(isFirst(SymbolType.NAME, currentNode))
        {
            children = addChild(parseVar(), children, SymbolType.NAME);
            children.add(match(":"));
            children = addChild(parseTYP(), children, SymbolType.TYPE);
        } else
            throw new Exception("[Parse Error] D has no action for "+currentNode);

        return new TreeNode(new Node(SymbolType.D.name(), null), children);
    }

    private TreeNode parseVar() throws Exception
    {
        if (isFirst(SymbolType.NAME, currentNode))
        {
            return new TreeNode(new Node(SymbolType.NAME.name(), null), new ArrayList<TreeNode>(){{add(match(UDN));}});
        }
        else
            throw new Exception("[Parse Error] NAME has no action for "+currentNode);
    }

    private TreeNode parseCode() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(isFirst(SymbolType.CALLP, currentNode)){
            children = addChild(parseCallP(), children, SymbolType.CALLP);
            children.add(match(";"));
//            children = addChild(parseCode(), children, SymbolType.CODE);
        }else if(isFirst(SymbolType.INOUT, currentNode)){
            children = addChild(parseInout(), children, SymbolType.INOUT);
            children.add(match(";"));
//            children = addChild(parseCode(), children, SymbolType.CODE);
        }
        else
            throw new Exception("[Parse Error] CODE has no action for " + currentNode);

        return new TreeNode(new Node(SymbolType.CODE.name(), null), children);

    }

    private TreeNode parseInout() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(currentNode.getValue().equals("input")){
            children.add(match("input"));
            children = addChild(parseVar(), children, SymbolType.NAME);
        } else if(currentNode.getValue().equals("print")){
            children.add(match("print"));
            if(isFirst(SymbolType.NAME, currentNode)){
                children = addChild(parseVar(), children, SymbolType.NAME);

            } else if (isFirst(SymbolType.CONST, currentNode)){
                children = addChild(parseConst(), children, SymbolType.CONST);
            }
        } else
            throw new Exception("[Parse Error] INOUT has no action for " + currentNode);

        return new TreeNode(new Node(SymbolType.CALLP.name(), null), children);

    }

    private TreeNode parseCallP() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(currentNode.getValue().equals("exec")){
            children.add(match("exec"));
            children = addChild(parseVar(), children, SymbolType.NAME);
        }else
            throw new Exception("[Parse Error] CALLP has no action for " + currentNode);

        return new TreeNode(new Node(SymbolType.CALLP.name(), null), children);
    }

    private TreeNode parseTYP() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        switch (currentNode.getValue())
        {
            case "num":
                children.add(match("num"));
                break;
            case "bool":
                children.add(match("bool"));
                break;
            case "text":
                children.add(match("text"));
                break;
            case "proc":
                children.add(match("proc"));
                break;
            default:
                throw new Exception("[Parse Error] TYPE has no action for "+currentNode);
        }
        return new TreeNode(new Node(SymbolType.TYPE.name(), null), children);
    }

    private TreeNode parseBody() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();

        if (isFirst(SymbolType.DECL, currentNode)){
            children = addChild(parseVarDecl(), children, SymbolType.DECL);
            children = addChild(parseAlg(), children, SymbolType.COMMANDS);
            return new TreeNode(new Node(SymbolType.BODY.name(), null), children);
        } else {
            throw new Exception("[Parse Error] BODY has no action for " + currentNode);
        }
    }

    private TreeNode parseAlg() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(isFirst(SymbolType.INSTR, currentNode))
        {
            children = addChild(parseInstr(), children, SymbolType.INSTR);
            children.add(match(";"));
            if (isFirst(SymbolType.COMMANDS, currentNode))
                children = addChild(parseAlg(), children, SymbolType.COMMANDS);
            children.add(match("dummy"));
            return new TreeNode(new Node(SymbolType.COMMANDS.name(), null), children) ;
        }
        else
            throw new Exception("[Parse Error] COMMANDS has no action for "+currentNode );
    }

    private TreeNode parseInstr() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(isFirst(SymbolType.ASSGN, currentNode))
            children = addChild(parseAssign(), children, SymbolType.ASSGN);
        else if (isFirst(SymbolType.BRANCH, currentNode))
            children = addChild(parseBranch(), children, SymbolType.BRANCH);
        else if (isFirst(SymbolType.LOOP, currentNode))
            children = addChild(parseLoop(), children, SymbolType.LOOP);
        else
            throw new Exception("[Parse Error] Instr has no action for "+currentNode);

        return new TreeNode(new Node(SymbolType.INSTR.name(), null), children);
    }

    private TreeNode parseLoop() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(currentNode.getValue().equals("repeat"))
        {
            children.add(match("repeat"));
            children.add(match("{"));
            if(isFirst(SymbolType.COMMANDS, currentNode))
                children = addChild(parseAlg(), children, SymbolType.COMMANDS);

            children.add(match("}"));
            children.add(match("until"));
            children = addChild(parseExpr(), children, SymbolType.EXPR);

        }
        else
            throw new Exception("[Parse Error] Loop has no action for "+currentNode);

        return new TreeNode(new Node(SymbolType.LOOP.name(), null), children);
    }

    private TreeNode parseExpr() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if (isFirst(SymbolType.CONST, currentNode))
            children = addChild(parseConst(), children, SymbolType.CONST);
        else if (isFirst(SymbolType.NAME,currentNode))
        {
            children = addChild(parseVar(), children, SymbolType.NAME);
        } else if (currentNode.getValue().equals("add")){
            children.add(match("add"));
            children.add(match("("));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(","));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(")"));
        } else if (currentNode.getValue().equals("mult")){
            children.add(match("mult"));
            children.add(match("("));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(","));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(")"));
        }else if (currentNode.getValue().equals("eq")){
            children.add(match("eq"));
            children.add(match("("));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(","));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(")"));
        }else if (currentNode.getValue().equals("larger")){
            children.add(match("larger"));
            children.add(match("("));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(","));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(")"));
        }else if (currentNode.getValue().equals("or")){
            children.add(match("or"));
            children.add(match("("));
//            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(","));
//            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(")"));
        }else if (currentNode.getValue().equals("and")){
            children.add(match("and"));
            children.add(match("("));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(","));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(")"));
        }else if (currentNode.getValue().equals("not")){
            children.add(match("not"));
            children.add(match("("));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match(")"));
        }else if(currentNode.getValue().equals("T")){
            children.add(match("T"));
        }else if(currentNode.getValue().equals("F")){
            children.add(match("F"));
        }
        else
            throw new Exception("[Parse Error] Expr has no action for "+currentNode);

        return new TreeNode(new Node(SymbolType.EXPR.name(), null), children);
    }

    private TreeNode parseConst() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(currentNode.getType() == NodeType.ShortString)
            children.add(match(SS));
        else if (currentNode.getType() == NodeType.Number)
            children.add(match(NUM));
        else if (currentNode.getType() == NodeType.Keyword && currentNode.getValue().equals("T"))
            children.add(match("T"));
        else if (currentNode.getType() == NodeType.Keyword && currentNode.getValue().equals("F"))
            children.add(match("F"));
        else
            throw new Exception("[Parse Error] Const has no action for "+currentNode);

        return new TreeNode(new Node(SymbolType.CONST.name(), null), children);
    }

    private TreeNode parseBranch() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(isFirst(SymbolType.BRANCH, currentNode))
        {
            children.add(match("if"));
            children = addChild(parseExpr(), children, SymbolType.EXPR);
            children.add(match("then"));
            children.add(match("{"));
            if(isFirst(SymbolType.COMMANDS, currentNode))
                children = addChild(parseAlg(), children, SymbolType.COMMANDS);

            children.add(match("}"));
            children.add(match("else"));
            children.add(match("{"));
            if(isFirst(SymbolType.COMMANDS, currentNode))
                children = addChild(parseAlg(), children, SymbolType.COMMANDS);
            children.add(match("}"));
            return new TreeNode(new Node(SymbolType.BRANCH.name(), null), children);
        }
        else
            throw new Exception("[Parse Error] Branch has no action for "+currentNode);
    }

    private TreeNode parseAssign() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if (isFirst(SymbolType.NAME, currentNode))
        {
            children = addChild(parseVar(), children, SymbolType.NAME);
            children.add(match("="));
            children = addChild(parseExpr(), children, SymbolType.EXPR);

            return new TreeNode(new Node(SymbolType.ASSGN.name(), null), children);
        }
        else
            throw new Exception("[Parse Error] Assign has no action for "+currentNode);
    }

    private TreeNode parseProcDefs() throws Exception
    {
        ArrayList<TreeNode> children = new ArrayList<>();
        if(isFirst(SymbolType.PROCDEFS, currentNode))
        {
            children.add(match("def"));
            children.add(match(UDN));
            children.add(match("{"));

            if(isFirst(SymbolType.BODY, currentNode))
                children = addChild(parseBody(), children, SymbolType.BODY);

            children.add(match("}"));

            return new TreeNode(new Node(SymbolType.PROCDEFS.name(), null), children);
        }
        else
            throw new Exception("[Parse Error] PD has no action for "+currentNode);
    }

//    ..........................Helpers I guess..........................................
    private ArrayList<TreeNode> addChild(TreeNode child, ArrayList<TreeNode> children, SymbolType type) throws Exception
    {
        if(child == null)
            throw new Exception(type+" cannot be null at "+currentNode);
        children.add(child);
        return children;
    }

    private boolean isFirst(SymbolType type, Node node)
    {
        ArrayList<String> list = first(type);
        switch (node.getType())
        {
            case EOC:
                return false;
            case Number:
                return list.contains(NUM);
            case UserDefinedName:
                return  list.contains(UDN);
            case ShortString:
                return list.contains(SS);
            case Keyword:
            case Grouping:
                return list.contains(node.getValue());
            default:
                LOGGER.log(Level.WARNING, "unexpect error at isFirst() : "+node);
        }
        return false;
    }

    private ArrayList<String> first(SymbolType type)
    {
        ArrayList<String> list = new ArrayList<>();
        switch (type)
        {
            case PROG:
                list.addAll(first(SymbolType.DECL));
                list.addAll(first(SymbolType.CODE));
                list.addAll(first(SymbolType.PROCDEFS));
                break;
            case CODE:
                list.addAll(first(SymbolType.INOUT));
                list.addAll(first(SymbolType.CALLP));
                list.add(";");
                break;
            case PROCDEFS:
                list.add("def");
                list.add(UDN);
                list.add("{");
                list.addAll(first(SymbolType.BODY));
                list.add("}");
                break;
            case BODY:
                list.addAll(first(SymbolType.DECL));
                list.addAll(first(SymbolType.COMMANDS));
            case COMMANDS:
                list.addAll(first(SymbolType.INSTR));
                list.add(";");
                break;
            case INSTR:
                list.addAll(first(SymbolType.ASSGN));
                list.addAll(first(SymbolType.BRANCH));
                list.addAll(first(SymbolType.LOOP));
                break;
            case ASSGN:
                list.add(UDN);
                list.add("=");
                list.addAll(first(SymbolType.EXPR));
                break;
            /*case LHS:
                list.add("output");
                list.addAll(first(SymbolType.NAME));
                break;
            case Var:
            case Field:
                list.add(UDN);
                break;*/
            case BRANCH:
                list.add("if");
                list.add("else");
                break;
            case LOOP:
                list.add("repeat");
                list.add("until");
                break;
            case CALLP:
                list.add("exec");
                list.add(UDN);
                break;
            case CONST:
                list.add(SS);
                list.add(NUM);
                list.addAll(first(SymbolType.TRUTH));
                break;
            case TRUTH:
                list.add("T");
                list.add("F");
                break;
            case INOUT:
                list.add("input");
                list.add("print");
                list.add(UDN);
                break;
            case EXPR:
                list.addAll(first(SymbolType.TRUTH));
                list.addAll(first(SymbolType.CONST));
                list.add("and");
                list.add("or");
                list.add("eq");
                list.add("larger");
                list.add("add");
                list.add("not");
                list.add("mult");
                break;
            case DECL:
                list.addAll(first(SymbolType.D));
                break;
            case D:
                list.add(UDN);
                list.add(":");
                list.addAll(first(SymbolType.TYPE));
                list.add(",");
                break;
            case TYPE:
                list.add("num");
                list.add("bool");
                list.add("text");
                list.add("proc");
                break;
            case NAME:
                list.add(UDN);
                break;
            default:
                LOGGER.log(Level.WARNING, "unexpect token in first(): "+type);
                return list;
        }
        return list;
    }

    public String printTree()
    {
        if(treeRoot != null)
        {
            printSubTree(treeRoot, "", 0);
            return treeString;
        }
        return "tree root is null";
    }

    private void printSubTree(TreeNode treeNode, String tabs, int index)
    {
        treeString += tabs + index +" - "+ treeNode.node.getValue() + "\n";
        tabs += treeNode.getChildren().size() > 1 ? "   |" : "    ";
        int i = 1;
        for (TreeNode child : treeNode.getChildren())
        {
            printSubTree(child, tabs, i++);
        }
    }

    public String getTreeString()
    {
        if(treeRoot != null)
        {
            return subTreeString(treeRoot, "",0);
        }
        return "No tree generated [tree root is null]";
    }

    private String subTreeString(TreeNode treeNode, String padding, int index)
    {
        String str = padding + index +" - "+ treeNode.node.getValue() + "\n";
        padding += treeNode.getChildren().size() > 1 ? "   |" : "    ";

        int i = 1;
        for (TreeNode child : treeNode.getChildren())
        {
            str += subTreeString(child, padding, i++);
        }
        return str;
    }

    private void removeGrouping(TreeNode node)
    {
        if(node != null)
        {
            boolean changed = true;
            ArrayList<TreeNode> newChildren = new ArrayList<>();
            for (int i = 0; i < node.getChildren().size(); i++)
            {
                if(node.getChildren().get(i) != null)
                {
                    if (!isRemovable(node.getChildren().get(i)))
                    {
                        newChildren.add(node.getChildren().get(i));
                    }
                }
            }

            newChildren.trimToSize();
            node.setChildren(newChildren);
            for (int i = 0; i < node.getChildren().size(); i++)
            {
                removeGrouping(node.getChildren().get(i));
            }
        }
    }

    private boolean isRemovable(TreeNode node)
    {
        String[] removableSymbol = {"{", "}", "(", ")", ";" , ",", "[", "]", "=" };
        for (String s : removableSymbol)
        {
            if (node.node.getValue().equals(s))
                return true;
        }
        return false;
    }

    public void pruneSubTree(TreeNode parent)
    {
        boolean changed = false;
        if(parent != null )
        {
            if (parent.getChildren().size() > 0)
            {
                for (int i = 0; i < parent.getChildren().size(); i++)
                {
                    TreeNode node = parent.getChildren().get(i);
                    if (node.getChildren().size() == 1 && node.getChildren().get(0).getChildren().size()  <= 1)
                    {
                        parent.getChildren().add(i, node.getChildren().get(0));
                        parent.getChildren().remove(node);
                        parent.getChildren().trimToSize();
                        changed = true;
                    }
                }
            }
            if(changed)
                pruneSubTree(parent);
            else
            {
                for (int i = 0; i < parent.getChildren().size() ; i++)
                {
                    pruneSubTree(parent.getChildren().get(i));
                }
            }
        }
    }
}
