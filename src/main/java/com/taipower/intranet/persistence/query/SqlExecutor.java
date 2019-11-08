package com.taipower.intranet.persistence.query;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Objects.requireNonNull;

/**
 * SQL 執行程式，提供一般 CRUD 功能。
 * <p>
 * 底層是 Spring Framework 的 {@link NamedParameterJdbcTemplate}，所以本程式所指的 SQL 均可以包含定名參數，例如
 * {@code SELECT * FROM T WHERE T.A = :a"}，當中的 {@code a} 就是參數 Map 中的鍵值。<strong>強烈建議使用
 * {@link Query#builder(CharSequence, Object...)} 協助產生 Query 物件。</strong>
 */
@Slf4j
@Component
public class SqlExecutor {

    @Getter
    @Setter
    private Class<? extends RowMapper> rowMapper = BeanPropertyRowMapper.class;


    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 執行無參數 DELETE SQL 語句。
     * 
     * @param sql
     *            DELETE SQL 語句。
     * @return 資料刪除筆數。
     */
    public int delete(CharSequence sql) {
        return delete(new Query(sql));
    }

    /**
     * 整批執行 DELETE SQL 語句。
     * 
     * @param sql
     *            DELETE SQL 語句。
     * @param listOfParameters
     *            每句 SQL 的參數 Map 集合。
     * @return 每次執行的資料刪除筆數。
     */
    public int[] delete(CharSequence sql, List<Map<String, ?>> listOfParameters) {
        return executeInBatch(sql, listOfParameters);
    }

    /**
     * 執行 DELETE SQL 語句。
     * 
     * @param sql
     *            DELETE SQL 語句。
     * @param parameters
     *            參數集。
     * @return 資料刪除筆數。
     */
    public int delete(CharSequence sql, Map<String, ?> parameters) {
        return delete(new Query(sql, parameters));
    }

    /**
     * 執行 DELETE Query。
     * 
     * @param query
     *            包含 SQL 語句，也可能有參數的 Query 物件。
     * @return 資料刪除筆數。
     */
    public int delete(Query query) {
        return execute(query);
    }

    private int execute(Query query) {
        requireNonNull(query, "Parameter \"query\" must not be null.");

        String queryString = query.getString();
        Map<String, Object> parameters = query.getParameters();
        logSqlExecute(queryString, parameters);

        return jdbcTemplate.update(queryString, parameters);
    }

    @SuppressWarnings("unchecked")
    private int[] executeInBatch(CharSequence sql, List<Map<String, ?>> parameters) {
        Preconditions.checkArgument(isNotNullAndNotEmpty(sql), "Query string must not be null.");
        requireNonNull(parameters,
                "Conditions must not be null. SQL without parameter is inadeqte to use batch method.");

        logExecuteInBatch(sql, parameters);

        return jdbcTemplate.batchUpdate(sql.toString(),
                parameters.toArray(new Map[parameters.size()]));
    }

    /**
     * 執行無參數 INSERT SQL 語句。
     * 
     * @param sql
     *            INSERT SQL 語句。
     * @return 資料新增筆數。
     */
    public int insert(CharSequence sql) {
        return insert(new Query(sql));
    }

    /**
     * 整批執行 INSERT SQL 語句。
     * 
     * @param sql
     *            INSERT SQL 語句。
     * @param listOfParameters
     *            每句 SQL 的參數 Map 集合。
     * @return 每次執行的資料新增筆數。
     */
    public int[] insert(CharSequence sql, List<Map<String, ?>> listOfParameters) {
        return executeInBatch(sql, listOfParameters);
    }

    /**
     * 執行 INSERT SQL 語句。
     * 
     * @param sql
     *            INSERT SQL 語句。
     * @param parameters
     *            參數集。
     * @return 資料新增筆數。
     */
    public int insert(CharSequence sql, Map<String, ?> parameters) {
        return insert(new Query(sql, parameters));
    }

    /**
     * 執行 INSERT Query。
     * 
     * @param query
     *            包含 SQL 語句，也可能有參數的 Query 物件。
     * @return 資料新增筆數。
     */
    public int insert(Query query) {
        return execute(query);
    }

    private boolean isNotNullAndNotEmpty(CharSequence sql) {
        return sql != null && !Strings.isNullOrEmpty(sql.toString());
    }

