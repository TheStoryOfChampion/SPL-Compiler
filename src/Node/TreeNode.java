package Node;
import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    public final Node node;
    private ArrayList<TreeNode> children;
    private final boolean isTerminal;
    private TreeNode parent;
    private Scope scope;
    private String semanticName;

    public TreeNode(Node node)
    {
        this.node = node;
        this.isTerminal = true;
        this.children = new ArrayList<>();
        this.scope = null;
        this.parent = null;
        this.semanticName = null;
    }

    public TreeNode(Node node, ArrayList<TreeNode> children)
    {
        this.node = node;
        this.isTerminal = false;
        this.setChildren(children);
        this.scope = null;
        this.semanticName = null;
    }

    public boolean isTerminal()
    {
        return isTerminal;
    }

    public ArrayList<TreeNode> getChildren()
    {
        return this.children;
    }

    public void setChildren(ArrayList<TreeNode> childNodes)
    {
        this.children = new ArrayList<>();
        for (TreeNode child : childNodes)
        {
            this.children.add(child);
            child.setParent(this);
        }
    }

    public String getValue()
    {
        return node.getValue();
    }

    public NodeType getType()
    {
        return node.getType();
    }

    public int getID()
    {
        return node.getId();
    }

    public Scope getScope()
    {
        return scope;
    }

    public void setScope(Scope scope)
    {
        this.scope = scope;
    }

    public String getScopeID()
    {
        return scope.getID();
    }

    public void setParent(TreeNode parent)
    {
        this.parent = parent;
    }

    public TreeNode getParent()
    {
        return parent;
    }

    public boolean isType(SymbolType type)
    {
        return getValue().equals(type.name()) && getType()==null;
    }

    public TreeNode lastChild()
    {
        int i = children.size();
        if(i>0)
        {
            return children.get(i-1);
        }
        else
            return null;
    }

    public TreeNode firstChild()
    {
        if(children.size() > 0)
            return children.get(0);
        else
            return null;
    }

    public String getSemanticName()
    {
        return semanticName;
    }

    public void setSemanticName(String semanticName)
    {
        this.semanticName = semanticName;
    }
}
