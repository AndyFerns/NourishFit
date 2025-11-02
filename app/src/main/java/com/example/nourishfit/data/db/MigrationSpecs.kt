package com.example.nourishfit.data.db

import androidx.room.migration.AutoMigrationSpec
import androidx.room.RenameColumn

// This class provides the explicit instructions that Room needs
// to handle the column rename from v2 to v3.
class MigrationSpecs {
    @RenameColumn(
        tableName = "foods",
        fromColumnName = "userID", // The old name from v2 (with uppercase 'D')
        toColumnName = "userId"   // The new name from v3 (with lowercase 'd')
    )
    class Migration2to3 : AutoMigrationSpec
}
