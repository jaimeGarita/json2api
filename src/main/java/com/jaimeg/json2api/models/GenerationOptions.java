package com.jaimeg.json2api.models;

import java.util.ArrayList;
import java.util.List;

import com.jaimeg.json2api.enums.ComponentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GenerationOptions {

        private boolean controller;
        private boolean service;
        private boolean repository;

        public List<ComponentType> getEnabledComponentType() {

                List<ComponentType> enabled = new ArrayList<>();
                if (controller)
                        enabled.add(ComponentType.CONTROLLER);
                if (service)
                        enabled.add(ComponentType.SERVICE);
                if (repository)
                        enabled.add(ComponentType.REPOSITORY);
                return enabled;
        }
}
