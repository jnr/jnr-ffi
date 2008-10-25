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

package com.kenai.jaffl;

/**
 * Options that apply to a library
 */
public enum LibraryOption {
    /**
     * Function calls should save the errno/last error after the call.
     * This option can be overridden on individual methods by use of the 
     * {@link com.googlecode.jffi.annotations.IgnoreError} annotation.
     */
    SaveError,
    /**
     * Function calls should NOT save the errno/last error after the call.
     * This option can be overridden on individual methods by use of the 
     * {@link com.googlecode.jffi.annotations.SaveError} annotation.
     */
    IgnoreError,
    
    /**
     * A type mapper which maps java types to native types is present.
     */
    TypeMapper,

    /**
     * A function mapper which maps from java function names to native function names.
     */
    FunctionMapper,
    
    /**
     * The type of calling convention.
     * @see CallingConvention
     */
    CallingConvention;
}
