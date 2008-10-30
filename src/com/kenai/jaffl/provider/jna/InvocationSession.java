
package com.kenai.jaffl.provider.jna;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds information for each invocation of a native function
 */
public class InvocationSession {
    private final List<PostInvoke> list;
    public InvocationSession(int n) {
        list = new ArrayList<PostInvoke>(n);
    }
    public static interface PostInvoke {
        void postInvoke();
    }
    public void finish() {
        for (PostInvoke p : list) {
            p.postInvoke();
        }
    }
    public void addPostInvoke(PostInvoke postInvoke) {
        list.add(postInvoke);
    }
}
