/*
 * Some of the design and code of this class is from the javolution project.
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
