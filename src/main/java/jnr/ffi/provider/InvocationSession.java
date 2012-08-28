/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.provider;

import java.util.ArrayList;

/**
 * Holds information for each invocation of a native function
 */
public class InvocationSession {
    private ArrayList<PostInvoke> list;
    private ArrayList<Object> liveObjects;
    public InvocationSession() {
        
    }
    public static interface PostInvoke {
        void postInvoke();
    }
    public void finish() {
        if (list != null) for (PostInvoke p : list) {
            try {
                p.postInvoke();
            } catch (Throwable t) {}
        }
    }
    public void addPostInvoke(PostInvoke postInvoke) {
        if (list == null) {
            list = new ArrayList<PostInvoke>();
        }
        list.add(postInvoke);
    }

    public void keepAlive(Object obj) {
        if (liveObjects == null) {
            liveObjects = new ArrayList<Object>();
        }
        liveObjects.add(obj);
    }
}
