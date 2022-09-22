package com.solana.mobilewalletadapter.walletlib.authorization;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IdentityRecordDao extends DbContentProvider<IdentityRecord>
        implements IdentityRecordDaoInterface, IdentityRecordSchema {

    public IdentityRecordDao(SQLiteDatabase db) {
        super(db);
    }

    @Override
    @NonNull
    public List<IdentityRecord> getAuthorizedIdentities() {
        final ArrayList<IdentityRecord> identities = new ArrayList<>();
        try (final Cursor c = super.query(IdentityRecordSchema.TABLE_IDENTITIES,
                IDENTITY_RECORD_COLUMNS,
                null,
                null,
                IdentityRecordSchema.COLUMN_IDENTITIES_NAME)) {
            while (c.moveToNext()) {
                identities.add(cursorToEntity(c));
            }
        }
        return identities;
    }

    @Override
    @Nullable
    public IdentityRecord findIdentityById(String id) {
        try (final Cursor c = super.query(IdentityRecordSchema.TABLE_IDENTITIES,
                IDENTITY_RECORD_COLUMNS,
                IdentityRecordSchema.COLUMN_IDENTITIES_ID + "=?",
                new String[] { id },
                null)) {
            if (!c.moveToNext()) {
                Log.w(TAG, "Identity not found: " + id);
                return null;
            }

            return cursorToEntity(c);
        }
    }

    @Nullable
    @Override
    public IdentityRecord findIdentityByParams(String name, String uri, String relativeIconUri) {
        try (final Cursor c = super.query(IdentityRecordSchema.TABLE_IDENTITIES,
                new String[] { IdentityRecordSchema.COLUMN_IDENTITIES_ID,
                        IdentityRecordSchema.COLUMN_IDENTITIES_SECRET_KEY,
                        IdentityRecordSchema.COLUMN_IDENTITIES_SECRET_KEY_IV },
                IdentityRecordSchema.COLUMN_IDENTITIES_NAME + "=? AND " +
                        IdentityRecordSchema.COLUMN_IDENTITIES_URI + "=? AND " +
                        IdentityRecordSchema.COLUMN_IDENTITIES_ICON_RELATIVE_URI + "=?",
                new String[] { name, uri, relativeIconUri},
                null)) {
            if (!c.moveToNext()) {
                Log.w(TAG, "Identity not found with name=" + name);
                return null;
            }
            return cursorToEntity(c);
        }
    }

    @Override
    protected IdentityRecord cursorToEntity(@NonNull Cursor cursor) {
        try {
            final int id = cursor.getInt(cursor.getColumnIndexOrThrow(IdentityRecordSchema.COLUMN_IDENTITIES_ID));
            final String name = cursor.getString(cursor.getColumnIndexOrThrow(IdentityRecordSchema.COLUMN_IDENTITIES_NAME));
            final String uri = cursor.getString(cursor.getColumnIndexOrThrow(IdentityRecordSchema.COLUMN_IDENTITIES_URI));
            final String iconRelativeUri = cursor.getString(cursor.getColumnIndexOrThrow(IdentityRecordSchema.COLUMN_IDENTITIES_ICON_RELATIVE_URI));
            final byte[] identityKeyCiphertext = cursor.getBlob(cursor.getColumnIndexOrThrow(IdentityRecordSchema.COLUMN_IDENTITIES_SECRET_KEY));
            final byte[] identityKeyIV = cursor.getBlob(cursor.getColumnIndexOrThrow(IdentityRecordSchema.COLUMN_IDENTITIES_SECRET_KEY_IV));
            return new IdentityRecord.IdentityRecordBuilder()
                    .setId(id)
                    .setName(name)
                    .setUri(Uri.parse(uri))
                    .setRelativeIconUri(Uri.parse(iconRelativeUri))
                    .setSecretKeyCiphertext(identityKeyCiphertext)
                    .setSecretKeyIV(identityKeyIV)
                    .build();
        } catch (IllegalArgumentException e) {
            return new IdentityRecord.IdentityRecordBuilder().build();
        }
    }

    private static final String TAG = "IDENTITY_RECORD_DAO";
}