package com.jo.application.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "zjt_table_info")
public class ZJTTableInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column
    private String table_name;

    @Column(name = "column_name")
    private String headers;

    @Column(name = "column_width")
    private String widths;

    @Column(name = "page_size")
    private Integer pageSize;

    @Column(name = "frozenCount")
    private Integer frozenCount;

    public String getTable_name() {
        return table_name;
    }

    public void setTable_name(String table_name) {
        this.table_name = table_name;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getWidths() {
        return widths;
    }

    public void setWidths(String widths) {
        this.widths = widths;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getFrozenCount() {
        return frozenCount;
    }

    public void setFrozenCount(Integer frozenCount) {
        this.frozenCount = frozenCount;
    }
}