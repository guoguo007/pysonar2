package org.yinwang.pysonar.ast;

import org.yinwang.pysonar.Binding;
import org.yinwang.pysonar.Indexer;
import org.yinwang.pysonar.Scope;
import org.yinwang.pysonar.types.Type;

public class Name extends Node {

    static final long serialVersionUID = -1160862551327528304L;

    public final String id;  // identifier

    public Name(String id) {
        this(id, -1, 0);
    }

    public Name(String id, int start, int end) {
        super(start, end);
        if (id == null) {
            throw new IllegalArgumentException("'id' param cannot be null");
        }
        this.id = id;
    }
    
    /**
     * Returns {@code true} if this name is structurally in a call position.
     */
    @Override
    public boolean isCall() {
        // foo(...)
        if (parent != null && parent.isCall() && this == ((Call)parent).func) {
            return true;
        }

        // <expr>.foo(...)
        Node gramps;
        return parent instanceof Attribute
                && this == ((Attribute)parent).attr
                && (gramps = parent.parent) instanceof Call
                && parent == ((Call)gramps).func;
    }
    
    @Override
    public Type resolve(Scope s, int tag) throws Exception {
        Binding b = s.lookup(id);
        if (b != null) {
            Indexer.idx.putLocation(this, b);
            return b.getType();
        } else {
            Indexer.idx.putProblem(this, "unbound variable " + getId());
            Type t = Indexer.idx.builtins.unknown;
            t.getTable().setPath(s.extendPath(getId()));
            return t;
        }
    }

    /**
     * Returns {@code true} if this name node is the {@code attr} child
     * (i.e. the attribute being accessed) of an {@link Attribute} node.
     */
    public boolean isAttribute() {
        return parent instanceof Attribute
                && ((Attribute)parent).getAttr() == this;
    }
    
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "<Name:" + start + ":" + id +  ">";
    }

    @Override
    public void visit(NodeVisitor v) {
        v.visit(this);
    }
}