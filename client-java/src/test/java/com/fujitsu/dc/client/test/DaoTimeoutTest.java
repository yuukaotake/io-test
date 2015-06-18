/**
 * personium.io
 * Copyright 2014 FUJITSU LIMITED
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fujitsu.dc.client.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.dc.client.Accessor;
import com.fujitsu.dc.client.DcContext;

/**
 * DAO関連テスト.
 */
/**
 * This class contains test cases for DAO-related operations of connection time out.
 */
public class DaoTimeoutTest extends AbstractCase {
     /** ログオブジェクト. */
    /** Log object. */
    private static Logger log = LoggerFactory.getLogger(DaoTimeoutTest.class);
     /** テストアクセス用Accessor. */
    /** Reference to Accessor for test. */
    private static Accessor testAs;
    /** Field Name. */
    private static final String FIELD_NAME = "Name";
    /** The requested URL. */
    static String baseUrl = "http://localhost:8080/dc1-core/";
     /** Cell名. */
    /** Cell Name. */
    static String cellName = "daotest";
    /** Box Name. */
    static String boxName = "boxname";
    /** Box Schema. */
    static String boxSchema = "http://example.com/testSchema/";

     /**
     * タイムアウト値指定なしの場合にデフォルト値が設定されていること.
     */
    /**
     * This test case specifies when the default value should be set if no timeout value is specified.
     */
    @Test
    public void タイムアウト値指定なしの場合にデフォルト値が設定されていること() {
        DcContext context = new DcContext(baseUrl, cellName, boxName, boxSchema);
        assertEquals(0, context.getDaoConfig().getConnectionTimeout());
    }

     /**
     * 任意のタイムアウト値が指定できること.
     */
    /**
     * This test case specifies the success scenario when any time-out value can be specified.
     */
    @Test
    public void 任意のタイムアウト値が指定できること() {
        DcContext context = new DcContext(baseUrl, cellName, boxName, boxSchema);
        context.setConnectionTimeout(30 * 1000);
        assertEquals(30 * 1000, context.getDaoConfig().getConnectionTimeout());
    }

     /**
     * タイムアウト値に0が指定できること.
     */
    /**
     * This test case specifies the scenario when 0 value can be specified.
     */
    @Test
    public void タイムアウト値に0が指定できること() {
        DcContext context = new DcContext(baseUrl, cellName, boxName, boxSchema);
        context.setConnectionTimeout(0);
        assertEquals(0, context.getDaoConfig().getConnectionTimeout());
    }

     /**
     * タイムアウト値に負数を指定した場合にエラーとなること.
     */
    /**
     * This test case specifies the error scenario when any negative value is specified.
     */
    @Test(expected = IllegalArgumentException.class)
    public void タイムアウト値に負数を指定した場合にエラーとなること() {
        DcContext context = new DcContext(baseUrl, cellName, boxName, boxSchema);
        context.setConnectionTimeout(-1);
    }

}
