package entity;

import lombok.Data;

/**
 * Copyright 2017 bejson.com
 */
@Data
public class TableSchedule {

    private String TABLE_CATALOG;
    private String TABLE_COMMENT;
    private String TABLE_NAME;
    private String TABLE_SCHEMA;
    private String ENGINE;
    private String TABLE_TYPE;
    private Long TABLE_ROWS;
    private Long AVG_ROW_LENGTH;
    private Long DATA_LENGTH;
    private Long DATA_FREE;
    private Long INDEX_LENGTH;
    private String ROW_FORMAT;
    private Long VERSION;
    private String CREATE_OPTIONS;
    private Long CREATE_TIME;
    private Long MAX_DATA_LENGTH;
    private String TABLE_COLLATION;

}