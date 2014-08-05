package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class ArcSinFunction implements Function
{
    public static double call(double param)
    {
        return Math.asin(param);
    }
}
