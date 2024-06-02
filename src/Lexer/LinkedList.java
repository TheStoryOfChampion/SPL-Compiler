package Lexer;
import Node.Node;
import Node.NodeSubType;
import Node.NodeType;

public class LinkedList {
    private Node m_oHead;
    private Node m_oTail;
    private static int m_iNodeCount = 0;

    public LinkedList()
    {
        m_oHead = null;
        m_oTail = null;
    }

    public Node getHead()
    {
        return m_oHead;
    }

    public void setHead(Node m_oHead)
    {
        this.m_oHead = m_oHead;
    }

    public Node getTail()
    {
        return m_oTail;
    }

    public void setTail(Node m_oTail)
    {
        this.m_oTail = m_oTail;
    }

    public int getNodeCount()
    {
        return m_iNodeCount;
    }

    public void add(Node pNode)
    {
        if(pNode != null)
        {
            if(m_oHead == null)
            {
                m_oHead = pNode;
            }
            else
            {
                m_oTail.next(pNode);
                pNode.prev(m_oTail);
            }
            m_oTail = pNode;
        }
        m_iNodeCount++;
    }

    public void add(String pNodeValue, NodeType pNodeType)
    {
        this.add(new Node(pNodeValue, pNodeType));
    }

    public void add(String pNodeValue, NodeType pNodeType, NodeSubType pNodeSubType)
    {
        this.add(new Node(pNodeValue, pNodeType, pNodeSubType));
    }

    @Override
    public String toString()
    {
        StringBuilder sNodeList = new StringBuilder();

        Node temp = m_oHead;
        while(temp!= null){
            sNodeList.append("[")
                    .append(temp.getId())
                    .append("-")
                    .append(temp.getSubType() == null ? temp.getType() : temp.getSubType())
                    .append("-")
                    .append(temp.getValue())
                    .append("]\n");
            temp = temp.next();
        }
        return sNodeList.toString();
    }

    public String toHTMLString()
    {
        StringBuilder sNodeList = new StringBuilder();

        Node temp = m_oHead;
        while(temp!= null){
            if(temp.getType() == NodeType.Error){
                sNodeList.append("<p style='color: blue;'>").append(temp.getValue()).append("</p></div>");
                return sNodeList.toString();
            }
            sNodeList.append(temp.getId()).append(" | ").append(temp.getType()).append(" | ").append(
                    temp.getValue()).append("<br>");
            temp = temp.next();
        }

        sNodeList.append("</div>");
        return sNodeList.toString();
    }
}
