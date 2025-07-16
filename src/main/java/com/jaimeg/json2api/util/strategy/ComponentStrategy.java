package com.jaimeg.json2api.util.strategy;

import com.jaimeg.json2api.models.EntityStructure;

public interface ComponentStrategy {
    String generate(EntityStructure entityStructure, String group, String artifact);
}
