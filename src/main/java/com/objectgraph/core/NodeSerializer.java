package com.objectgraph.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

/**
 * Created with IntelliJ IDEA.
 * User: emanuele
 * Date: 25/03/13
 * Time: 19.23
 * To change this template use File | Settings | File Templates.
 */
public class NodeSerializer extends Serializer<Node> {

    @Override
    public void write(Kryo kryo, Output output, Node node) {
        Serializer<Node> serializer = new FieldSerializer<>(kryo, node.getClass());
        serializer.write(kryo, output, node);
    }

    @Override
    public Node read(Kryo kryo, Input input, Class<Node> nodeClass) {
        Serializer<Node> serializer = new FieldSerializer<>(kryo, nodeClass);
        Node ret = serializer.read(kryo, input, nodeClass);
        ParentRegistry.registerTree(ret);
        return ret;
    }

    @Override
    public Node copy(Kryo kryo, Node original) {
        try {
            Object root = kryo.getContext().get("root");
            if (root == null) {
                // original is the root
                kryo.getContext().put("root", original);
                root = original;
            }
            Serializer<Node> serializer = new FieldSerializer<>(kryo, original.getClass());
            Node ret = serializer.copy(kryo, original);
            if (root == original) {
                // Register tree only for the root (should save some work)
                ParentRegistry.registerTree(ret);
            }
            return ret;
        } finally {
            kryo.getContext().remove("root");
        }
    }
}
