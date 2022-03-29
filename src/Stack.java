public class Stack {
    private Object[] arr;
    private int top;

    Stack (int capacity){
        arr = new Object[capacity];
        top = -1;
    }

    public void push(Object data){
        if (isFull()) System.out.println("This stack is full.");
        else {
            top++;
            arr[top] = data;
        }
    }

    public Object pop(){
        if (isEmpty()){
           // System.out.println("This stack is empty.");
            return 0;
        }
        else{
            Object data = arr[top];
            top--;
            return data;
        }
    }

    public Object peek(){
        if (isEmpty()){
          //  System.out.println("This stack is empty.");
            return 0;
        }
        else return arr[top];
    }

    public boolean isEmpty(){return (top == -1);}
    public boolean isFull(){return (top == arr.length - 1);}
    public int size(){return top + 1;}
}
