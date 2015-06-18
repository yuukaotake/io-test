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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fujitsu.dc.client.Accessor;
import com.fujitsu.dc.client.Account;
import com.fujitsu.dc.client.Ace;
import com.fujitsu.dc.client.Acl;
import com.fujitsu.dc.client.AssociationEnd;
import com.fujitsu.dc.client.Box;
import com.fujitsu.dc.client.Cell;
import com.fujitsu.dc.client.ComplexType;
import com.fujitsu.dc.client.ComplexTypeProperty;
import com.fujitsu.dc.client.DaoException;
import com.fujitsu.dc.client.Entity;
import com.fujitsu.dc.client.EntityType;
import com.fujitsu.dc.client.Event;
import com.fujitsu.dc.client.ExtCell;
import com.fujitsu.dc.client.ExtRole;
import com.fujitsu.dc.client.ODataBatch;
import com.fujitsu.dc.client.ODataResponse;
import com.fujitsu.dc.client.OwnerAccessor;
import com.fujitsu.dc.client.Principal;
import com.fujitsu.dc.client.Property;
import com.fujitsu.dc.client.Relation;
import com.fujitsu.dc.client.Role;
import com.fujitsu.dc.client.ServiceCollection;
import com.fujitsu.dc.client.WebDAV;
import com.fujitsu.dc.client.http.DcResponse;
import com.fujitsu.dc.client.utils.JsonUtils;
import com.fujitsu.dc.client.utils.Log;

/**
 * DAO関連テスト.
 */
/**
 * This is the test class which contains various unit and integration test cases for all the functionalities. It is the
 * DAO related test class.
 */
public class DaoTest extends AbstractCase {
    /** ログオブジェクト. */
    /** Log Object. */
    private static Logger log = LoggerFactory.getLogger(DaoTest.class);
    /** Utility Log. */
    private Log utilLog = new Log(DaoTest.class);
    /** テスト用cell. */
    /** Test Cell Name. */
    private static Cell testCell;
    /** テスト用box. */
    /** Test Box Name. */
    private static Box testBox;
    /** テスト用box(スキーマ無し). */
    /** Test box Name(no schema). */
    private static Box testBox2;
    /** テスト用Account. */
    /** Test Account Name. */
    private static Account testAccount;

    /** テストアクセス用Accessor. */
    /** Reference to accessor for testing. */
    private static Accessor testAs;
    /** Field Name. */
    private static final String FIELD_NAME = "Name";
    /** Field Schema. */
    private static final String FIELD_SCHEMA = "Schema";
    /** Field Box Name. */
    private static final String FIELD_BOXNAME = "_Box.Name";
    /** Field Multiplicity. */
    private static final String FIELD_MULTIPLICITY = "Multiplicity";
    /** Field EntityType Name. */
    private static final String FIELD_ENTITYTYPE_NAME = "_EntityType.Name";
    /** Field ComplexType Name. */
    private static final String FIELD_COMPLEXTYPE_NAME = "_ComplexType.Name";
    /** Field Type Name. */
    private static final String FIELD_TYPE_NAME = "Type";
    /** Etag. */
    private static final String HEADER_KEY_ETAG = "ETag";

    /**
     * テスト前準備.
     */
    /**
     * This method contains the test preparation code and gets executed before the execution of every test case. It
     * performs following operations. Creates a Cell, Creates two Boxes, Creates an Account
     */
    @BeforeClass
    public static void beforeClass() {
        log.debug("\n★☆★☆★☆★☆ beforeClass START ");
        AbstractCase.beforeClass();
        try {
            // TODO 右記対応時にURL指定を削除する。 バグ #22833 X-Dc-Unit-Userヘッダーに不正な値が設定できる
            // dc.setDefaultHeader("X-Dc-Unit-User", "dao");
            dc.setDefaultHeader("X-Dc-Unit-User", baseUrl + "#dao");

            /** Accessorセット. */
            /** set token in accessor. */
            testAs = dc.withToken(AbstractCase.masterTokenName);

            // Proxy経由でアクセスする場合は以下のようにしてProxyのホスト名、ポートを設定する
            // testAs.getDaoConfig().setProxyHostname("hostname");
            // testAs.getDaoConfig().setProxyPort(8080);

            /** Cellの作成. */
            /** Creating a Cell. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, cellName + suf);
            testCell = testAs.asCellOwner().unit.cell.create(cellMap);
            // dc.removeDefaultHeader("X-Dc-Unit-User");

            /** Boxの作成. */
            /** Creating a Box. */
            HashMap<String, Object> boxMap = new HashMap<String, Object>();
            boxMap.put(FIELD_NAME, boxName + suf);
            boxMap.put(FIELD_SCHEMA, boxSchema + suf);
            testBox = testAs.cell(testCell.getName()).box.create(boxMap);

            /** Boxの作成. */
            /** Creating another Box. */
            HashMap<String, Object> boxMap2 = new HashMap<String, Object>();
            boxMap2.put(FIELD_NAME, boxName + "2" + suf);
            testBox2 = testAs.cell(testCell.getName()).box.create(boxMap2);

            /** Accountの作成. */
            /** Creating an Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName + suf);
            testAccount = testAs.cell(testCell.getName()).account.create(accountMap, accountPassword);

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            log.debug("\n★☆★☆★☆★☆ beforeClass END ");
        }

    }

    /**
     * テスト後処理.
     */
    /**
     * This method contains the test post-processing code and gets executed after the execution of every test case. It
     * performs following operations. Deletes two Boxes, Deletes an Account, Deletes the Cell.
     */
    @AfterClass
    public static void afterClass() {
        log.debug("\n★☆★☆★☆★☆ afterClass START ");
        try {
            /** Boxの削除. */
            /** Deleting the Box. */
            if (testBox != null) {
                testAs.cell(testCell.getName()).box.del(testBox.getName());
                testBox = null;
            }
            /** Boxの削除. */
            /** Deleting the Box. */
            if (testBox2 != null) {
                testAs.cell(testCell.getName()).box.del(testBox2.getName());
                testBox2 = null;
            }
            /** accountの削除. */
            /** Deleting the Account. */
            if (testAccount != null) {
                testAs.cell(testCell.getName()).account.del(testAccount.getName());
                testAccount = null;
            }
            /** Cellの削除. */
            /** Deleting the Cell. */
            if (testCell != null) {
                testAs.asCellOwner().unit.cell.del(testCell.getName());
                testCell = null;
            }

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            log.debug("\n★☆★☆★☆★☆ afterClass END ");
        }
    }

    /**
     * CellのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the CRUD test of cell using JSONObject. It performs following functions. Create a Cell, Create another
     * Cell with same name to get 409 exception, Retrieve the Cell, Deletes the Cell.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void cellのCRUD_JSONObjectを使用() throws DaoException {
        Cell cell = null;
        String name = cellName + "_" + suf;
        try {
            /** データ作成. */
            /** Data Creation. */
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();
            json.put(FIELD_NAME, name);

            /** Cellの作成. */
            /** Creating a Cell. */
            log.debug("\n◆ Cellの作成");
            cell = testAs.asCellOwner().unit.cell.create(json);
            log.debug("\n Cell:[" + cell.toJSONString() + "]");
            assertEquals(name, cell.getName());

            /** 同じ名前のCellを登録し、409になる事を確認. */
            /** Register the Cell of the same name, you should get 409 status code. */
            log.debug("\n◆ Cellの作成 409");
            try {
                testAs.asCellOwner().unit.cell.create(json);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            /** Cellの取得. */
            /** Get the Cell. */
            log.debug("\n◆ Cellの取得");
            cell = testAs.asCellOwner().unit.cell.retrieve(cell.getName());
            assertEquals(name, cell.getName());

            log.debug("\n◆ 存在しないCellのexist");
            assertFalse(testAs.asCellOwner().unit.cell.exists(cell.getName() + "none"));

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Cellの削除. */
            /** Deleting the Cell. */
            log.debug("\n◆ Cellのexist");
            if ((cell != null) && (testAs.asCellOwner().unit.cell.exists(cell.getName()))) {
                log.debug("\n◆ Cellの削除");
                testAs.asCellOwner().unit.cell.del(cell.getName(), "*");
            }
        }
    }

    /**
     * CellのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the CRUD test of cell using HashMap. It performs following functions. Create a Cell, Create another Cell
     * with same name to get 409 exception, Retrieve the Cell, Deletes the Cell.
     * @throws DaoException Exception thrown
     */
    @Test
    public void cellのCRUD() throws DaoException {
        Cell cell = null;
        String name = cellName + "_" + suf;
        String etag = null;
        try {
            /** データ作成. */
            /** Data Creation. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, name);

            /** Cellの作成. */
            /** Creating a Cell. */
            log.debug("\n◆ Cellの作成");
            cell = testAs.asCellOwner().unit.cell.create(cellMap);
            log.debug("\n Cell:[" + cell.toJSONString() + "]");
            assertEquals(name, cell.getName());
            etag = cell.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);

            /** 同じ名前のCellを登録し、409になる事を確認. */
            /** Register the Cell of the same name, you should get 409 status code. */
            log.debug("\n◆ Cellの作成 409");
            try {
                testAs.asCellOwner().unit.cell.create(cellMap);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            /** Cellの取得. */
            /** Get the Cell. */
            log.debug("\n◆ Cellの取得");
            cell = testAs.asCellOwner().unit.cell.retrieve(cell.getName());
            assertEquals(name, cell.getName());

            log.debug("\n◆ 存在しないCellのexist");
            assertFalse(testAs.asCellOwner().unit.cell.exists(cell.getName() + "none"));

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Cellの削除. */
            /** Deleting the Cell. */
            log.debug("\n◆ Cellのexist");
            if ((cell != null) && (testAs.asCellOwner().unit.cell.exists(cell.getName()))) {
                log.debug("\n◆ Cellの削除");
                testAs.asCellOwner().unit.cell.del(cell.getName(), etag);
            }
        }
    }

    /**
     * CellのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This test case validates the cell creation process using Cell instance.
     * @throws DaoException Exception thrown
     */
    @Test
    public void cellのCRUD_オブジェクト渡し() throws DaoException {
        Cell cell = null;
        String name = cellName + "_" + suf;
        try {
            /** Cellの作成. */
            /** Creating a Cell. */
            log.debug("\n◆ Cellの作成");
            cell = new Cell();
            cell.setName(name);
            cell = testAs.asCellOwner().unit.cell.create(cell);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Cellの削除. */
            /** Deleting the Cell. */
            log.debug("\n◆ Cellのexist");
            if ((cell != null) && (testAs.asCellOwner().unit.cell.exists(cell.getName()))) {
                log.debug("\n◆ Cellの削除");
                testAs.asCellOwner().unit.cell.del(cell.getName(), "*");
            }
        }
    }

    /**
     * Cellの検索テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This test case validates the cell retrieval process for 10 records using $top command.
     * @throws DaoException Exception thrown
     */
    @Ignore
    @Test
    public void cellの検索() throws DaoException {
        Cell cell = null;
        try {
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, cellName);

            /** Cellの作成. */
            /** Creating a Cell. */
            cell = testAs.asCellOwner().unit.cell.create(cellMap);
            assertEquals(cellName, cell.getName());

            /** Fetch first 10 cells. */
            HashMap<String, Object> res = testAs.asCellOwner().unit.cell.query().top(10).skip(0).run();
            // JSONObject ar = (JSONObject)((JSONObject) res.get("d")).get("results");
            log.debug(JsonUtils.toJsonString(res));

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Deleting the Cell. */
            if (cell != null) {
                testAs.asCellOwner().unit.cell.del(cell.getName());
                cell = null;
            }
        }
    }

    /**
     * BoxのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the CRUD test of Box using HashMap. It performs following functions. Create a Box, Create another Box
     * with same name to get 409 exception, Retrieve the Box, Deletes the Box.
     * @throws DaoException Exception thrown
     */
    @Test
    public void boxのCRUD() throws DaoException {
        Box box = null;
        Cell cell = null;
        String etag = null;
        try {
            /** データ作成. */
            /** Data creation. */
            HashMap<String, Object> boxMap = new HashMap<String, Object>();
            boxMap.put(FIELD_NAME, boxName);
            boxMap.put(FIELD_SCHEMA, boxSchema);

            log.debug("\n◆ Box Creaate");
            cell = testAs.cell(testCell.getName());
            box = cell.box.create(boxMap);
            log.debug("\n Box:[" + box.toJSON().toJSONString() + "]");
            assertEquals(boxName, box.getName());
            assertEquals(boxSchema, box.getSchema());
            etag = box.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);

            /** 同じ名前のBoxを登録して409になることを確認. */
            /** Register the Box of the same name, you should get 409 status code. */
            log.debug("\n◆ Box Creaate 409");
            try {
                cell.box.create(boxMap);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            log.debug("\n◆ Box retrieve");
            /** Retrieve the Box. */
            cell.box.retrieve(boxName);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete the Box. */
            log.debug("\n◆ Boxの削除");
            if (box != null) {
                cell.box.del(box.getName(), etag);
            }
        }
    }

    /**
     * schemaからBox名取得テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method tests the Box name acquisition from test schema.
     * @throws DaoException Exception thrown
     */
    @Test
    public void schemaからBox名取得() throws DaoException {
        Box box = null;
        Cell cell = null;
        String etag = null;
        try {
            /** データ作成. */
            /** Data Creation. */
            HashMap<String, Object> boxMap = new HashMap<String, Object>();
            boxMap.put(FIELD_NAME, boxName);
            boxMap.put(FIELD_SCHEMA, boxSchema);

            /** Creating a Box. */
            log.debug("\n◆ Box Creaate");
            cell = testAs.cell(testCell.getName());
            box = cell.box.create(boxMap);

            log.debug("\n◆ schemaからboxを引く");
            Box b = cell.box(boxSchema);
            assertEquals(b.getName(), boxName);

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Deleting the Box. */
            log.debug("\n◆ Boxの削除");
            if (box != null) {
                cell.box.del(box.getName(), etag);
            }
        }
    }

    /**
     * アクセストークンからBox名取得テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the Box name acquisition test from the access token. It performs following operations. Creates a
     * cell, Creates an Account, Creates a Role, Create Account Role Link, Creates a Box, Perform Authentication, Delete
     * Account Role Link, Delete Role, Delete Box, Delete Account, Delete Cell.
     * @throws DaoException Exception thrown
     */
    @Ignore
    @Test
    public void アクセストークンからBox名取得() throws DaoException {

        Box box = null;
        Cell cellOther = null;
        Cell cell = null;
        Account account = null;
        Role role1 = null;
        try {
            /** Cellを作る. */
            /** Creating a Cell. */
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "other_cell_name" + suf);
            cellOther = testAs.asCellOwner().unit.cell.create(map);
            /** アカウントを作る. */
            /** Creating an Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell(cellOther.getName()).account.create(accountMap, accountPassword);

            /** Creating a Role. */
            log.debug("\n◆ Role Creaate");
            map.put(FIELD_NAME, "role1");
            role1 = testAs.cell(cellOther.getName()).role.create(map);
            /** Account Role Link. */
            account.role.link(role1);

            /** データ作成. */
            /** Data Creation. */
            HashMap<String, Object> boxMap = new HashMap<String, Object>();
            boxMap.put(FIELD_NAME, boxName);
            boxMap.put(FIELD_SCHEMA, testCell.getUrl());

            /** Creating a Box. */
            log.debug("\n◆ Box Creaate");
            box = testAs.cell(cellOther.getName()).box.create(boxMap);

            Acl acl = new Acl();
            acl.setRequireSchemaAuthz("public");
            Ace ace;

            ace = new Ace();
            ace.setRole(role1);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            box.acl.set(acl);

            /** スキーマ認証. */
            /** Authentication schema. */
            cell = dc.asAccountWithSchemaAuthn(cellOther.getUrl(), account.getName(), accountPassword,
                    testCell.getUrl(), testAccount.getName(), accountPassword).cell();

            log.debug("\n◆ schemaからboxを引く");
            Box b = cell.box();
            assertEquals(b.getName(), boxName);

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            if (role1 != null) {
                /** Delete Account Role Link. */
                log.debug("\n◆ Account - RoleのLink削除");
                account.role.unLink(role1);
                log.debug("\n◆ Roleの削除");
                /** Delete Role. */
                cellOther.role.del("role1");
            }
            /** Delete Box. */
            log.debug("\n◆ Boxの削除");
            if (box != null) {
                testAs.cell(cellOther.getName()).box.del(box.getName());
            }
            /** Delete Account. */
            if (account != null) {
                testAs.cell(cellOther.getName()).account.del(accountName);
                account = null;
            }
            /** Delete cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
        }
    }

    /**
     * BoxのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method is the CRUD test of Box by passing Box object.
     * @throws DaoException Exception thrown
     */
    @Test
    public void boxのCRUD_オブジェクト渡し() throws DaoException {
        Box box = null;
        Cell cell = null;
        try {
            /** Creating a Box. */
            log.debug("\n◆ Box Creaate");
            cell = testAs.cell(testCell.getName());
            box = new Box();
            box.setName(boxName);
            box.setSchema(boxSchema);
            box = cell.box.create(box);

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Deleting the Box. */
            log.debug("\n◆ Boxの削除");
            if (box != null) {
                cell.box.del(box.getName());
            }
        }
    }

    /**
     * BoxのSchema指定のテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test of the Schema specification of the Box. It tests the creation of a collection based on
     * Box Schema. It performs the following operations. Creates two Boxes, Create tow collections, Delete two
     * collections, Delete two boxes.
     * @throws DaoException Exception thrown
     */
    @Test
    public void BoxのSchema指定() throws DaoException {
        Box boxHttp = null;
        Box boxHttps = null;

        try {
            log.debug("\n◆ Box Create Schemaにhttp://・・・");
            /** Creating a Box. */
            boxHttp = new Box();
            boxHttp.setName(boxName + "http");
            boxHttp.setSchema("http://example.com");
            testCell.box.create(boxHttp);
            log.debug("\n◆ Box Create Schemaにhttps://・・・");
            /** Creating another Box. */
            boxHttps = new Box();
            boxHttps.setName(boxName + "https");
            boxHttps.setSchema("https://example.com");
            testCell.box.create(boxHttps);
            log.debug("\n◆ https// にコレクションの作成");
            /** Creating a Collection. */
            testCell.box(boxHttps.getSchema()).mkCol("colHttps");
            log.debug("\n◆ http// にコレクションの作成");
            /** Creating another Collection. */
            testCell.box(boxHttp.getSchema()).mkCol("colHttp");
        } finally {
            log.debug("\n◆ https// に作ったコレクションの削除");
            /** Deleting the collection. */
            boxHttps.del("colHttps");
            log.debug("\n◆ http// に作ったコレクションの削除");
            /** Deleting the collection. */
            boxHttp.del("colHttp");
            log.debug("\n◆ Box Creaate Schemaにhttp://のBox削除");
            /** Deleting the Box. */
            testCell.box.del(boxHttp.getName());
            log.debug("\n◆ Box Creaate Schemaにhttps:のBox削除");
            /** Deleting the Box. */
            testCell.box.del(boxHttps.getName());
        }
    }

    /**
     * AccountのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the CRUD test of Account using HashMap. It creates an account, then creates a duplicate account
     * with same name to get Status Code of 409 as conflict, Retrieves the Account, Deletes the Account.
     * @throws DaoException Exception thrown
     */
    @Test
    public void AccountのCRUD() throws DaoException {
        Account account = null;
        Cell cell = null;
        String etag = null;
        try {
            /** データ作成. */
            /** Data creation. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);

            /** Accountの作成. */
            /** Creating an Account. */
            cell = testAs.cell(testCell.getName());
            account = cell.account.create(accountMap, accountPassword);
            log.debug("\n Account:[" + account.toJSONString() + "]");
            assertEquals(accountName, account.getName());
            etag = account.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);

            /** 同じ名前のAccountを登録して409になることを確認. */
            /** Register the Account of the same name, you should get 409 status code. */
            try {
                cell.account.create(accountMap, accountPassword);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            /** Accountの取得. */
            /** Retrieve Account. */
            account = cell.account.retrieve(accountName);
            assertEquals(accountName, account.getName());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Accountの削除. */
            /** Delete the Account. */
            if (account != null) {
                cell.account.del(accountName, etag);
            }
        }
    }

    /**
     * 記号を含むAccountのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the CRUD test of Account using HashMap. It creates an account, then creates a duplicate account
     * with same name to get Status Code of 409 as conflict, Updates the Account, Retrieves the Account, Deletes the
     * Account.
     * @throws DaoException Exception thrown
     */
    @Test
    public void 記号を含むAccountのCRUD() throws DaoException {
        Account account = null;
        Cell cell = null;
        String etag = null;

        String testAccountName = "abcde12345-_!$*=^`{|}~.@";
        try {
            /** データ作成. */
            /** Data creation. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, testAccountName);

            /** Accountの作成. */
            /** Creating an Account. */
            cell = testAs.cell(testCell.getName());
            account = cell.account.create(accountMap, accountPassword);
            log.debug("\n Account:[" + account.toJSONString() + "]");
            assertEquals(testAccountName, account.getName());
            etag = account.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);

            /** 同じ名前のAccountを登録して409になることを確認. */
            /** Register the Account of the same name, you should get 409 status code. */
            try {
                cell.account.create(accountMap, accountPassword);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }
            /** AccountのUpdate. */
            /** Update Account. */
            cell.account.update(testAccountName, accountMap, "*");

            /** Accountの取得. */
            /** Retrieve Account. */
            account = cell.account.retrieve(testAccountName);
            assertEquals(testAccountName, account.getName());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Accountの削除. */
            /** Delete Account. */
            if (account != null) {
                cell.account.del(testAccountName, "*");
            }
        }
    }

