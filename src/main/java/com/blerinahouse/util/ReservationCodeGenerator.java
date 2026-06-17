package com.blerinahouse.util;

public final class ReservationCodeGenerator {

    private ReservationCodeGenerator() {}

    // Format identik me skemën: BH-2026-000123 (seq global, viti vetëm prefiks)
    public static String generate(int year, long seq) {
        return String.format("BH-%d-%06d", year, seq);
    }
}