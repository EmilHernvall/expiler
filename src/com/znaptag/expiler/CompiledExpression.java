package com.znaptag.expiler;

import java.util.Map;

public interface CompiledExpression
{
    public double compute(Map<String, Double> variables);
}
