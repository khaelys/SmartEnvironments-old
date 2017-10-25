package com.unime.tensorflowproject.utilities;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 *
 */

public class CommandTrigger {
    private List<SmartObject> smartObjectList;
    private String name;
    private String command;

    public CommandTrigger(String name, String command) {
        smartObjectList = new ArrayList<>();
        this.name = name;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public List<SmartObject> getSmartObjectList() {
        return smartObjectList;
    }

    private void fill() {
        SmartObject lampObject = new SmartObject("lamp");
        SmartObject doorObject = new SmartObject("door");

        lampObject.setCommands(Arrays.asList("turn on", "turn off"));
        doorObject.setCommands(Arrays.asList("open", "close"));

        smartObjectList.add(lampObject);
        smartObjectList.add(doorObject);
    }

    public void tryCommand() {
        fill(); // TODO remove this fill method and fill the List from the file
        if(isValid(getName(), getCommand())) {
            startCommand(getName(), getCommand());
        }
    }

    private boolean isValid(String name, String command) {
        Boolean []flag = new Boolean[1];
        flag[0] = false;


        // check if the command is contained in the commands associated with the object recognized
        getSmartObjectList().stream()
                .filter(smartObject -> name.equals(getName()))
                .filter(smartObject -> smartObject.getCommands().contains(command))
                .forEach(smartObject -> flag[0] = true);

        return flag[0];
    }

    private void startCommand(String name, String command) {
        Log.d(TAG, "startCommand: ok");
        return;
    }

}
