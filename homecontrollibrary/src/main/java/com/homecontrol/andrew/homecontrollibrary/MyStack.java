package com.homecontrol.andrew.homecontrollibrary;

import android.util.Log;

import java.util.Stack;

/**
 * Created by andrew on 1/1/15.
 */
public class MyStack {
    private static final String TAG = "MyStack";
    private Stack<String> nodeStack = new Stack();
    private static MyStack myStack = null;

    private MyStack(){
        Log.d(TAG, "MyStack Constructor");
    }

    public static MyStack getInstance(){
        if(myStack == null){
            myStack = new MyStack();
        }
        return myStack;
    }

    public void push(String s){
        Log.e(TAG, "adding to stack");
        nodeStack.add(s);
    }

    public String pop(){
        Log.e(TAG, "poping from stack");
        return nodeStack.pop();
    }

    public int size(){
        return nodeStack.size();
    }
}
