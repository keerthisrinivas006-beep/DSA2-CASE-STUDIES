package mediflow;

class AVLNode {
    int key;
    AVLNode left, right;
    int height;

    AVLNode(int key) {
        this.key = key;
        this.height = 1;
    }
}

public class MediflowAVLSystem {

    // Helper to safely get the height of a node
    static int height(AVLNode n) {
        return n == null ? 0 : n.height;
    }

    // Calculate balance factor: left height - right height
    static int balance(AVLNode n) {
        return n == null ? 0 : height(n.left) - height(n.right);
    }

    // Update height based on child subtrees
    static void updateHeight(AVLNode n) {
        if (n != null) {
            n.height = 1 + Math.max(height(n.left), height(n.right));
        }
    }

    // TODO 1: Perform a right rotation around y
    static AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;

        // Perform rotation
        x.right = y;
        y.left = T2;

        // Update heights
        updateHeight(y);
        updateHeight(x);

        // Return new root of the subtree
        return x;
    }

    // TODO 2: Perform a left rotation around x
    static AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        // Perform rotation
        y.left = x;
        x.right = T2;

        // Update heights
        updateHeight(x);
        updateHeight(y);

        // Return new root of the subtree
        return y;
    }

    // TODO 3: Standard BST Insert + Automatic AVL Rebalancing
    static AVLNode insert(AVLNode node, int key) {
        // 1. Perform standard BST insertion
        if (node == null) {
            return new AVLNode(key);
        }

        if (key < node.key) {
            node.left = insert(node.left, key);
        } else if (key > node.key) {
            node.right = insert(node.right, key);
        } else {
            return node; // Duplicate keys are not allowed in patient indexing
        }

        // 2. Update height of this ancestor node
        updateHeight(node);

        // 3. Get the balance factor to check for imbalances
        int bf = balance(node);

        // Case 1: Left-Left (LL) Case -> Requires Single Right Rotation
        if (bf > 1 && key < node.left.key) {
            System.out.println("Triggered LL Rotation at node: " + node.key);
            return rotateRight(node);
        }

        // Case 2: Right-Right (RR) Case -> Requires Single Left Rotation
        if (bf < -1 && key > node.right.key) {
            System.out.println("Triggered RR Rotation at node: " + node.key);
            return rotateLeft(node);
        }

        // Case 3: Left-Right (LR) Case -> Requires Double Rotation (Left then Right)
        if (bf > 1 && key > node.left.key) {
            System.out.println("Triggered LR Rotation at node: " + node.key);
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Case 4: Right-Left (RL) Case -> Requires Double Rotation (Right then Left)
        if (bf < -1 && key < node.right.key) {
            System.out.println("Triggered RL Rotation at node: " + node.key);
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    // Helper method to find the node with the minimum value (In-order Successor)
    static AVLNode minValueNode(AVLNode node) {
        AVLNode current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    // Deletion implementation with automatic rebalancing
    static AVLNode deleteNode(AVLNode root, int key) {
        if (root == null) {
            return root;
        }

        if (key < root.key) {
            root.left = deleteNode(root.left, key);
        } else if (key > root.key) {
            root.right = deleteNode(root.right, key);
        } else {
            // Node with only one child or no child
            if ((root.left == null) || (root.right == null)) {
                AVLNode temp = (root.left != null) ? root.left : root.right;
                if (temp == null) {
                    temp = root;
                    root = null;
                } else {
                    root = temp; // Copy contents of non-empty child
                }
            } else {
                // Node with two children: Get the in-order successor
                AVLNode temp = minValueNode(root.right);
                root.key = temp.key;
                root.right = deleteNode(root.right, temp.key);
            }
        }

        if (root == null) {
            return root;
        }

        updateHeight(root);
        int bf = balance(root);

        // Rebalancing after deletion
        if (bf > 1 && balance(root.left) >= 0) return rotateRight(root);
        if (bf > 1 && balance(root.left) < 0) {
            root.left = rotateLeft(root.left);
            return rotateRight(root);
        }
        if (bf < -1 && balance(root.right) <= 0) return rotateLeft(root);
        if (bf < -1 && balance(root.right) > 0) {
            root.right = rotateRight(root.right);
            return rotateLeft(root);
        }

        return root;
    }

    // In-order traversal utility to print the tree
    static void inorder(AVLNode root) {
        if (root != null) {
            inorder(root.left);
            System.out.print(root.key + " (H:" + root.height + ") ");
            inorder(root.right);
        }
    }

    public static void main(String[] args) {
        AVLNode root = null;
        int[] insertions = {20, 30, 35, 40, 45, 50, 60, 65, 70, 75, 80, 85, 90};

        System.out.println("--- Starting Patient ID Insertions ---");
        for (int key : insertions) {
            System.out.println("\nInserting Patient ID: " + key);
            root = insert(root, key);
        }

        System.out.print("\nFinal Balanced AVL Tree (In-order): ");
        inorder(root);
        System.out.println("\nFinal Tree Height: " + (height(root) - 1) + " edges.");

        System.out.println("\n--- Starting Noon Deletions (30, 70, 50) ---");
        root = deleteNode(root, 30);
        root = deleteNode(root, 70);
        root = deleteNode(root, 50);

        System.out.print("\nPost-Deletion AVL Tree (In-order): ");
        inorder(root);
        System.out.println("\nPost-Deletion Tree Height: " + (height(root) - 1) + " edges.");
    }
}
