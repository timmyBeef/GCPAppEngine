package com.taipower.intranet.persistence.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 繼承 Spring 的 AbstractRoutingDataSource，重寫 determineCurrentLookupKey 方法
 * 動態db來源
 * determineCurrentLookupKey() 決定使用哪一個資料來源
 *
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * ThreadLocal 在 multi thread 下,  保證各 thread 獨立
     */
    private static final ThreadLocal<DataSourceNames> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     *
     * @param defaultTargetDataSource 預設 DataSource
     * @param targetDataSources       目標 DataSource
     */
    public DynamicDataSource(DataSource defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return getDataSource();
    }

    public static void setDataSource(DataSourceNames dataSource) {
        CONTEXT_HOLDER.set(dataSource);
    }

    public static DataSourceNames getDataSource() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSource() {
        CONTEXT_HOLDER.remove();
    }

}
