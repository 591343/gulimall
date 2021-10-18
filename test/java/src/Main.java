import java.net.CookieHandler;
import java.util.*;

public class Main<T> {
    static class TreeNode {
         int val;
         TreeNode left;
         TreeNode right;
         TreeNode(int x) { val = x; }
    }

    static class ListNode {
      int val;
      ListNode next;
      ListNode(int val) { this.val = val; }
      ListNode(int val, ListNode next) { this.val = val; this.next = next; }
  }

    static class Node{
        int val;
        int pos;
        Node(int val,int pos){
            this.val=val;
            this.pos=pos;
        }
    }

    public void printArray(T []array){
        for (T a:array
             ) {
            System.out.print(a+" ");
        }
        System.out.println();
    }

    public void printList(List<T> list){
        for (T a:list
        ) {
            System.out.print(a+" ");
        }
        System.out.println();
    }

    public static void main(String[] args) {


        Main<Integer> main=new Main<>();
        //main.spiralOrder(new int [][]{{1,2,3},{4,5,6},{7,8,9}});
        //System.out.println(main.spiralOrder(null));
        String[] s = "_500".split("_");
        System.out.println(s.length);
        for (String str:s){
            if(str.equals("")){
                System.out.println("为空");
            }
        }

    }

    public TreeNode deserialize(String data) {
        StringBuilder stringBuilder=new StringBuilder();

        String[] split = data.split(",");
        return constructTree(split,0);
    }

    public TreeNode constructTree(String[] split,int i){
        int num=0;
        TreeNode node=null;
        if(i<split.length&&!split[i].equals("$")) {
            node = new TreeNode(Integer.parseInt(split[i]));
            node.left = constructTree(split, i + 1);
            node.right = constructTree(split, i + 1);
        }
        return node;
    }



}
