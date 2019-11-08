package com.taipower.intranet.service;


import com.taipower.intranet.dto.APMCUST;
import com.taipower.intranet.dto.News;
import com.taipower.intranet.dto.SysUser;
import com.taipower.intranet.persistence.query.Query;
import com.taipower.intranet.persistence.query.SqlExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CPSService {

    @Autowired
    SqlExecutor sqlExecutor;


    public List<News> findDistinctData() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT DISTINCT id, type, source_Type sourceType, title, new_Date newDate, ")
                .append("photo, content FROM News order by new_Date desc");

        List<News> list = sqlExecutor.queryForList(builder.toString(), News.class);
        return list;
    }

    public List<News> findDistinctData2() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT DISTINCT id, type, source_Type sourceType, title, new_Date newDate, ")
                .append("photo, content FROM News order by new_Date desc");

        List<News> list = sqlExecutor.queryForList(builder.toString(), News.class);
        return list;
    }

    public SysUser findUser(long id) {

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT user_id userId, username, password, salt, email, mobile, status, create_user_id createUserId ")
                .append(", create_time createTime FROM sys_user");

        List<SysUser> list = sqlExecutor.queryForList(builder.toString(), SysUser.class);
        return list.get(0);
    }

    public APMCUST getAPMCUSTList(String cusNo) {


        Query.Builder builder = Query.builder();

        builder.append(" select BUSS_ID bussId, MAIL_CITY mailCity,MAIL_TOWN mailTown, ");
        builder.append(" MAIL_ROAD mailRoad, MAIL_LINE mailLine, CUST_NO custNo, CUST_NAME custName, CUST_CITY custCity, CUST_TOWN custTown,");
        builder.append(" CUST_ROAD custRoad, CUST_LINE custLine, CUST_PTCOD custPtcod, USE_TP useTp,CONT_TP contTp, DISC_TP discTp, SPE_CUST_TP speCustTp, CYCLE_DAY cycleDay ");
        builder.append(" from APMCUST where CUST_NO = :cusNo ", cusNo);

        Query query = builder.build();

        List<APMCUST> list = sqlExecutor.queryForList(query, APMCUST.class);

        return list.get(0);
    }

}
