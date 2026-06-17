package com.blerinahouse.entity.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum ReservationStatus {
    PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, EXPIRED;

    /** Statuset që "zënë" një dhomë — identike me exclusion constraint no_double_booking. */
    public static final Set<ReservationStatus> BLOCKING =
            Collections.unmodifiableSet(EnumSet.of(PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT));
}