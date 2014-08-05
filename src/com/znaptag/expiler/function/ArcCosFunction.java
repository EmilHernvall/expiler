package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class ArcCosFunction implements Function
{
    public static double call(double param)
    {
        return Math.acos(param);
    }
}
