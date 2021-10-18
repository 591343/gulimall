import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

class Solution {
    public static class TreeNode {
      int val;
      TreeNode left;
      TreeNode right;
      TreeNode() {}
      TreeNode(int val) { this.val = val; }
      TreeNode(int val, TreeNode left, TreeNode right) {
          this.val = val;
          this.left = left;
          this.right = right;
      }
    }
      public class ListNode {
      int val;
      ListNode next;
      ListNode() {}
      ListNode(int val) { this.val = val; }
      ListNode(int val, ListNode next) { this.val = val; this.next = next; }
    }

    static class Node {
        int val;
        Node next;
        Node random;

        public Node(int val) {
            this.val = val;
            this.next = null;
            this.random = null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Solution solution=new Solution();
        solution.calculate( " 3+5 / 2 ");
    }

    public int calculate(String s) {
        String infix= s.replaceAll(" ", "");
        List<String> list=new ArrayList<>();
        Set<String> set=new HashSet<>();
        Stack<String> stack=new Stack<>();
        set.add("+");
        set.add("-");
        set.add("*");
        set.add("/");
        int pos=-1;
        for(int i=0;i<infix.length();i++){
            if(Character.isDigit(infix.charAt(i))){
                if(pos==-1)
                    pos=i;
                if(i==infix.length()-1){
                    list.add(infix.substring(pos));
                }
            }else {
                if(pos!=-1){
                    list.add(infix.substring(pos,i));
                    pos=-1;
                }
                list.add(String.valueOf(infix.charAt(i)));
            }
        }
        int res=0;

        for(int i=0;i<list.size();i++){
            String item = list.get(i);
            if(set.contains(item)){

            }
        }
        return 1;
    }







}
