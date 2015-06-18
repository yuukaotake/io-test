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

import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import com.fujitsu.dc.client.DcContext;

/**
 * 複数のテストケース共通処理.
 */
/**
 * This is the Test case of multiple common processing. It declares and initializes various common variables which will
 * be used during test cases execution.
 */
@RunWith(DcRunner.class)
public abstract class AbstractCase {
    /** テスト用のCell名の後ろに付与する識別子. 複数人で同時テストする際、409になるのを防止するため. */
    /**
     * In order to prevent the 409 Conflict error, this randomly generated field will be appended to cell name etc when
     * tested simultaneously.
     */
    static String suf;
    /** DCコンテキスト. */
    /** DC Context reference. */
    static DcContext dc;
    // /** TOP値のデフォルト値. */
    // private static final int DEFAULT_TOP = 10;
    // /** トークンの最低サイズ. */
    // private static final int MIN_TOKEN_LENGTH = 100;
    // /** リクエスト元のURL. */
    /** The requested URL. */
    private static final String BASE_URL = "personium.io.test.baseurl";
    static String baseUrl = System.getProperty(BASE_URL, "http://localhost:8080/dc1-core/");

    /** Cell名. */
    /** Cell Name. */
    static String cellName = "daotest";
    /** Box Name. */
    static String boxName = "boxname";
    /** Box Schema. */
    static String boxSchema = "http://example.com/testSchema/";
    /** Account Name. */
    static String accountName = "user001";
    /** Account password. */
    static String accountPassword = "pass001";
    /** DC Version. */
    private static final String DC_VERSION = "personium.io.test.dcversion";
    /** Version. */
    private static String version = System.getProperty(DC_VERSION);
    /** MasterToken. */
    private static final String MASTER_TOKEN_NAME = "personium.io.test.masterToken";
    static String masterTokenName = System.getProperty(MASTER_TOKEN_NAME, "MasterTokenName");

    /**
     * テストの前に必ず呼び出されるメソッド.
     */
    /**
     * This method will be called before the test execution. It initializes random variable, DcContext and sets token
     * value and version.
     */
    @BeforeClass
    public static void beforeClass() {
        suf = Long.toString(Calendar.getInstance().getTimeInMillis());
        dc = new DcContext(baseUrl, cellName + suf, boxSchema + suf, boxName + suf);
        dc.setClientToken(masterTokenName);
        if (version != null && !(version.equals(""))) {
            dc.setDcVersion(version);
        }
    }
}
