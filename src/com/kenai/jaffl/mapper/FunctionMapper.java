/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.jaffl.mapper;

import com.kenai.jaffl.Library;


/**
 *
 * @author wayne
 */
public interface FunctionMapper {
    static interface Context {
        Library getLibrary();
    }
    public String mapFunctionName(String functionName, Context context);
}
