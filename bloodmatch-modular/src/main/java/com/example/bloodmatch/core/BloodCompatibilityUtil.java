package com.example.bloodmatch.core;

import java.util.*;

public class BloodCompatibilityUtil {

    private static final Map<String, List<String>> map = new HashMap<>();

    static {
        map.put("A+", Arrays.asList("A+", "A-", "O+", "O-"));
        map.put("A-", Arrays.asList("A-", "O-"));
        map.put("B+", Arrays.asList("B+", "B-", "O+", "O-"));
        map.put("B-", Arrays.asList("B-", "O-"));
        map.put("AB+", Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        map.put("AB-", Arrays.asList("A-", "B-", "AB-", "O-"));
        map.put("O+", Arrays.asList("O+", "O-"));
        map.put("O-", Arrays.asList("O-"));
    }

    public static boolean isCompatible(String donor, String required) {
        return map.getOrDefault(required, new ArrayList<>()).contains(donor);
    }
}