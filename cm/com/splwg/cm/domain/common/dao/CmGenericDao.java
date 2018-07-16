package com.splwg.cm.domain.common.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.GenericBusinessObject;
import com.splwg.base.api.Query;
import com.splwg.base.api.SimpleKeyedBusinessEntity;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.DateTime;
import com.splwg.base.api.datatypes.DayInMonth;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.datatypes.Id;
import com.splwg.base.api.datatypes.IntegerId;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.api.datatypes.MonthInYear;
import com.splwg.base.api.datatypes.StringId;
import com.splwg.base.api.datatypes.Time;
import com.splwg.base.api.sql.PreparedStatement;
import com.splwg.base.api.sql.SQLResultRow;
import com.splwg.base.support.context.SessionHolder;
import com.splwg.cm.domain.common.constant.CmConstants;
import com.splwg.cm.domain.common.dao.constants.CmGenericRequest;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;

/**
 * DAO generique
 * @author ADA
 */
public class CmGenericDao<T> extends GenericBusinessObject {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger( CmGenericDao.class );

    /**
     * 
     * @param clazz
     */
    public CmGenericDao( final Class<T> clazz ) {
        //this.invocateur = new CmInvocateurDao<T>( clazz );
    }

    /**
     * @author CBAUWENS
     * @param pRequete la requete
     * @return a query
     * @deprecated Methode n'ajoutant aucun plus : a supprimer
     */
    @Deprecated
    public Query getQuery( String pRequete ) {
        return createQuery( pRequete );
    }

    /**
     * Build à string of values in the List<String> the default delimitor is : ' and the default separator is : ,
     * @see com.splwg.cm.domain.common.dao.CmGenericDao#buildContentClauseIn(List, String, String)
     * @param pListString list of string values
     * @return String, null if the list is null, or a joined values of the list example :"'toto', 'tata', 'titi'"
     */
    protected final String buildContentClauseIn( List<String> pListString ) {
        return this.buildContentClauseIn( pListString, "'", "," );
    }

    /**
     * Build à string of values in the List<? extends StringId<?>> the default delimitor is : ' and the default separator is : ,
     * @see com.splwg.cm.domain.common.dao.CmGenericDao#buildContentClauseIn(List, String, String)
     * @param pListString list of string values
     * @return String, null if the list is null, or a joined values of the list example :"'758327', '371221', '871523'"
     */
    protected final String buildContentClauseInById( List<? extends StringId<?>> pListString ) {
        return this.buildContentClauseInById( pListString, "'", "," );
    }

    /**
     * Build à string of values in the List<String>
     * @param pListString list of string values
     * @param pDelimitor the delimitor example : '
     * @param pSeparator the separator example : , 
     * @return String, null if the list is null, or a joined values of the list example : "'toto', 'tata', 'titi'" 
     */
    protected final String buildContentClauseIn( List<String> pListString, String pDelimitor, String pSeparator ) {
        if ( pListString != null ) {

            final StringBuilder vStringBuilder = new StringBuilder();

            boolean vIsFirst = true;

            for ( final String vToken : pListString ) {
                if ( vIsFirst ) {
                    vStringBuilder.append( pDelimitor );
                    vIsFirst = false;
                } else {
                    vStringBuilder.append( pSeparator + pDelimitor );
                }
                vStringBuilder.append( vToken ).append( pDelimitor );
            }

            return vStringBuilder.toString();
        }

        return null;
    }

    /**
     * Build à string of values in the List<? extends StringId<?>>
     * @param pListString list of string values
     * @param pDelimitor the delimitor example : '
     * @param pSeparator the separator example : , 
     * @return String, null if the list is null, or a joined values of the list example : "'758327', '371221', '871523'" 
     */
    protected final String buildContentClauseInById( List<? extends StringId<?>> pListString, String pDelimitor, String pSeparator ) {
        if ( pListString != null ) {

            final StringBuilder vStringBuilder = new StringBuilder();

            boolean vIsFirst = true;

            for ( final StringId<?> vToken : pListString ) {
                if ( vIsFirst ) {
                    vStringBuilder.append( pDelimitor );
                    vIsFirst = false;
                } else {
                    vStringBuilder.append( pSeparator + pDelimitor );
                }
                vStringBuilder.append( vToken.getTrimmedValue() ).append( pDelimitor );
            }

            return vStringBuilder.toString();
        }

        return null;
    }

