package Semantics;
import Node.Scope;
import Node.TreeNode;
import Node.SymbolType;

public class Scoping {
    int sIDCount=1;
    TreeNode treeRoot;


    public Scoping(TreeNode treeRoot)
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
                    subTreeScope(child, scope);
                }
                return treeRoot;
            }
        }
        throw new Exception("Program is not in SPL");

    }

    private void subTreeScope(TreeNode parent, Scope parentScope)
    {
        Scope scope = parentScope;
        if (parent.getValue().equals(SymbolType.PROCDEFS.name()) && !parent.isTerminal())
        {
            scope = new Scope(scope.getID()+"."+sIDCount++);
            parentScope.addChildScope(scope);
            scope.setParentScope(parentScope);
        }

        parent.setScope(scope);

        for (TreeNode child : parent.getChildren())
        {
            subTreeScope(child, scope);
        }
    }

    public String printTree()
    {
        if(treeRoot != null)
        {
            return printSubTree(treeRoot, "", sIDCount);
        }
        return "tree root is null";
    }

    private String printSubTree(TreeNode treeNode, String tabs, int index)
    {
        String treeString = tabs +" - "+ treeNode.getValue() + " " +treeNode.getScopeID() +"\n";
        tabs += treeNode.getChildren().size() > 1 ? "   |" : "    ";
        int i = 1;
        for (TreeNode child : treeNode.getChildren())
        {
            treeString += this.printSubTree(child, tabs, i++);
        }
        return treeString;
    }
}
