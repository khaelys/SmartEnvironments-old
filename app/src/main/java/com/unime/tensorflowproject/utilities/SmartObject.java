package com.unime.tensorflowproject.utilities;

import java.util.ArrayList;
import java.util.List;

public class SmartObject {
    private String name;
    private List<String> commands;

    public SmartObject(String name) {
        this.name = name;
        commands = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
}
