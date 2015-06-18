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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.HashMap;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.dc.client.Accessor;
import com.fujitsu.dc.client.Cell;
import com.fujitsu.dc.client.DaoException;
import com.fujitsu.dc.client.DcContext;
import com.fujitsu.dc.client.utils.Log;

/**
 * 認証Proxyのテスト.
 */
/**
 * This is the test class which contains various unit and integration test cases for all the functionalities. It is the
 * DAO related test class.
 */
public class AuthProxyTest extends AbstractCase {
    /** ログオブジェクト. */
    /** Log Object. */
    private static Logger log = LoggerFactory.getLogger(AuthProxyTest.class);
    /** Utility Log. */
    private Log utilLog = new Log(AuthProxyTest.class);
    /** テスト用cell. */
    /** Test Cell Name. */
    private static Cell testCell;

    /** テストアクセス用Accessor. */
    /** Reference to accessor for testing. */
    private static Accessor testAs;
    /** Field Name. */
    private static final String FIELD_NAME = "Name";

    /** baseUrl. */
    // please specify your server name.
    static String baseUrl = "https://testserver/";

    /** proxy. */
    // please specify your proxy host name.
    static String proxyHostName = "proxy.host.name";

    /**
     * 認証Proxyを通してセル作成ができることを確認.
     */
    @Test
    @Ignore
    public void 認証Proxy設定でDaoが正常に動作すること() {
        suf = Long.toString(Calendar.getInstance().getTimeInMillis());
        dc = new DcContext(baseUrl, cellName + suf, boxSchema + suf, boxName + suf);
        dc.setClientToken(AbstractCase.masterTokenName);
        try {
            // TODO 右記対応時にURL指定を削除する。 バグ #22833 X-Dc-Unit-Userヘッダーに不正な値が設定できる
            // dc.setDefaultHeader("X-Dc-Unit-User", "dao");
            dc.setDefaultHeader("X-Dc-Unit-User", baseUrl + "#dao");

            /** Accessorセット. */
            /** set token in accessor. */
            testAs = dc.withToken(AbstractCase.masterTokenName);

            // Proxy経由でアクセスする場合は以下のようにしてProxyのホスト名、ポートを設定する
            testAs.getDaoConfig().setProxyHostname(proxyHostName);
            testAs.getDaoConfig().setProxyPort(8080);
            testAs.getDaoConfig().setProxyUsername("XXXXXXXXXXXXXXXXXXXXX");
            testAs.getDaoConfig().setProxyPassword("XXXXXXXXXXXXXXXXXXXXX");

            /** Cellの作成. */
            /** Creating a Cell. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, cellName + suf);
            try {
                testCell = testAs.asCellOwner().unit.cell.create(cellMap);
                assertNotNull(testCell);
            } finally {
                testAs.asCellOwner().unit.cell.recursiveDelete(cellName + suf);
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }
}