    /**
     * AccountのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method is the CRUD test of Account using Account object. It creates an account, then deletes the Account.
     * @throws DaoException Exception thrown
     */
    @Test
    public void AccountのCRUD_オブジェクト渡し() throws DaoException {
        Account account = null;
        Cell cell = null;
        try {
            /** Accountの作成. */
            /** Creating an Account. */
            cell = testAs.cell(testCell.getName());
            account = new Account();
            account.setName(accountName);
            account.setPassword(accountPassword);
            account = cell.account.create(account);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Accountの削除. */
            /** Deleting the Account. */
            try {
                if (cell != null) {
                    cell.account.del(accountName);
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * AccountのPUTで行うpassword変更テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the Password change test carried out in a PUT of the Account. It performs following operations. Creates
     * an Account, Authenticates asExtCell, Change password, New password authentication AsExtCell, Authentication
     * asExtCell 401 check, Deletes the Account.
     * @throws DaoException Exception thrown
     */
    @Test
    public void AccountのPUTで行うpassword変更テスト() throws DaoException {
        Account account = null;
        final String newPassword = "newpassword";
        try {
            /** Accountの登録. */
            /** Registration of Account. */
            /** データ作成. */
            /** Creation of data. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);

            /** Accountの作成. */
            /** Creating an Account. */
            account = testCell.account.create(accountMap, accountPassword);
            /** 認証 asExtCell. */
            /** Authentication asExtCell. */
            dc.asAccount(testCell.getName(), account.getName(), accountPassword).cell();
            /** Password変更. */
            /** Password Change. */
            testCell.account.changePassword(account.getName(), newPassword);
            /** 認証 新しいパスワードでasExtCell. */
            /** New password authentication AsExtCell. */
            dc.asAccount(testCell.getName(), account.getName(), newPassword).cell();
            /** 認証 asExtCell 401チェック. */
            /** Authentication asExtCell 401 check. */
            try {
                dc.asAccount(testCell.getName(), account.getName(), accountPassword).cell();
                fail();
            } catch (DaoException ex) {
                assertEquals(HttpStatus.SC_BAD_REQUEST, Integer.parseInt(ex.getCode()));
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Accountの削除. */
            /** Deleting the Account. */
            if (account != null) {
                testCell.account.del(accountName);
            }
        }
    }

    /**
     * password変更テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the Password Change test for Account. It performs the following operations. Creates an Account, Password
     * Change, Validation of new password, Delete the Account.
     * @throws DaoException Exception thrown
     */
    @Test
    public void password変更テスト() throws DaoException {
        Account account = null;
        final String newPassword = "newpassword";
        try {
            /** データ作成. */
            /** Creation of data. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);

            /** Create Account. */
            log.debug("\n◆ Accountの作成");
            account = testCell.account.create(accountMap, accountPassword);
            log.debug("\n◆  パスワード認証");
            Accessor as = dc.asAccount(testCell.getName(), account.getName(), accountPassword);
            as.cell();
            log.debug("\n◆  Password変更");
            /** Password Change. */
            as.changePassword(newPassword);
            log.debug("\n◆  新しいパスワードでPassword認証");
            dc.asAccount(testCell.getName(), account.getName(), newPassword).cell();
            log.debug("\n◆  古いパスワードでPassword認証 401チェック");
            try {
                /** Validates the new password, old password gives exception. */
                dc.asAccount(testCell.getName(), account.getName(), accountPassword).cell();
                fail();
            } catch (DaoException ex) {
                assertEquals(HttpStatus.SC_BAD_REQUEST, Integer.parseInt(ex.getCode()));
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Deleting the Account. */
            log.debug("\n◆  Accountの削除");
            if (account != null) {
                testCell.account.del(accountName);
            }
        }
    }

    /**
     * 自分で認証してpassword変更テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the Password Change test for Account. It performs the following operations. Creates an Account, Password
     * Change, Validation of old and new password, Delete the Account.
     * @throws DaoException Exception thrown
     */
    @Test
    public void 自分で認証してpassword変更テスト() throws DaoException {
        Account account = null;
        final String newPassword = "newpassword";
        try {
            /** データ作成. */
            /** Data Creation. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);

            /** Creating an Account. */
            log.debug("\n◆ Accountの作成");
            account = testCell.account.create(accountMap, accountPassword);

            log.debug("\n◆  Password変更");
            Accessor as = dc.asAccount(testCell.getName(), account.getName(), accountPassword);
            /** Password Change. */
            as.changePassword(newPassword);
            log.debug("\n◆  新しいパスワードでPassword認証");
            /** Authenticate with new password. */
            dc.asAccount(testCell.getName(), account.getName(), newPassword).cell();
            log.debug("\n◆  古いパスワードでPassword認証 401チェック");
            try {
                /** Validates the new password, old password gives exception. */
                dc.asAccount(testCell.getName(), account.getName(), accountPassword).cell();
                fail();
            } catch (DaoException ex) {
                assertEquals(HttpStatus.SC_BAD_REQUEST, Integer.parseInt(ex.getCode()));
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Deleting the Account. */
            log.debug("\n◆  Accountの削除");
            if (account != null) {
                testCell.account.del(accountName);
            }
        }
    }

    /**
     * 記号を含むアカウントを自分で認証してpassword変更テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the Password change test to authenticate yourself an account that contains symbols. It performs the
     * following operations. Creates an Account, Password Change, Validation of old and new password, Delete the
     * Account.
     * @throws DaoException Exception thrown
     */
    @Test
    public void 記号を含むアカウントを自分で認証してpassword変更テスト() throws DaoException {
        Account account = null;
        final String newPassword = "newpassword";
        String testAccountName = "abcde12345-_!$*=^`{|}~.@";

        try {
            /** データ作成. */
            /** Data Creation. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, testAccountName);

            /** Create an Account. */
            log.debug("\n◆ Accountの作成");
            account = testCell.account.create(accountMap, accountPassword);

            log.debug("\n◆  Password変更");
            Accessor as = dc.asAccount(testCell.getName(), account.getName(), accountPassword);
            /** Password Change. */
            as.changePassword(newPassword);
            log.debug("\n◆  新しいパスワードでPassword認証");
            /** Authenticate with new password. */
            dc.asAccount(testCell.getName(), account.getName(), newPassword).cell();
            log.debug("\n◆  古いパスワードでPassword認証 401チェック");
            try {
                /** Authenticate with old password, should give exception. */
                dc.asAccount(testCell.getName(), account.getName(), accountPassword).cell();
                fail();
            } catch (DaoException ex) {
                assertEquals(HttpStatus.SC_BAD_REQUEST, Integer.parseInt(ex.getCode()));
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete the Account. */
            log.debug("\n◆  Accountの削除");
            if (account != null) {
                testCell.account.del(testAccountName);
            }
        }
    }

    /**
     * eventのpostテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the POST test of the Event through JSONObject.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void eventのpostテスト() throws DaoException {
        try {
            /** データ作成. */
            /** Data Creation. */
            JSONObject body = new JSONObject();
            body.put("level", "ERROR");
            body.put("action", "actionData");
            body.put("object", "objectData");
            body.put("result", "resultData");

            /** イベント登録. */
            /** Event Registration. */
            testAs.cell(testCell.getName()).box().event.post(body);

            // 不正なデータを登録したときに400になることを確認
            // body.remove("level");
            // try {
            // testAs.cell(testCell.getName()).box().event.post(new JSONObject());
            // fail(e.getMessage());
            // } catch (DaoException ex) {
            // assertEquals(HttpStatus.SC_BAD_REQUEST, Integer.parseInt(ex.getCode()));
            // }
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * eventのpostテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method is the POST test of the Event(passing Event object).
     * @throws DaoException Exception thrown
     */
    @Test
    public void eventのpostテスト_オブジェクト渡し() throws DaoException {
        try {
            Event event = new Event();
            event.setAction("action");
            event.setLevel("ERROR");
            event.setObject("object");
            event.setResult("result");
            /** イベント登録. */
            /** Event Registration. */
            testAs.cell(testCell.getName()).box().event.post(event);
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * 外部イベント受付テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the External events acceptance test.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void 外部イベント受付テスト() throws DaoException {
        try {
            /** データ作成. */
            /** Data Creation. */
            String level = "ERROR";
            String action = "actionData";
            String object = "objectData";
            String result = "resultData";
            JSONObject body = new JSONObject();
            body.put("level", level);
            body.put("action", action);
            body.put("object", object);
            body.put("result", result);
            String dcRequestKey = "DaoTest";

            /** イベント登録(body). */
            /** Event registration (body). */
            testAs.cell(testCell.getName()).event.post(body);

            /** イベント登録(body+dcRequestKey). */
            /** Event registration (body + dcRequestKey). */
            testAs.cell(testCell.getName()).event.post(body, dcRequestKey);

            /** イベント登録(パラメタ). */
            /** Event registration (parameters). */
            testAs.cell(testCell.getName()).event.post(level, action, object, result);

            /** イベント登録(パラメタ+dcRequestKey). */
            /** Event registration (parameters + dcRequestKey). */
            testAs.cell(testCell.getName()).event.post(level, action, object, result, dcRequestKey);

        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * ログファイルをString形式で取得するテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is a test to get a String representation of the log file.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void ログファイルをString形式で取得するテスト() throws DaoException {
        try {
            /** データ作成. */
            /** Data Creation. */
            String level = "ERROR";
            String action = "actionData";
            String object = "objectData";
            String result = "resultData";
            JSONObject body = new JSONObject();
            body.put("level", level);
            body.put("action", action);
            body.put("object", object);
            body.put("result", result);
            String dcRequestKey = "DaoTest";
            int lines = 0;

            /** イベント取得_String(ファイル名). */
            /** Event acquisition _String (file name). */
            WebDAV currentLog = testAs.cell(testCell.getName()).currentLog.getString("default.log");
            String logContents = currentLog.getStringBody();
            int startLineCount = 0;
            if (!logContents.equals("")) {
                startLineCount = logContents.split("\n").length;
            }

            /** イベント登録(body). */
            /** Event registration (body). */
            testAs.cell(testCell.getName()).event.post(body);
            lines++;

            /** イベント取得_String(ファイル名). */
            /** Event acquisition _String (file name). */
            currentLog = testAs.cell(testCell.getName()).currentLog.getString("default.log");
            logContents = currentLog.getStringBody();
            assertNotNull(logContents);
            assertEquals(lines, logContents.split("\n").length - startLineCount);

            /** イベント取得_String(ファイル名+dcRequestKey). */
            /** Event acquisition _String (file name + dcRequestKey). */
            currentLog = testAs.cell(testCell.getName()).currentLog.getString("default.log", dcRequestKey);
            logContents = currentLog.getStringBody();
            assertNotNull(logContents);
            assertEquals(lines, logContents.split("\n").length - startLineCount);

            /** ローテート済イベント取得_String(ファイル名). */
            /** Rotate already event acquisition _String (file name). */
            /** 現在core未対応のため404が返却される. */
            /** 404 is returned for the core not supported currently. */
            try {
                testAs.cell(testCell.getName()).archiveLog.getString("default.log");
            } catch (DaoException e) {
                String code = e.getCode();
                assertEquals("404", code);
            }

            /** ローテート済イベント取得_String(ファイル名+dcRequestKey). */
            /** Rotate already event acquisition _String (file name + dcRequestKey). */
            /** 現在core未対応のため404が返却される. */
            /** 404 is returned for the core not supported currently. */
            try {
                testAs.cell(testCell.getName()).archiveLog.getString("default.log", dcRequestKey);
            } catch (DaoException e) {
                String code = e.getCode();
                assertEquals("404", code);
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * ログファイルをStream形式で取得するテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is a test to get the log file in Stream format.
     * @throws DaoException .
     */
    @SuppressWarnings("unchecked")
    @Test
    public void ログファイルをStream形式で取得するテスト() throws DaoException {
        InputStream logContents = null;
        InputStream logContents2 = null;
        try {
            /** データ作成. */
            /** Data Creation. */
            String level = "ERROR";
            String action = "actionData";
            String object = "objectData";
            String result = "resultData";
            JSONObject body = new JSONObject();
            body.put("level", level);
            body.put("action", action);
            body.put("object", object);
            body.put("result", result);
            String dcRequestKey = "DaoTest";

            /** イベント登録(body). */
            /** Event registration (body). */
            testAs.cell(testCell.getName()).event.post(body);

            /** イベント取得_Stream(ファイル名). */
            /** Event acquisition _Stream (file name). */
            WebDAV currentLog = testAs.cell(testCell.getName()).currentLog.getStream("default.log");
            logContents = currentLog.getStreamBody();
            assertNotNull(logContents);

            /** イベント取得_Stream(ファイル名+dcRequestKey). */
            /** Event acquisition _Stream (file name + dcRequestKey). */
            currentLog = testAs.cell(testCell.getName()).currentLog.getStream("default.log", dcRequestKey);
            logContents2 = currentLog.getStreamBody();
            assertNotNull(logContents2);

            /** ローテート済イベント取得_Stream(ファイル名). */
            /** Rotate already event acquisition _Stream (file name). */
            /** 現在core未対応のため404が返却される. */
            /** 404 is returned for the core not supported currently. */
            try {
                testAs.cell(testCell.getName()).archiveLog.getStream("default.log");
            } catch (DaoException e) {
                String code = e.getCode();
                assertEquals("404", code);
            }

            /** ローテート済イベント取得_Stream(ファイル名+dcRequestKey). */
            /** Rotate already event acquisition _Stream (file name + dcRequestKey). */
            /** 現在core未対応のため404が返却される. */
            /** 404 is returned for the core not supported currently. */
            try {
                testAs.cell(testCell.getName()).archiveLog.getStream("default.log", dcRequestKey);
            } catch (DaoException e) {
                String code = e.getCode();
                assertEquals("404", code);
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            if (logContents != null) {
                try {
                    logContents.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (logContents2 != null) {
                try {
                    logContents2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * mkcolのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the MKCOL creation test. It performs following operations. Retrieves a Box, Creates a Collection,
     * Creates another collection with same name to get exception. Then creates two other collections and then deletes
     * them.
     * @throws DaoException .
     */
    @Test
    public void mkcolのテスト() throws DaoException {
        try {
            /** Retrieval of Box. */
            Box box = testAs.cell(testCell.getName()).box(testBox.getName());
            /** コレクションを作成. */
            /** Create a Collection. */
            box.mkCol("col001");

            /** 同じ場所に同じ名前のコレクションを作成し、405になることを確認. */
            /** Register the Collection of the same name at same location, you should get 409 status code. */
            try {
                box.mkCol("col001");
                fail();
            } catch (DaoException ex) {
                assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, Integer.parseInt(ex.getCode()));
            }

            /** box/col001/col002 を作成. */
            /** Create box/col001/col002. */
            box.col("col001").mkCol("col002");

            /** box/col001/col002/col003を作成,削除. */
            /** Create box/col001/col002/col003. */
            box.col("col001/col002").mkCol("col003");
            box.col("col001/col002").del("col003", "*");

            /** box/col001/col002 を削除. */
            /** Delete box/col001/col002. */
            box.col("col001").del("col002", "*");

            /** 作ったコレクションの削除. */
            /** Delete the collection. */
            box.del("col001", "*");
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * mkodataのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for MKODATA creation. It creates a duplicate odata collection with same name to get 409
     * exception.
     * @throws DaoException Exception thrown
     */
    @Test
    public void mkodataのテスト() throws DaoException {

        try {
            Cell cell = testAs.cell(testCell.getName());
            /** ODataコレクション作成. */
            /** OData Collection Creation. */
            cell.box(testBox.getName()).mkOData("odata001");

            /** 同じ場所に同じ名前のODataコレクションを作成し、405になることを確認. */
            /** Register the ODataCollection of the same name at same location, you should get 409 status code. */
            try {
                cell.box(testBox.getName()).mkOData("odata001");
                fail();
            } catch (DaoException ex) {
                assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, Integer.parseInt(ex.getCode()));
            }
            /** 作ったコレクションの削除. */
            /** Delete the OData collection. */
            cell.box().del("odata001", "*");
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * mkServiceのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for MKSERVICE creation. It creates a duplicate service collection with same name to get
     * 409 exception.
     * @throws DaoException Exception thrown
     */
    @Test
    public void mkServiceのテスト() throws DaoException {

        try {
            Cell cell = testAs.cell(testCell.getName());
            /** Serviceコレクション作成. */
            /** Service Collection Creation. */
            cell.box(testBox.getName()).mkService("service001");
            /** 同じ場所に同じ名前のServiceコレクションを作成し、405になることを確認. */
            /** Register the ServiceCollection of the same name at same location, you should get 409 status code. */
            try {
                cell.box(testBox.getName()).mkOData("service001");
                fail();
            } catch (DaoException ex) {
                assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, Integer.parseInt(ex.getCode()));
            }
            /** 作ったコレクションの削除. */
            /** Delete the Service collection. */
            cell.box().del("service001", "*");
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * WebDavのテスト.
     */
    /**
     * This method is the test of WebDav on cache, read and write operations of text files to a WebDAV. It performs
     * following operations. Writes a File, Gets its content, Gets its content from cache, write the file again, get its
     * content from memory as well as cache, Delete the file.
     */
    @Test
    public final void WebDAVへテキストファイルの読み書きとキャッシュ() {
        Box box = null;
        String createEtag = null;
        String updateEtag = null;
        try {
            box = testAs.cell(testCell.getName()).box(testBox.getName());
            /** DAVファイル登録. */
            /** DAV file registration. */
            dc.setChunked(false);
            WebDAV webDAV = box.put("test.txt", "plane/text", "あいうえお", "*");
            createEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(createEtag);

            /** DAVファイル内容を文字列で取得. */
            /** Get a string DAV file contents. */
            String davString = box.getString("test.txt");
            assertEquals("あいうえお", davString);

            /** キャッシュされたファイルを取得する. */
            /** Get the cached files. */
            davString = box.getString("test.txt");
            assertEquals("あいうえお", davString);

            /** 同じファイルに、別の内容で置き換える. */
            /** In the same file, replace the contents of another. */
            WebDAV updateWebDAV = box.put("test.txt", "plane/text", "かきくけこ", createEtag);
            updateEtag = updateWebDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(updateEtag);

            /** ファイルを取得し、キャッシュからではなく、personium.ioから取得できた事を確認. */
            /** Get the file, and not from the cache, and ensure that it can be acquired from server location. */
            davString = box.getString("test.txt");
            assertEquals("かきくけこ", davString);

            /** もう一度、キャッシュから取得する. */
            /** Once again, get the file from the cache. */
            davString = box.getString("test.txt");
            assertEquals("かきくけこ", davString);

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            if (box != null) {
                try {
                    /** Delete the file. */
                    box.del("test.txt", updateEtag);
                } catch (DaoException e) {
                    fail(e.getMessage());
                }
            }
        }
    }

    /**
     * WebDavのテスト.
     */
    /**
     * This method is the error test of WebDav when old etag is specified to WebDav after the file contents are updated.
     * It performs the following operations. Writes a File, Gets its content, Gets its content from cache, write the
     * file again so that etag is changed, Update file using old etag to get the error, Delete the file.
     */
    @Test
    public final void WebDAVへ最新ETagと別のETagを指定した場合エラーとなること() {
        Box box = null;
        String createEtag = null;
        String updateEtag = null;
        try {
            box = testAs.cell(testCell.getName()).box(testBox.getName());
            /** DAVファイル登録. */
            /** DAV file registration. */
            dc.setChunked(false);
            WebDAV webDAV = box.put("test.txt", "plane/text", "あいうえお", "*");
            createEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(createEtag);

            /** DAVファイル内容を文字列で取得. */
            /** Get a string of DAV file contents. */
            String davString = box.getString("test.txt");
            assertEquals("あいうえお", davString);

            /** キャッシュされたファイルを取得する. */
            /** Get the cached files. */
            davString = box.getString("test.txt");
            assertEquals("あいうえお", davString);

            /** 同じファイルに、別の内容で置き換える. */
            /** In the same file, replace the contents of another. */
            WebDAV updateWebDAV = box.put("test.txt", "plane/text", "かきくけこ", createEtag);
            updateEtag = updateWebDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(updateEtag);

            /** 同じファイルに、別の内容で置き換える. */
            /** In the same file, replace the contents of another. */
            box.put("test.txt", "plane/text", "さしすせそ", createEtag);
            fail("DaoException is not occurred.");
        } catch (DaoException e) {
            log.debug("DaoException occurred.");
        } finally {
            if (box != null) {
                try {
                    /** Delete the file. */
                    box.del("test.txt", updateEtag);
                } catch (DaoException e) {
                    fail(e.getMessage());
                }
            }
        }
    }

    /**
     * WebDavのテスト(ファイル/コレクション一覧).
     */
    /**
     * This method is the test of WebDav (file / collection list). Collection list acquisition and files from the
     * collection of WebDAV.
     */
    @Test
    public final void WebDAVのコレクションからファイルとコレクション一覧取得() {
        Box box = null;
        try {
            box = testAs.cell(testCell.getName()).box(testBox.getName());

            /** DAVファイル登録. */
            /** DAV file registration. */
            box.put("test1.txt", "plane/text", "あいうえお", "*");
            box.put("test2.txt", "plane/text", "あいうえお", "*");

            /** Creation of collections. */
            box.mkCol("col1");
            box.mkCol("col2");

            /** Get file list. */
            String[] files = box.getFileList();
            /** Get collection list. */
            String[] cols = box.getColList();
            assertTrue(files.length > 0);
            assertTrue(cols.length > 0);

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            if (box != null) {
                try {
                    /** Delete files and collections. */
                    box.del("test1.txt");
                    box.del("test2.txt");
                    box.del("col1");
                    box.del("col2");
                } catch (DaoException e) {
                    fail(e.getMessage());
                }
            }
        }
    }

    /**
     * WebDavのテスト.
     */
    /**
     * This method is the test of WebDav for read and write operations on text files using character code to a WebDAV.
     * It performs following operations. Registering ShiftJIS file, Get a string DAV file contents, Registration of UTF8
     * file, Get a string DAV file contents, Delete DAV file.
     */
    @Test
    public final void WebDAVへの文字コードを意識したテキストファイルの読み書き() {
        try {
            Cell cell = testAs.cell(testCell.getName());
            Box box = cell.box(testBox.getName());
            String davString;

            /** ShiftJISファイルの登録. */
            /** Registration of ShiftJIS file. */
            WebDAV webDAV = box.put("shiftjis.txt", "text/plane", "MS932", "あいうえお", "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            String shiftEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(shiftEtag);

            /** DAVファイル内容を文字列で取得. */
            /** Get a string DAV file contents. */
            davString = box.getString("shiftjis.txt", "MS932");
            assertEquals("あいうえお", davString);

            /** UTF8ファイルの登録. */
            /** Registration of UTF8 file. */
            webDAV = box.put("utf8.txt", "text/plane", "UTF-8", "あいうえお", "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            String utfEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(utfEtag);

            /** DAVファイル内容を文字列で取得. */
            /** Get a string DAV file contents. */
            davString = box.getString("utf8.txt", "UTF-8");
            assertEquals("あいうえお", davString);

            /** DAVファイル削除. */
            /** DAV file Delete. */
            box.del("utf8.txt", utfEtag);
            box.del("shiftjis.txt", shiftEtag);
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * WebDavバイナリ入出力のテスト.
     */
    /**
     * This is the WebDav test of binary input and output to read and write binary files to a WebDAV. It performs
     * following operations. Test image file acquisition, DAV file registration, Get the DAV file contents, DAV File
     * Delete.
     */
    @Test
    public final void WebDAVへバイナリファイルの読み書きを行う() {
        try {
            Box box = testAs.cell(testCell.getName()).box(testBox.getName());

            /** テスト用画像ファイル取得. */
            /** Test image file acquisition. */
            String testFilename = getClass().getResource("/webdavtest.jpg").getFile();

            /** DAVファイル登録. */
            /** DAV file registration. */
            dc.setChunked(false);
            FileInputStream fis = new FileInputStream(testFilename);
            WebDAV webDAV = box.put("test.jpg", "plane/text", "", fis, "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            String etag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);

            /** DAVファイル内容を取得. */
            /** Get the DAV file contents. */
            InputStream is = box.getStream("test.jpg");

            boolean equals = expectStreamBody(testFilename, is);
            assertTrue(equals);

            /** DAVファイル削除. */
            /** DAV File Delete. */
            box.del("test.jpg", etag);
        } catch (DaoException e) {
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * WebDavのテスト.
     */
    /**
     * This method is the test of WebDav for string input and output test _WebDAV object and returns WebDAV. It performs
     * following operations. DAV file registration, Get a string DAV file contents, Get the cached files, Update file
     * contents, Get file from server, Get the file from cache, Delete the file.
     */
    @Test
    public final void WebDAVの文字列入出力テスト_WebDAVオブジェクト返却() {
        Box box = null;
        String createEtag = null;
        String updateEtag = null;
        try {
            box = testAs.cell(testCell.getName()).box(testBox.getName());
            /** DAVファイル登録. */
            /** DAV file registration. */
            dc.setChunked(false);
            WebDAV webDAV = box.put("test.txt", "plane/text", "あいうえお", "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            createEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(createEtag);

            /** DAVファイル内容を文字列で取得. */
            /** Get a string DAV file contents. */
            WebDAV getWebDAV = box.getStringWebDAV("test.txt");
            assertEquals(HttpStatus.SC_OK, getWebDAV.getStatusCode());
            assertEquals("あいうえお", getWebDAV.getStringBody());
            assertEquals(createEtag, getWebDAV.getHeaderValue(HEADER_KEY_ETAG));

            /** キャッシュされたファイルを取得する. */
            /** Get the cached files. */
            getWebDAV = box.getStringWebDAV("test.txt");
            assertEquals(HttpStatus.SC_NOT_MODIFIED, getWebDAV.getStatusCode());
            assertEquals("あいうえお", getWebDAV.getStringBody());
            assertEquals(createEtag, getWebDAV.getHeaderValue(HEADER_KEY_ETAG));

            /** 同じファイルに、別の内容で置き換える. */
            /** In the same file, replace the contents of another. */
            WebDAV updateWebDAV = box.put("test.txt", "plane/text", "かきくけこ", createEtag);
            assertEquals(HttpStatus.SC_NO_CONTENT, updateWebDAV.getStatusCode());
            updateEtag = updateWebDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(updateEtag);

            /** ファイルを取得し、キャッシュからではなく、personium.ioから取得できた事を確認. */
            /** Get the file, and not from the cache, but from server memory location. */
            getWebDAV = box.getStringWebDAV("test.txt");
            assertEquals(HttpStatus.SC_OK, getWebDAV.getStatusCode());
            assertEquals("かきくけこ", getWebDAV.getStringBody());
            assertEquals(updateEtag, getWebDAV.getHeaderValue(HEADER_KEY_ETAG));

            /** もう一度、キャッシュから取得する. */
            /** Get the file from cache. */
            getWebDAV = box.getStringWebDAV("test.txt");
            assertEquals(HttpStatus.SC_NOT_MODIFIED, getWebDAV.getStatusCode());
            assertEquals("かきくけこ", getWebDAV.getStringBody());
            assertEquals(updateEtag, getWebDAV.getHeaderValue(HEADER_KEY_ETAG));

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            if (box != null) {
                try {
                    /** Delete the file. */
                    box.del("test.txt", updateEtag);
                } catch (DaoException e) {
                    fail(e.getMessage());
                }
            }
        }
    }

    /**
     * WebDavバイナリ入出力のテスト.
     */
    /**
     * This is the WebDav test of binary input and output files of WebDav and returns WebDav. It performs following
     * operations. Test image file acquisition, DAV file registration, Get the DAV file contents, DAV file Delete.
     */
    @Test
    public final void WebDAVのバイナリ入出力テスト_WebDAVオブジェクト返却() {
        try {
            Box box = testAs.cell(testCell.getName()).box(testBox.getName());

            /** テスト用画像ファイル取得. */
            /** Test image file acquisition. */
            String testFilename = getClass().getResource("/webdavtest.jpg").getFile();

            /** DAVファイル登録. */
            /** DAV file registration. */
            dc.setChunked(false);
            FileInputStream fis = new FileInputStream(testFilename);
            WebDAV webDAV = box.put("test.jpg", "plane/text", "", fis, "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            String etag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);

            /** DAVファイル内容を取得. */
            /** Get the DAV file contents. */
            WebDAV getWebDAV = box.getStreamWebDAV("test.jpg");
            assertEquals(HttpStatus.SC_OK, getWebDAV.getStatusCode());
            InputStream is = getWebDAV.getStreamBody();
            assertEquals(etag, getWebDAV.getHeaderValue(HEADER_KEY_ETAG));

            assertTrue(expectStreamBody(testFilename, is));

            /** DAVファイル削除. */
            /** DAV file Delete. */
            box.del("test.jpg", etag);
        } catch (DaoException e) {
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * This is the private method used to compare the InputStream.
     * @param testFilename FileName
     * @param is InputStream
     * @return Equal value
     * @throws IOException Exception thrown
     */
    private boolean expectStreamBody(String testFilename, InputStream is) throws IOException {
        /** 登録元データを読み込む. */
        /** Read the original registration data. */
        int size;
        FileInputStream fis2 = new FileInputStream(testFilename);
        BufferedInputStream bis2 = new BufferedInputStream(fis2);
        size = bis2.available();
        byte[] valueExpected = new byte[size];
        bis2.read(valueExpected);
        bis2.close();
        fis2.close();

        /** DAVで取得したデータを読み込む. */
        /** To read the data acquired by the DAV. */
        byte[] value = new byte[size + 1];
        int offset = 0;
        int len;
        while ((len = is.read(value, offset, size - offset)) != -1) {
            offset += len;
            if (offset >= size && len == 0) {
                break;
            }
        }
        is.close();

        /** 比較する. */
        /** Compare. */
        assertEquals(offset, size);
        boolean equals = true;
        for (int i = 0; i < valueExpected.length; i++) {
            if (valueExpected[i] != value[i]) {
                equals = false;
                break;
            }
        }
        return equals;
    }

    /**
     * IfNoneMatchのテスト_更新なし.
     */
    /**
     * This method tests the IfNoneMatch scenario when file contents are updated. It performs the following operations.
     * DAV file registration, Get a string DAV file contents. Again get a string DAV file contents. Delete the file with
     * invalid etag to get exception.
     */
    @Test
    public final void IfNoneMatchのテスト_更新なし() {
        Box box = null;
        String createEtag = null;
        String updateEtag = null;
        try {
            box = testAs.cell(testCell.getName()).box(testBox.getName());
            /** DAVファイル登録. */
            /** DAV file registration. */
            dc.setChunked(false);
            WebDAV webDAV = box.put("test.txt", "plane/text", "あいうえお", "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            createEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(createEtag);

            /** DAVファイル内容を文字列で取得. */
            /** Get a string DAV file contents. */
            WebDAV getStringWebDAV = box.getStringWebDAV("test.txt", createEtag, "utf-8");
            assertEquals(HttpStatus.SC_NOT_MODIFIED, getStringWebDAV.getStatusCode());
            assertNull(getStringWebDAV.getStringBody());

            /** DAVファイル内容を文字列で取得. */
            /** Get a string DAV file contents. */
            WebDAV getStreamWebDAV = box.getStreamWebDAV("test.txt", createEtag);
            assertEquals(HttpStatus.SC_NOT_MODIFIED, getStreamWebDAV.getStatusCode());
            assertNull(getStreamWebDAV.getStreamBody());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete file with invalid etag to get exception. */
            if (box != null) {
                try {
                    box.del("test.txt", updateEtag);
                } catch (DaoException e) {
                    fail(e.getMessage());
                }
            }
        }
    }

    /**
     * IfNoneMatchのテスト_テキストファイル更新あり.
     */
    /**
     * This method tests the IfNoneMatch scenario when file contents are updated. It performs the following operations.
     * DAV file registration, DAV file update, Get a string DAV file contents. Again get a string DAV file contents with
     * IFNoneMatch set to null. Delete the file.
     */
    @Test
    public final void IfNoneMatchのテスト_テキストファイル更新あり() {
        Box box = null;
        String createEtag = null;
        String updateEtag = null;
        try {
            box = testAs.cell(testCell.getName()).box(testBox.getName());
            /** DAVファイル登録. */
            /** DAV file registration. */
            dc.setChunked(false);
            WebDAV webDAV = box.put("test.txt", "plane/text", "あいうえお", "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            createEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(createEtag);

            /** DAVファイル更新. */
            /** DAV file update. */
            webDAV = box.put("test.txt", "plane/text", "かきくけこ", "*");
            assertEquals(HttpStatus.SC_NO_CONTENT, webDAV.getStatusCode());
            updateEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(updateEtag);

            /** DAVファイル内容を文字列で取得. */
            /** Get a string DAV file contents. */
            WebDAV getStringWebDAV = box.getStringWebDAV("test.txt", createEtag, "utf-8");
            assertEquals(HttpStatus.SC_OK, getStringWebDAV.getStatusCode());
            assertEquals("かきくけこ", getStringWebDAV.getStringBody());
            assertEquals(updateEtag, getStringWebDAV.getHeaderValue(HEADER_KEY_ETAG));
            /** 更新なしであることを確認. */
            /** Make sure that there is no update. */
            getStringWebDAV = box.getStringWebDAV("test.txt", updateEtag, "utf-8");
            assertEquals(HttpStatus.SC_NOT_MODIFIED, getStringWebDAV.getStatusCode());
            assertNull(getStringWebDAV.getStringBody());

            /** If-None-Matchがnullの場合. */
            /** If-None-Match is null. */
            getStringWebDAV = box.getStringWebDAV("test.txt", null, "utf-8");
            assertEquals(HttpStatus.SC_OK, getStringWebDAV.getStatusCode());
            assertEquals("かきくけこ", getStringWebDAV.getStringBody());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete the File. */
            if (box != null) {
                try {
                    box.del("test.txt", updateEtag);
                } catch (DaoException e) {
                    fail(e.getMessage());
                }
            }
        }
    }

    /**
     * IfNoneMatchのテスト_バイナリファイル更新あり.
     */
    /**
     * This method tests the IfNoneMatch scenario when binary file contents are updated. It performs the following
     * operations. Test image file acquisition, DAV file registration, DAV file update, Get the DAV in binary file
     * contents. Again get a string DAV file contents with IFNoneMatch set to null. Delete the file.
     */
    @Test
    public final void IfNoneMatchのテスト_バイナリファイル更新あり() {
        Box box = null;
        String createEtag = null;
        String updateEtag = null;
        try {
            /** テスト用画像ファイル取得. */
            /** Test image file acquisition. */
            String testFilename = getClass().getResource("/webdavtest.jpg").getFile();

            box = testAs.cell(testCell.getName()).box(testBox.getName());

            /** DAVファイル登録. */
            /** DAV file registration. */
            FileInputStream fis = new FileInputStream(testFilename);
            WebDAV webDAV = box.put("test.jpg", "plane/text", "", fis, "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            createEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            fis.close();
            log.debug(createEtag);

            /** DAVファイル更新. */
            /** DAV file update. */
            fis = new FileInputStream(testFilename);
            webDAV = box.put("test.jpg", "plane/text", "", fis, "*");
            assertEquals(HttpStatus.SC_NO_CONTENT, webDAV.getStatusCode());
            updateEtag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            fis.close();
            log.debug(updateEtag);

            /** DAVファイル内容をバイナリで取得. */
            /** Get the DAV in binary file contents. */
            WebDAV getStreamWebDAV = box.getStreamWebDAV("test.jpg", createEtag);
            assertEquals(HttpStatus.SC_OK, getStreamWebDAV.getStatusCode());
            InputStream is = getStreamWebDAV.getStreamBody();
            assertTrue(expectStreamBody(testFilename, is));
            assertEquals(updateEtag, getStreamWebDAV.getHeaderValue(HEADER_KEY_ETAG));
            /** 更新なしであることを確認. */
            /** Make sure that there is no update. */
            getStreamWebDAV = box.getStreamWebDAV("test.jpg", updateEtag);
            assertEquals(HttpStatus.SC_NOT_MODIFIED, getStreamWebDAV.getStatusCode());
            assertNull(getStreamWebDAV.getStreamBody());

            /** If-None-Matchがnullの場合. */
            /** If-None-Match is null. */
            getStreamWebDAV = box.getStreamWebDAV("test.jpg", null);
            assertEquals(HttpStatus.SC_OK, getStreamWebDAV.getStatusCode());
            is = getStreamWebDAV.getStreamBody();
            assertTrue(expectStreamBody(testFilename, is));
        } catch (DaoException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } finally {
            /** Delete the file. */
            if (box != null) {
                try {
                    box.del("test.jpg", updateEtag);
                } catch (DaoException e) {
                    fail(e.getMessage());
                }
            }
        }
    }

    /**
     * CellレベルACLのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for the Cell level ACL Setting. It performs following operations. Create two Roles, Set
     * ACL, Get ACL, Delete two Roles.
     * @throws DaoException Exception thrown
     */
    @Test
    @Ignore
    public void CellレベルACLの設定取得のテスト() throws DaoException {
        try {
            Cell cell = testCell;

            /** Create Roles. */
            log.debug("\n◆  Role1作成");
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "role1");
            Role role1 = cell.role.create(map);

            log.debug("\n◆  Role2作成");
            map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "role2");
            Role role2 = cell.role.create(map);

            /** Set ACL. */
            log.debug("\n◆  ACL設定");
            Acl acl = new Acl();
            Ace ace;

            ace = new Ace();
            ace.setRole(role1);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            ace = new Ace();
            ace.setRole(role2);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            cell.acl.set(acl);

            /** Get ACL Setting. */
            log.debug("\n◆  ACL取得");
            Acl retAcl = cell.acl.get();

            System.out.println(retAcl.toXmlString());
            System.out.println(acl.toXmlString());

            assertEquals(retAcl.getAceList().size(), acl.getAceList().size());

            /** Delete roles. */
            log.debug("\n◆  Role1削除");
            cell.role.del("role1");
            log.debug("\n◆  Role2削除");
            cell.role.del("role2");
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * 認証したCellと別のCellにACLを行うテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method tests the ACL for another Cell that has been authenticated. It performs following operations. Creates
     * a Cell, Creates a Box, Creates a Role, Set ACL, Delete the Role, Delete the Box, Delete the Cell.
     * @throws DaoException .
     */
    @Test
    @Ignore
    public void 認証したCellと別のCellにACLを行うテスト() throws DaoException {

        Role role1 = null;
        Box box = null;
        Cell bCell = null;
        try {
            /** データ作成. */
            /** Data Creation. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "bCell010909");

            /** Creating a Cell. */
            log.debug("\n◆ bCellの作成");
            OwnerAccessor oa = testAs.asCellOwner();
            bCell = oa.unit.cell.create(cellMap);
            log.debug("\n Cell:[" + bCell.toJSONString() + "]");

            /** Creating a Box. */
            log.debug("\n◆ Boxの作成");
            box = new Box();
            box.setName("aclbox");
            bCell.box.create(box);

            /** Creating a Role. */
            log.debug("\n◆ Roleの作成");
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "role1");
            map.put(FIELD_BOXNAME, box.getName());
            role1 = bCell.role.create(map);

            log.debug("\n◆ Aclの作成");
            /** ACL設定. */
            /** ACL settings. */
            Acl acl = new Acl();
            acl.setRequireSchemaAuthz("public");
            Ace ace;

            ace = new Ace();
            ace.setRole(role1);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            bCell.acl.set(acl);
        } finally {
            /** Deleting the Role. */
            if (role1 != null) {
                log.debug("\n◆ Roleの削除");
                bCell.role.del("role1", box.getName());
            }
            /** Deleting the Box. */
            if (box != null) {
                log.debug("\n◆ Boxの削除");
                bCell.box.del(box.getName());
            }
            /** Deleting the Cell. */
            if (bCell != null) {
                log.debug("\n◆ Cellの削除");
                testAs.asCellOwner().unit.cell.del(bCell.getName(), "*");
            }
        }

    }

    /**
     * ACLのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test of setting acquisition of ACL. It performs following operations. Creates a Box, Creates
     * two Roles, Set ACL, Get ACL, Delete the Roles, Delete the Box.
     * @throws DaoException .
     */
    @Test
    public void ACLの設定取得のテスト() throws DaoException {
        try {
            /** コレクションを作成. */
            /** Creating a Box. */
            Cell cell = testCell;
            Box box = new Box();
            box.setName("aclbox");
            box = testCell.box.create(box);

            // box.mkCol("col001");

            /** Role1作成. */
            /** Creating a Role. */
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "role1");
            map.put(FIELD_BOXNAME, box.getName());
            Role role1 = cell.role.create(map);

            /** Creating another Role. */
            map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "role2");
            map.put(FIELD_BOXNAME, box.getName());
            Role role2 = cell.role.create(map);
            /** ACL設定. */
            /** ACL settings. */
            Acl acl = new Acl();
            acl.setRequireSchemaAuthz("public");
            Ace ace;

            ace = new Ace();
            // ace.setRole(role1);
            ace.setPrincipal(role1);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            ace = new Ace();
            // ace.setRole(role2);
            ace.setPrincipal(role2);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            ace = new Ace();
            ace.setPrincipal(Principal.ALL);
            ace.addPrivilege("read");
            acl.addAce(ace);

            box.acl.set(acl);

            /** Get ACL Settings. */
            Acl retAcl = box.acl.get();

            System.out.println(retAcl.toXmlString());
            System.out.println(acl.toXmlString());

            assertEquals(retAcl.getAceList().size(), acl.getAceList().size());

            /** requireSchemaAuthzの値が設定されていることの確認. */
            assertEquals(acl.getRequireSchemaAuthz(), retAcl.getRequireSchemaAuthz());

            /** Role削除. */
            /** Deleting the roles. */
            /** TODO β１では、コレクション削除前にRole削除を行うと、コレクション削除が400となるため. */
            /** TODO In order β1, If you do Role Delete to delete before collection, collection Delete becomes 400. */
            cell.role.del("role1", box.getName());
            cell.role.del("role2", box.getName());

            /** Deleting the Box. */
            cell.box.del(box.getName());
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * ace無しな場合のACLのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test case of the ACL without ACE with requireSchemaAuthz is set. It performs following
     * operations. Creates a Box, Sets ACL, Gets ACL, Deletes the Box.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ace無しな場合のACLのテスト() throws DaoException {
        try {
            /** コレクションを作成. */
            /** Creating a Box. */
            Cell cell = testCell;
            Box box = new Box();
            box.setName("aclbox");
            box = testCell.box.create(box);

            /** ACL設定. */
            /** ACL settings. */
            Acl acl = new Acl();
            acl.setRequireSchemaAuthz("public");

            box.acl.set(acl);

            /** Get ACL. */
            Acl retAcl = box.acl.get();

            System.out.println(retAcl.toXmlString());
            System.out.println(acl.toXmlString());

            assertEquals(retAcl.getAceList().size(), acl.getAceList().size());

            /** requireSchemaAuthzの値が設定されていることの確認. */
            /** confirm that the value of requireSchemaAuthz is set. */
            assertEquals(acl.getRequireSchemaAuthz(), retAcl.getRequireSchemaAuthz());

            /** Deleting the Box. */
            cell.box.del(box.getName());
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * aceがnullな場合のACLのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test case of the ACL when ACE is null and requireSchemaAuthz is set. It performs following
     * operations. Creates a Box, Sets ACL, Gets ACL, Deletes the Box.
     * @throws DaoException Exception thrown
     */
    @Test
    public void aceがnullな場合のACLのテスト() throws DaoException {
        try {
            /** コレクションを作成. */
            /** Creating a Box. */
            Cell cell = testCell;
            Box box = new Box();
            box.setName("aclbox");
            box = testCell.box.create(box);

            /** ACL設定. */
            /** ACL Settings. */
            Acl acl = new Acl();
            acl.setRequireSchemaAuthz("public");
            acl.addAce(null);

            box.acl.set(acl);

            /** get ACL. */
            Acl retAcl = box.acl.get();

            System.out.println(retAcl.toXmlString());
            System.out.println(acl.toXmlString());

            assertEquals(retAcl.getAceList().size(), 0);

            /** requireSchemaAuthzの値が設定されていることの確認. */
            /** confirm that the value of requireSchemaAuthz is set. */
            assertEquals(acl.getRequireSchemaAuthz(), retAcl.getRequireSchemaAuthz());

            /** Deleting the Box. */
            cell.box.del(box.getName());
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * ace_Roleがnullな場合のACLのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test case of the ACL when ACE-Role is null and requireSchemaAuthz is set. It performs
     * following operations. Creates a Box, Sets ACL, Deletes the Box.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ace_Roleがnullな場合のACLのテスト() throws DaoException {
        Cell cell = testCell;
        Box box = null;
        try {
            /** コレクションを作成. */
            /** Creating a Box. */
            box = new Box();
            box.setName("aclbox");
            box = testCell.box.create(box);

            /** ACL設定. */
            /** ACL Settings. */
            Acl acl = new Acl();
            acl.setRequireSchemaAuthz("public");
            Ace ace;

            ace = new Ace();
            ace.setRole(null);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            box.acl.set(acl);

            fail();
        } catch (DaoException e) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, Integer.parseInt(e.getCode()));
        } finally {
            /** Deleting the Box. */
            if (box != null) {
                cell.box.del(box.getName());
            }
        }
    }

    /**
     * ace_Privilegeがnullな場合のACLのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test case of the ACL when ACE-Privilege is null and requireSchemaAuthz is set. It performs
     * following operations. Creates a Box, Creates a Role, Sets ACL, Gets the ACL, Deletes the Role, Deletes the Box.
     * @throws DaoException Exception thrown
     */
    @Test
    @Ignore
    public void ace_Privilegeがnullな場合のACLのテスト() throws DaoException {
        try {
            /** コレクションを作成. */
            /** Creating a Box. */
            Cell cell = testCell;
            Box box = new Box();
            box.setName("aclbox");
            box = testCell.box.create(box);

            // box.mkCol("col001");

            /** Role1作成. */
            /** Creating a Role. */
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "role1");
            map.put(FIELD_BOXNAME, box.getName());
            Role role1 = cell.role.create(map);

            /** ACL設定. */
            /** ACL Settings. */
            Acl acl = new Acl();
            acl.setRequireSchemaAuthz("public");
            Ace ace;

            ace = new Ace();
            ace.setRole(role1);
            ace.addPrivilege(null);
            acl.addAce(ace);

            box.acl.set(acl);

            /** Gets ACL. */
            Acl retAcl = box.acl.get();

            System.out.println(retAcl.toXmlString());
            System.out.println(acl.toXmlString());

            assertEquals(retAcl.getAceList().size(), acl.getAceList().size());

            /** requireSchemaAuthzの値が設定されていることの確認. */
            /** confirm that the value of requireSchemaAuthz is set. */
            assertEquals(acl.getRequireSchemaAuthz(), retAcl.getRequireSchemaAuthz());

            /** Role削除. */
            /** Deleting the Role. */
            /** TODO β１では、コレクション削除前にRole削除を行うと、コレクション削除が400となるため. */
            /** TODO In order β1, If you do Role Delete to delete before collection, collection Delete becomes 400. */
            cell.role.del("role1", box.getName());

            /** Deleting the Box. */
            cell.box.del(box.getName());
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Role(Cell)のCRUDテスト(_box.name省略時).
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for CRUD operation on Role(Cell) when _box.name is default. When a new Role with existing
     * name is created, it gives 409 exception.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RoleCellのCRUD() throws DaoException {
        final String roleName = "role01";
        Role role = null;
        Cell cell = null;
        try {
            /** Roleの作成(_box.name 省略). */
            /** Creating a Role (_box.name omitted). */
            HashMap<String, Object> roleMap = new HashMap<String, Object>();
            roleMap.put(FIELD_NAME, roleName);
            cell = testAs.cell(testCell.getName());
            role = cell.role.create(roleMap);
            log.debug("\n Role:[" + role.toJSONString() + "]");
            assertEquals(roleName, role.getName());

            /** 同じ名前のRoleを登録し、409になることを確認. */
            /** When a Role with same name is created, 409 exception is thrown. */
            try {
                cell.role.create(roleMap);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }

            /** 取得したロールの名前をチェック. */
            /** Check the name of the role that has been acquired. */
            assertEquals(roleName, cell.role.retrieve(role.getName()).getName());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** ロールの削除. */
            /** Deleting the Role. */
            if (role != null) {
                cell.role.del(role.getName());
            }
        }
    }

    /**
     * Role(Box)のCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for CRUD operation on Role(Cell) when _box.name is set and using Map.
     * When a new Role with existing name is created, it gives 409 exception.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RoleBoxのCRUD() throws DaoException {
        final String roleName = "role01";
        Role role = null;
        Cell cell = null;
        try {
            cell = testAs.cell(testCell.getName());

            /** Role作成用のJSONを生成. */
            /** Creating a JSON for Role. */
            HashMap<String, Object> roleMap = new HashMap<String, Object>();
            roleMap.put(FIELD_NAME, roleName);
            roleMap.put(FIELD_BOXNAME, testBox.getName());

            /** Roleの作成. */
            /** Creating a Role. */
            role = cell.role.create(roleMap);
            log.debug("\n Role:[" + role.toJSONString() + "]");
            String retRoleName = role.getName();
            String retBoxName = role.getBoxName();
            assertEquals(roleName, retRoleName);
            assertEquals(testBox.getName(), retBoxName);
            log.debug(role.getHeaderValue(HEADER_KEY_ETAG));

            /** 取得したロールの名前をチェック. */
            /** Check the name of the role that has been acquired. */
            assertEquals(roleName, cell.role.retrieve(retRoleName, retBoxName).getName());

            /** 同じ名前のRoleを登録し、409になることを確認. */
            /** When a Role with same name is created, 409 exception is thrown. */
            try {
                cell.role.create(roleMap);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Deleting the Role. */
            if (role != null) {
                cell.role.del(role.getName(), role.getBoxName());
            }
        }
    }

    /**
     * Role(Box)のCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for CRUD operation on Role(Cell) when _box.name is set and using Role object.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RoleBoxのCRUD_オブジェクト渡し() throws DaoException {
        final String roleName = "role01";
        Role role = null;
        Cell cell = null;
        try {
            cell = testAs.cell(testCell.getName());

            /** Roleの作成. */
            /** Create a Role. */
            role = new Role();
            role.setName(roleName);
            role.setBoxName(testBox.getName());
            role = cell.role.create(role);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete the Role. */
            try {
                if (role != null) {
                    cell.role.del(role.getName(), role.getBoxName());
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * Relation(Cell)のCRUDテスト(_box.name省略時).
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for CRUD operation on Relation(Cell) when _box.name is default. When a new Relation with existing
     * name is created, it gives 409 exception.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RelationCellのCRUD() throws DaoException {
        final String relationName = "relation01";
        Relation relation = null;
        Cell cell = null;
        try {
            /** Relationの作成(_box.name 省略). */
            /** Creating a Relation (_box.name omitted). */
            HashMap<String, Object> relationMap = new HashMap<String, Object>();
            relationMap.put(FIELD_NAME, relationName);
            cell = testAs.cell(testCell.getName());
            relation = cell.relation.create(relationMap);
            log.debug("\n Relation:[" + relation.toJSONString() + "]");
            assertEquals(relationName, relation.getName());

            /** 同じ名前のRelationを登録し、409になることを確認. */
            /** When a Relation with same name is created, 409 exception is thrown. */
            try {
                cell.relation.create(relationMap);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }

            /** 取得. */
            /** Retrieve Relation. */
            relation = cell.relation.retrieve(relation.getName());
            assertEquals(relationName, relation.getName());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** ロールの削除. */
            /** Delete the relation. */
            if (relation != null) {
                cell.relation.del(relation.getName());
            }
        }
    }

    /**
     * Relation(Box)のCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for CRUD operation on Relation(Cell) when _box.name is set and using Map.
     * When a new Relation with existing name is created, it gives 409 exception.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RelationBoxのCRUD() throws DaoException {
        final String relationName = "relation01";
        Relation relation = null;
        Cell cell = null;
        try {
            cell = testAs.cell(testCell.getName());

            HashMap<String, Object> relationMap = new HashMap<String, Object>();
            relationMap.put(FIELD_NAME, relationName);
            relationMap.put(FIELD_BOXNAME, testBox.getName());
            /** Relationの作成. */
            /** Creating a Relation. */
            relation = cell.relation.create(relationMap);
            log.debug("\n Relation:[" + relation.toJSONString() + "]");
            String retRelationName = relation.getName();
            String retBoxName = relation.getBoxName();
            assertEquals(relationName, retRelationName);
            assertEquals(testBox.getName(), retBoxName);
            log.debug(relation.getHeaderValue(HEADER_KEY_ETAG));

            /** 同じ名前のRelationを登録し、409になることを確認. */
            /** When a Relation with same name is created, 409 exception is thrown. */
            try {
                cell.relation.create(relationMap);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }

            /** 取得. */
            /** Retrieve the Relation. */
            relation = cell.relation.retrieve(retRelationName, retBoxName);
            assertEquals(relationName, relation.getName());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Deleting the Relation. */
            if (relation != null) {
                cell.relation.del(relation.getName(), relation.getBoxName());
            }
        }
    }

    /**
     * Relation(Box)のCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test for CRUD operation on Relation(Cell) when _box.name is set and using Relation object.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RelationBoxのCRUD_オブジェクト渡し() throws DaoException {
        final String relationName = "relation01";
        Relation relation = null;
        Cell cell = null;
        try {
            cell = testAs.cell(testCell.getName());
            /** Relationの作成. */
            /** Creating a Relation. */
            relation = new Relation();
            relation.setName(relationName);
            relation.setBoxName(testBox.getName());
            relation = cell.relation.create(relation);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Deleting the Relation. */
            try {
                if (relation != null) {
                    cell.relation.del(relation.getName(), relation.getBoxName());
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * ExtRoleのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the CRUD test for ExtRole operations. It performs following operations.
     * Create a Relation, Create an ExtRole, Create another ExtRole with same name to get 409 exception,
     * Retrieve ExtRole, Delete ExtRole, Delete Relation.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ExtRoleのCRUD() throws DaoException {
        String roleUrl = "http://extrole/jp";
        ExtRole extRole = null;
        Relation relation = null;
        Cell cell = null;

        try {
            cell = testAs.cell(testCell.getName());

            /** Create a Relation. */
            log.debug("\n◆ Relation作成");
            HashMap<String, Object> json = new HashMap<String, Object>();
            json.put("Name", "relation");
            relation = cell.relation.create(json);

            /** データ作成. */
            /** Data creation. */
            json = new HashMap<String, Object>();
            json.put("ExtRole", roleUrl);
            json.put("_Relation.Name", relation.getName());

            /** Create ExtRole. */
            log.debug("\n◆ ExtRole作成");
            extRole = cell.extRole.create(json);
            assertEquals(roleUrl, extRole.getName());
            log.debug(extRole.getHeaderValue(HEADER_KEY_ETAG));

            /** 同じ名前のExtRoleを登録し、409になることを確認. */
            /** When an ExtRole with same name is created, 409 exception is thrown. */
            try {
                cell.extRole.create(json);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            log.debug("\n◆ ExtRole取得");
            /** Retrieve ExtRole. */
            extRole = cell.extRole.retrieve(roleUrl, extRole.getRelationName(), extRole.getRelationBoxName());
            assertEquals(roleUrl, extRole.getName());
            assertEquals(relation.getName(), extRole.getRelationName());
            assertNull(extRole.getRelationBoxName());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete ExtRole. */
            log.debug("\n◆ ExtRole削除");
            if (extRole != null) {
                cell.extRole.del(roleUrl, extRole.getRelationName(), extRole.getRelationBoxName());
            }
            /** Delete Relation. */
            log.debug("\n◆ Relation削除");
            if (relation != null) {
                cell.relation.del(relation.getName(), relation.getBoxName());
            }
        }
    }

    /**
     * ExtRole(複合キー)のCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the CRUD test of ExtRole with composite key. It performs following operations.
     * Create a Relation, Create an ExtRole, Create another ExtRole with same name to get 409 exception,
     * Retrieve ExtRole, Update ExtRole, Delete ExtRole, Delete Relation.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ExtRole複合キーのCRUD() throws DaoException {
        String roleUrl = "http://extrole/jp";
        String roleUrlNew = "http://extrole.new.jp";
        String relationName = "relation";
        ExtRole extRole = null;
        Cell cell = null;
        Relation relation = null;

        try {
            cell = testAs.cell(testCell.getName());

            log.debug("\n◆ Relation作成");
            HashMap<String, Object> relationMap = new HashMap<String, Object>();
            relationMap.put(FIELD_NAME, relationName);
            relationMap.put(FIELD_BOXNAME, testBox.getName());
            /** Relationの作成. */
            /** Create a Relation. */
            relation = cell.relation.create(relationMap);

            /** データ作成. */
            /** Data Creation. */
            HashMap<String, Object> json = new HashMap<String, Object>();
            json.put("ExtRole", roleUrl);
            json.put("_Relation.Name", relationName);
            json.put("_Relation._Box.Name", testBox.getName());

            /** Create an ExtRole. */
            log.debug("\n◆ ExtRole作成");
            extRole = cell.extRole.create(json);
            assertEquals(roleUrl, extRole.getName());

            /** 同じ名前のExtRoleを登録し、409になることを確認. */
            /** When an ExtRole with same name is created, 409 exception is thrown. */
            try {
                cell.extRole.create(json);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }

            /** Retrieve ExtRole. */
            log.debug("\n◆ ExtRole取得");
            extRole = cell.extRole.retrieve(roleUrl, relationName, testBox.getName());
            assertEquals(roleUrl, extRole.getName());
            assertEquals(relationName, extRole.getRelationName());
            assertEquals(testBox.getName(), extRole.getRelationBoxName());

            /** Update ExtRole. */
            log.debug("\n◆ ExtRole更新");
            json = new HashMap<String, Object>();
            json.put("ExtRole", roleUrlNew);
            json.put("_Relation.Name", relationName);
            json.put("_Relation._Box.Name", testBox.getName());
            cell.extRole.update(roleUrl, relationName, testBox.getName(), json, "*");

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete ExtRole. */
            log.debug("\n◆ ExtRole削除");
            if (extRole != null) {
                cell.extRole.del(roleUrlNew, extRole.getRelationName(), extRole.getRelationBoxName());
            }

            /** Delete Relation. */
            log.debug("\n◆ Relation削除");
            if (relation != null) {
                cell.relation.del(relation.getName(), relation.getBoxName());
            }
        }
    }

    /**
     * ExtCellのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the CRUD test for ExtCell operations using Map. It performs following operations.
     * Create a Cell, Create an ExtCell, Create another ExtCell with same name to get 409 exception,
     * Delete ExtCell, Delete Cell.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void ExtCellのCRUD() throws DaoException {
        // final String url = testCell.getUrl();
        ExtCell extCell = null;
        Cell cell = null;
        String etag = null;
        try {
            /** Cellの作成. */
            /** Create a Cell. */
            cell = createCell("testcell02" + suf);
            /** ExtCellの作成. */
            /** Create an ExtCell. */
            HashMap<String, Object> extCellMap = new HashMap<String, Object>();
            extCellMap.put("Url", cell.getUrl());
            extCell = testCell.extCell.create(extCellMap);
            log.debug("\n ExtCell:[" + extCell.toJSONString() + "]");
            etag = extCell.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);

            assertEquals(cell.getUrl(), extCell.getUrl());

            /** 取得したExtCellのurlをチェック. */
            /** Check the url of ExtCell acquired. */
            HashMap<String, Object> res = testCell.extCell.query().run();
            ArrayList<Object> ar = (ArrayList<Object>) ((HashMap<String, Object>) res.get("d")).get("results");
            assertEquals(cell.getUrl(), ((HashMap<String, Object>) ar.get(0)).get("Url"));

            /** 取得したExtCellの名前をチェック. */
            /** Check the name of the ExtCell acquired. */
            assertEquals(cell.getUrl(), testCell.extCell.retrieve(cell.getUrl()).getUrl());

            /** 同じ名前のRelationを登録し、409になることを確認. */
            /** When an ExtCell with same name is created, 409 exception is thrown. */
            try {
                testCell.extCell.create(extCellMap);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
        } catch (DaoException e) {
            /** Delete ExtCell */
            if (extCell != null) {
                testCell.extCell.del(cell.getUrl());
            }
            fail(e.getMessage());
        } finally {
            /** Delete ExtCell. */
            if (extCell != null) {
                testCell.extCell.del(extCell.getUrl(), etag);
            }
            /** Delete Cell. */
            if (cell != null) {
                testAs.asCellOwner().unit.cell.del(cell.getName());
            }
        }
    }

    /**
     * ExtCellのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method is the CRUD test for ExtCell operations using ExtCell object. It performs following operations.
     * Create a Cell, Create an ExtCell, Delete ExtCell, Delete Cell.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ExtCellのCRUD_オブジェクト渡し() throws DaoException {
        // final String url = testCell.getUrl();
        ExtCell extCell = null;
        Cell cell = null;
        try {
            /** Cellの作成. */
            /** Craete a Cell. */
            cell = createCell("testcell02" + suf);
            extCell = new ExtCell();
            extCell.setUrl(cell.getUrl());
            /** Create an ExtCell. */
            extCell = testCell.extCell.create(extCell);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            try {
                /** Delete ExtCell. */
                if (extCell != null) {
                    testCell.extCell.del(extCell.getUrl());
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
            /** Delete Cell. */
            if (cell != null) {
                testAs.asCellOwner().unit.cell.del(cell.getName());
            }
        }
    }

    /**
     * asSelfで自分のセルの認証.
     */
    /**
     * This method is to perform authentication of the current cell.
     */
    @Test
    public void asSelfで自分のセルの認証() {
        try {
            Cell cell = testAs.cell();
            cell.getAccessToken();
        } catch (DaoException e) {
            assertEquals(HttpStatus.SC_UNAUTHORIZED, Integer.parseInt(e.getCode()));
        }
    }

    /**
     * パスワード認証ー自分セルトークン取得.
     * @throws DaoException DAO例外
     */
    /**
     * This method is for Password authentication over their cell token acquisition.
     * @throws DaoException Exception thrown
     */
    @Test
    public void パスワード認証ー自分セルトークン取得() throws DaoException {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(FIELD_NAME, "other_cell_name" + suf);
        Cell cellOther = null;
        /** Cellの作成. */
        /** Create Cell. */
        try {
            cellOther = testAs.asCellOwner().unit.cell.create(map);

            /** asAccount. */
            Accessor accessor = dc.asAccount(testCell.getName(), testAccount.getName(), accountPassword);

            Cell cell = accessor.cell();
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();

            /** getAccessorWithAccount. */
            accessor = dc.getAccessorWithAccount(testCell.getName(), testAccount.getName(), accountPassword);
            cell = accessor.cell();
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
        }

    }

    /**
     * パスワード認証ートランスセルトークン取得.
     * @throws DaoException DAO例外
     */
    /**
     * This method is for Password authentication over transformer cell token acquisition.
     * @throws DaoException Exception thrown
     */
    @Test
    public void パスワード認証ートランスセルトークン取得() throws DaoException {
        Cell cellOther = null;
        try {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "other_cell_name" + suf);
            /** Cellの作成. */
            /** Create Cell. */
            cellOther = testAs.asCellOwner().unit.cell.create(map);

            Accessor accessor = dc.asAccount(testCell.getName(), testAccount.getName(), accountPassword);
            Cell cell = accessor.cell(cellOther.getName());
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
        }
    }

    /**
     * トークン認証ー他人セルトークン取得.
     * @throws DaoException DAO例外
     */
    /**
     * This method is for Token authentication over others cell token acquisition.
     * @throws DaoException Exception thrown
     */
    @Test
    public void トークン認証ー他人セルトークン取得() throws DaoException {
        Cell cellOther = null;
        Account account = null;
        try {
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other_cell_name" + suf);
            /** Cellの作成. */
            /** Create Cell. */
            cellOther = testAs.asCellOwner().unit.cell.create(cellMap);
            /** アカウントを作る. */
            /** Account Creation. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell(cellOther.getName()).account.create(accountMap, accountPassword);

            Cell cell = dc.asAccount(testCell.getName(), testAccount.getName(), accountPassword).cell(
                    cellOther.getName());

            Accessor accessor = dc.getAccessorWithTransCellToken(cell.getName(), cell.getAccessToken());
            cell = accessor.cell();

            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Account. */
            if (account != null) {
                testAs.cell(cellOther.getName()).account.del(accountName);
                account = null;
            }
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
        }
    }

    /**
     * トークン認証ートランセルトークン取得.
     * @throws DaoException DAO例外
     */
    /**
     * This method is for Token authentication over Transformer cell token acquisition.
     * @throws DaoException Exception thrown
     */
    @Test
    public void トークン認証ートランセルトークン取得() throws DaoException {
        Cell cellOther = null;
        Cell cellOther2 = null;
        Account account = null;
        try {
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other_cell_name" + suf);
            /** Cellの作成. */
            /** Cell Creation. */
            cellOther = testAs.asCellOwner().unit.cell.create(cellMap);
            cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other2_cell_name" + suf);
            /** Cellの作成. */
            /** Create another Cell. */
            cellOther2 = testAs.asCellOwner().unit.cell.create(cellMap);
            /** アカウントを作る. */
            /** Create an Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell(cellOther.getName()).account.create(accountMap, accountPassword);

            Cell cell = dc.asAccount(testCell.getName(), testAccount.getName(), accountPassword).cell(
                    cellOther.getName());

            Accessor accessor = dc.getAccessorWithTransCellToken(cell.getName(), cell.getAccessToken());
            cell = accessor.cell(cellOther2.getName());

            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Account. */
            if (account != null) {
                testAs.cell(cellOther.getName()).account.del(accountName);
                account = null;
            }
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
            /** Delete Cell. */
            if (cellOther2 != null) {
                testAs.asCellOwner().unit.cell.del(cellOther2.getName());
                cellOther2 = null;
            }
        }
    }

    /**
     * リフレッシュトークンー自分セルトークン.
     * @throws DaoException DAO例外
     */
    /**
     * This method is for test of Refresh token over their cell token.
     * @throws DaoException Exception thrown
     */
    @Test
    public void リフレッシュトークンー自分セルトークン() throws DaoException {
        Cell cellOther = null;
        HashMap<String, Object> cellMap = new HashMap<String, Object>();
        cellMap.put(FIELD_NAME, "other_cell_name" + suf);

        try {
            /** Cellの作成. */
            /** Create cell. */
            cellOther = testAs.asCellOwner().unit.cell.create(cellMap);

            Cell cell = dc.asAccount(testCell.getName(), testAccount.getName(), accountPassword).cell();

            Accessor accessor = dc.getAccessorWithRefreshToken(cell.getName(), cell.getRefreshToken());
            cell = accessor.cell();
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
        }

    }

    /**
     * リフレッシュトークンートランスセルトークン.
     * @throws DaoException Dao例外
     */
    /**
     * This method is for test of Refresh token over transformer cell token.
     * @throws DaoException Exception thrown
     */
    @Test
    public void リフレッシュトークンートランスセルトークン() throws DaoException {
        Cell cellOther = null;
        Cell cell = null;
        Account account = null;
        try {
            /** Cellを作る. */
            /** Create a Cell. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other_cell_name" + suf);
            cellOther = testAs.asCellOwner().unit.cell.create(cellMap);
            /** アカウントを作る. */
            /** Create an Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell(cellOther.getName()).account.create(accountMap, accountPassword);

            cell = dc.asAccount(testCell.getName(), testAccount.getName(), accountPassword).cell();
            Accessor accessor = dc.getAccessorWithRefreshToken(cell.getName(), cell.getRefreshToken());
            cell = accessor.cell(cellOther.getName());
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Account. */
            if (account != null) {
                testAs.cell(cellOther.getName()).account.del(accountName);
                account = null;
            }
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
        }
    }

    /**
     * スキーマ付きーパスワード認証ー自分セルトークン取得.
     * @throws DaoException Dao例外
     */
    /**
     * This method is for test of Schema with password authentication over their cell token acquisition.
     * @throws DaoException Exception thrown
     */
    @Test
    public void スキーマ付きーパスワード認証ー自分セルトークン取得() throws DaoException {
        Cell cellOther = null;
        Cell cell = null;
        Account account = null;
        try {
            /** Cellを作る. */
            /** Create Cell. */
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "other_cell_name" + suf);
            cellOther = testAs.asCellOwner().unit.cell.create(map);
            /** アカウントを作る. */
            /** Create an Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell(cellOther.getName()).account.create(accountMap, accountPassword);

            /** スキーマ認証. */
            /** Authentication schema. */
            Accessor accessor = dc.asAccountWithSchemaAuthn(testCell.getUrl(), testAccount.getName(), accountPassword,
                    cellOther.getUrl(), account.getName(), accountPassword);
            cell = accessor.cell();
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Account. */
            if (account != null) {
                testAs.cell(cellOther.getName()).account.del(accountName);
                account = null;
            }
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
        }
    }

    /**
     * スキーマ付きーパスワード認証ートランスセルトークン取得.
     * @throws DaoException Dao例外
     */
    /**
     * This method is for test of Schema with password authentication over transformer cell token acquisition.
     * @throws DaoException Exception thrown
     */
    @Test
    public void スキーマ付きーパスワード認証ートランスセルトークン取得() throws DaoException {
        Cell cellOther = null;
        Cell cellOther2 = null;
        Cell cell = null;
        Account account = null;
        try {
            /** Cellを作る. */
            /** Create a cell. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other_cell_name" + suf);
            cellOther = testAs.asCellOwner().unit.cell.create(cellMap);
            /** Cellを作る. */
            /** Create another Cell. */
            cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other2_cell_name" + suf);
            cellOther2 = testAs.asCellOwner().unit.cell.create(cellMap);
            /** アカウントを作る. */
            /** Create an Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell(cellOther.getName()).account.create(accountMap, accountPassword);

            /** スキーマ認証(asAccountWithSchemaAuthn). */
            /** Authentication schema (asAccountWithSchemaAuthn). */
            Accessor accessor = dc.asAccountWithSchemaAuthn(testCell.getUrl(), testAccount.getName(), accountPassword,
                    cellOther.getUrl(), account.getName(), accountPassword);
            cell = accessor.cell(cellOther2.getName());
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();

            /** スキーマ認証(getAccessorWithAccountAndSchemaAuthn). */
            /** Authentication schema (getAccessorWithAccountAndSchemaAuthn). */
            accessor = dc.getAccessorWithAccountAndSchemaAuthn(testCell.getUrl(), testAccount.getName(),
                    accountPassword, cellOther.getUrl(), account.getName(), accountPassword);
            cell = accessor.cell(cellOther2.getName());
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Account. */
            if (account != null) {
                testAs.cell(cellOther.getName()).account.del(accountName);
                account = null;
            }
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
            /** Delete cell. */
            if (cellOther2 != null) {
                testAs.asCellOwner().unit.cell.del(cellOther2.getName());
                cellOther2 = null;
            }
        }
    }

    // /**
    // * スキーマ付きートークン認証ー他人セルトークン取得.
    // * @throws DaoException Dao例外
    // */
    /**
     * This method is for test of Schema with token authentication over others cell token acquisition.
     * @throws DaoException Exception thrown
     */
    @Test
    public void スキーマ付きートークン認証ー他人セルトークン取得() throws DaoException {
        Cell cellOther = null;
        Cell cellOther2 = null;
        Cell cellOther3 = null;
        Cell cell = null;
        Account account = null;
        Account account2 = null;
        try {
            /** Cellを作る. */
            /** Create cell. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other_cell_name" + suf);
            cellOther = testAs.asCellOwner().unit.cell.create(cellMap);
            /** Cellを作る. */
            /** Create another cell. */
            cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other2_cell_name" + suf);
            cellOther2 = testAs.asCellOwner().unit.cell.create(cellMap);
            /** Cellを作る. */
            /** Create another cell. */
            cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other3_cell_name" + suf);
            cellOther3 = testAs.asCellOwner().unit.cell.create(cellMap);
            /** アカウントを作る. */
            /** Create Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell(cellOther.getName()).account.create(accountMap, accountPassword);
            /** アカウントを作る. */
            /** Create another Account. */
            accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName + "2");
            account2 = testAs.cell(cellOther2.getName()).account.create(accountMap, accountPassword);

            /** トランスセル取得. */
            /** Fetch Transformer cell. */
            Accessor accessor = dc.asAccount(cellOther.getName(), account.getName(), accountPassword);
            cell = accessor.cell(testCell.getName());

            /** スキーマ認証. */
            /** Authentication schema. */
            cell = dc.getAccessorWithTransCellTokenAndSchemaAuthn(cell.getName(), cell.getAccessToken(),
                    cellOther2.getUrl(), account2.getName(), accountPassword).cell(testCell.getName());
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Account. */
            if (account != null) {
                testAs.cell(cellOther.getName()).account.del(accountName);
                account = null;
            }
            /** Delete Account. */
            if (account2 != null) {
                testAs.cell(cellOther2.getName()).account.del(account2.getName());
                account2 = null;
            }
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
            /** Delete Cell. */
            if (cellOther2 != null) {
                testAs.asCellOwner().unit.cell.del(cellOther2.getName());
                cellOther2 = null;
            }
            /** Delete Cell. */
            if (cellOther3 != null) {
                testAs.asCellOwner().unit.cell.del(cellOther3.getName());
                cellOther3 = null;
            }
        }
    }

    /**
     * スキーマ付きートークン認証ートランスセルトークン取得.
     * @throws DaoException Dao例外
     */
    /**
     * This method is for test of Schema with token authentication over transformer cell token acquisition.
     * @throws DaoException Exception thrown
     */
    @Test
    public void スキーマ付きートークン認証ートランスセルトークン取得() throws DaoException {
        Cell cellOther = null;
        Cell cellOther2 = null;
        Cell cellOther3 = null;
        Cell cell = null;
        Account account = null;
        Account account2 = null;
        try {
            /** Cellを作る. */
            /** Create a Cell. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other_cell_name" + suf);
            cellOther = testAs.asCellOwner().unit.cell.create(cellMap);
            /** Cellを作る. */
            /** Create a Cell. */
            cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other2_cell_name" + suf);
            cellOther2 = testAs.asCellOwner().unit.cell.create(cellMap);
            /** Cellを作る. */
            /** Create a Cell. */
            cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, "other3_cell_name" + suf);
            cellOther3 = testAs.asCellOwner().unit.cell.create(cellMap);
            /** アカウントを作る. */
            /** Create an Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell(cellOther.getName()).account.create(accountMap, accountPassword);
            /** アカウントを作る. */
            /** Create an Account. */
            accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName + "2");
            account2 = testAs.cell(cellOther2.getName()).account.create(accountMap, accountPassword);

            /** トランスセル取得. */
            /** Fetch Transformer cell. */
            cell = dc.asAccount(cellOther.getName(), account.getName(), accountPassword).cell(testCell.getName());

            /** スキーマ認証. */
            /** Authentication schema. */
            Accessor accessor = dc.getAccessorWithTransCellTokenAndSchemaAuthn(cell.getName(), cell.getAccessToken(),
                    cellOther2.getUrl(), account2.getName(), accountPassword);
            cell = accessor.cell();
            assertFalse(cell.getAccessToken().equals(""));
            assertFalse(cell.getExpiresIn().equals(0));
            assertFalse(cell.getRefreshToken().equals(""));
            assertFalse(cell.getRefreshExpiresIn().equals(0));
            assertFalse(cell.getTokenType().equals(""));

            /** Cell(String)呼出しを行った場合にCell()の認証でエラーとならないこと. */
            /** It is not an error in the authentication of the Cell () when subjected to (String) call. */
            accessor.cell(cellOther.getName());
            accessor.cell();

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Account. */
            if (account != null) {
                testAs.cell(cellOther.getName()).account.del(accountName);
                account = null;
            }
            /** Delete Account. */
            if (account2 != null) {
                testAs.cell(cellOther2.getName()).account.del(account2.getName());
                account2 = null;
            }
            /** Delete Cell. */
            if (cellOther != null) {
                testAs.asCellOwner().unit.cell.del(cellOther.getName());
                cellOther = null;
            }
            /** Delete Cell. */
            if (cellOther2 != null) {
                testAs.asCellOwner().unit.cell.del(cellOther2.getName());
                cellOther2 = null;
            }
            /** Delete Cell. */
            if (cellOther3 != null) {
                testAs.asCellOwner().unit.cell.del(cellOther3.getName());
                cellOther3 = null;
            }
        }
    }

    /**
     * トークン指定.
     */
    /**
     * This method is to test withToken method by Box creation and deletion.
     */
    @Test
    public void withTokenのテスト() {
        try {
            Box box = new Box();
            box.setName("withToken");
            dc.withToken(AbstractCase.masterTokenName).cell().box.create(box);
            dc.withToken(AbstractCase.masterTokenName).cell().box.del(box.getName());
        } catch (DaoException e) {
            fail(e.getMessage());
        }

    }

    /**
     * EntityTypeのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for EntityType. It performs following operations.
     * Create OData Collection, Create EntityType, Create another EntityType with existing name
     * to get 409 exception, Retrieve EntityType, Delete EntityType, Delete OData Collection.
     * @throws DaoException .
     */
    @Test
    public void EntityTypeのCRUD() throws DaoException {
        EntityType entityType = null;
        String etag = null;
        try {
            /** データ作成. */
            /** Create OData Collection. */
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "entity");
            /** ODataコレクション作成. */
            testBox.mkOData("odata");

            /** Create EntityType. */
            entityType = testBox.odata("odata").entityType.create(dataMap);
            log.debug("\n EntityType:[" + entityType.toJSONString() + "]");
            assertEquals("entity", entityType.getName());
            etag = entityType.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);

            /** 同じ名前のBoxを登録して409になることを確認. */
            /** When another EntityType with same name is created, 409 exception should be thrown. */
            try {
                entityType = testBox.odata("odata").entityType.create(dataMap);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            /** 取得. */
            /** Retrieve EntityType. */
            entityType = testBox.odata("odata").entityType.retrieve(entityType.getName());
            assertEquals("entity", entityType.getName());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** EntityTypeを削除. */
            /** Delete EntityType. */
            if (entityType != null) {
                testBox.odata("odata").entityType.del(entityType.getName(), etag);
            }
            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * EntityTypeのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for EntityType. It performs following operations.
     * Create OData Collection, Create EntityType, Delete EntityType, Delete OData Collection.
     * @throws DaoException .
     */
    @Test
    public void EntityTypeのCRUD_オブジェクト渡し() throws DaoException {
        EntityType entityType = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            testBox.mkOData("odata");

            /** Create EntityType. */
            entityType = new EntityType();
            entityType.setName("entity");
            entityType = testBox.odata("odata").entityType.create(entityType);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** EntityTypeを削除. */
            /** Delete EntityType. */
            try {
                if (entityType != null) {
                    testBox.odata("odata").entityType.del(entityType.getName());
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * AssociationEndのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for AssociationEnd. It performs following operations.
     * Create OData Collection, Create EntityType, Create AssociationEnd, Create another AssociationEnd
     * with existing name to get 409 exception, Retrieve AssociationEnd, Delete AssociationEnd,
     * Delete EntityType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void AssociationEndのCRUD() throws DaoException {
        AssociationEnd associationEnd = null;
        String associationName = "associationName";
        String multiplicity = "*";
        String entityTypeName = "keeper";
        EntityType et = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataCol [odata]  create");
            testBox.mkOData("odata");

            /** Create EntityType. */
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", entityTypeName);
            log.debug("\n◆ EntityType Creaate");
            et = testBox.odata("odata").entityType.create(dataMap);

            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, associationName);
            dataMap.put(FIELD_MULTIPLICITY, multiplicity);
            dataMap.put(FIELD_ENTITYTYPE_NAME, entityTypeName);

            /** Create AssociationEnd. */
            log.debug("\n◆ associationEnd Creaate");
            associationEnd = testBox.odata("odata").associationEnd.create(dataMap);
            log.debug("\n AssociationEnd:[" + associationEnd.toJSONString() + "]");
            assertEquals(associationName, associationEnd.getName());
            assertEquals(multiplicity, associationEnd.getMultiplicity());
            assertEquals(entityTypeName, associationEnd.getEntityTypeName());
            log.debug(associationEnd.getHeaderValue(HEADER_KEY_ETAG));

            /** 同じ名前のBoxを登録して409になることを確認. */
            /** When another AssociationEnd with same name is created, 409 exception should be thrown. */
            log.debug("\n◆ associationEnd Creaate 409");
            try {
                associationEnd = testBox.odata("odata").associationEnd.create(dataMap);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            /** 取得. */
            /** Retrieve AssociationEnd. */
            log.debug("\n◆ associationEnd retrieve");
            associationEnd = testBox.odata("odata").associationEnd.retrieve(associationName, entityTypeName);
            assertEquals(associationName, associationEnd.getName());
            assertEquals(multiplicity, associationEnd.getMultiplicity());
            assertEquals(entityTypeName, associationEnd.getEntityTypeName());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** AssociationEndを削除. */
            /** Delete AssociationEnd. */
            log.debug("\n◆ associationEnd delete");
            if (associationEnd != null) {
                testBox.odata("odata").associationEnd.del(associationName, entityTypeName);
            }
            /** EntityType削除. */
            /** Delete EntityType. */
            log.debug("\n◆ EntityType削除");
            if (et != null) {
                testBox.odata("odata").entityType.del(et.getName());
            }

            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataCol [odata]  delete");
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * AssociationEndのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for AssociationEnd. It performs following operations.
     * Create OData Collection, Create EntityType, Create AssociationEnd, Delete AssociationEnd,
     * Delete EntityType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void AssociationEndのCRUD_オブジェクト渡し() throws DaoException {
        AssociationEnd associationEnd = null;
        String associationName = "associationName";
        String multiplicity = "*";
        String entityTypeName = "keeper";
        EntityType et = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataCol [odata]  create");
            testBox.mkOData("odata");

            /** Create EntityType. */
            log.debug("\n◆ EntityType Creaate");
            et = new EntityType();
            et.setName(entityTypeName);
            et = testBox.odata("odata").entityType.create(et);

            /** Create AssociationEnd. */
            log.debug("\n◆ associationEnd Creaate");
            associationEnd = new AssociationEnd();
            associationEnd.setName(associationName);
            associationEnd.setEntityTypeName(entityTypeName);
            associationEnd.setMultiplicity(multiplicity);
            associationEnd = testBox.odata("odata").associationEnd.create(associationEnd);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** AssociationEndを削除. */
            /** Delete AssociationEnd. */
            log.debug("\n◆ associationEnd delete");
            try {
                if (associationEnd != null) {
                    testBox.odata("odata").associationEnd.del(associationName, entityTypeName);
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
            /** EntityType削除. */
            /** Delete EntityType. */
            log.debug("\n◆ EntityType削除");
            try {
                if (et != null) {
                    testBox.odata("odata").entityType.del(et.getName());
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }

            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataCol [odata]  delete");
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * ComplexTypeのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for ComplexType. It performs following operations.
     * Create OData Collection, Create ComplexType, Create another ComplexType
     * with existing name to get 409 exception, Retrieve ComplexType, Delete ComplexType,
     * Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ComplexTypeのCRUD() throws DaoException {
        ComplexType complexType = null;
        String name = "complexName";
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataCol [odata]  create");
            testBox.mkOData("odata");

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, name);

            /** Create ComplexType. */
            log.debug("\n◆ ComplexType Creaate");
            complexType = testBox.odata("odata").complexType.create(dataMap);
            log.debug("\n ComplexType:[" + complexType.toJSONString() + "]");
            assertEquals(name, complexType.getName());
            log.debug(complexType.getHeaderValue(HEADER_KEY_ETAG));

            /** 409になることを確認. */
            /** When another ComplexType with same name is created, 409 exception should be thrown. */
            log.debug("\n◆ ComplexType Creaate 409");
            try {
                complexType = testBox.odata("odata").complexType.create(dataMap);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            /** 取得. */
            /** Retrieve ComplexType. */
            log.debug("\n◆ ComplexType retrieve");
            complexType = testBox.odata("odata").complexType.retrieve(name);
            assertEquals(name, complexType.getName());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** ComplexTypeを削除. */
            /** Delete ComplexType. */
            log.debug("\n◆ ComplexType delete");
            if (complexType != null) {
                testBox.odata("odata").complexType.del(name);
            }

            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataCol [odata]  delete");
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * ComplexTypeのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for ComplexType. It performs following operations.
     * Create OData Collection, Create ComplexType, Delete ComplexType,
     * Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ComplexTypeのCRUD_オブジェクト渡し() throws DaoException {
        ComplexType complexType = null;
        String name = "complexTypeName";
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataCol [odata]  create");
            testBox.mkOData("odata");

            /** Create ComplexType. */
            log.debug("\n◆ ComplexType Creaate");
            complexType = new ComplexType();
            complexType.setName(name);
            complexType = testBox.odata("odata").complexType.create(complexType);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** ComplexTypeを削除. */
            /** Delete ComplexType. */
            log.debug("\n◆ ComplexType delete");
            try {
                if (complexType != null) {
                    testBox.odata("odata").complexType.del(name);
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }

            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataCol [odata]  delete");
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * PropertyのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for Property. It performs following operations.
     * Create OData Collection, Create EntityType, Create Property, Create another Property
     * with existing name to get 409 exception, Retrieve Property, Delete Property, Delete EntityType,
     * Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void PropertyのCRUD() throws DaoException {
        Property property = null;
        String name = "propertyName";
        String entityTypeName = "keeper";
        String type = "Edm.String";
        EntityType et = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataCol [odata]  create");
            testBox.mkOData("odata");

            /** Create EntityType. */
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", entityTypeName);
            log.debug("\n◆ EntityType Creaate");
            et = testBox.odata("odata").entityType.create(dataMap);

            /** Create Property. */
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, name);
            dataMap.put(FIELD_ENTITYTYPE_NAME, entityTypeName);
            dataMap.put(FIELD_TYPE_NAME, type);

            log.debug("\n◆ Property Creaate");
            property = testBox.odata("odata").property.create(dataMap);
            log.debug("\n ComplexType:[" + property.toJSONString() + "]");
            assertEquals(name, property.getName());
            assertEquals(entityTypeName, property.getEntityTypeName());
            assertEquals(type, property.getType());
            assertEquals(true, property.getNullable());
            assertEquals(null, property.getDefaultValue());
            assertEquals("None", property.getCollectionKind());
            assertEquals(false, property.getIsKey());
            assertEquals(null, property.getUniqueKey());
            log.debug(property.getHeaderValue(HEADER_KEY_ETAG));

            /** 409になることを確認. */
            /** When another Property with same name is created, 409 exception should be thrown. */
            log.debug("\n◆ Property Creaate 409");
            try {
                property = testBox.odata("odata").property.create(dataMap);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            /** 取得. */
            /** Retrieve Property. */
            log.debug("\n◆ Property retrieve");
            property = testBox.odata("odata").property.retrieve(name, entityTypeName);
            assertEquals(name, property.getName());
            assertEquals(entityTypeName, property.getEntityTypeName());
            assertEquals(type, property.getType());
            assertEquals(true, property.getNullable());
            assertEquals(null, property.getDefaultValue());
            assertEquals("None", property.getCollectionKind());
            assertEquals(false, property.getIsKey());
            assertEquals(null, property.getUniqueKey());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** 削除. */
            /** Delete Property. */
            log.debug("\n◆ Property delete");
            if (property != null) {
                testBox.odata("odata").property.del(name, entityTypeName);
            }
            /** EntityType削除. */
            /** Delete EntityType. */
            log.debug("\n◆ EntityType削除");
            if (et != null) {
                testBox.odata("odata").entityType.del(et.getName());
            }

            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataCol [odata]  delete");
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * PropertyのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for Property. It performs following operations.
     * Create OData Collection, Create EntityType, Create Property, Delete Property, Delete EntityType,
     * Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void PropertyのCRUD_オブジェクト渡し() throws DaoException {
        Property property = null;
        String name = "propertyName";
        String entityTypeName = "keeper";
        String type = "Edm.String";
        EntityType et = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataCol [odata]  create");
            testBox.mkOData("odata");

            /** Create EntityType. */
            log.debug("\n◆ EntityType Creaate");
            et = new EntityType();
            et.setName(entityTypeName);
            et = testBox.odata("odata").entityType.create(et);

            /** Create Property. */
            log.debug("\n◆ Property Creaate");
            property = new Property();
            property.setName(name);
            property.setEntityTypeName(entityTypeName);
            property.setType(type);
            property = testBox.odata("odata").property.create(property);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Propertyを削除. */
            /** Delete Property. */
            log.debug("\n◆ Property delete");
            try {
                if (property != null) {
                    testBox.odata("odata").property.del(name, entityTypeName);
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
            /** EntityType削除. */
            /** Delete EntityType. */
            log.debug("\n◆ EntityType削除");
            try {
                if (et != null) {
                    testBox.odata("odata").entityType.del(et.getName());
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }

            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataCol [odata]  delete");
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * ComplexTypePropertyのCRUDテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for ComplexTypeProperty. It performs following operations.
     * Create OData Collection, Create ComplexType, Create ComplexTypeProperty, Create another ComplexTypeProperty
     * with existing name to get 409 exception, Retrieve ComplexTypeProperty, Delete ComplexTypeProperty,
     * Delete ComplexType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ComplexTypePropertyのCRUD() throws DaoException {
        ComplexTypeProperty compProp = null;
        String name = "compPropertyName";
        String complexTypeName = "complexName";
        String type = "Edm.String";
        ComplexType complexType = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataCol [odata]  create");
            testBox.mkOData("odata");

            /** Create ComplexType. */
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", complexTypeName);
            log.debug("\n◆ ComplexType Creaate");
            complexType = testBox.odata("odata").complexType.create(dataMap);

            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, name);
            dataMap.put(FIELD_COMPLEXTYPE_NAME, complexTypeName);
            dataMap.put(FIELD_TYPE_NAME, type);

            /** Create ComplexTypeProperty. */
            log.debug("\n◆ ComplexTypeProperty Creaate");
            compProp = testBox.odata("odata").complexTypeProperty.create(dataMap);
            log.debug("\n ComplexTypeProperty:[" + compProp.toJSONString() + "]");
            assertEquals(name, compProp.getName());
            assertEquals(complexTypeName, compProp.getComplexTypeName());
            assertEquals(type, compProp.getType());
            assertEquals(true, compProp.getNullable());
            assertEquals(null, compProp.getDefaultValue());
            assertEquals("None", compProp.getCollectionKind());
            log.debug(compProp.getHeaderValue(HEADER_KEY_ETAG));

            /** 409になることを確認. */
            /** When another ComplexTypeProperty with same name is created, 409 exception should be thrown. */
            log.debug("\n◆ ComplexTypeProperty Creaate 409");
            try {
                compProp = testBox.odata("odata").complexTypeProperty.create(dataMap);
                fail();
            } catch (DaoException e) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e.getCode()));
            }

            /** 取得. */
            /** Retrieve ComplexTypeProperty. */
            log.debug("\n◆ ComplexTypeProperty retrieve");
            compProp = testBox.odata("odata").complexTypeProperty.retrieve(name, complexTypeName);
            assertEquals(name, compProp.getName());
            assertEquals(complexTypeName, compProp.getComplexTypeName());
            assertEquals(type, compProp.getType());
            assertEquals(true, compProp.getNullable());
            assertEquals(null, compProp.getDefaultValue());
            assertEquals("None", compProp.getCollectionKind());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** 削除. */
            /** Delete ComplexTypeProperty. */
            log.debug("\n◆ ComplexTypeProperty delete");
            if (compProp != null) {
                testBox.odata("odata").complexTypeProperty.del(name, complexTypeName);
            }
            /** ComplexType削除. */
            /** Delete ComplexType. */
            log.debug("\n◆ ComplexType削除");
            if (complexType != null) {
                testBox.odata("odata").complexType.del(complexType.getName());
            }

            /** ODataコレクションを削除. */
            /** Delete OdataCollection. */
            log.debug("\n◆ ODataCol [odata]  delete");
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * ComplexTypePropertyのCRUDテスト(オブジェクト渡し).
     * @throws DaoException DAO例外
     */
    /**
     * This method performs CRUD test for ComplexTypeProperty. It performs following operations.
     * Create OData Collection, Create ComplexType, Create ComplexTypeProperty, Delete ComplexTypeProperty,
     * Delete ComplexType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ComplexTypePropertyのCRUD_オブジェクト渡し() throws DaoException {
        ComplexTypeProperty compProp = null;
        String name = "compPropertyName";
        String complexTypeName = "complexName";
        String type = "Edm.String";
        ComplexType complexType = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataCol [odata]  create");
            testBox.mkOData("odata");

            /** Create ComplexType. */
            log.debug("\n◆ ComplexType Creaate");
            complexType = new ComplexType();
            complexType.setName(complexTypeName);
            complexType = testBox.odata("odata").complexType.create(complexType);

            /** Create ComplexTypeProperty. */
            log.debug("\n◆ ComplexTypeProperty Creaate");
            compProp = new ComplexTypeProperty();
            compProp.setName(name);
            compProp.setComplexTypeName(complexTypeName);
            compProp.setType(type);
            compProp = testBox.odata("odata").complexTypeProperty.create(compProp);
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** ComplexTypePropertyを削除. */
            /** Delete ComplexTypeProperty. */
            log.debug("\n◆ ComplexTypeProperty delete");
            try {
                if (compProp != null) {
                    testBox.odata("odata").complexTypeProperty.del(name, complexTypeName);
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
            /** ComplexType削除. */
            /** Delete ComplexType. */
            log.debug("\n◆ ComplexType削除");
            try {
                if (complexType != null) {
                    testBox.odata("odata").complexType.del(complexType.getName());
                }
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }

            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataCol [odata]  delete");
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * RelationからRoleへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Relation to Role. It performs following operations.
     * Create Relation, Create Role, Create Link between Relation and Role, Create another
     * link between same Relation and Role to get 409, Unlink Role and Relation, Call Unlink Role and Relation
     * again to get 404, Delete Relation, Delete Role.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RelationからRoleへのlink() throws DaoException {
        Relation relation = null;
        Role role = null;
        try {
            /** Relationを作る. */
            /** Create Relation. */
            relation = createRelationCell("relation001");
            /** Roleを作る. */
            /** Create Role. */
            role = createRoleCell("role001");
            /** Relationのリンク (204). */
            /** Create Link between Relation and Role to get status 204. */
            relation.role.link(role);
            /** Relationのリンク (409 になること). */
            /** Create another link between same relation and role to get error status 409. */
            try {
                relation.role.link(role);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** Relationのアンリンク (204). */
            /** Unlink Role and Relation to get 204. */
            relation.role.unLink(role);
            /** Relationのアンリンク (404). */
            /** Call unlink again between same relation and role to get error status 404. */
            try {
                relation.role.unLink(role);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** Relationを削除. */
            /** Delete Relation. */
            if (relation != null) {
                testCell.relation.del(relation.getName());
            }
            /** Roleを削除. */
            /** Delete Role. */
            if (role != null) {
                testCell.role.del(role.getName());
            }
        }
    }

    /**
     * RelationからRole（複合キー）へのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Relation to Role(composite key). It performs following operations.
     * Create Relation, Create Role, Create Link between Relation and Role, Create another
     * link between same Relation and Role to get 409, Unlink Role and Relation, Call Unlink Role and Relation
     * again to get 404, Delete Relation, Delete Role.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RelationからRole複合キーへのlink() throws DaoException {
        Relation relation = null;
        Role role = null;
        try {
            /** Relationを作る. */
            /** Create Relation. */
            relation = createRelationBox("relation001");
            /** Roleを作る. */
            /** Create Role. */
            role = createRoleBox("role001");
            /** Relationのリンク (204). */
            /** Create Link between Relation and Role to get status 204. */
            relation.role.link(role);
            /** Relationのリンク (409 になること). */
            /** Create another link between same relation and role to get error status 409. */
            try {
                relation.role.link(role);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** Relationのアンリンク (204). */
            /** Unlink Role and Relation to get 204. */
            relation.role.unLink(role);
            /** Relationのアンリンク (404). */
            /** Call unlink again between same relation and role to get error status 404. */
            try {
                relation.role.unLink(role);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** Relationを削除. */
            /** Delete Relation. */
            if (relation != null) {
                testCell.relation.del(relation.getName(), relation.getBoxName());
            }
            /** Roleを削除. */
            /** Delete Role. */
            if (role != null) {
                testCell.role.del(role.getName(), role.getBoxName());
            }
        }
    }

    /**
     * RelationからExtCellへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Relation to ExtCell. It performs following operations.
     * Create Relation, Create ExtCell, Create Link between Relation and ExtCell, Create another
     * link between same Relation and ExtCell to get 409, Unlink ExtCell and Relation, Call Unlink ExtCell
     * and Relation again to get 404, Delete Relation, Delete ExtCell, Delete Cell.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RelationからExtCellへのlink() throws DaoException {
        Relation relation = null;
        Cell cell = null;
        ExtCell extCell = null;
        try {
            /** Relationを作る. */
            /** Create Relation. */
            relation = createRelationCell("relation001");
            /** ExtCellを作る. */
            /** Create ExtCell. */
            cell = createCell("cell001" + suf);
            extCell = createExtCell(cell.getUrl());
            /** Relationのリンク (204). */
            /** Create Link between Relation and ExtCell to get status code 204. */
            relation.extCell.link(extCell);
            /** Relationのリンク (409 になること). */
            /** Create another link between same relation and ExtCell to get error status 409. */
            try {
                relation.extCell.link(extCell);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** Relationのアンリンク (204). */
            /** Unlink ExtCell and Relation to get 204. */
            relation.extCell.unLink(extCell);
            /** Relationのアンリンク (404). */
            /** Call unlink again between same relation and ExtCell to get error status 404. */
            try {
                relation.extCell.unLink(extCell);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** Relationを削除. */
            /** Delete Relation. */
            if (relation != null) {
                testCell.relation.del(relation.getName());
            }
            /** ExtCellを削除. */
            /** Delete Extcell. */
            if (extCell != null) {
                testCell.extCell.del(extCell.getUrl());
            }
            /** Delete Cell. */
            if (cell != null) {
                testAs.asCellOwner().unit.cell.del(cell.getName());
            }
        }
    }

    /**
     * ExtCellからRelationへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Relation to ExtCell. It performs following operations.
     * Create Relation, Create ExtCell, Create Link between Relation and ExtCell, Create another
     * link between same Relation and ExtCell to get 409, Unlink ExtCell and Relation, Call Unlink ExtCell
     * and Relation again to get 404, Delete Relation, Delete ExtCell, Delete Cell.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ExtCellからRelationへのlink() throws DaoException {
        Relation relation = null;
        Cell cell = null;
        ExtCell extCell = null;
        try {
            /** Relationを作る. */
            /** Create Relation. */
            relation = createRelationCell("relation001");
            /** ExtCellを作る. */
            /** Create ExtCell. */
            cell = createCell("cell001" + suf);
            extCell = createExtCell(cell.getUrl());
            /** ExtCellのリンク (204). */
            /** Create Link between Relation and ExtCell to get status code 204. */
            extCell.relation.link(relation);
            /** ExtCellのリンク (409 になること). */
            /** Create another link between same relation and ExtCell to get error status 409. */
            try {
                extCell.relation.link(relation);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** ExtCellのアンリンク (204). */
            /** Unlink ExtCell and Relation to get 204. */
            extCell.relation.unLink(relation);
            /** ExtCellのアンリンク (404). */
            /** Call unlink again between same relation and ExtCell to get error status 404. */
            try {
                extCell.relation.unLink(relation);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** Relationを削除. */
            /** Delete Relation. */
            if (relation != null) {
                testCell.relation.del(relation.getName());
            }
            /** ExtCellを削除. */
            /** Delete ExtCell. */
            if (extCell != null) {
                testCell.extCell.del(extCell.getUrl());
            }
            /** Delete Cell. */
            if (cell != null) {
                testAs.asCellOwner().unit.cell.del(cell.getName());
            }
        }
    }

    /**
     * ExtCellからRoleへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Role to ExtCell. It performs following operations.
     * Create Role, Create ExtCell, Create Link between Role and ExtCell, Create another
     * link between same Role and ExtCell to get 409, Unlink ExtCell and Role, Call Unlink ExtCell
     * and Role again to get 404, Delete Role, Delete ExtCell, Delete Cell.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ExtCellからRoleへのlink() throws DaoException {
        Role role = null;
        Cell cell = null;
        ExtCell extCell = null;
        try {
            /** Roleを作る. */
            /** Create Role. */
            role = createRoleBox("role001");
            /** ExtCellを作る. */
            /** Create ExtCell. */
            cell = createCell("cell001" + suf);
            extCell = createExtCell(cell.getUrl());
            /** ExtCellのリンク (204). */
            /** Create Link between Role and ExtCell to get status 204. */
            extCell.role.link(role);
            /** ExtCellのリンク (409 になること). */
            /** Create another link between same Role and ExtCell to get error status 409. */
            try {
                extCell.role.link(role);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** ExtCellのアンリンク (204). */
            /** Unlink ExtCell and Role to get 204. */
            extCell.role.unLink(role);
            /** ExtCellのアンリンク (404). */
            /** Call unlink again between same Role and ExtCell to get error status 404. */
            try {
                extCell.role.unLink(role);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** Roleを削除. */
            /** Delete Role. */
            if (role != null) {
                testCell.role.del(role.getName(), role.getBoxName());
            }
            /** ExtCellを削除. */
            /** Delete ExtCell. */
            if (extCell != null) {
                testCell.extCell.del(extCell.getUrl());
            }
            /** Delete Cell. */
            if (cell != null) {
                testAs.asCellOwner().unit.cell.del(cell.getName());
            }
        }
    }

    /**
     * RoleからRelationへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Role to Relation. It performs following operations.
     * Create Role, Create Relation, Create Link between Role and Relation, Create another
     * link between same Role and Relation to get 409, Unlink Relation and Role, Call Unlink Relation
     * and Role again to get 404, Delete Role, Delete Relation.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RoleからRelationへのlink() throws DaoException {
        Relation relation = null;
        Role role = null;
        try {
            /** Relationを作る. */
            /** Create Relation. */
            relation = createRelationCell("relation001");
            /** Roleを作る. */
            /** Create Role. */
            role = createRoleCell("role001");
            /** Relationのリンク (204). */
            /** Create Link between Role and Relation to get status 204. */
            role.relation.link(relation);
            /** Relationのリンク (409 になること). */
            /** Create another link between same Role and Relation to get error status 409. */
            try {
                role.relation.link(relation);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** Relationのアンリンク (204). */
            /** Unlink Relation and Role to get 204. */
            role.relation.unLink(relation);
            /** Relationのアンリンク (404). */
            /** Call unlink again between same Role and Relation to get error status 404. */
            try {
                role.relation.unLink(relation);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** Relationを削除. */
            /** Delete Relation. */
            if (relation != null) {
                testCell.relation.del(relation.getName());
            }
            /** Roleを削除. */
            /** Delete Role. */
            if (role != null) {
                testCell.role.del(role.getName());
            }
        }
    }

    /**
     * ExtRoleからRoleへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Role to ExtRole. It performs following operations.
     * Create Relation, Create ExtRole, Create Role, Create Link between Role and ExtRole, Create another
     * link between same Role and ExtRole to get 409, Unlink ExtRole and Role, Call Unlink ExtRole
     * and Role again to get 404, Delete ExtRole, Delete Relation, Delete Role.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ExtRoleからRoleへのlink() throws DaoException {
        Relation relation = null;
        Role role = null;
        ExtRole extRole = null;
        String roleUrl = "http://extRole.jp";
        try {
            /** Create Relation. */
            log.debug("\n◆ Relation作成");
            relation = createRelationCell("relation001");

            /** Create ExtRole. */
            log.debug("\n◆ ExtRole作成");
            HashMap<String, Object> json = new HashMap<String, Object>();
            json.put("ExtRole", roleUrl);
            json.put("_Relation.Name", relation.getName());
            json.put("_Relation._Box.Name", relation.getBoxName());
            extRole = testCell.extRole.create(json);

            /** Create Role. */
            log.debug("\n◆ Role作成");
            role = createRoleCell("role001");

            /** Create Link between Role and ExtRole to get status 204. */
            log.debug("\n◆ ExtRoleとRoleのリンク (204)");
            extRole.role.link(role);
            /** Create another link between same Role and ExtRole to get error status 409. */
            log.debug("\n◆ ExtRoleとRoleのリンク (409)");
            try {
                extRole.role.link(role);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** Unlink ExtRole and Role to get 204. */
            log.debug("\n◆ ExtRoleとRoleのアンリンク (204)");
            extRole.role.unLink(role);
            /** Call unlink again between same Role and ExtRole to get error status 404. */
            log.debug("\n◆ ExtRoleとRoleのアンリンク (404)");
            try {
                extRole.role.unLink(role);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** Delete ExtRole. */
            log.debug("\n◆ ExtRole削除");
            if (extRole != null) {
                testCell.extRole.del(roleUrl, extRole.getRelationName(), extRole.getRelationBoxName());
            }
            /** Delete Relation. */
            log.debug("\n◆ Relation削除");
            if (relation != null) {
                testCell.relation.del(relation.getName());
            }
            /** Delete Role. */
            log.debug("\n◆ Role削除");
            if (role != null) {
                testCell.role.del(role.getName());
            }
        }
    }

    /**
     * RoleからExtCellへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Role to ExtCell. It performs following operations.
     * Create ExtCell, Create Role, Create Link between Role and ExtCell, Create another
     * link between same Role and ExtCell to get 409, Unlink ExtCell and Role, Call Unlink ExtCell
     * and Role again to get 404, Delete ExtCell, Delete Cell, Delete Role.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RoleからExtCellへのlink() throws DaoException {
        Cell cell = null;
        ExtCell extcell = null;
        Role role = null;
        try {
            /** ExtCellを作る. */
            /** Create ExtCell. */
            cell = createCell("cell001" + suf);
            extcell = createExtCell(cell.getUrl());
            /** Roleを作る. */
            /** Create Role. */
            role = createRoleCell("role001");
            /** Relationのリンク (204). */
            /** Create Link between Role and ExtCell to get status 204. */
            role.extCell.link(extcell);
            /** Relationのリンク (409 になること). */
            /** Create another link between same Role and ExtCell to get error status 409. */
            try {
                role.extCell.link(extcell);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** Relationのアンリンク (204). */
            /** Unlink ExtCell and Role to get 204. */
            role.extCell.unLink(extcell);
            /** Relationのアンリンク (404). */
            /** Call unlink again between same Role and ExtCell to get error status 404. */
            try {
                role.extCell.unLink(extcell);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** ExtCellを削除. */
            /** Delete ExtCell. */
            if (extcell != null) {
                testCell.extCell.del(extcell.getUrl());
            }
            /** Delete Cell. */
            if (cell != null) {
                testAs.asCellOwner().unit.cell.del(cell.getName());
            }
            /** Roleを削除. */
            /** Delete Role. */
            if (role != null) {
                testCell.role.del(role.getName());
            }
        }
    }

    /**
     * AssociationEndのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Role to ExtCell. It performs following operations.
     * Create OData Collection, Create EntityType One, Create Association End One,
     * Create EntityType Two, Create AssociationEnd Two, Create Link between two Association Ends, Create another
     * link between same AssociationEnds to get 409, Unlink two AssociationEnds, Call Unlink again between
     * the same two AssociationEnds to get 404, Delete two AssociationEnds, Delete two EntityTypes,
     * Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void AssociationEndのlink() throws DaoException {

        AssociationEnd associationEndKeeper = null;
        AssociationEnd associationEndAnimal = null;
        String associationNameKeeper = "associationNameKeeper";
        String associationNameAnimal = "associationNameAnimal";
        String multiplicity = "*";
        String entityTypeNameKeeper = "keeper";
        String entityTypeNameAnimal = "animal";
        EntityType etKeeper = null;
        EntityType etAnimal = null;
        try {
            /** AssociationEnd (Keeper)作成. */
            /** Create Odata Collection. */
            testBox.mkOData("odata");

            /** Create EntityType. */
            log.debug("\n◆ EntityType登録");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, entityTypeNameKeeper);
            etKeeper = testBox.odata("odata").entityType.create(dataMap);

            /** Create AssociationEnd (Keeper). */
            HashMap<String, Object> assocMap = new HashMap<String, Object>();
            assocMap.put(FIELD_NAME, associationNameKeeper);
            assocMap.put(FIELD_MULTIPLICITY, multiplicity);
            assocMap.put(FIELD_ENTITYTYPE_NAME, entityTypeNameKeeper);
            associationEndKeeper = testBox.odata("odata").associationEnd.create(assocMap);

            /** Create another EntityType. */
            log.debug("\n◆ EntityType登録");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, entityTypeNameAnimal);
            etAnimal = testBox.odata("odata").entityType.create(dataMap);

            /** AssociationEnd (Animal)作成. */
            /** Create AssociationEnd (Animal). */
            assocMap = new HashMap<String, Object>();
            assocMap.put(FIELD_NAME, associationNameAnimal);
            assocMap.put(FIELD_MULTIPLICITY, multiplicity);
            assocMap.put(FIELD_ENTITYTYPE_NAME, entityTypeNameAnimal);
            associationEndAnimal = testBox.odata("odata").associationEnd.create(assocMap);

            /** AssociationEndのリンク (204). */
            /** Create link between two AssociationEnds to get status code 204. */
            associationEndKeeper.associationEnd.link(associationEndAnimal);
            /** Relationのリンク (409 になること). */
            /** Create link between the same two AssociationEnds to get error status code 409. */
            try {
                associationEndKeeper.associationEnd.link(associationEndAnimal);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** Relationのアンリンク (204). */
            /** Call unlink between two AssociationEnds to get status code 204. */
            associationEndKeeper.associationEnd.unLink(associationEndAnimal);
            /** Relationのアンリンク (404). */
            /** Call unlink again between the same two AssociationEnds to get error status code 409. */
            try {
                associationEndKeeper.associationEnd.unLink(associationEndAnimal);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** AssociationEndを削除. */
            /** Delete the two AssociationEnds. */
            if (associationEndKeeper != null) {
                testBox.odata("odata").associationEnd.del(associationNameKeeper, entityTypeNameKeeper);
            }
            if (associationEndAnimal != null) {
                testBox.odata("odata").associationEnd.del(associationNameAnimal, entityTypeNameAnimal);
            }

            /** EntityTypeを削除. */
            /** Delete the two EntityTypes. */
            if (etKeeper != null) {
                testBox.odata("odata").entityType.del(entityTypeNameKeeper);
            }
            if (etAnimal != null) {
                testBox.odata("odata").entityType.del(entityTypeNameAnimal);
            }
            /** ODataコレクションを削除. */
            /** Delete OadatCollection. */
            try {
                testBox.del("odata");
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        }
    }

    /**
     * RoleからAccountへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Role to Account. It performs following operations.
     * Create Account, Create Role, Create Link between Role and Account, Create another
     * link between same Role and Account to get 409, Unlink Account and Role, Call Unlink Account
     * and Role again to get 404, Delete Account, Delete Role.
     * @throws DaoException Exception thrown
     */
    @Test
    public void RoleからAccountへのlink() throws DaoException {
        Account account = null;
        Role role = null;
        try {
            /** アカウントを作る. */
            /** Create Account. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);
            account = testAs.cell().account.create(accountMap, accountPassword);
            /** Roleを作る. */
            /** Create Role. */
            role = createRoleCell("role001");
            /** Relationのリンク (204). */
            /** Create Link between Role and Account to get status 204. */
            role.account.link(account);
            /** Relationのリンク (409 になること). */
            /** Create another link between same Role and Account to get error status 409. */
            try {
                role.account.link(account);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_CONFLICT, Integer.parseInt(e1.getCode()));
            }
            /** Relationのアンリンク (204). */
            /** Unlink Account and Role to get 204. */
            role.account.unLink(account);
            /** Relationのアンリンク (404). */
            /** Call unlink again between same Role and Account to get error status 404. */
            try {
                role.account.unLink(account);
            } catch (DaoException e1) {
                assertEquals(HttpStatus.SC_NOT_FOUND, Integer.parseInt(e1.getCode()));
            }
        } finally {
            /** Delete Account. */
            if (account != null) {
                testAs.cell().account.del(accountName);
                account = null;
            }
            /** Roleを削除. */
            /** Delete Role. */
            if (role != null) {
                testCell.role.del(role.getName());
            }
        }
    }

    /**
     * BoxからRoleへのlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the link test of Role to Box. It performs following operations.
     * Create Role, Create Link between Role and Box, Unlink Box and Role, Delete Role.
     * @throws DaoException Exception thrown
     */
    @Test
    public void BoxからRoleへのlinkテスト() throws DaoException {
        Role role = null;
        try {
            /** Roleを作る. */
            /** Create Role. */
            role = createRoleCell("role001");
            /** Create Link between Role and Box to get status 204. */
            testBox2.role.link(role);
            role = testCell.role.retrieve("role001", testBox2.getName());
            /** Unlink Box and Role to get 204. */
            testBox2.role.unLink(role);
        } finally {
            /** Roleを削除. */
            /** Delete Role. */
            if (role != null) {
                testCell.role.del(role.getName());
            }
        }
    }

    /**
     * ODataユーザデータの登録.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the test of OData registration of user data. It performs following operations.
     * Create OData Collection, Create EntityType, Create two User Data, Get user data, Update user data,
     * Execute various query commands, Delete two User Data, Delete EntityType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void ユーザデータのCRUDテスト() throws DaoException {
        String pochi = null;
        String tama = null;
        EntityType et = null;
        ArrayList<Object> results;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataコレクション作成");
            testBox.mkOData("odata");

            /** Create EntityType. */
            log.debug("\n◆ EntityType登録");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "animal");
            et = testBox.odata("odata").entityType.create(dataMap);

            /** POST用のデータ作成. */
            /** Creating data for POST request. */
            log.debug("\n◆ ユーザーデータ登録（１件目)");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "pochi");
            dataMap.put("species", "cat");
            dataMap.put("weight", 50);
            dataMap.put("food", "肉");
            /** POST登録. */
            /** POST registration for user data. */
            HashMap<String, Object> resMap = testBox.odata("odata").entitySet("animal").createAsJson(dataMap);
            log.debug(JsonUtils.toJsonString(resMap));
            pochi = (String) resMap.get("__id");

            /** POST用のデータ作成. */
            /** Creating data for POST request. */
            log.debug("\n◆ ユーザーデータ登録（2件目)");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "tama");
            dataMap.put("species", "cat");
            dataMap.put("weight", 20);
            dataMap.put("food", "魚");
            /** POST登録. */
            /** POST registration for user data. */
            resMap = testBox.odata("odata").entitySet("animal").createAsJson(dataMap);
            log.debug(JsonUtils.toJsonString(resMap));
            tama = (String) resMap.get("__id");

            /** 登録したデータを取得. */
            /** Get the data registered. */
            log.debug("\n◆ 登録したユーザーデータを取得(１件取得)");
            resMap = testBox.odata("odata").entitySet("animal").retrieveAsJson(pochi);
            log.debug(JsonUtils.toJsonString(resMap));

            /** 登録したデータを更新. */
            /** Update data registered. */
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "pochi");
            dataMap.put("species", "dog");
            dataMap.put("weight", 50);
            dataMap.put("food", "肉");
            log.debug("\n◆ ユーザーデータを更新");
            testBox.odata("odata").entitySet("animal").update(pochi, dataMap, "*");

            /** 一覧取得($top). */
            /** List acquisition ($ top) fetching first record. */
            log.debug("\n◆ 登録したユーザーデータ一覧を取得(１件($top=1)のみ取得)");
            resMap = testBox.odata("odata").entitySet("animal").query().top(1).run();
            log.debug(JsonUtils.toJsonString(resMap));

            /** 一覧取得($filter). */
            /** List acquisition ($ filter) fetching only one property cat. */
            log.debug("\n◆ 一覧を取得(species eq cat)");
            resMap = testBox.odata("odata").entitySet("animal").query().filter("species eq 'cat'").run();
            results = (ArrayList<Object>) this.getResultObject(resMap);
            assertEquals(1, results.size());
            log.debug(JsonUtils.toJsonString(resMap));

            /** 一覧取得($filter). */
            /** List acquisition ($ filter) fetching only one property dog. */
            log.debug("\n◆ 一覧を取得(species eq dog)");
            resMap = testBox.odata("odata").entitySet("animal").query().filter("species eq 'dog'").run();
            results = (ArrayList<Object>) this.getResultObject(resMap);
            assertEquals(1, results.size());
            log.debug(JsonUtils.toJsonString(resMap));

            /** SKIP. */
            /** List acquisition ($ skip) fetching all records except first record. */
            log.debug("\n◆ 一覧取得 skip=1");
            resMap = testBox.odata("odata").entitySet("animal").query().skip(1).run();
            results = (ArrayList<Object>) this.getResultObject(resMap);
            assertEquals(1, results.size());
            log.debug(JsonUtils.toJsonString(resMap));

            /** inlinecount. */
            /** Fetching the total number of user data ($ inlinecount = allpages). */
            log.debug("\n◆ 一覧取得 inlinecount=allpages");
            resMap = testBox.odata("odata").entitySet("animal").query().inlinecount("allpages").run();
            assertNotNull(((HashMap<String, Object>) resMap.get("d")).get("__count"));
            log.debug(JsonUtils.toJsonString(resMap));

            /** Fetching the no user data ($ inlinecount = none). */
            log.debug("\n◆ 一覧取得 inlinecount=none");
            resMap = testBox.odata("odata").entitySet("animal").query().inlinecount("none").run();
            assertNull(((HashMap<String, Object>) resMap.get("d")).get("__count"));
            log.debug(JsonUtils.toJsonString(resMap));

            /** List acquisition ($ filter) fetching user data having weight greater than 30. */
            log.debug("\n◆ 一覧取得 filter(gt)");
            resMap = testBox.odata("odata").entitySet("animal").query().filter("weight gt 30").run();
            results = (ArrayList<Object>) this.getResultObject(resMap);
            assertEquals(1, results.size());
            log.debug(JsonUtils.toJsonString(resMap));

            /** List acquisition ($ filter) fetching user data having property species starting with c. */
            log.debug("\n◆ 一覧取得 startswith");
            resMap = testBox.odata("odata").entitySet("animal").query().filter("startswith(species,'c')").run();
            results = (ArrayList<Object>) this.getResultObject(resMap);
            assertEquals(1, results.size());
            log.debug(JsonUtils.toJsonString(resMap));

            /** List obtaining full-text search q = tama. */
            log.debug("\n◆ 一覧取得 全文検索 q=tama");
            resMap = testBox.odata("odata").entitySet("animal").query().q("tama").run();
            results = (ArrayList<Object>) this.getResultObject(resMap);
            assertEquals(1, results.size());
            log.debug(JsonUtils.toJsonString(resMap));

            /** List obtaining full-text search q = meat. */
            log.debug("\n◆ 一覧取得 全文検索 q=肉");
            resMap = testBox.odata("odata").entitySet("animal").query().q("肉").run();
            results = (ArrayList<Object>) this.getResultObject(resMap);
            assertEquals(1, results.size());
            log.debug(JsonUtils.toJsonString(resMap));

            // // ｓｅｌｅｃｔ
            // log.debug("\n◆ 一覧取得 ｓｅｌｅｃｔ=name");
            // json = testBox.odata("odata").entitySet("animal").query(). select("name").run();
            // log.debug(json.toJSONString());

            // // orderby
            // log.debug("\n◆ 一覧取得 orderby=name");
            // json = testBox.odata("odata").entitySet("animal").query(). orderby("name").run();
            // log.debug(json.toJSONString());
            //
            // log.debug("\n◆ 一覧取得 orderby=name desc");
            // json = testBox.odata("odata").entitySet("animal").query(). orderby("name desc").run();
            // log.debug(json.toJSONString());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {

            /** データ削除. */
            /** Delete data. */
            log.debug("\n◆ 登録したユーザーデータを削除");
            log.debug(pochi + "," + tama);
            if (pochi != null) {
                testBox.odata("odata").entitySet("animal").del(pochi);
            }
            if (tama != null) {
                testBox.odata("odata").entitySet("animal").del(tama);
            }
            /** EntityType削除. */
            /** Delete EntityType. */
            log.debug("\n◆ EntityType削除");
            if (et != null) {
                testBox.odata("odata").entityType.del("animal");
            }
            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataコレクション削除");
            testBox.del("odata");
        }
    }

    /**
     * ユーザデータのEntity返却テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the return test of user data. It performs following operations.
     * Create OData Collection, Create EntityType, Create User Data, Get user data, Update user data,
     * Get updated user data, Merge user data, Delete User Data, Delete EntityType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void ユーザデータのEntity返却テスト() throws DaoException {
        String pochi = null;
        String tama = null;
        EntityType et = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataコレクション作成");
            testBox.mkOData("odata");

            /** Create EntityType. */
            log.debug("\n◆ EntityType登録");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "animal");
            et = testBox.odata("odata").entityType.create(dataMap);

            /** POST用のデータ作成. */
            /** Creating data for POST request. */
            log.debug("\n◆ ユーザーデータ登録（１件目)");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "pochi");
            dataMap.put("species", "cat");
            dataMap.put("weight", 50);
            dataMap.put("food", "肉");
            /** POST登録. */
            /** POST registration. */
            Entity entity = testBox.odata("odata").entitySet("animal").createAsEntity(dataMap);
            HashMap<String, Object> resMap = entity.getBody();
            log.debug(JsonUtils.toJsonString(resMap));
            pochi = (String) resMap.get("__id");
            String etag = entity.getHeaderValue(HEADER_KEY_ETAG);

            /** 登録したデータを取得. */
            /** Get the data registered. */
            log.debug("\n◆ 登録したユーザーデータを取得(１件取得)");
            resMap = testBox.odata("odata").entitySet("animal").retrieveAsJson(pochi);
            log.debug(JsonUtils.toJsonString(resMap));

            /** 登録したデータを更新. */
            /** Update data registered. */
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "pochi");
            dataMap.put("species", "dog");
            dataMap.put("weight", 50);
            dataMap.put("food", "肉");
            log.debug("\n◆ ユーザーデータを更新");
            Entity updateEntity = testBox.odata("odata").entitySet("animal").updateAsEntity(pochi, dataMap, etag);
            String updateEtag = updateEntity.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(updateEtag);

            /** 更新したデータを取得. */
            /** Get the updated data. */
            log.debug("\n◆ 更新したユーザーデータを取得(１件取得)");
            resMap = testBox.odata("odata").entitySet("animal").retrieveAsJson(pochi);
            log.debug(JsonUtils.toJsonString(resMap));

            /** 更新したデータを取得. */
            /** Get the updated data. */
            log.debug("\n◆ 更新したユーザーデータを取得(１件取得)");
            entity = testBox.odata("odata").entitySet("animal").retrieveAsEntity(pochi);
            log.debug(JsonUtils.toJsonString(entity.getBody()));
            log.debug(entity.getHeaderValue(HEADER_KEY_ETAG));

            /** Merge user data. */
            log.debug("\n◆ 部分更新");
            dataMap = new HashMap<String, Object>();
            dataMap.put("weight", 52);
            Entity mergeEntity = testBox.odata("odata").entitySet("animal").mergeAsEntity(pochi, dataMap, updateEtag);
            log.debug(mergeEntity.getHeaderValue(HEADER_KEY_ETAG));

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** データ削除. */
            /** Delete User Data. */
            log.debug("\n◆ 登録したユーザーデータを削除");
            log.debug(pochi + "," + tama);
            if (pochi != null) {
                testBox.odata("odata").entitySet("animal").del(pochi);
            }
            /** EntityType削除. */
            /** Delete EntityType. */
            log.debug("\n◆ EntityType削除");
            if (et != null) {
                testBox.odata("odata").entityType.del("animal");
            }
            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataコレクション削除");
            testBox.del("odata");
        }
    }

    /**
     * ODataユーザデータの部分更新.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the test of Partial update of OData user data. It performs following operations.
     * Create OData Collection, Create EntityType, Create User Data, Partial Update of user data,
     * Delete User Data, Delete EntityType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void ユーザデータのMERGEテスト() throws DaoException {
        String pochi = null;
        String tama = null;
        EntityType et = null;
        try {
            /** ODataコレクション作成. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataコレクション作成");
            testBox.mkOData("odata");

            /** Create EntityType. */
            log.debug("\n◆ EntityType登録");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "animal");
            et = testBox.odata("odata").entityType.create(dataMap);

            /** POST用のデータ作成. */
            /** Creating data for POST request. */
            log.debug("\n◆ ユーザーデータ登録（１件目)");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "pochi");
            dataMap.put("species", "dog");
            dataMap.put("weight", 50);
            dataMap.put("food", "肉");
            /** POST登録. */
            /** POST registration. */
            HashMap<String, Object> resMap = testBox.odata("odata").entitySet("animal").createAsJson(dataMap);
            log.debug(JsonUtils.toJsonString(resMap));
            pochi = (String) resMap.get("__id");

            /** Partial update of user data. */
            log.debug("\n◆ 部分更新");
            dataMap = new HashMap<String, Object>();
            dataMap.put("weight", 52);
            testBox.odata("odata").entitySet("animal").merge(pochi, dataMap, "*");
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** データ削除. */
            /** Delete User Data. */
            log.debug("\n◆ 登録したユーザーデータを削除");
            log.debug(pochi + "," + tama);
            if (pochi != null) {
                testBox.odata("odata").entitySet("animal").del(pochi);
            }
            /** EntityType削除. */
            /** Delete EntityType. */
            log.debug("\n◆ EntityType削除");
            if (et != null) {
                testBox.odata("odata").entityType.del("animal");
            }
            /** ODataコレクションを削除. */
            /** Delete OData Collection. */
            log.debug("\n◆ ODataコレクション削除");
            testBox.del("odata");
        }
    }

    /**
     * NavProp経由登録.
     * @throws DaoException DAO例外
     */
    /**
     * This method performs the NavProp test via registration. It performs following operations.
     * Create OData Collection, Create two EntityTypes, Create two AssociationEnds, Create Link
     * between AssociationEnds, Create User Data, Create two Navigation properties, Execute various queries,
     * Get User Data, Delete two Navigation Properties, Delete User Data, Delete Link between AssociationEnds,
     * Delete AssociationEnd, Delete two EntityTypes, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void NavProp経由登録テスト() throws DaoException {
        EntityType etKeeper = null;
        EntityType etAnimal = null;
        AssociationEnd aeKeeper = null;
        AssociationEnd aeAnimal = null;
        String keeperId = null;
        String animalId1 = null;
        String animalId2 = null;
        try {
            /** Create OData Collection. */
            log.debug("\n◆ ODataコレクションの作成");
            testBox.mkOData("odata");

            /** Create EntityType. */
            log.debug("\n◆ EntityType (keeper)");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", "testkeeper");
            etKeeper = testBox.odata("odata").entityType.create(dataMap);

            /** Create EntityType. */
            log.debug("\n◆ EntityType (animal)");
            dataMap = new HashMap<String, Object>();
            dataMap.put("Name", "testdog");
            etAnimal = testBox.odata("odata").entityType.create(dataMap);

            /** Create AssociationEnd. */
            log.debug("\n◆ AssociationEnd (keeper)");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "aeKeeper");
            dataMap.put(FIELD_MULTIPLICITY, "1");
            dataMap.put(FIELD_ENTITYTYPE_NAME, etKeeper.getName());
            aeKeeper = testBox.odata("odata").associationEnd.create(dataMap);

            /** Create AssociationEnd. */
            log.debug("\n◆ AssociationEnd (animal)");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "aeAnimal");
            dataMap.put(FIELD_MULTIPLICITY, "*");
            dataMap.put(FIELD_ENTITYTYPE_NAME, etAnimal.getName());
            aeAnimal = testBox.odata("odata").associationEnd.create(dataMap);

            /** Create Link between two AssociationEnds. */
            log.debug("\n◆ AssociationEnd $links");
            aeKeeper.associationEnd.link(aeAnimal);

            /** Create User Data. */
            log.debug("\n◆ keeperユーザデータ登録");
            dataMap = new HashMap<String, Object>();
            dataMap.put("name", "testKeeper");
            dataMap.put("age", "17");
            HashMap<String, Object> resMap = testBox.odata("odata").entitySet(etKeeper.getName()).createAsJson(dataMap);
            keeperId = (String) resMap.get("__id");

            /** Create Navigation property. */
            log.debug("\n◆ animalユーザデータ登録(1件目)");
            dataMap = new HashMap<String, Object>();
            dataMap.put("name", "testAnimal1");
            dataMap.put("species", "dog");
            resMap = testBox.odata("odata").entitySet(etKeeper.getName()).key(keeperId).nav(etAnimal.getName())
                    .createAsJson(dataMap);
            animalId1 = (String) resMap.get("__id");

            /** Create Navigation property. */
            log.debug("\n◆ animalユーザデータ登録(2件目)");
            dataMap = new HashMap<String, Object>();
            dataMap.put("name", "testAnimal2");
            dataMap.put("species", "dog");
            resMap = testBox.odata("odata").entitySet(etKeeper.getName()).key(keeperId).nav(etAnimal.getName())
                    .createAsJson(dataMap);
            animalId2 = (String) resMap.get("__id");

            /** Execute $inlinecount query to fetch total number of records. */
            log.debug("\n◆ animalユーザデータ一覧取得");
            HashMap<String, Object> listResponse = testBox.odata("odata").entitySet(etKeeper.getName()).key(keeperId)
                    .nav(etAnimal.getName()).query().inlinecount("allpages").run();
            ArrayList<Object> ar = (ArrayList<Object>) ((HashMap<String, Object>) listResponse.get("d")).get("results");
            assertEquals(2, ar.size());

            /** Get User Data. */
            log.debug("\n◆ animalユーザデータ取得");
            resMap = testBox.odata("odata").entitySet(etAnimal.getName()).retrieveAsJson(animalId1);
            log.debug(JsonUtils.toJsonString(resMap));
            assertEquals(animalId1, (String) resMap.get("__id"));

            /** Execute $inlinecount and $expand query. */
            log.debug("\n◆ keeper指定で$expand(_testdog)ユーザデータ取得");
            HashMap<String, Object> extResponse = testBox.odata("odata").entitySet(etKeeper.getName()).key(keeperId)
                    .query().inlinecount("allpages").expand("_" + etAnimal.getName()).run();
            log.debug(extResponse.toString());

            /** Execute $expand query. */
            log.debug("\n◆ _testdog指定で$expand(_keeper)ユーザデータ取得");
            HashMap<String, Object> extkeeperResponse = testBox.odata("odata").entitySet(etKeeper.getName())
                    .key(keeperId).nav(etAnimal.getName()).query().expand("_" + etKeeper.getName()).run();
            ArrayList<Object> arkeeper = (ArrayList<Object>) ((HashMap<String, Object>) extkeeperResponse.get("d"))
                    .get("results");
            assertEquals(2, arkeeper.size());
            log.debug(extkeeperResponse.toString());

        } finally {
            /** Delete Navigation property. */
            log.debug("\n◆ animalユーザデータ削除");
            if (animalId1 != null) {
                testBox.odata("odata").entitySet(etAnimal.getName()).del(animalId1);
            }
            /** Delete Navigation property. */
            if (animalId2 != null) {
                testBox.odata("odata").entitySet(etAnimal.getName()).del(animalId2);
            }

            /** Delete User Data. */
            log.debug("\n◆ keeperユーザデータ削除");
            if (keeperId != null) {
                testBox.odata("odata").entitySet(etKeeper.getName()).del(keeperId);
            }

            /** Delete Link between AssociationEnds. */
            log.debug("\n◆ $unLink AND AssociationEnd (animal)削除");
            if (aeAnimal != null) {
                aeKeeper.associationEnd.unLink(aeAnimal);
                testBox.odata("odata").associationEnd.del(aeAnimal.getName(), aeAnimal.getEntityTypeName());
            }

            /** Delete AssociationEnd. */
            log.debug("\n◆ AssociationEnd (keeper)削除");
            if (aeKeeper != null) {
                testBox.odata("odata").associationEnd.del(aeKeeper.getName(), aeKeeper.getEntityTypeName());
            }

            /** Delete EntityType. */
            log.debug("\n◆ EntityType (animal)削除");
            if (etAnimal != null) {
                testBox.odata("odata").entityType.del(etAnimal.getName());
            }

            /** Delete EntityType. */
            log.debug("\n◆ EntityType (keeper)削除");
            if (etKeeper != null) {
                testBox.odata("odata").entityType.del(etKeeper.getName());
            }

            /** Delete ODataCollection. */
            log.debug("\n◆ ODataコレクションの削除");
            testBox.del("odata");
        }

    }

    /**
     * Entityを利用したlinkテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the Link using the Entity. It performs following operations.
     * Create OData Collection, Create two EntityTypes, Create two AssociationEnds, Create Link
     * between AssociationEnds, Create three User Data, Create Links between entities, Execute query,
     * Delete Entity Links, Delete three User Data, Delete Link between AssociationEnds,
     * Delete AssociationEnd, Delete two EntityTypes, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void Entityを利用したlinkテスト() throws DaoException {
        EntityType etKeeper = null;
        EntityType etAnimal = null;
        AssociationEnd aeKeeper = null;
        AssociationEnd aeAnimal = null;
        String keeperId = null;
        String animalId1 = null;
        String animalId2 = null;
        Entity eKeeper = null;
        Entity eAnimal1 = null;
        Entity eAnimal2 = null;
        try {
            /** Create OData Collection. */
            log.debug("\n◆ ODataコレクションの作成");
            testBox.mkOData("odata");

            /** Create EntityType. */
            log.debug("\n◆ EntityType (keeper)");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", "testkeeper");
            etKeeper = testBox.odata("odata").entityType.create(dataMap);

            /** Create EntityType. */
            log.debug("\n◆ EntityType (animal)");
            dataMap = new HashMap<String, Object>();
            dataMap.put("Name", "testdog");
            etAnimal = testBox.odata("odata").entityType.create(dataMap);

            /** Create AssociationEnd. */
            log.debug("\n◆ AssociationEnd (keeper)");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "aeKeeper");
            dataMap.put(FIELD_MULTIPLICITY, "1");
            dataMap.put(FIELD_ENTITYTYPE_NAME, etKeeper.getName());
            aeKeeper = testBox.odata("odata").associationEnd.create(dataMap);

            /** Create AssociationEnd. */
            log.debug("\n◆ AssociationEnd (animal)");
            dataMap = new HashMap<String, Object>();
            dataMap.put(FIELD_NAME, "aeAnimal");
            dataMap.put(FIELD_MULTIPLICITY, "*");
            dataMap.put(FIELD_ENTITYTYPE_NAME, etAnimal.getName());
            aeAnimal = testBox.odata("odata").associationEnd.create(dataMap);

            /** Create AssociationEnd LInk. */
            log.debug("\n◆ AssociationEnd $links");
            aeKeeper.associationEnd.link(aeAnimal);

            /** Create User Data. */
            log.debug("\n◆ keeperユーザデータ登録");
            dataMap = new HashMap<String, Object>();
            dataMap.put("name", "testKeeper");
            dataMap.put("age", "17");
            eKeeper = testBox.odata("odata").entitySet(etKeeper.getName()).createAsEntity(dataMap);
            keeperId = (String) eKeeper.getBody().get("__id");

            /** Create User Data. */
            log.debug("\n◆ animalユーザデータ登録(1件目)");
            dataMap = new HashMap<String, Object>();
            dataMap.put("name", "testAnimal1");
            dataMap.put("species", "dog");
            eAnimal1 = testBox.odata("odata").entitySet(etAnimal.getName()).createAsEntity(dataMap);
            animalId1 = (String) eAnimal1.getBody().get("__id");

            /** Create User Data. */
            log.debug("\n◆ animalユーザデータ登録(2件目)");
            dataMap = new HashMap<String, Object>();
            dataMap.put("name", "testAnimal2");
            dataMap.put("species", "dog");
            eAnimal2 = testBox.odata("odata").entitySet(etAnimal.getName()).createAsEntity(dataMap);
            animalId2 = (String) eAnimal2.getBody().get("__id");

            /** Create Link between two Entities. */
            log.debug("\n◆ animal(1件目)とkeeperの$links登録");
            eKeeper.entity.link(eAnimal1);

            /** Create Link between two Entities. */
            log.debug("\n◆ animal(2件目)とkeeperの$links登録");
            eKeeper.entity.link(eAnimal2);

            /** Execute Query. */
            log.debug("\n◆ keeper指定で$expand(_testdog)ユーザデータ取得");
            HashMap<String, Object> extResponse = testBox.odata("odata").entitySet(etKeeper.getName()).key(keeperId)
                    .query().inlinecount("allpages").expand("_" + etAnimal.getName()).run();
            // log.debug(extResponse.toString());

            HashMap<String, Object> obj = (HashMap<String, Object>) extResponse.get("d");
            HashMap<String, Object> res = (HashMap<String, Object>) obj.get("results");
            ArrayList<Object> animals = (ArrayList<Object>) res.get("_testdog");
            assertEquals(2, animals.size());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Entity Links. */
            log.debug("\n◆ $linksを削除");
            eKeeper.entity.unLink(eAnimal1);
            eKeeper.entity.unLink(eAnimal2);

            /** Delete User Data. */
            log.debug("\n◆ animalユーザデータ削除");
            if (animalId1 != null) {
                testBox.odata("odata").entitySet(etAnimal.getName()).del(animalId1);
            }

            /** Delete User Data. */
            if (animalId2 != null) {
                testBox.odata("odata").entitySet(etAnimal.getName()).del(animalId2);
            }

            /** Delete User Data. */
            log.debug("\n◆ keeperユーザデータ削除");
            if (keeperId != null) {
                testBox.odata("odata").entitySet(etKeeper.getName()).del(keeperId);
            }

            /** Delete AssociationEnd Link. */
            log.debug("\n◆ $unLink AND AssociationEnd (animal)削除");
            if (aeAnimal != null) {
                aeKeeper.associationEnd.unLink(aeAnimal);
                testBox.odata("odata").associationEnd.del(aeAnimal.getName(), aeAnimal.getEntityTypeName());
            }

            /** Delete AssociationEnd. */
            log.debug("\n◆ AssociationEnd (keeper)削除");
            if (aeKeeper != null) {
                testBox.odata("odata").associationEnd.del(aeKeeper.getName(), aeKeeper.getEntityTypeName());
            }

            /** Delete EntityType. */
            log.debug("\n◆ EntityType (animal)削除");
            if (etAnimal != null) {
                testBox.odata("odata").entityType.del(etAnimal.getName());
            }

            /** Delete EntityType. */
            log.debug("\n◆ EntityType (keeper)削除");
            if (etKeeper != null) {
                testBox.odata("odata").entityType.del(etKeeper.getName());
            }

            /** Delete OData Collection. */
            log.debug("\n◆ ODataコレクションの削除");
            testBox.del("odata");
        }

    }

    /**
     * serviceCollectionの疎通テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the communication test of ServiceCollection. It performs following operations.
     * Create Service Collection, Configure the Service, PUT file, Call GET, POST, PUT, DELETE on service,
     * Delete File, Delete Service Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void serviceCollection疎通() throws DaoException {
        ServiceCollection svcCol = null;
        boolean isScript = false;
        try {
            /** Create Service Collection. */
            log.debug("\n◆ サービスコレクションの作成 svccol");
            testBox.mkService("svccol");
            svcCol = testBox.service("svccol");

            /** Configure the Service. */
            log.debug("\n◆ サービスの設定 （PROPPATCH）");
            svcCol.configure("servicetest", "servicetest.js", "engine");

            /** PUT file. */
            log.debug("\n◆ スクリプトの登録 （Davのput）");
            String script = "function(request){return {status: 200,"
                    + "headers: {\"Content-Type\":\"text/html\"},body: [\"hello world!\"]};}";
            svcCol.put("servicetest.js", "text/plane", script, "*");
            isScript = true;

            /** Call GET method on ServiceCollection. */
            log.debug("\n◆ サービスの実行 servicetest GET");
            HttpResponse resp = svcCol.call("GET", "servicetest", "");
            assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());

            /** Call POST method on ServiceCollection. */
            log.debug("\n◆ サービスの実行 servicetest POST");
            resp = svcCol.call("POST", "servicetest", "");
            assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());

            /** Call PUT method on ServiceCollection. */
            log.debug("\n◆ サービスの実行 servicetest PUT");
            resp = svcCol.call("PUT", "servicetest", "");
            assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());

            /** Call DELETE method on ServiceCollection. */
            log.debug("\n◆ サービスの実行 servicetest DELETE");
            resp = svcCol.call("DELETE", "servicetest", "");
        } finally {
            /** Delete File. */
            log.debug("\n◆ スクリプトの削除 servicetest.js DELETE");
            if (isScript) {
                svcCol.del("servicetest.js");
            }
            /** Delete ServiceCollection. */
            log.debug("\n◆ サービスコレクションの削除  servicetest DELETE");
            if (null != svcCol) {
                testBox.del("svccol");
            }
        }
    }

    /**
     * Unit昇格関連のテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test of the Unit-related promotion. It performs following operations.
     * Create Cell, Create Account, Create a Cell by Unit promoted, Delete Account, Delete two cells.
     * @throws DaoException Exception thrown
     */
    @Test
    public void Unit昇格関連テスト() throws DaoException {
        Cell ownerCell = null;
        Cell cell = null;
        Account account = null;
        String name = cellName + "_" + suf;
        try {
            /** Cellの作成. */
            /** Create Cell. */
            log.debug("\n◆ Cellの作成");
            ownerCell = new Cell();
            ownerCell.setName(name);
            ownerCell = testAs.asCellOwner().unit.cell.create(ownerCell);

            /** Accountの登録. */
            /** Create Account. */
            log.debug("\n◆ Account登録");
            account = new Account();
            account.setName("user");
            account.setPassword("password");
            account = ownerCell.account.create(account);

            /** ownerRepresentativeAccountsのセット. */
            /** Set of ownerRepresentativeAccounts. */
            log.debug("\n◆ ownerRepresentativeAccountsのセット");

            ArrayList<String> names = new ArrayList<String>();
            names.add("user");

            ownerCell.setOwnerRepresentativeAccounts(names.toArray(new String[0]));

            /** Unit昇格してCellを作成. */
            /** Create a Cell by Unit promoted. */
            log.debug("\n◆ Unit昇格してCellを作成する");
            Accessor as = dc.asAccount(ownerCell.getName(), "user", "password");
            cell = new Cell();
            cell.setName("unit" + "_" + suf);

            cell = as.asCellOwner().unit.cell.create(cell);

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            if (ownerCell != null) {
                /** Accountの削除. */
                /** Delete Account. */
                log.debug("\n◆ Accountの削除");
                if (account != null) {
                    ownerCell.account.del(account.getName());
                }
                /** Cellの削除. */
                /** Delete Cell. */
                log.debug("\n◆ Cellの削除");
                if (testAs.asCellOwner().unit.cell.exists(ownerCell.getName())) {
                    log.debug("\n◆ Cellの削除");
                    testAs.asCellOwner().unit.cell.del(ownerCell.getName(), "*");
                }
            }
            /** Cellの削除. */
            /** Delete Cell. */
            log.debug("\n◆ Cellの削除");
            if (cell != null) {
                if (testAs.asCellOwner().unit.cell.exists(cell.getName())) {
                    log.debug("\n◆ Cellの削除");
                    testAs.asCellOwner().unit.cell.del(cell.getName(), "*");
                }
            }
        }
    }

    /**
     * Unit昇格後のCellアクセスで認証処理が行われないこと.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test of the Unit-related promotion in which authentication process is not performed
     * in the Cell. It performs following operations.
     * Create Cell, Create Account, Validate authentication is not performed, Delete Account, Delete cell.
     * @throws DaoException Exception thrown
     */
    @Test
    public void Unit昇格後のCellアクセスで認証処理が行われないこと() throws DaoException {
        Cell ownerCell = null;
        Account account = null;
        String name = cellName + "_" + suf;
        try {
            /** Cellの作成. */
            /** Create Cell. */
            log.debug("\n◆ Cellの作成");
            ownerCell = new Cell();
            ownerCell.setName(name);
            ownerCell = testAs.asCellOwner().unit.cell.create(ownerCell);

            /** Accountの登録. */
            /** Create Account. */
            log.debug("\n◆ Account登録");
            account = new Account();
            account.setName("user");
            account.setPassword("password");
            account = ownerCell.account.create(account);

            /** ownerRepresentativeAccountsのセット. */
            /** Set of ownerRepresentativeAccounts. */
            log.debug("\n◆ ownerRepresentativeAccountsのセット");
            ArrayList<String> names = new ArrayList<String>();
            names.add("user");
            ownerCell.setOwnerRepresentativeAccounts(names.toArray(new String[0]));

            /** 認証が行われていないため、ここでトークンが変更されていないことを確認. */
            /** Since authentication is not performed, confirming that the token has not been changed here. */
            Accessor as = dc.asAccount(ownerCell.getName(), "user", "password");
            Accessor ownerAccessor = as.asCellOwner();
            String ownerAccessToken = ownerAccessor.getAccessToken();
            ownerAccessor.cell();
            assertEquals(ownerAccessToken, ownerAccessor.getAccessToken());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            if (ownerCell != null) {
                /** Accountの削除. */
                /** Delete Account. */
                log.debug("\n◆ Accountの削除");
                if (account != null) {
                    ownerCell.account.del(account.getName());
                }
                /** Cellの削除. */
                /** Delete cell. */
                log.debug("\n◆ Cellの削除");
                if (testAs.asCellOwner().unit.cell.exists(ownerCell.getName())) {
                    log.debug("\n◆ Cellの削除");
                    testAs.asCellOwner().unit.cell.del(ownerCell.getName(), "*");
                }
            }
        }
    }

    /**
     * Batchのテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test of Batch operations. It performs following operations.
     * Create OData Collection, Create EntityType, Prepare data for Batch execution,
     * Execute Batch, Delete EntityType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void Batchのテスト() throws DaoException {

        EntityType etKeeper = null;
        ODataBatch odataBatch = null;
        ArrayList<Integer> expectedSts = new ArrayList<Integer>();
        try {
            /** Create OData Collection. */
            log.debug("\n◆ ODataColection [odatabatch] の作成");
            testBox.mkOData("odatabatch");

            /** Create EntityType. */
            log.debug("\n◆ EntityType (testkeeper)");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", "testkeeper");
            etKeeper = testBox.odata("odatabatch").entityType.create(dataMap);

            log.debug("\n◆ ODataBatchオブジェクト生成");
            odataBatch = testBox.odata("odatabatch").makeODataBatch(false);

            /** Prepare data for Batch execution. */
            HashMap<String, Object> json = new HashMap<String, Object>();
            log.debug("\n◆ ユーザデータ登録 １件目");
            json.put("name", "keeper01");
            json.put("__id", "batchtest001");
            json.put("age", "15");
            odataBatch.entitySet(etKeeper.getName()).createAsJson(json);
            expectedSts.add(Integer.valueOf(HttpStatus.SC_CREATED));
            log.debug("\n◆ ユーザデータ登録 ２件目");
            json.put("name", "keeper02");
            json.put("__id", "batchtest002");
            json.put("age", "25");
            odataBatch.entitySet(etKeeper.getName()).createAsJson(json);
            expectedSts.add(Integer.valueOf(HttpStatus.SC_CREATED));
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();
            log.debug("\n◆ ユーザデータ更新 ２件目");
            json.put("name", "keeper02up1");
            json.put("age", "25");
            odataBatch.entitySet(etKeeper.getName()).update("batchtest002", json, "*");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ ユーザデータ更新 ２件目");
            json.put("name", "keeper02up2");
            json.put("age", "26");
            odataBatch.entitySet(etKeeper.getName()).update("batchtest002", json, "*");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();
            log.debug("\n◆ ユーザデータ更新 ２件目");
            json.put("name", "keeper02up3");
            json.put("age", "27");
            odataBatch.entitySet(etKeeper.getName()).update("batchtest002", json, "*");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ ユーザデータ更新 ２件目");
            json.put("name", "keeper02up4");
            json.put("age", "28");
            odataBatch.entitySet(etKeeper.getName()).update("batchtest002", json, "*");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ ユーザデータ更新 ２件目");
            json.put("name", "keeper02up5");
            json.put("age", "29");
            odataBatch.entitySet(etKeeper.getName()).update("batchtest002", json, "*");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ ユーザデータ取得 １件目");
            odataBatch.entitySet(etKeeper.getName()).retrieveAsJson("batchtest001");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_OK));
            log.debug("\n◆ ユーザデータ削除 １件目");
            odataBatch.entitySet(etKeeper.getName()).del("batchtest001");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ ユーザデータ取得 ２件目");
            odataBatch.entitySet(etKeeper.getName()).retrieveAsJson("batchtest002");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_OK));
            log.debug("\n◆ ユーザデータ削除 ２件目 削除");
            odataBatch.entitySet(etKeeper.getName()).del("batchtest002");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ Batch Send");
            /** Execute Batch. */
            odataBatch.send();
            ODataResponse[] batchRes = odataBatch.getResponses();
            utilLog.debug("■ $Batch レスポンス数 ： " + batchRes.length);
            for (int i = 0; i < batchRes.length; i++) {
                ODataResponse res = batchRes[i];
                utilLog.debug("【No " + String.valueOf(i) + " - Response 】");
                utilLog.debug("   status : " + res.getStatusCode());
                assertEquals(expectedSts.get(i).intValue(), res.getStatusCode());
                HashMap<String, String> headers = res.getHeaders();
                Iterator<Entry<String, String>> it = headers.entrySet().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    utilLog.debug("   " + o + " : " + headers.get(o));
                }
                utilLog.debug("   Body : " + res.bodyAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.debug("\n◆◆ FINALLY");
            /** Delete EntityType. */
            log.debug("\n◆ EntityType (testkeeper)削除");
            if (etKeeper != null) {
                testBox.odata("odatabatch").entityType.del(etKeeper.getName());
            }
            /** Delete OData Collection. */
            log.debug("\n◆ ODataコレクションの削除");
            testBox.del("odatabatch");
        }
    }

    /**
     * Batchの取得テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the Acquisition test of Batch. It performs following operations.
     * Create OData Collection, Create EntityType, Create two UserData, Prepare data for Batch execution,
     * Execute Batch, Delete EntityType, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void Batchの取得テスト() throws DaoException {

        EntityType etKeeper = null;
        ODataBatch odataBatch = null;
        ArrayList<Integer> expectedSts = new ArrayList<Integer>();
        try {
            /** テスト準備. */
            /** Test Preparation. */
            /** Create OData Collection. */
            log.debug("\n◆ ODataColection [odatabatch] の作成");
            testBox.mkOData("odatabatch");
            /** Create EntityType. */
            log.debug("\n◆ EntityType (testkeeper)");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", "testkeeper");
            etKeeper = testBox.odata("odatabatch").entityType.create(dataMap);

            /** Create User Data. */
            log.debug("\n◆ ODataBatchオブジェクト生成");
            HashMap<String, Object> json = new HashMap<String, Object>();
            log.debug("\n◆ ユーザデータ登録 １件目");
            json.put("name", "keeper01");
            json.put("__id", "batchtest001");
            json.put("age", "15");
            testBox.odata("odatabatch").entitySet(etKeeper.getName()).createAsJson(json);

            /** Create User Data. */
            log.debug("\n◆ ユーザデータ登録 ２件目");
            json.put("name", "keeper02");
            json.put("__id", "batchtest002");
            json.put("age", "25");
            testBox.odata("odatabatch").entitySet(etKeeper.getName()).createAsJson(json);
            log.debug("\n◆ BatchBoundary 挿入");

            /** テスト開始. */
            /** Test start. */
            odataBatch = testBox.odata("odatabatch").makeODataBatch(false);
            log.debug("\n◆ ユーザデータ取得 １件目");
            odataBatch.entitySet(etKeeper.getName()).retrieveAsJson("batchtest001");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_OK));
            log.debug("\n◆ ユーザデータ取得 全件検索");
            odataBatch.entitySet(etKeeper.getName()).query().run();
            expectedSts.add(Integer.valueOf(HttpStatus.SC_OK));
            // query未対応のためコメントアウト
            // log.debug("\n◆ ユーザデータ取得 Query検索");
            // odataBatch.entitySet(etKeeper.getName()).query().inlinecount("allpages").run();
            // expectedSts.add(Integer.valueOf(HttpStatus.SC_OK));
            log.debug("\n◆ ユーザデータ削除 １件目");
            odataBatch.entitySet(etKeeper.getName()).del("batchtest001");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ ユーザデータ削除 ２件目 削除");
            odataBatch.entitySet(etKeeper.getName()).del("batchtest002");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ Batch Send");

            /** Batch Execution. */
            odataBatch.send();
            ODataResponse[] batchRes = odataBatch.getResponses();
            utilLog.debug("■ $Batch レスポンス数 ： " + batchRes.length);
            for (int i = 0; i < batchRes.length; i++) {
                ODataResponse res = batchRes[i];
                utilLog.debug("【No " + String.valueOf(i) + " - Response 】");
                utilLog.debug("   status : " + res.getStatusCode());
                assertEquals(expectedSts.get(i).intValue(), res.getStatusCode());
                HashMap<String, String> headers = res.getHeaders();
                Iterator<Entry<String, String>> it = headers.entrySet().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    utilLog.debug("   " + o + " : " + headers.get(o));
                }
                utilLog.debug("   Body : " + res.bodyAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /** Delete EntityType. */
            log.debug("\n◆◆ FINALLY");
            log.debug("\n◆ EntityType (testkeeper)削除");
            if (etKeeper != null) {
                testBox.odata("odatabatch").entityType.del(etKeeper.getName());
            }
            /** Delete OData Collection. */
            log.debug("\n◆ ODataコレクションの削除");
            testBox.del("odatabatch");
        }
    }

    /**
     * BatchのNavPro経由テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test of NavPro via Batch. It performs following operations.
     * Create OData Collection, Create two EntityTypes, Create two AssociationEnds, Create links
     * between AssociationEnds, Get User Data, BatchBoundary insertion, User data registration via NavPro,
     * Execute Batch, Delete two AssociationEnds, Delete two EntityTypes, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void BatchのNavPro経由テスト() throws DaoException {

        EntityType etUser = null;
        EntityType etLog = null;
        ODataBatch odataBatch = null;
        AssociationEnd associationEndUser = null;
        AssociationEnd associationEndLog = null;
        ArrayList<Integer> expectedSts = new ArrayList<Integer>();
        try {
            /** Create OData Collection. */
            log.debug("\n◆ ODataColection [odatabatch] の作成");
            testBox.mkOData("odatabatch");

            /** Create EntityType. */
            log.debug("\n◆ EntityType (user)");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", "user");
            etUser = testBox.odata("odatabatch").entityType.create(dataMap);

            /** Create EntityType. */
            log.debug("\n◆ EntityType (log)");
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("Name", "log");
            etLog = testBox.odata("odatabatch").entityType.create(logDataMap);

            /** Create AssociationEnd. */
            log.debug("\n◆ Associationend (user)");
            HashMap<String, Object> userAssocMap = new HashMap<String, Object>();
            userAssocMap.put(FIELD_NAME, "associationEndUser");
            userAssocMap.put(FIELD_MULTIPLICITY, "0..1");
            userAssocMap.put(FIELD_ENTITYTYPE_NAME, etUser.getName());
            associationEndUser = testBox.odata("odatabatch").associationEnd.create(userAssocMap);

            /** Create AssociationEnd. */
            log.debug("\n◆ Associationend (log)");
            HashMap<String, Object> logAssocMap = new HashMap<String, Object>();
            logAssocMap.put(FIELD_NAME, "associationEndLog");
            logAssocMap.put(FIELD_MULTIPLICITY, "*");
            logAssocMap.put(FIELD_ENTITYTYPE_NAME, etLog.getName());
            associationEndLog = testBox.odata("odatabatch").associationEnd.create(logAssocMap);

            log.debug("\n◆ Associationend - Associationend $links(user - log)");
            /** AssociationEndのリンク (204). */
            /** Create Link AssociationEnd (204). */
            associationEndUser.associationEnd.link(associationEndLog);

            log.debug("\n◆ ODataBatchオブジェクト生成");
            odataBatch = testBox.odata("odatabatch").makeODataBatch(false);

            /** Get User Data. */
            log.debug("\n◆ ユーザデータ取得 １件目");
            odataBatch.entitySet(etUser.getName()).retrieveAsJson("0000");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NOT_FOUND));

            /** BatchBoundary insertion. */
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();
            HashMap<String, Object> json = new HashMap<String, Object>();
            log.debug("\n◆ ユーザデータ登録");
            json.put("name", "ユーザ１");
            json.put("__id", "0000");
            /** Create User Data. */
            odataBatch.entitySet(etUser.getName()).createAsJson(json);
            expectedSts.add(Integer.valueOf(HttpStatus.SC_CREATED));
            log.debug("\n◆ ユーザデータ更新");
            json.put("familyName", "ユーザ");
            json.put("givenName", "１");
            /** Update User Data. */
            odataBatch.entitySet(etUser.getName()).update("0000", json, "*");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));

            /** BatchBoundary insertion. */
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();
            log.debug("\n◆ ユーザデータ取得");
            /** Get User Data. */
            odataBatch.entitySet(etUser.getName()).retrieveAsJson("0000");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_OK));

            /** BatchBoundary insertion. */
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();

            /** User data registration via NavPro. */
            log.debug("\n◆ ユーザデータNavPro経由登録");
            HashMap<String, Object> logJson = new HashMap<String, Object>();
            logJson.put("name", "ユーザログ");
            logJson.put("__id", "0001");
            odataBatch.entitySet(etUser.getName()).key((String) json.get("__id")).nav(etLog.getName())
                    .createAsJson(logJson);
            expectedSts.add(Integer.valueOf(HttpStatus.SC_CREATED));
            log.debug("\n◆ ユーザデータ削除(log)");
            odataBatch.entitySet(etLog.getName()).del("0001");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));
            log.debug("\n◆ ユーザデータ削除 (user)");
            odataBatch.entitySet(etUser.getName()).del("0000");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));

            /** Batch Execution. */
            log.debug("\n◆ Batch Send");
            odataBatch.send();
            ODataResponse[] batchRes = odataBatch.getResponses();
            utilLog.debug("■ $Batch レスポンス数 ： " + batchRes.length);
            for (int i = 0; i < batchRes.length; i++) {
                ODataResponse res = batchRes[i];
                utilLog.debug("【No " + String.valueOf(i) + " - Response 】");
                utilLog.debug("   status : " + res.getStatusCode());
                assertEquals(expectedSts.get(i).intValue(), res.getStatusCode());
                HashMap<String, String> headers = res.getHeaders();
                Iterator<Entry<String, String>> it = headers.entrySet().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    utilLog.debug("   " + o + " : " + headers.get(o));
                }
                utilLog.debug("   Body : " + res.bodyAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.debug("\n◆◆ FINALLY");
            /** Delete AssociationEnd. */
            log.debug("\n◆ Associationend (user)削除");
            if (associationEndUser != null) {
                testBox.odata("odatabatch").associationEnd.del(associationEndUser.getName(), etUser.getName());
            }
            /** Delete AssociationEnd. */
            log.debug("\n◆ Associationend (log)削除");
            if (associationEndLog != null) {
                testBox.odata("odatabatch").associationEnd.del(associationEndLog.getName(), etLog.getName());
            }
            /** Delete EntityType. */
            log.debug("\n◆ EntityType (user)削除");
            if (etUser != null) {
                testBox.odata("odatabatch").entityType.del(etUser.getName());
            }
            /** Delete EntityType. */
            log.debug("\n◆ EntityType (log)削除");
            if (etLog != null) {
                testBox.odata("odatabatch").entityType.del(etLog.getName());
            }
            /** Delete OData Collection. */
            log.debug("\n◆ ODataコレクションの削除");
            testBox.del("odatabatch");
        }
    }

    /**
     * Batchの$links登録テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the test of Batch $ links. It performs following operations.
     * Create OData Collection, Create two EntityTypes, Create two AssociationEnds, Create links
     * between AssociationEnds, Get User Data, BatchBoundary insertions, Create User Data's, Delete
     * User Data, Execute Batch, Delete two AssociationEnds, Delete two EntityTypes, Delete OData Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void Batchの$links登録テスト() throws DaoException {

        EntityType etUser = null;
        EntityType etLog = null;
        ODataBatch odataBatch = null;
        AssociationEnd associationEndUser = null;
        AssociationEnd associationEndLog = null;
        ArrayList<Integer> expectedSts = new ArrayList<Integer>();
        try {
            /** Create OData Collection. */
            log.debug("\n◆ ODataColection [odatabatch] の作成");
            testBox.mkOData("odatabatch");

            /** Create two EntityTypes. */
            log.debug("\n◆ EntityType (user)");
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("Name", "user");
            etUser = testBox.odata("odatabatch").entityType.create(dataMap);
            log.debug("\n◆ EntityType (log)");
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("Name", "log");
            etLog = testBox.odata("odatabatch").entityType.create(logDataMap);

            /** Create two AssociationEnds. */
            log.debug("\n◆ Associationend (user)");
            HashMap<String, Object> userAssocMap = new HashMap<String, Object>();
            userAssocMap.put(FIELD_NAME, "associationEndUser");
            userAssocMap.put(FIELD_MULTIPLICITY, "0..1");
            userAssocMap.put(FIELD_ENTITYTYPE_NAME, etUser.getName());
            associationEndUser = testBox.odata("odatabatch").associationEnd.create(userAssocMap);
            log.debug("\n◆ Associationend (log)");
            HashMap<String, Object> logAssocMap = new HashMap<String, Object>();
            logAssocMap.put(FIELD_NAME, "associationEndLog");
            logAssocMap.put(FIELD_MULTIPLICITY, "*");
            logAssocMap.put(FIELD_ENTITYTYPE_NAME, etLog.getName());
            associationEndLog = testBox.odata("odatabatch").associationEnd.create(logAssocMap);

            log.debug("\n◆ Associationend - Associationend $links(user - log)");
            /** AssociationEndのリンク (204). */
            /** Create links between AssociationEnds(204). */
            associationEndUser.associationEnd.link(associationEndLog);

            log.debug("\n◆ ODataBatchオブジェクト生成");
            odataBatch = testBox.odata("odatabatch").makeODataBatch(false);

            /** BatchBoundary insertion. */
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();
            String userId = "0000";
            HashMap<String, Object> json = new HashMap<String, Object>();
            log.debug("\n◆ ユーザデータ登録（user）");
            json.put("name", "ユーザ１");
            json.put("__id", userId);

            /** Create User Data. */
            odataBatch.entitySet(etUser.getName()).createAsJson(json);
            expectedSts.add(Integer.valueOf(HttpStatus.SC_CREATED));

            /** BatchBoundary insertion. */
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();
            String logId = "0001";
            HashMap<String, Object> logJson = new HashMap<String, Object>();
            log.debug("\n◆ ユーザデータ登録（log）");
            logJson.put("name", "ユーザログ");
            logJson.put("__id", logId);
            /** Create User Data. */
            odataBatch.entitySet(etLog.getName()).createAsJson(logJson);
            expectedSts.add(Integer.valueOf(HttpStatus.SC_CREATED));

            /** BatchBoundary insertion. */
            log.debug("\n◆ BatchBoundary 挿入");
            odataBatch.insertBoundary();
            log.debug("\n◆ ユーザデータ$links登録");
            // odataBatch.batchLinksEntity("user", userId).entity.link(new BatchLinksEntity("log", logId));
            odataBatch.batchLinksEntity("user", userId).entity.link(odataBatch.batchLinksTarget("log", logId));
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));

            /** Delete user data (log). */
            log.debug("\n◆ ユーザデータ削除(log)");
            odataBatch.entitySet(etLog.getName()).del("0001");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));

