package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class CosFunction implements Function
{
    public static double call(double param)
    {
        return Math.cos(param);
    }
}
