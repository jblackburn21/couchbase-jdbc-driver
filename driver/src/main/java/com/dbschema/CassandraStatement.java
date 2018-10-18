package com.dbschema;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.exceptions.SyntaxError;
import com.dbschema.resultSet.CassandraResultSet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CassandraStatement extends CassandraBaseStatement {

    private final Session session;
    private final List<String> batchStatements = new ArrayList<>();

    CassandraStatement(Session session) {
        this.session = session;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        checkClosed();
        try {
            result = new CassandraResultSet(this, session.execute(sql));
            return result;
        } catch (SyntaxError ex) {
            throw new SQLSyntaxErrorException(ex);
        } catch (DriverException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        checkClosed();
        try {
            result = new CassandraResultSet(this, session.execute(sql));
            if (result.isQuery()) {
                throw new SQLException("Not an update statement");
            }
            return 1;
        } catch (SyntaxError ex) {
            throw new SQLSyntaxErrorException(ex);
        } catch (DriverException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        checkClosed();
        return executeInner(session.execute(sql));
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        checkClosed();
        return result;
    }

    @Override
    public void addBatch(String sql) {
        batchStatements.add(sql);
    }

    @Override
    public int[] executeBatch() throws SQLException {
        StringBuilder builder = new StringBuilder("BEGIN BATCH\n");
        for (String batchStatement : batchStatements) {
            builder.append(batchStatement).append(";\n");
        }
        builder.append("APPLY BATCH");
        execute(builder.toString());
        int[] res = new int[batchStatements.size()];
        for (int i = 0; i < batchStatements.size(); i++) {
            res[i] = SUCCESS_NO_INFO;
        }
        return res;
    }

    @Override
    public void clearBatch() {
        batchStatements.clear();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
