package com.xinchen.zookeeper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author xinchen
 * @version 1.0
 * @date 13/10/2020 14:03
 */
public class TreeNode {
    private String path;
    private List<TreeNode> children = new ArrayList<>();

    public TreeNode(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public List<TreeNode> getChildren() {
        return children;
    }
}
