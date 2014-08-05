package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class SinFunction implements Function
{
    public static double call(double param)
    {
        return Math.sin(param);
    }
}
