package com.feri.redmedalertandroidapp.api.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.feri.redmedalertandroidapp.api.model.HealthDataEntity;
import java.lang.Class;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HealthDataDao_Impl implements HealthDataDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HealthDataEntity> __insertionAdapterOfHealthDataEntity;

  public HealthDataDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHealthDataEntity = new EntityInsertionAdapter<HealthDataEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `health_data` (`id`,`timestamp`,`data_type`,`value`,`uploaded`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final HealthDataEntity entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.timestamp);
        if (entity.dataType == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.dataType);
        }
        statement.bindDouble(4, entity.value);
        final int _tmp = entity.uploaded ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
  }

  @Override
  public void insert(final HealthDataEntity data) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfHealthDataEntity.insert(data);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<HealthDataEntity> getUnuploadedData() {
    final String _sql = "SELECT * FROM health_data WHERE uploaded = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfDataType = CursorUtil.getColumnIndexOrThrow(_cursor, "data_type");
      final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
      final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
      final List<HealthDataEntity> _result = new ArrayList<HealthDataEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final HealthDataEntity _item;
        _item = new HealthDataEntity();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        if (_cursor.isNull(_cursorIndexOfDataType)) {
          _item.dataType = null;
        } else {
          _item.dataType = _cursor.getString(_cursorIndexOfDataType);
        }
        _item.value = _cursor.getDouble(_cursorIndexOfValue);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfUploaded);
        _item.uploaded = _tmp != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public void markAsUploaded(final List<Long> ids) {
    __db.assertNotSuspendingTransaction();
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("UPDATE health_data SET uploaded = 1 WHERE id IN (");
    final int _inputSize = ids == null ? 1 : ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
    int _argIndex = 1;
    if (ids == null) {
      _stmt.bindNull(_argIndex);
    } else {
      for (Long _item : ids) {
        if (_item == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, _item);
        }
        _argIndex++;
      }
    }
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