    private void logSqlExecute(CharSequence sql, Map<String, ?> parameters) {
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL:{}", sql);
            if (parameters == null || parameters.isEmpty()) {
                log.debug("With NO parameters!");
            } else {
                for (Entry<String, ?> entry : parameters.entrySet()) {
                    log.debug("Param:{}, Value:{}", entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void logExecuteInBatch(CharSequence sql, List<Map<String, ?>> parametersList) {
        if (log.isDebugEnabled()) {
            log.debug("Executing SQL:{}", sql);
            for (int i = 0; i < parametersList.size(); i++) {
                Map<String, ?> parameters = parametersList.get(0);
                log.debug("List[{}]", i);
                if (parameters == null || parameters.isEmpty()) {
                    log.debug("With NO parameters!");
                } else {
                    for (Entry<String, ?> entry : parameters.entrySet()) {
                        log.debug("Param:{}, Value:{}", entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }


    /**
     * 執行 SELECT SQL 語句，取得所有符合的資料。
     * 
     * @param <T>
     *            資料型別。
     * @param sql
     *            SELECT SQL 語句。
     * @param parameters
     *            參數集。
     * @param clazz
     *            查詢資料型別。
     * @return 符合查詢條件的資料清單; 如果完全沒有符合的資料，則回傳空的清單。
     */
    public <T> List<T> queryForList(CharSequence sql, Map<String, ?> parameters, Class<T> clazz) {
        return queryForList(new Query(sql, parameters), clazz);
    }

    /**
     * 執行 SELECT SQL 語句，取得所有符合的資料。
     *
     * @param <T>
     *            資料型別。
     * @param sql
     *            SELECT SQL 語句。
     * @param clazz
     *            查詢資料型別。
     * @return 符合查詢條件的資料清單; 如果完全沒有符合的資料，則回傳空的清單。
     */
    public <T> List<T> queryForList(CharSequence sql, Class<T> clazz) {
        return queryForList(new Query(sql), clazz);
    }

    /**
     * 執行 SELECT SQL 語句，取得所有符合的資料。
     * 
     * @param <T>
     *            資料型別。
     * @param query
     *            包含 SQL 語句，也可能有參數的 Query 物件。
     * @param clazz
     *            查詢資料型別。
     * @return 符合查詢條件的資料清單; 如果完全沒有符合的資料，則回傳空的清單。
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> queryForList(Query query, Class<T> clazz) {
        String queryString = query.getString();
        Map<String, Object> parameters = query.getParameters();

        logSqlExecute(queryString, parameters);

        if (isSimpleType(clazz)) {
            return this.jdbcTemplate.queryForList(queryString, parameters, clazz);

        } else {
            RowMapper mapper;
            try {
                 mapper = this.rowMapper.getDeclaredConstructor(Class.class).newInstance(clazz);

            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                String message = String.format("Failed to initialize RowMapper of class %s.",  this.rowMapper);
                throw new RuntimeException(message, e);
            }
            return this.jdbcTemplate.query(queryString, parameters, mapper);
        }
    }

    
    private <T> boolean isSimpleType(Class<T> clazz) {
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.equals(String.class)
                || clazz.equals(BigDecimal.class) || clazz.equals(BigInteger.class);
    }


    /**
     * 執行無參數 UPDATE SQL 語句。
     * 
     * @param sql
     *            UPDATE SQL 語句。
     * @return 資料異動筆數。
     */
    public int update(CharSequence sql) {
        return update(new Query(sql));
    }

    /**
     * 整批執行 UPDATE SQL 語句。
     * 
     * @param sql
     *            UPDATE SQL 語句。
     * @param listOfParameters
     *            每句 SQL 的參數 Map 集合。
     * @return 每次執行的資料異動筆數。
     */
    public int[] update(CharSequence sql, List<Map<String, ?>> listOfParameters) {
        return executeInBatch(sql, listOfParameters);
    }

    /**
     * 執行 UPDATE SQL 語句。
     * 
     * @param sql
     *            UPDATE SQL 語句。
     * @param parameters
     *            參數集。
     * @return 資料異動筆數。
     */
    public int update(CharSequence sql, Map<String, ?> parameters) {
        return update(new Query(sql, parameters));
    }

    /**
     * 執行 UPDATE Query。
     * 
     * @param query
     *            包含 SQL 語句，也可能有參數的 Query 物件。
     * @return 資料異動筆數。
     */
    public int update(Query query) {
        return execute(query);
    }

}
