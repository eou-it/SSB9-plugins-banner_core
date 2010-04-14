/*
 * Copyright (c) SunGard 2009. All rights reserved.
 */
package com.sungardhe.banner.framework.persistence.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.AbstractPostInsertGenerator;
import org.hibernate.id.IdentifierGeneratorFactory;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.id.SequenceIdentityGenerator.NoCommentsInsert;
import org.hibernate.id.insert.AbstractReturningDelegate;
import org.hibernate.id.insert.IdentifierGeneratingInsert;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;

/**
 * A generator with immediate retrieval through JDBC3
 * {@link java.sql.Connection#prepareStatement(String, String[])
 * getGeneratedKeys}. The value of the identity column must be set from a
 * "before insert trigger" <p/> This generator only known to work with newer
 * Oracle drivers compiled for JDK 1.4 (JDBC3). The minimum version is 10.2.0.1
 * <p/> Note: Due to a bug in Oracle drivers, sql comments on these insert
 * statements are completely disabled.
 *
 * @author Sungard Higher Education
 */

public class TriggerAssignedIdentityGenerator
                extends
                    AbstractPostInsertGenerator {

    //==========================================================================
    // Static Attributes
    //==========================================================================

    //==========================================================================
    // Static Methods
    //==========================================================================

    //==========================================================================
    // Attributes
    //==========================================================================

    //==========================================================================
    // Constructors
    //==========================================================================

    //==========================================================================
    // Methods
    //==========================================================================
    /**
     * Return <code>InsertGeneratedIdentifierDelegate</code>.
     *
     * @param persister
     *            the <code>PostInsertIdentityPersister</code>
     * @param dialect
     *            the <code>Dialect</code>
     * @param isGetGeneratedKeysEnabled
     *            indicates whether generated keys are enabled
     * @throws HibernateException
     *             <code>HibernateException</code>
     * @return InsertGeneratedIdentifierDelegate
     */
    public InsertGeneratedIdentifierDelegate getInsertGeneratedIdentifierDelegate(
                    PostInsertIdentityPersister persister,
                    Dialect dialect,
                    boolean isGetGeneratedKeysEnabled) {
        return new Delegate(persister, dialect);
    }

    //==========================================================================
    // Inner Classes
    //==========================================================================

    /**
     * The delegate.
     */
    public static class Delegate extends AbstractReturningDelegate {

        /**
         * The <code>Dialect</code>.
         */
        private final Dialect dialect;

        /**
         * The key columns.
         */
        private final String[] keyColumns;

        /**
         *
         * Constructor for Delegate.
         *
         * @param persister
         *            <code>PostInsertIdentityPersister</code>
         * @param dialect
         *            <code>Dialect</code>
         */
        public Delegate(PostInsertIdentityPersister persister, Dialect dialect) {
            super(persister);
            this.dialect = dialect;
            this.keyColumns = getPersister().getRootTableKeyColumnNames();
            if (keyColumns.length > 1) {
                throw new HibernateException(
                    "trigger assigned identity generator cannot be used with multi-column keys");
            }
        }

        /**
         * Prepare the insert.
         *
         * @return <code>IdentifierGeneratingInsert</code>
         */
        public IdentifierGeneratingInsert prepareIdentifierGeneratingInsert() {
            NoCommentsInsert insert = new NoCommentsInsert(dialect);
            return insert;
        }

        /**
         * Prepare the statement.
         *
         * @param insertSQL
         *            the sql for the insert
         * @param session
         *            the <code>SessionImplementor</code>
         * @throws SQLException
         *             <code>SQLException</code>
         * @return <code>PreparedStatement</code>
         */
        protected PreparedStatement prepare(
                        String insertSQL,
                        SessionImplementor session) throws SQLException {
            return session.getBatcher().prepareStatement(insertSQL, keyColumns);
        }

        /**
         * Execute the prepared statement.
         *
         * @param insert
         *            <code>PreparedStatement</code> to be executed
         * @throws SQLException
         *             <code>SQLException</code>
         * @return <code>Serializable</code>
         */
        protected Serializable executeAndExtract(PreparedStatement insert)
                        throws SQLException {
            insert.executeUpdate();
            return IdentifierGeneratorFactory.getGeneratedIdentity(insert
                .getGeneratedKeys(), getPersister().getIdentifierType());
        }
    }
}
