package com.znaptag.expiler.function;

import com.znaptag.expiler.Function;

public class LogFunction implements Function
{
    public static double call(double param)
    {
        return Math.log(param);
    }
}
