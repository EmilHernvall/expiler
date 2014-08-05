package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class SqrtFunction implements Function
{
    public static double call(double param)
    {
        return Math.sqrt(param);
    }
}
