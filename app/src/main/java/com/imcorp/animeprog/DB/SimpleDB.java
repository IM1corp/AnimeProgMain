package com.imcorp.animeprog.DB;

import androidx.annotation.Nullable;

import org.intellij.lang.annotations.Language;

public interface SimpleDB {
    @Language("RoomSql")
    @Nullable String[] getInitQuery();
}
