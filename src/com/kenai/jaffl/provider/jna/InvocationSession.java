/*
 * Copyright (C) 2008 Wayne Meissner
 *
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kenai.jaffl.provider.jna;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds information for each invocation of a native function
 */
public class InvocationSession {
    private final List<PostInvoke> list;
    InvocationSession(int n) {
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
