package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class TanFunction implements Function
{
    public static double call(double param)
    {
        return Math.tan(param);
    }
}
