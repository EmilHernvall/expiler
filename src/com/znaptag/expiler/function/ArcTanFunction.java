package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class ArcTanFunction implements Function
{
    public static double call(double param)
    {
        return Math.atan(param);
    }
}
