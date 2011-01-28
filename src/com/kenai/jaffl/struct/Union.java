/*
 * This file is covered by the license described in the LICENSE file in the root of
 * the project, however, it incorporates some code from the javolution project,
 * and that copyright is reproduced here.
 *
 * Copyright (C) 2006 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */

package com.kenai.jaffl.struct;

/**
 * Represents a C union
 */
public abstract class Union extends Struct {
    protected Union() {
        super(true);
    }
}