    /**
     * Retrieve the entire entity by the Id
     * @param clazz the class which represents the entity
     * @param key Id of the entity
     * @return Object (Entity)
     */
    public static <T> T getEntity( final Class<?> clazz, final EntityId<?> key ) {

        T t = null;
        try {
            if ( key == null ) {
                return null;
            }
            final Query<T> query =
                SessionHolder.getSession().createQuery( CmGenericRequest.GET_CLASS_BY_ID.replace( ":pName", clazz.getName() ) );

            query.bindId( "pId", key );
            t = query.firstRow();

        } catch ( Exception e ) {
            LOGGER.error( "Error in generic DAO : ", e );
        }

        return t;
    }

    /**
     * Returns the first row.
     * @param pQuery Query string
     * @param pParams Query Parameters
     * @return First SQL row, null if no data found or an exception occurred
     */
    public SQLResultRow getFirstRow( final String pQuery, final QueryParameters pParams ) {
        final long time = System.currentTimeMillis();
        SQLResultRow singleRow = null;

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = this.createPreparedStatement( pQuery );
            addParametersToStatement( preparedStatement, pParams );
            singleRow = preparedStatement.firstRow();

        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        } finally {
            if ( preparedStatement != null ) {
                preparedStatement.close();
            }
        }

        LOGGER.debug( "- getFirstRow (" + LOGGER.getElapsedTime( time ) + "|ms) " + singleRow );
        return singleRow;
    }

    /**
     * Returns the first row.
     * @param pQuery Query string
     * @param pParams Query Parameters
     * @return First SQL row, null if no data found or an exception occurred
     */
    public SQLResultRow getFirstRow( final String pQuery, final Object... pParams ) {
        final long time = System.currentTimeMillis();
        SQLResultRow singleRow = null;

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = this.createPreparedStatement( pQuery );
            addParametersToStatement( preparedStatement, pParams );
            singleRow = preparedStatement.firstRow();

        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        } finally {
            if ( preparedStatement != null ) {
                preparedStatement.close();
            }
        }

        LOGGER.debug( "- getFirstRow (" + LOGGER.getElapsedTime( time ) + "|ms) " + singleRow );
        return singleRow;
    }

    /**
     * Returns the result list.
     * @param pQuery Query string
     * @param pParams Query Parameters
     * @return SQL result list, null if an exception occurred
     */
    public List<SQLResultRow> getList( final String pQuery, final QueryParameters pParams ) {
        final long time = System.currentTimeMillis();
        List<SQLResultRow> rowList = null;

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = this.createPreparedStatement( pQuery );
            addParametersToStatement( preparedStatement, pParams );
            rowList = preparedStatement.list();

        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        } finally {
            if ( preparedStatement != null ) {
                preparedStatement.close();
            }
        }

        LOGGER.debug( "- getList (" + LOGGER.getElapsedTime( time ) + "|ms) " );
        return rowList;
    }

    /**
     * Returns the result list.
     * @param pQuery Query string
     * @param pParams Query Parameters
     * @return SQL result list, null if an exception occurred
     */
    public List<SQLResultRow> getList( final String pQuery, final Object... pParams ) {
        final long time = System.currentTimeMillis();
        List<SQLResultRow> rowList = null;

        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = this.createPreparedStatement( pQuery );
            addParametersToStatement( preparedStatement, pParams );
            rowList = preparedStatement.list();

        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        } finally {
            if ( preparedStatement != null ) {
                preparedStatement.close();
            }
        }

        LOGGER.debug( "- getList (" + LOGGER.getElapsedTime( time ) + "|ms) " );
        return rowList;
    }

    /**
     * Returns the first string result
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pParams Query parameters
     * @return first string result, null if no data found or an exception occurred
     */
    public String getFirstString( final String pQuery, final String pKey, final QueryParameters pParams ) {
        String stringValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                stringValue = vRow.getString( pKey );
            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return stringValue;
    }

    /**
     * Returns the first string result
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pParams Query parameters
     * @return first string result, null if no data found or an exception occurred
     */
    public String getFirstString( final String pQuery, final String pKey, Object... pParams ) {
        String stringValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                stringValue = vRow.getString( pKey );
            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return stringValue;
    }

    /**
     * Returns the first integer result
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pParams Query parameters
     * @return first integer result, null if no data found or an exception occurred
     */
    public BigInteger getFirstInteger( final String pQuery, final String pKey, final QueryParameters pParams ) {
        BigInteger intValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                intValue = vRow.getInteger( pKey );
            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return intValue;
    }

    /**
     * Returns the first integer result
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pParams Query parameters
     * @return first integer result, null if no data found or an exception occurred
     */
    public BigInteger getFirstInteger( final String pQuery, final String pKey, final Object... pParams ) {
        BigInteger intValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                intValue = vRow.getInteger( pKey );
            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return intValue;
    }

    /**
     * Returns the first decimal result
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pParams Query parameters
     * @return first string result, null if no data found or an exception occurred
     */
    public BigDecimal getFirstDecimal( final String pQuery, final String pKey, QueryParameters pParams ) {
        BigDecimal decimalValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                decimalValue = vRow.getBigDecimal( pKey );
            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return decimalValue;
    }

    /**
     * Returns the first decimal result
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pParams Query parameters
     * @return first string result, null if no data found or an exception occurred
     */
    public BigDecimal getFirstDecimal( final String pQuery, final String pKey, Object... pParams ) {
        BigDecimal decimalValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                decimalValue = vRow.getBigDecimal( pKey );
            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return decimalValue;
    }

    /**
     * Returns the first entity ID
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pType Entity type
     * @param pParams Query parameters
     * @param <E> type
     * @return First entity ID, null if no data found or an exception occurred
     */
    public <E extends SimpleKeyedBusinessEntity<E>> Id getFirstId( final String pQuery, final String pKey, final Class<E> pType,
                                                                   final QueryParameters pParams ) {
        Id idValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                idValue = vRow.getId( pKey, pType );

            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return idValue;
    }

    /**
     * Returns the first entity ID
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pType Entity type
     * @param pParams Query parameters
     * @param <E> type
     * @return First entity ID, null if no data found or an exception occurred
     */
    public <E extends SimpleKeyedBusinessEntity<E>> Id getFirstId( final String pQuery, final String pKey, final Class<E> pType,
                                                                   Object... pParams ) {
        Id idValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                idValue = vRow.getId( pKey, pType );

            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return idValue;
    }

    /**
     * Returns the first entity
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pType Entity type
     * @param pParams Query parameters
     * @param <E> type
     * @return First entity, null if no data found or an exception occurred
     */
    public <E extends SimpleKeyedBusinessEntity<?>> E getFirstEntity( final String pQuery, final String pKey, final Class<E> pType,
                                                                      QueryParameters pParams ) {
        E idValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                idValue = vRow.getEntity( pKey, pType );

            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return idValue;
    }

    /**
     * Returns the first entity
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pType Entity type
     * @param pParams Query parameters
     * @param <E> type
     * @return First entity, null if no data found or an exception occurred
     */
    public <E extends SimpleKeyedBusinessEntity<?>> E getFirstEntity( final String pQuery, final String pKey, final Class<E> pType,
                                                                      Object... pParams ) {
        E idValue = null;

        try {
            final SQLResultRow vRow = this.getFirstRow( pQuery, pParams );
            if ( vRow != null ) {
                idValue = vRow.getEntity( pKey, pType );

            }
        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return idValue;
    }

    /**
     * Returns the entities list
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pType Entity type
     * @param pParams Query parameters
     * @param <E> type
     * @return Entities list, null if an exception occurred
     */
    public <E extends SimpleKeyedBusinessEntity<?>> List<E> getEntities( final String pQuery, final String pKey, final Class<E> pType,
                                                                         QueryParameters pParams ) {
        List<E> entities = null;

        try {
            final List<SQLResultRow> vRows = this.getList( pQuery, pParams );
            if ( vRows != null ) {
                entities = new ArrayList<E>();
                for ( final SQLResultRow vRow : vRows ) {
                    entities.add( vRow.getEntity( pKey, pType ) );

                }
            }

        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return entities;
    }

    /**
     * Returns the entities list
     * @param pQuery Query String
     * @param pKey Row Id key
     * @param pType Entity type
     * @param pParams Query parameters
     * @param <E> type
     * @return Entities list, null if an exception occurred
     */
    public <E extends SimpleKeyedBusinessEntity<?>> List<E> getEntities( final String pQuery, final String pKey, final Class<E> pType,
                                                                         Object... pParams ) {
        List<E> entities = null;

        try {
            final List<SQLResultRow> vRows = this.getList( pQuery, pParams );
            if ( vRows != null ) {
                entities = new ArrayList<E>();
                for ( final SQLResultRow vRow : vRows ) {
                    entities.add( vRow.getEntity( pKey, pType ) );

                }
            }

        } catch ( final Exception excp ) {
            LOGGER.error( CmConstants.EXCEPTION, excp );
        }

        return entities;
    }

    /**
     * Binds supplied parameters to the prepared statement statement
     * @param pStatement Statement
     * @param pParams Parameters
     */
    private void addParametersToStatement( final PreparedStatement pStatement, final QueryParameters pParams ) {
        for ( Entry<String, Object> param : pParams.getParameters() ) {
            addParameterToStatement( pStatement, param.getKey(), param.getValue() );
        }
    }

    /**
     * Binds supplied parameters to the prepared statement statement
     * @param pStatement Statement
     * @param pParams Parameters
     */
    private void addParametersToStatement( final PreparedStatement pStatement, final Object... pParams ) {

        if ( pParams.length % 2 != 0 ) {
            throw new IllegalArgumentException( "Le nombre de parametres fournis n'est pas suffisant (" + pParams.length + ")" );
        }
        for ( int i = 0; i < pParams.length; i = i + 2 ) {
            addParameterToStatement( pStatement, pParams[i].toString(), pParams[i + 1] );
        }
    }

    /**
     * Binds the supplied parameter to the prepared statement
     * @param pStatement Statement
     * @param pParamName Parameter name
     * @param pParamValue Parameter value
     */
    private void addParameterToStatement( final PreparedStatement pStatement, final String pParamName, final Object pParamValue ) {
        if ( pParamValue instanceof String ) {
            pStatement.bindString( pParamName, ( String ) pParamValue, null );
        } else if ( pParamValue instanceof StringId ) {
            pStatement.bindId( pParamName, ( StringId<?> ) pParamValue );
        } else if ( pParamValue instanceof IntegerId<?> ) {
            pStatement.bindId( pParamName, ( IntegerId<?> ) pParamValue );
        } else if ( pParamValue instanceof Lookup ) {
            pStatement.bindLookup( pParamName, ( Lookup ) pParamValue );
        } else if ( pParamValue instanceof BigDecimal ) {
            pStatement.bindBigDecimal( pParamName, ( BigDecimal ) pParamValue );
        } else if ( pParamValue instanceof BigInteger ) {
            pStatement.bindBigInteger( pParamName, ( BigInteger ) pParamValue );
        } else if ( pParamValue instanceof Bool ) {
            pStatement.bindBoolean( pParamName, ( Bool ) pParamValue );
        } else if ( pParamValue instanceof Date ) {
            pStatement.bindDate( pParamName, ( Date ) pParamValue );
        } else if ( pParamValue instanceof DateTime ) {
            pStatement.bindDateTime( pParamName, ( DateTime ) pParamValue );
        } else if ( pParamValue instanceof DayInMonth ) {
            pStatement.bindDayInMonth( pParamName, ( DayInMonth ) pParamValue );
        } else if ( pParamValue instanceof SimpleKeyedBusinessEntity<?> ) {
            pStatement.bindEntity( pParamName, ( SimpleKeyedBusinessEntity<?> ) pParamValue );
        } else if ( pParamValue instanceof Money ) {
            pStatement.bindMoney( pParamName, ( Money ) pParamValue );
        } else if ( pParamValue instanceof MonthInYear ) {
            pStatement.bindMonthInYear( pParamName, ( MonthInYear ) pParamValue );
        } else if ( pParamValue instanceof Time ) {
            pStatement.bindTime( pParamName, ( Time ) pParamValue );
        }
    }

    /**
     * 
     * @author yelkassi
     *
     */
    protected static class QueryParameters {

        /**
         * 
         */
        public QueryParameters() {

        }

        /**
         * 
         * @param pParamName Parameter name
         * @param pParamValue Parameter value
         */
        public QueryParameters( String pParamName, Object pParamValue ) {
            addParameter( pParamName, pParamValue );
        }

        /**
         * 
         */
        private final Map<String, Object> parametersMap = new HashMap<String, Object>();

        /**
         * 
         * @param pParamName Parameter name
         * @param pParamValue Parameter value
         */
        public void addParameter( String pParamName, Object pParamValue ) {
            this.parametersMap.put( pParamName, pParamValue );
        }

        /**
         * 
         * @return Parameters set
         */
        public Set<Entry<String, Object>> getParameters() {
            return this.parametersMap.entrySet();
        }
    }

}