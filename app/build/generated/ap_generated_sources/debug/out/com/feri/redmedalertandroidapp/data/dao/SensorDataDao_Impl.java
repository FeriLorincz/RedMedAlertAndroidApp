package com.feri.redmedalertandroidapp.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.feri.redmedalertandroidapp.data.model.SensorDataEntity;
import java.lang.Class;
import java.lang.Double;
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
public final class SensorDataDao_Impl implements SensorDataDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SensorDataEntity> __insertionAdapterOfSensorDataEntity;

  private final EntityDeletionOrUpdateAdapter<SensorDataEntity> __updateAdapterOfSensorDataEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldSyncedData;

  private final SharedSQLiteStatement __preparedStmtOfInsertRaw;

  public SensorDataDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSensorDataEntity = new EntityInsertionAdapter<SensorDataEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `sensor_data` (`id`,`deviceId`,`userId`,`sensorType`,`value`,`secondaryValue`,`unit`,`timestamp`,`isAnomalous`,`additionalInfo`,`isSynced`,`uploadAttempts`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SensorDataEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getDeviceId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDeviceId());
        }
        if (entity.getUserId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getUserId());
        }
        if (entity.getSensorType() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSensorType());
        }
        statement.bindDouble(5, entity.getValue());
        if (entity.getSecondaryValue() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getSecondaryValue());
        }
        if (entity.getUnit() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getUnit());
        }
        statement.bindLong(8, entity.getTimestamp());
        final int _tmp = entity.isAnomalous() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getAdditionalInfo() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getAdditionalInfo());
        }
        final int _tmp_1 = entity.isSynced() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getUploadAttempts());
      }
    };
    this.__updateAdapterOfSensorDataEntity = new EntityDeletionOrUpdateAdapter<SensorDataEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `sensor_data` SET `id` = ?,`deviceId` = ?,`userId` = ?,`sensorType` = ?,`value` = ?,`secondaryValue` = ?,`unit` = ?,`timestamp` = ?,`isAnomalous` = ?,`additionalInfo` = ?,`isSynced` = ?,`uploadAttempts` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SensorDataEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getDeviceId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDeviceId());
        }
        if (entity.getUserId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getUserId());
        }
        if (entity.getSensorType() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSensorType());
        }
        statement.bindDouble(5, entity.getValue());
        if (entity.getSecondaryValue() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getSecondaryValue());
        }
        if (entity.getUnit() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getUnit());
        }
        statement.bindLong(8, entity.getTimestamp());
        final int _tmp = entity.isAnomalous() ? 1 : 0;
        statement.bindLong(9, _tmp);
        if (entity.getAdditionalInfo() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getAdditionalInfo());
        }
        final int _tmp_1 = entity.isSynced() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getUploadAttempts());
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteOldSyncedData = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sensor_data WHERE isSynced = 1 AND timestamp < ?";
        return _query;
      }
    };
    this.__preparedStmtOfInsertRaw = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT INTO sensor_data (deviceId, userId, sensorType, value, unit, timestamp, isSynced, uploadAttempts) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return _query;
      }
    };
  }

  @Override
  public long insert(final SensorDataEntity sensorData) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfSensorDataEntity.insertAndReturnId(sensorData);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Long> insertAll(final List<SensorDataEntity> sensorDataList) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final List<Long> _result = __insertionAdapterOfSensorDataEntity.insertAndReturnIdsList(sensorDataList);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final SensorDataEntity sensorData) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfSensorDataEntity.handle(sensorData);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteOldSyncedData(final long timestamp) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldSyncedData.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, timestamp);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteOldSyncedData.release(_stmt);
    }
  }

  @Override
  public Long insertRaw(final String deviceId, final String userId, final String sensorType,
      final double value, final String unit, final long timestamp, final boolean isSynced,
      final int uploadAttempts) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfInsertRaw.acquire();
    int _argIndex = 1;
    if (deviceId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, deviceId);
    }
    _argIndex = 2;
    if (userId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, userId);
    }
    _argIndex = 3;
    if (sensorType == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, sensorType);
    }
    _argIndex = 4;
    _stmt.bindDouble(_argIndex, value);
    _argIndex = 5;
    if (unit == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, unit);
    }
    _argIndex = 6;
    _stmt.bindLong(_argIndex, timestamp);
    _argIndex = 7;
    final int _tmp = isSynced ? 1 : 0;
    _stmt.bindLong(_argIndex, _tmp);
    _argIndex = 8;
    _stmt.bindLong(_argIndex, uploadAttempts);
    try {
      __db.beginTransaction();
      try {
        final Long _result = _stmt.executeInsert();
        __db.setTransactionSuccessful();
        return _result;
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfInsertRaw.release(_stmt);
    }
  }

  @Override
  public List<SensorDataEntity> getUnsyncedData() {
    final String _sql = "SELECT * FROM sensor_data WHERE isSynced = 0 ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfSensorType = CursorUtil.getColumnIndexOrThrow(_cursor, "sensorType");
      final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
      final int _cursorIndexOfSecondaryValue = CursorUtil.getColumnIndexOrThrow(_cursor, "secondaryValue");
      final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfIsAnomalous = CursorUtil.getColumnIndexOrThrow(_cursor, "isAnomalous");
      final int _cursorIndexOfAdditionalInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalInfo");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final int _cursorIndexOfUploadAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadAttempts");
      final List<SensorDataEntity> _result = new ArrayList<SensorDataEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SensorDataEntity _item;
        _item = new SensorDataEntity();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpDeviceId;
        if (_cursor.isNull(_cursorIndexOfDeviceId)) {
          _tmpDeviceId = null;
        } else {
          _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
        }
        _item.setDeviceId(_tmpDeviceId);
        final String _tmpUserId;
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _tmpUserId = null;
        } else {
          _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        }
        _item.setUserId(_tmpUserId);
        final String _tmpSensorType;
        if (_cursor.isNull(_cursorIndexOfSensorType)) {
          _tmpSensorType = null;
        } else {
          _tmpSensorType = _cursor.getString(_cursorIndexOfSensorType);
        }
        _item.setSensorType(_tmpSensorType);
        final double _tmpValue;
        _tmpValue = _cursor.getDouble(_cursorIndexOfValue);
        _item.setValue(_tmpValue);
        final Double _tmpSecondaryValue;
        if (_cursor.isNull(_cursorIndexOfSecondaryValue)) {
          _tmpSecondaryValue = null;
        } else {
          _tmpSecondaryValue = _cursor.getDouble(_cursorIndexOfSecondaryValue);
        }
        _item.setSecondaryValue(_tmpSecondaryValue);
        final String _tmpUnit;
        if (_cursor.isNull(_cursorIndexOfUnit)) {
          _tmpUnit = null;
        } else {
          _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
        }
        _item.setUnit(_tmpUnit);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.setTimestamp(_tmpTimestamp);
        final boolean _tmpIsAnomalous;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAnomalous);
        _tmpIsAnomalous = _tmp != 0;
        _item.setAnomalous(_tmpIsAnomalous);
        final String _tmpAdditionalInfo;
        if (_cursor.isNull(_cursorIndexOfAdditionalInfo)) {
          _tmpAdditionalInfo = null;
        } else {
          _tmpAdditionalInfo = _cursor.getString(_cursorIndexOfAdditionalInfo);
        }
        _item.setAdditionalInfo(_tmpAdditionalInfo);
        final boolean _tmpIsSynced;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_1 != 0;
        _item.setSynced(_tmpIsSynced);
        final int _tmpUploadAttempts;
        _tmpUploadAttempts = _cursor.getInt(_cursorIndexOfUploadAttempts);
        _item.setUploadAttempts(_tmpUploadAttempts);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<SensorDataEntity> getRecentDataByType(final String type, final long since) {
    final String _sql = "SELECT * FROM sensor_data WHERE sensorType = ? AND timestamp > ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, since);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfSensorType = CursorUtil.getColumnIndexOrThrow(_cursor, "sensorType");
      final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
      final int _cursorIndexOfSecondaryValue = CursorUtil.getColumnIndexOrThrow(_cursor, "secondaryValue");
      final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfIsAnomalous = CursorUtil.getColumnIndexOrThrow(_cursor, "isAnomalous");
      final int _cursorIndexOfAdditionalInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalInfo");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final int _cursorIndexOfUploadAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadAttempts");
      final List<SensorDataEntity> _result = new ArrayList<SensorDataEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SensorDataEntity _item;
        _item = new SensorDataEntity();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item.setId(_tmpId);
        final String _tmpDeviceId;
        if (_cursor.isNull(_cursorIndexOfDeviceId)) {
          _tmpDeviceId = null;
        } else {
          _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
        }
        _item.setDeviceId(_tmpDeviceId);
        final String _tmpUserId;
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _tmpUserId = null;
        } else {
          _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        }
        _item.setUserId(_tmpUserId);
        final String _tmpSensorType;
        if (_cursor.isNull(_cursorIndexOfSensorType)) {
          _tmpSensorType = null;
        } else {
          _tmpSensorType = _cursor.getString(_cursorIndexOfSensorType);
        }
        _item.setSensorType(_tmpSensorType);
        final double _tmpValue;
        _tmpValue = _cursor.getDouble(_cursorIndexOfValue);
        _item.setValue(_tmpValue);
        final Double _tmpSecondaryValue;
        if (_cursor.isNull(_cursorIndexOfSecondaryValue)) {
          _tmpSecondaryValue = null;
        } else {
          _tmpSecondaryValue = _cursor.getDouble(_cursorIndexOfSecondaryValue);
        }
        _item.setSecondaryValue(_tmpSecondaryValue);
        final String _tmpUnit;
        if (_cursor.isNull(_cursorIndexOfUnit)) {
          _tmpUnit = null;
        } else {
          _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
        }
        _item.setUnit(_tmpUnit);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item.setTimestamp(_tmpTimestamp);
        final boolean _tmpIsAnomalous;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAnomalous);
        _tmpIsAnomalous = _tmp != 0;
        _item.setAnomalous(_tmpIsAnomalous);
        final String _tmpAdditionalInfo;
        if (_cursor.isNull(_cursorIndexOfAdditionalInfo)) {
          _tmpAdditionalInfo = null;
        } else {
          _tmpAdditionalInfo = _cursor.getString(_cursorIndexOfAdditionalInfo);
        }
        _item.setAdditionalInfo(_tmpAdditionalInfo);
        final boolean _tmpIsSynced;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_1 != 0;
        _item.setSynced(_tmpIsSynced);
        final int _tmpUploadAttempts;
        _tmpUploadAttempts = _cursor.getInt(_cursorIndexOfUploadAttempts);
        _item.setUploadAttempts(_tmpUploadAttempts);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int countUnsyncedById(final List<Long> ids) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT COUNT(*) FROM sensor_data WHERE id IN (");
    final int _inputSize = ids == null ? 1 : ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") AND isSynced = 0");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    if (ids == null) {
      _statement.bindNull(_argIndex);
    } else {
      for (Long _item : ids) {
        if (_item == null) {
          _statement.bindNull(_argIndex);
        } else {
          _statement.bindLong(_argIndex, _item);
        }
        _argIndex++;
      }
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<SensorDataEntity> getByIds(final List<Long> ids) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM sensor_data WHERE id IN (");
    final int _inputSize = ids == null ? 1 : ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    if (ids == null) {
      _statement.bindNull(_argIndex);
    } else {
      for (Long _item : ids) {
        if (_item == null) {
          _statement.bindNull(_argIndex);
        } else {
          _statement.bindLong(_argIndex, _item);
        }
        _argIndex++;
      }
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
      final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
      final int _cursorIndexOfSensorType = CursorUtil.getColumnIndexOrThrow(_cursor, "sensorType");
      final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
      final int _cursorIndexOfSecondaryValue = CursorUtil.getColumnIndexOrThrow(_cursor, "secondaryValue");
      final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfIsAnomalous = CursorUtil.getColumnIndexOrThrow(_cursor, "isAnomalous");
      final int _cursorIndexOfAdditionalInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "additionalInfo");
      final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
      final int _cursorIndexOfUploadAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadAttempts");
      final List<SensorDataEntity> _result = new ArrayList<SensorDataEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final SensorDataEntity _item_1;
        _item_1 = new SensorDataEntity();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _item_1.setId(_tmpId);
        final String _tmpDeviceId;
        if (_cursor.isNull(_cursorIndexOfDeviceId)) {
          _tmpDeviceId = null;
        } else {
          _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
        }
        _item_1.setDeviceId(_tmpDeviceId);
        final String _tmpUserId;
        if (_cursor.isNull(_cursorIndexOfUserId)) {
          _tmpUserId = null;
        } else {
          _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
        }
        _item_1.setUserId(_tmpUserId);
        final String _tmpSensorType;
        if (_cursor.isNull(_cursorIndexOfSensorType)) {
          _tmpSensorType = null;
        } else {
          _tmpSensorType = _cursor.getString(_cursorIndexOfSensorType);
        }
        _item_1.setSensorType(_tmpSensorType);
        final double _tmpValue;
        _tmpValue = _cursor.getDouble(_cursorIndexOfValue);
        _item_1.setValue(_tmpValue);
        final Double _tmpSecondaryValue;
        if (_cursor.isNull(_cursorIndexOfSecondaryValue)) {
          _tmpSecondaryValue = null;
        } else {
          _tmpSecondaryValue = _cursor.getDouble(_cursorIndexOfSecondaryValue);
        }
        _item_1.setSecondaryValue(_tmpSecondaryValue);
        final String _tmpUnit;
        if (_cursor.isNull(_cursorIndexOfUnit)) {
          _tmpUnit = null;
        } else {
          _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
        }
        _item_1.setUnit(_tmpUnit);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _item_1.setTimestamp(_tmpTimestamp);
        final boolean _tmpIsAnomalous;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAnomalous);
        _tmpIsAnomalous = _tmp != 0;
        _item_1.setAnomalous(_tmpIsAnomalous);
        final String _tmpAdditionalInfo;
        if (_cursor.isNull(_cursorIndexOfAdditionalInfo)) {
          _tmpAdditionalInfo = null;
        } else {
          _tmpAdditionalInfo = _cursor.getString(_cursorIndexOfAdditionalInfo);
        }
        _item_1.setAdditionalInfo(_tmpAdditionalInfo);
        final boolean _tmpIsSynced;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsSynced);
        _tmpIsSynced = _tmp_1 != 0;
        _item_1.setSynced(_tmpIsSynced);
        final int _tmpUploadAttempts;
        _tmpUploadAttempts = _cursor.getInt(_cursorIndexOfUploadAttempts);
        _item_1.setUploadAttempts(_tmpUploadAttempts);
        _result.add(_item_1);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int markAsSynced(final List<Long> ids) {
    __db.assertNotSuspendingTransaction();
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("UPDATE sensor_data SET isSynced = 1 WHERE id IN (");
    final int _inputSize = ids == null ? 1 : ids.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") AND isSynced = 0");
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
      final int _result = _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void incrementUploadAttempts(final List<Long> ids) {
    __db.assertNotSuspendingTransaction();
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("UPDATE sensor_data SET uploadAttempts = uploadAttempts + 1 WHERE id IN (");
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
