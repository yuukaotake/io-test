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
package com.fujitsu.dc.core.model.jaxb;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import com.fujitsu.dc.common.auth.token.Role;
import com.fujitsu.dc.common.utils.DcCoreUtils;
import com.fujitsu.dc.core.DcCoreException;
import com.fujitsu.dc.core.auth.AccessContext;
import com.fujitsu.dc.core.auth.BoxPrivilege;
import com.fujitsu.dc.core.auth.CellPrivilege;
import com.fujitsu.dc.core.auth.Privilege;

/**
 * ACLを表すモデルオブジェクト.
 * WebDAV ACLの D:acl タグに対応したJAXBオブジェクトとしても振る舞い、
 * ACLメソッドで受けるXMLをそのまま unmarshall してオブジェクト生成可能。
 * 一方で、JSONへの シリアライズ及び JSONからのデシリアライズもサポートし、
 * ElasticSearchをはじめとするJSONベースの永続化機構での利用を可能とする。
 * また、AccessContextオブジェクトに本オブジェクトを与えることで、
 * 与えられるべきPrivilege一覧を生成する。
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "", propOrder = { "aces" })
@XmlRootElement(namespace = "DAV:", name = "acl")
public final class Acl {
    /**
     * xml:base.
     */
    @XmlAttribute(namespace = "http://www.w3.org/XML/1998/namespace")
    String base;

    /**
     * dc:requireSchemaAuthz.
     */
    @XmlAttribute(namespace = DcCoreUtils.XmlConst.NS_DC1)
    String requireSchemaAuthz;

    /**
     * dc:requireSchemaAuthz setter.
     * @param requireSchemaAuthz requireSchemaAuthz
     */
    public void setRequireSchemaAuthz(String requireSchemaAuthz) {
        this.requireSchemaAuthz = requireSchemaAuthz;
    }

    /**
     * dc:requireSchemaAuthz getter.
     * @return requireSchemaAuthz
     */
    public String getRequireSchemaAuthz() {
        return requireSchemaAuthz;
    }

    /**
     * Aceタグ.
     */
    @XmlElements({ @XmlElement(namespace = "DAV:", name = "ace", type = Ace.class) })
    List<Ace> aces;

    /**
     * xml:base setter.
     * @param base baseUrl
     */
    public void setBase(String base) {
        this.base = base;
    }

    /**
     * xml:base getter.
     * @return base
     */
    public String getBase() {
        return base;
    }
    /**
     * Ace.
     * @return Ace Object
     */
    public List<Ace> getAceList() {
        return aces;
    }
    /**
     * JSON化する.
     * @return Mapオブジェクト
     */
    public String toJSON() {
        StringWriter sw = new StringWriter();
        try {
            ObjectIo.toJson(this, sw);
            return sw.toString();
        } catch (IOException e) {
            throw DcCoreException.Server.DATA_STORE_UNKNOWN_ERROR.reason(e);
        } catch (JAXBException e) {
            throw DcCoreException.Server.DATA_STORE_UNKNOWN_ERROR.reason(e);
        }
    }
    /**
     * @param jsonString acl json
     * @return Acl obj
     */
    public static Acl fromJson(final String jsonString) {
        StringReader sr = new StringReader(jsonString);
        try {
            return ObjectIo.fromJson(sr, Acl.class);
        } catch (IOException e) {
            throw DcCoreException.Server.DATA_STORE_UNKNOWN_ERROR.reason(e);
        } catch (JAXBException e) {
            throw DcCoreException.Server.DATA_STORE_UNKNOWN_ERROR.reason(e);
        }
    }

    /**
     * AccessContextに対して、このACLがどのようなPrivilegeを与えるかを返す.
     * @param ac AccessContextオブジェクト
     * @return Privilege List
     */
    public List<String> allows(final AccessContext ac) {
        List<Role> roles = ac.getRoleList();
        List<String> ret = new ArrayList<String>();
        for (Role role : roles) {
            for (Ace ace : this.aces) {
                if (ace.getPrincipalHref().equals(role.createUrl())) {
                    List<String> privList = ace.getGrantedPrivilegeList();
                    for (String priv : privList) {
                        ret.add(priv);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * AccessContextに対して、このACLが特定のPrivilegeを与えるかどうかを返す.
     * @param priv チェックしたいPrivilege
     * @param ac AccessContextオブジェクト
     * @param privilegeMap Privilege管理
     * @return 与える場合は真
     */
    public boolean allows(final Privilege priv, final AccessContext ac, Map<String, Privilege> privilegeMap) {
        List<String> privs = this.allows(ac);
        boolean ret = false;
        for (String p : privs) {
            Privilege pObj = privilegeMap.get(p);
            if (pObj.includes(priv)) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * ACLのバリデートチェック処理.
     * @param isCellLevel Cellレベルかのフラグ
     * @return バリデートに異常がある場合はfalseを返却
     */
    public boolean validateAcl(boolean isCellLevel) {
        // <!ELEMENT acl (ace*) >
        if (aces == null) {
            return true;
        }
        for (Ace ace : aces) {
            // <!ELEMENT ace ((principal or invert), (grant or deny), protected?,inherited?)>
            if (ace.grant == null || ace.principal == null) {
                return false;
            }
            // <!ELEMENT grant (privilege+)>
            if (ace.grant.privileges == null) {
                return false;
            }
            Map<String, CellPrivilege> cellPrivilegeMap = CellPrivilege.getPrivilegeMap();
            Map<String, BoxPrivilege> boxPrivilegeMap = BoxPrivilege.getPrivilegeMap();

            for (com.fujitsu.dc.core.model.jaxb.Privilege privilege : ace.grant.privileges) {
                // privilegeが空でないこと
                if (privilege.body == null) {
                    return false;
                }
                // Privilegeに設定可能なタグであるかチェック
                if (isCellLevel) {
                    if (!cellPrivilegeMap.containsKey(privilege.body.getLocalName())) {
                        return false;
                    }
                } else {
                    if (!boxPrivilegeMap.containsKey(privilege.body.getLocalName())) {
                        return false;
                    }
                }
            }
            // <!ELEMENT principal (href or all)>
            // <!ELEMENT href ANY>
            if (ace.principal.all == null && (ace.principal.href == null || ace.principal.href.equals(""))) {
                return false;
            }
        }
        return true;
    }

}