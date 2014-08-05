package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class ExpFunction implements Function
{
    public static double call(double param)
    {
        return Math.exp(param);
    }
}
