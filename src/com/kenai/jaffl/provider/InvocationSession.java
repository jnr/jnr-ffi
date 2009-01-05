
package com.kenai.jaffl.provider;

import java.util.ArrayList;

/**
 * Holds information for each invocation of a native function
 */
public class InvocationSession {
    private ArrayList<PostInvoke> list;
    public InvocationSession() {
        
    }
    public static interface PostInvoke {
        void postInvoke();
    }
    public void finish() {
        if (list != null) for (PostInvoke p : list) {
            p.postInvoke();
        }
    }
    public void addPostInvoke(PostInvoke postInvoke) {
        if (list == null) {
            list = new ArrayList<PostInvoke>();
        }
        list.add(postInvoke);
    }
}
