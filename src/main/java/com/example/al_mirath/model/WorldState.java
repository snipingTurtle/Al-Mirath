package com.example.al_mirath.model;

import java.util.HashSet;
import java.util.Set;

public class WorldState {

    private final Set<String> flags = new HashSet<>();

    public void addFlag(String flag) {
        flags.add(flag);
    }

    public void addFlags(Iterable<String> flagsToAdd) {
        for (String flag : flagsToAdd) {
            flags.add(flag);
        }
    }

    public Set<String> getFlags() {
        return flags;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public boolean hasAllFlags(Iterable<String> requiredFlags) {
        for (String flag : requiredFlags) {
            if (!hasFlag(flag)) {
                return false;
            }
        }

        return true;
    }

    public boolean hasAnyFlag(Iterable<String> blockedFlags) {
        for (String flag : blockedFlags) {
            if (hasFlag(flag)) {
                return true;
            }
        }

        return false;
    }

    public String getFlagsText() {
        if (flags.isEmpty()) {
            return "None";
        }

        return String.join(", ", flags);
    }
}