            /** Delete user data (user). */
            log.debug("\n◆ ユーザデータ削除 (user)");
            odataBatch.entitySet(etUser.getName()).del("0000");
            expectedSts.add(Integer.valueOf(HttpStatus.SC_NO_CONTENT));

            /** Execute Batch. */
            log.debug("\n◆ Batch Send");
            odataBatch.send();
            ODataResponse[] batchRes = odataBatch.getResponses();
            utilLog.debug("■ $Batch レスポンス数 ： " + batchRes.length);
            for (int i = 0; i < batchRes.length; i++) {
                ODataResponse res = batchRes[i];
                utilLog.debug("【No " + String.valueOf(i) + " - Response 】");
                utilLog.debug("   status : " + res.getStatusCode());
                assertEquals(expectedSts.get(i).intValue(), res.getStatusCode());
                HashMap<String, String> headers = res.getHeaders();
                Iterator<Entry<String, String>> it = headers.entrySet().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    utilLog.debug("   " + o + " : " + headers.get(o));
                }
                utilLog.debug("   Body : " + res.bodyAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.debug("\n◆◆ FINALLY");
            /** Delete AssociationEnd. */
            log.debug("\n◆ Associationend (user)削除");
            if (associationEndUser != null) {
                testBox.odata("odatabatch").associationEnd.del(associationEndUser.getName(), etUser.getName());
            }
            /** Delete AssociationEnd. */
            log.debug("\n◆ Associationend (log)削除");
            if (associationEndLog != null) {
                testBox.odata("odatabatch").associationEnd.del(associationEndLog.getName(), etLog.getName());
            }
            /** Delete EntityType. */
            log.debug("\n◆ EntityType (user)削除");
            if (etUser != null) {
                testBox.odata("odatabatch").entityType.del(etUser.getName());
            }
            /** Delete EntityType. */
            log.debug("\n◆ EntityType (log)削除");
            if (etLog != null) {
                testBox.odata("odatabatch").entityType.del(etLog.getName());
            }
            /** Delete OData Collection. */
            log.debug("\n◆ ODataコレクションの削除");
            testBox.del("odatabatch");
        }
    }

    /**
     * coreのバージョン情報取得のテスト.
     * @throws DaoException DAO例外
     */
    /**
     * This is the test version of the information acquisition of the core.
     * @throws DaoException Exception thrown
     */
    @SuppressWarnings("unchecked")
    @Test
    public void coreのバージョン情報取得のテスト() throws DaoException {
        Cell cell = null;
        String name = cellName + "_" + suf;
        try {
            /** coreのバージョンを設定. */
            /** Set the version of the core. */
            dc.setDcVersion("1.0.0");
            assertNull(dc.getServerVersion());

            /** coreのバージョンを初期値（最新）に設定. */
            /** Set initial value (latest) version of the core. */
            dc.setDcVersion(null);

            /** Cellの作成. */
            /** Creating a Cell. */
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();
            json.put(FIELD_NAME, name);
            log.debug("\n◆ Cellの作成");
            cell = testAs.asCellOwner().unit.cell.create(json);
            assertNotNull(dc.getServerVersion());

        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Cellの削除. */
            /** Delete the Cell. */
            log.debug("\n◆ Cellのexist");
            if ((cell != null) && (testAs.asCellOwner().unit.cell.exists(cell.getName()))) {
                log.debug("\n◆ Cellの削除");
                testAs.asCellOwner().unit.cell.del(cell.getName(), "*");
            }
        }
    }

    /**
     * Cell名のみで作成したCellのURL取得テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the URL retrieval test of the Cell created in beforeClass method.
     * It tests the following manager URL's. AccountManager, BoxManager, RoleManager,
     * RelationManager, ExtCellManager, ExtRoleManager.
     * @throws DaoException Exception thrown
     */
    @Test
    public void Cell名のみで作成したCellのURL取得テスト() throws DaoException {
        String cellUrl = AbstractCase.baseUrl + testCell.getName() + "/";
        Cell cell = testAs.cell(testCell.getName());
        /** AccountManagerとの比較. */
        /** Comparison with the AccountManager. */
        String url = cell.account.getUrl();
        assertEquals(cellUrl + "__ctl/Account", url);
        /** BoxManagerとの比較. */
        /** Comparison with the BoxManager. */
        url = cell.box.getUrl();
        assertEquals(cellUrl + "__ctl/Box", url);
        /** RoleManagerとの比較. */
        /** Comparison with the RoleManager. */
        url = cell.role.getUrl();
        assertEquals(cellUrl + "__ctl/Role", url);
        /** RelationManagerとの比較. */
        /** Comparison with the RelationManager. */
        url = cell.relation.getUrl();
        assertEquals(cellUrl + "__ctl/Relation", url);
        /** ExtCellManagerとの比較. */
        /** Comparison with the ExtCellManager. */
        url = cell.extCell.getUrl();
        assertEquals(cellUrl + "__ctl/ExtCell", url);
        /** ExtRoleManagerとの比較. */
        /** Comparison with the ExtRoleManager. */
        url = cell.extRole.getUrl();
        assertEquals(cellUrl + "__ctl/ExtRole", url);
    }

    /**
     * 末尾にパス区切り子が付与されたURL形式で作成したCellのURL取得テスト.
     * @throws DaoException DAO例外
     */
    /**
     * This method is the URL retrieval test of the Cell created in URL format the path delimiter is
     * applied to the end. It tests the following manager URL's. AccountManager, BoxManager, RoleManager,
     * RelationManager, ExtCellManager, ExtRoleManager.
     * @throws DaoException Exception thrown
     */
    @Test
    public void 末尾にパス区切り子が付与されたURL形式で作成したCellのURL取得テスト() throws DaoException {
        String cellUrl = AbstractCase.baseUrl + testCell.getName() + "/";
        Cell cell = testAs.cell(cellUrl);
        /** AccountManagerとの比較. */
        /** Comparison with the AccountManager. */
        String url = cell.account.getUrl();
        assertEquals(cellUrl + "__ctl/Account", url);
        /** BoxManagerとの比較. */
        /** Comparison with the BoxManager. */
        url = cell.box.getUrl();
        assertEquals(cellUrl + "__ctl/Box", url);
        /** RoleManagerとの比較. */
        /** Comparison with the RoleManager. */
        url = cell.role.getUrl();
        assertEquals(cellUrl + "__ctl/Role", url);
        /** RelationManagerとの比較. */
        /** Comparison with the RelationManager. */
        url = cell.relation.getUrl();
        assertEquals(cellUrl + "__ctl/Relation", url);
        /** ExtCellManagerとの比較. */
        /** Comparison with the ExtCellManager. */
        url = cell.extCell.getUrl();
        assertEquals(cellUrl + "__ctl/ExtCell", url);
        /** ExtRoleManagerとの比較. */
        /** Comparison with the ExtRoleManager. */
        url = cell.extRole.getUrl();
        assertEquals(cellUrl + "__ctl/ExtRole", url);
    }

    /**
     * 末尾にパス区切り子が付与されていないURL形式で作成したCellのURL取得テスト.
     * @throws DaoException DAO例外
     */
    @Test
    /**
     * This method is the URL retrieval test of the Cell created in URL format the path delimiter is
     * not applied to the end. It tests the following manager URL's. AccountManager, BoxManager, RoleManager,
     * RelationManager, ExtCellManager, ExtRoleManager.
     * @throws DaoException Exception thrown
     */
    public void 末尾にパス区切り子が付与されていないURL形式で作成したCellのURL取得テスト() throws DaoException {
        String cellUrl = AbstractCase.baseUrl + testCell.getName();
        Cell cell = testAs.cell(cellUrl);
        /** AccountManagerとの比較. */
        /** Comparison with the AccountManager. */
        String url = cell.account.getUrl();
        assertEquals(cellUrl + "/__ctl/Account", url);
        /** BoxManagerとの比較. */
        /** Comparison with the BoxManager. */
        url = cell.box.getUrl();
        assertEquals(cellUrl + "/__ctl/Box", url);
        /** RoleManagerとの比較. */
        /** Comparison with the RoleManager. */
        url = cell.role.getUrl();
        assertEquals(cellUrl + "/__ctl/Role", url);
        /** RelationManagerとの比較. */
        /** Comparison with the RelationManager. */
        url = cell.relation.getUrl();
        assertEquals(cellUrl + "/__ctl/Relation", url);
        /** ExtCellManagerとの比較. */
        /** Comparison with the ExtCellManager. */
        url = cell.extCell.getUrl();
        assertEquals(cellUrl + "/__ctl/ExtCell", url);
        /** ExtRoleManagerとの比較. */
        /** Comparison with the ExtRoleManager. */
        url = cell.extRole.getUrl();
        assertEquals(cellUrl + "/__ctl/ExtRole", url);
    }

    /**
     * URL形式で作成したCellを認証する.
     * @throws DaoException DAO例外
     */
    /**
     * This method is used to authenticate the Cell created in URL format.
     * @throws DaoException Exception thrown
     */
    @Test
    public void URL形式で作成したCellを認証する() throws DaoException {
        Account account = null;
        try {
            /** Accoutの登録. */
            /** Registration of Account . */
            /** データ作成. */
            /** Create data. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);

            /** Accountの作成. */
            /** Create Account. */
            account = testCell.account.create(accountMap, accountPassword);
            /** 認証 asCell(=testCell). */
            /** Authentication asCell (= testCell). */
            String cellUrl = AbstractCase.baseUrl + testCell.getName() + "/";
            Cell cell = dc.asAccount(cellUrl, account.getName(), accountPassword).cell();
            /** 取得したCellオブジェクトから正しくCellURLが取得できること. */
            /** CellURL that can get correctly from the Cell object obtained. */
            assertEquals(cellUrl, cell.getUrl());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Accountの削除. */
            /** Delete Account. */
            if (account != null) {
                testCell.account.del(accountName);
            }
        }
    }

    /**
     * URL形式で作成したCellにACLを設定する.
     * @throws DaoException DAO例外
     */
    /**
     * This method is to test the ACL Settings on the Cell created in URL format.
     * It performs follwoing operations. Creates two Roles, Sets ACL, Deletes two Roles.
     * @throws DaoException Exception thrown
     */
    @Test
    @Ignore
    public void URL形式で作成したCellにACLを設定する() throws DaoException {
        Cell cell = null;
        try {
            String cellUrl = AbstractCase.baseUrl + testCell.getName() + "/";
            cell = testAs.cell(cellUrl);

            /** Create Role. */
            log.debug("\n◆  Role1作成");
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "role1");
            Role role1 = cell.role.create(map);

            /** Create Role. */
            log.debug("\n◆  Role2作成");
            map = new HashMap<String, Object>();
            map.put(FIELD_NAME, "role2");
            Role role2 = cell.role.create(map);

            /** ACL Settings. */
            log.debug("\n◆  ACL設定");
            Acl acl = new Acl();
            Ace ace;

            ace = new Ace();
            ace.setRole(role1);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            ace = new Ace();
            ace.setRole(role2);
            ace.addPrivilege("read");
            ace.addPrivilege("write");
            acl.addAce(ace);

            cell.acl.set(acl);

            log.debug("\n◆  ACL取得");
            Acl retAcl = cell.acl.get();

            System.out.println(retAcl.toXmlString());
            System.out.println(acl.toXmlString());

            assertEquals(retAcl.getAceList().size(), acl.getAceList().size());
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Roles. */
            if (cell != null) {
                log.debug("\n◆  Role1削除");
                cell.role.del("role1");
                log.debug("\n◆  Role2削除");
                cell.role.del("role2");
            }
        }
    }

    /**
     * URL形式で作成したCell配下のAccountでパスワードを変更する.
     * @throws DaoException DAO例外
     */
    /**
     * This test is used to change the password in the Account of the Cell created in URL format.
     * @throws DaoException Exception thrown
     */
    @Test
    public void URL形式で作成したCell配下のAccountでパスワードを変更する() throws DaoException {
        Account account = null;
        final String newPassword = "newpassword";
        Cell cell = null;
        try {
            String cellUrl = AbstractCase.baseUrl + testCell.getName() + "/";
            cell = testAs.cell(cellUrl);

            /** データ作成. */
            /** Data creation. */
            HashMap<String, Object> accountMap = new HashMap<String, Object>();
            accountMap.put(FIELD_NAME, accountName);

            /** Create Account. */
            log.debug("\n◆ Accountの作成");
            account = cell.account.create(accountMap, accountPassword);
            log.debug("\n◆  パスワード認証");
            Accessor as = dc.asAccount(cell.getName(), account.getName(), accountPassword);
            as.cell();
            /** Password Change. */
            log.debug("\n◆  Password変更");
            as.changePassword(newPassword);
            log.debug("\n◆  新しいパスワードでPassword認証");
            dc.asAccount(cell.getName(), account.getName(), newPassword).cell();
            /** 401 Check Password authentication with the old password. */
            log.debug("\n◆  古いパスワードでPassword認証 401チェック");
            try {
                dc.asAccount(cell.getName(), account.getName(), accountPassword).cell();
                fail();
            } catch (DaoException ex) {
                assertEquals(HttpStatus.SC_BAD_REQUEST, Integer.parseInt(ex.getCode()));
            }
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            /** Delete Account. */
            log.debug("\n◆  Accountの削除");
            if (account != null) {
                cell.account.del(accountName);
            }
        }
    }

    /**
     * Test case Create a Cell and call bar installation API with two parameters boxPath and barFile URL boxPath is the.
     * source boxPath where box is supposed to be created after successful bar installation barFileURL is the actual URL
     * of bar file
     * @throws DaoException DAO例外
     */
    /**
     * This test case creates a Cell and call bar installation API with two parameters boxPath and barFile URL.
     * boxPath is the source boxPath where box is supposed to be created after successful bar installation.
     * barFileURL is the actual URL of bar file.
     * @throws DaoException Exception thrown
     */
    @Test
    @Ignore
    public void testBarInstallation() throws DaoException {
         /** Start: Prerequisite of uploading a bar file to a public path, so that its URL can be used
          * for testing purpose. It will be deleted at the end of this test case.
         */
        /** box where bar file will be uploaded. */
        final String barFileURL = baseUrl + testCell.getName() + "/" + testBox.getName() + "/V1_1_2_bar_maximum.zip";
        Box sourceBox = null;
        String etag = null;
        try {
            sourceBox = testAs.cell(testCell.getName()).box(testBox.getName());
            String testFilename = getClass().getResource("/V1_1_2_bar_maximum.zip").getFile();
            dc.setChunked(false);
            FileInputStream fis = new FileInputStream(testFilename);
            WebDAV webDAV = sourceBox.put("V1_1_2_bar_maximum.zip", "application/zip", "", fis, "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            etag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);
        } catch (DaoException e) {
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        }
        /** End: Prerequisite of uploading a bar file. */

        /** Start: Executing the test scenario of installing the bar file. */
        /** this box should be created as the result of bar file installation. */
        Box targetBox = null;
        /** A test cell where bar file installation will be tested, it will deleted at the end of test case. */
        Cell targetCell = null;
        try {
            String targetCellName = "targetCell" + suf;
            String targetBoxName = "targetBox" + suf;

            /** Test cell creation - start. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, targetCellName);
            OwnerAccessor oa = testAs.asCellOwner();
            targetCell = oa.unit.cell.create(cellMap);
            log.debug("\n Cell:[" + targetCell.toJSONString() + "]");
            /** Test cell creation - end. */

            log.debug("\nInstantiating Box reference, this will be created after successful bar installation");
            targetBox = new Box();
            targetBox.setName(targetBoxName);
            String boxPath = baseUrl + targetCell.getName() + "/" + targetBox.getName();
            targetBox = testAs.cell(targetCell.getName()).installBox(boxPath, barFileURL);
            assertTrue("Box installation successful with no exception", targetBox != null);
        } catch (DaoException e) {
            /** exception occurred, box installation has issues. */
            fail(e.getMessage());
        } finally {
            /** Clean up data. */
            /** delete the uploaded bar file. */
            sourceBox.del("V1_1_2_bar_maximum.zip", etag);

            /** Delete the newly created box where Bar file has just been installed. */
            if (targetBox != null) {
                log.debug("\nCleaning Operation - deleting box " + targetBox.getName());
                targetCell.box.del(targetBox.getName());
            }

            /** Delete the test cell. */
            if (targetCell != null) {
                log.debug("\nCleaning Operation - deleting box " + targetCell.getName());
                testAs.asCellOwner().unit.cell.del(targetCell.getName(), "*");
            }
        }
    }

    /**
     * Test case Create a Cell and call bar installation API with two parameters boxPath and barFile URL boxPath is the.
     * source boxPath where box is supposed to be created after successful bar installation barFileURL is the actual URL
     * of bar file This scenario test when source box already exist and there is a duplicate request to create the box
     * with name
     * @throws DaoException DAO例外
     */
    /**
     * This test case creates a Cell and call bar installation API with two parameters boxPath and barFile URL.
     * boxPath is the source boxPath where box is supposed to be created after successful bar installation.
     * barFileURL is the actual URL of bar file.
     * This scenario test when source box already exist and there is a duplicate request to create the box
     * with name.
     * @throws DaoException Exception thrown
     */
    @Test
    @Ignore
    public void testBarInstallDuplicateBox() throws DaoException {
        /** Start: Prerequisite of uploading a bar file to a public path, so that its URL can be used for
         * testing purpose. It will be deleted at the end of this test case.
         */
        /** box where bar file will be uploaded. */
        final String barFileURL = baseUrl + testCell.getName() + "/" + testBox.getName() + "/V1_1_2_bar_maximum.zip";
        Box sourceBox = null;
        String etag = null;
        try {
            sourceBox = testAs.cell(testCell.getName()).box(testBox.getName());
            String testFilename = getClass().getResource("/V1_1_2_bar_maximum.zip").getFile();
            dc.setChunked(false);
            FileInputStream fis = new FileInputStream(testFilename);
            WebDAV webDAV = sourceBox.put("V1_1_2_bar_maximum.zip", "application/zip", "", fis, "*");
            assertEquals(HttpStatus.SC_CREATED, webDAV.getStatusCode());
            etag = webDAV.getHeaderValue(HEADER_KEY_ETAG);
            log.debug(etag);
        } catch (DaoException e) {
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        }
        /** End: Prerequisite of uploading a bar file. */

        /** Start: Executing the test scenario of installing the bar file. */
        /** this box should be created as the result of bar file installation. */
        Box targetBox = null;
        /** A test cell where bar file installation will be tested, it will deleted at the end of test case. */
        Cell targetCell = null;
        try {
            String targetCellName = "targetCell" + suf;
            String targetBoxName = "targetBox" + suf;

            /** Test cell creation - start. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, targetCellName);
            OwnerAccessor oa = testAs.asCellOwner();
            targetCell = oa.unit.cell.create(cellMap);
            log.debug("\n Cell:[" + targetCell.toJSONString() + "]");
            /** Test cell creation - end. */

            log.debug("Creating Box reference, this will be considered as duplicate during bar file installation");
            targetBox = new Box();
            targetBox.setName(targetBoxName);
            targetCell.box.create(targetBox);

            String boxPath = baseUrl + targetCell.getName() + "/" + targetBox.getName();
            targetBox = testAs.cell(targetCell.getName()).installBox(boxPath, barFileURL);

        } catch (DaoException e) {
            /** exception occurred, box installation has issues. */
            if ("405".equals(e.getCode()) || e.getMessage().contains("405")) {
                assertTrue("Box installation denied with 405 code", targetBox == null);
            } else {
                fail(e.getMessage());
            }
        } finally {
            /** Clean up data. */
            /** delete the uploaded bar file. */
            sourceBox.del("V1_1_2_bar_maximum.zip", etag);

            /** Delete the newly created box where Bar file has just been installed. */
            if (targetBox != null) {
                log.debug("\nCleaning Operation - deleting box " + targetBox.getName());
                targetCell.box.del(targetBox.getName());
            }

            /** Delete the test cell. */
            if (targetCell != null) {
                log.debug("\nCleaning Operation - deleting box " + targetCell.getName());
                testAs.asCellOwner().unit.cell.del(targetCell.getName(), "*");
            }
        }
    }

    /**
     * Test case Create a Cell and call bar installation API with boxPath and barFile contents contentLength boxPath is.
     * the source boxPath where box is supposed to be created after successful installation & barFile is the actual
     * content/stream of bar file
     * @throws DaoException DAO例外
     */
    /**
     * This test case create a Cell and call bar installation API with boxPath and barFile contents contentLength.
     * boxPath is the source boxPath where box is supposed to be created after successful installation.
     * barFile is the actual content/stream of bar file.
     * @throws DaoException Exception thrown
     */
    @Test
    @Ignore
    public void testBarInstallWithFileContents() throws DaoException {

        /** Start: Executing the test scenario of installing the bar file prerequisite is to have the file. */
        /** this box should be created as the result of bar file installation. */
        Box targetBox = null;
        String testFilename = getClass().getResource("/V1_1_2_bar_maximum.zip").getFile();
        /** A test cell where bar file installation will be tested, it will deleted at the end of test case. */
        Cell targetCell = null;
        try {
            FileInputStream fis = new FileInputStream(testFilename);
            String targetCellName = "targetCell" + suf;
            String targetBoxName = "targetBox" + suf;

            /** Test cell creation - start. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, targetCellName);
            OwnerAccessor oa = testAs.asCellOwner();
            targetCell = oa.unit.cell.create(cellMap);
            log.debug("\n Cell:[" + targetCell.toJSONString() + "]");
            /** Test cell creation - end. */

            log.debug("\nInstantiating Box reference, this will be created after successful bar installation");
            targetBox = new Box();
            targetBox.setName(targetBoxName);
            String boxPath = baseUrl + targetCell.getName() + "/" + targetBox.getName();
            targetBox = testAs.cell(targetCell.getName()).installBox(boxPath, fis);
            assertTrue("Box installation successful with no exception", targetBox != null);
        } catch (DaoException e) {
            /** exception occurred, box installation has issues. */
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } finally {
            /** Clean up data. */
            /** Delete the newly created box where Bar file has just been installed. */
            if (targetBox != null) {
                log.debug("\nCleaning Operation - deleting box " + targetBox.getName());
                targetCell.box.del(targetBox.getName());
            }
            /** Delete the test cell. */
            if (targetCell != null) {
                log.debug("\nCleaning Operation - deleting box " + targetCell.getName());
                testAs.asCellOwner().unit.cell.del(targetCell.getName(), "*");
            }
        }
    }

    /**
     * Method serviceCollectionCall overloaded call method with header.
     * @throws DaoException DAO例外
     */
    /**
     * This method is for the test of serviceCollectionCall overloaded call method with header.
     * it performs following operations. Create Service Collection, PRPPATCH execution, PUT File,
     * Delete File, Delete Service Collection.
     * @throws DaoException Exception thrown
     */
    @Test
    public void serviceCollectionCall() throws DaoException {
        ServiceCollection svcCol = null;
        boolean isScript = false;
        try {
            /** Create Service Collection. */
            Map<String, String> headerMap = new HashMap<String, String>();
            headerMap.put(HttpHeaders.CONTENT_TYPE, "text/html");
            log.debug("\n◆ サービスコレクションの作成 svccol");
            testBox.mkService("svccol");
            svcCol = testBox.service("svccol");

            /** Set of services (PROPPATCH). */
            log.debug("\n◆ サービスの設定 （PROPPATCH）");
            svcCol.configure("servicetest", "servicetest.js", "engine");

            /** DAV PUT File. */
            log.debug("\n◆ スクリプトの登録 （Davのput）");
            String script = "function(request){return {status: 200,"
                    + "headers: {\"Content-Type\":\"text/html\"},body: [\"hello world!\"]};}";
            svcCol.put("servicetest.js", "text/plane", script, "*");
            isScript = true;

            log.debug("\n◆ サービスの実行 servicetest GET");
            HttpResponse resp = svcCol.call("GET", "servicetest", "", headerMap);
            assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());

            log.debug("\n◆ サービスの実行 servicetest POST");
            resp = svcCol.call("POST", "servicetest", "", headerMap);
            assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());

            log.debug("\n◆ サービスの実行 servicetest PUT");
            resp = svcCol.call("PUT", "servicetest", "", headerMap);
            assertEquals(HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());

            /** Execute call. */
            log.debug("\n◆ サービスの実行 servicetest DELETE");
            resp = svcCol.call("DELETE", "servicetest", "", headerMap);
        } finally {
            /** Delete File. */
            log.debug("\n◆ スクリプトの削除 servicetest.js DELETE");
            if (isScript) {
                svcCol.del("servicetest.js");
            }
            /** Delete Service Collection. */
            log.debug("\n◆ サービスコレクションの削除  servicetest DELETE");
            if (null != svcCol) {
                testBox.del("svccol");
            }
        }
    }

    /**
     * This method creates a testCell and testBox inside it, then delete the testCell with recursive delete.
     * @throws DaoException library exception
     */
    @Test
    public void recursiveDeleteCell() throws DaoException {
        final String testCellName = "cellRecDelete";
        final String testBoxName = "boxRecDelete";
        try {
            /** create test cell. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, testCellName);
            testAs.asCellOwner().unit.cell.create(cellMap);

            /** create test box inside test Cell. */
            HashMap<String, Object> boxMap = new HashMap<String, Object>();
            boxMap.put(FIELD_NAME, testBoxName);
            testAs.cell(testCellName).box.create(boxMap);
        } catch (DaoException e) {
            /** This try catch block deals with scenario if somehow test cell could not be deleted in last test run
            * and it already exist.
            * */
            System.out.println("test cell could not be deleted in last test run and it already exist.");
        }

        try {
            /** try deleting test cell when test cell. */
            testAs.asCellOwner().unit.cell.del(testCellName);
        } catch (DaoException e) {
            assertEquals("Non recursive delete, test case successfull", e.getCode(), "409");
        }

        /** Recursive delete of cell. */
        try {
            DcResponse response = testAs.asCellOwner().unit.cell.recursiveDelete(testCellName);
            assertEquals("Recursive delete, test case successfull", response.getStatusCode(), 204);
            log.debug("Recursive cell delete successfull");
        } catch (DaoException e) {
            fail(e.getMessage());
        }
    }

    /**
     * This test case creates an OData in which box name will be fetched from the schema URL of the box specified in
     * DcContext of the cell.
     * @throws DaoException library exception
     */
    @Test
    public void createOdataWithBoxNameFromSchemaURL() throws DaoException {
        final String testCellName = "cellForSchemaURL" + suf;
        final String testBoxName = "boxForSchemaURL";
        final String testOdataName = "odataForSchemaURL";
        Cell testCellForSchemaURL = null;
        Box testBoxForSchemaURL = null;
        try {
            /** create test cell. */
            HashMap<String, Object> cellMap = new HashMap<String, Object>();
            cellMap.put(FIELD_NAME, testCellName);
            testCellForSchemaURL = testAs.asCellOwner().unit.cell.create(cellMap);

            /** create test box inside test Cell. */
            HashMap<String, Object> boxMap = new HashMap<String, Object>();
            boxMap.put(FIELD_NAME, testBoxName);
            boxMap.put(FIELD_SCHEMA, boxSchema + suf);
            testBoxForSchemaURL = testAs.cell(testCellForSchemaURL.getName()).box.create(boxMap);

            testAs.asCellOwner().cell(testCellName).box().mkOData(testOdataName);
            assertEquals(testBoxForSchemaURL.getSchema(), boxSchema + suf);
            log.debug("Odata created successfully with box name fetched from Schema URL");
        } catch (DaoException e) {
            fail(e.getMessage());
        } finally {
            try {
                /** Delete odata that was created. */
                testCellForSchemaURL.box().del(testOdataName, "*");

                /** Delete the box that was created. */
                if (testBoxForSchemaURL != null) {
                    testAs.cell(testCellForSchemaURL.getName()).box.del(testBoxForSchemaURL.getName());
                    testBoxForSchemaURL = null;
                }

                /** Delete the cell that was created. */
                if (testCellForSchemaURL != null) {
                    testAs.asCellOwner().unit.cell.del(testCellForSchemaURL.getName());
                    testCellForSchemaURL = null;
                }
            } catch (DaoException e) {
                fail(e.getMessage());
            }
            log.debug("Odata, box and cell deleted successfully as a part of clean up process");
        }
    }

    /**
     * This is the private method used to create a specified Cell for testing purpose.
     * @param name CellName
     * @return Cell instance that is created
     * @throws DaoException Exception thrown
     */
    private Cell createCell(String name) throws DaoException {
        // Cellの作成
        /** Creating a Cell. */
        HashMap<String, Object> cellMap = new HashMap<String, Object>();
        cellMap.put(FIELD_NAME, name);
        return testAs.asCellOwner().unit.cell.create(cellMap);
    }

    /**
     * This is the private method used to create a specified ExtCell for testing purpose.
     * @param url ExtCell URL
     * @return ExtCell instance that is created
     * @throws DaoException Exception thrown
     */
    private ExtCell createExtCell(String url) throws DaoException {
        HashMap<String, Object> extCellMap = new HashMap<String, Object>();
        extCellMap.put("Url", url);
        return testCell.extCell.create(extCellMap);
    }

    /**
     * tesctCellにRelationを作成する.
     * @throws DaoException DAO例外
     */
    /**
     * This is the private method used to create a specified Relation for a Cell for testing purpose.
     * @param name Relation Name
     * @return Relation object that is created
     * @throws DaoException Exception thrown
     */
    private Relation createRelationCell(String name) throws DaoException {
        /** Relation作成用のJSONを生成. */
        /** Generate JSON for Relation. */
        HashMap<String, Object> relationMap = new HashMap<String, Object>();
        relationMap.put(FIELD_NAME, name);

        /** Relationの作成. */
        /** Creating a Relation. */
        return testCell.relation.create(relationMap);
    }

    /**
     * tesctCellにRelationを作成する.
     * @throws DaoException DAO例外
     */
    /**
     * This is the private method used to create a specified Relation for a Cell with Box for testing purpose.
     * @param name Relation Name
     * @return Relation object that is created
     * @throws DaoException Exception thrown
     */
    private Relation createRelationBox(String name) throws DaoException {
        /** Relation作成用のJSONを生成. */
        /** Generate JSON for Relation. */
        HashMap<String, Object> relationMap = new HashMap<String, Object>();
        relationMap.put(FIELD_NAME, name);
        relationMap.put(FIELD_BOXNAME, testBox.getName());

        /** Relationの作成. */
        /** Creating a Relation. */
        return testCell.relation.create(relationMap);
    }

    /**
     * tesctCellにRoleを作成する.
     * @throws DaoException DAO例外
     */
    /**
     * This is the private method used to create a specified Role for a Cell for testing purpose.
     * @param name Role Name
     * @return Role object that is created
     * @throws DaoException Exception thrown
     */
    private Role createRoleCell(String name) throws DaoException {
        /** Roleの作成(_box.name 省略). */
        /** Creating a Role (_box.name omitted). */
        HashMap<String, Object> roleMap = new HashMap<String, Object>();
        roleMap.put(FIELD_NAME, name);
        return testAs.cell(testCell.getName()).role.create(roleMap);
    }

    /**
     * tesctCellにRoleを作成する.
     * @throws DaoException DAO例外
     */
    /**
     * This is the private method used to create a specified Role for a Cell with Box for testing purpose.
     * @param name Role Name
     * @return Role object that is created
     * @throws DaoException Exception thrown
     */
    private Role createRoleBox(String name) throws DaoException {
        /** Roleの作成(_box.name 省略). */
        /** Creating a Role with Box name specified. */
        HashMap<String, Object> roleMap = new HashMap<String, Object>();
        roleMap.put(FIELD_NAME, name);
        roleMap.put(FIELD_BOXNAME, testBox.getName());
        return testAs.cell(testCell.getName()).role.create(roleMap);
    }

    /**
     * This method is used to get the result object from the HashMap.
     * @param value HashMap
     * @return Object fetched
     */
    @SuppressWarnings("unchecked")
    private Object getResultObject(HashMap<String, Object> value) {
        return ((HashMap<String, Object>) value.get("d")).get("results");
    }

}
